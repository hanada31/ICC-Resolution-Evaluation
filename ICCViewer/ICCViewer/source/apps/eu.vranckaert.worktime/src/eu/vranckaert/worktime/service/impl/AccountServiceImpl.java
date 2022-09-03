/*
 * Copyright 2013 Dirk Vranckaert
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.vranckaert.worktime.service.impl;

import android.content.Context;
import android.util.Log;
import com.google.inject.Inject;
import eu.vranckaert.worktime.dao.*;
import eu.vranckaert.worktime.dao.impl.*;
import eu.vranckaert.worktime.dao.web.WorkTimeWebDao;
import eu.vranckaert.worktime.dao.web.impl.WorkTimeWebDaoImpl;
import eu.vranckaert.worktime.dao.web.model.response.sync.*;
import eu.vranckaert.worktime.exceptions.SDCardUnavailableException;
import eu.vranckaert.worktime.exceptions.backup.BackupException;
import eu.vranckaert.worktime.exceptions.backup.BackupFileCouldNotBeCreated;
import eu.vranckaert.worktime.exceptions.backup.BackupFileCouldNotBeWritten;
import eu.vranckaert.worktime.exceptions.network.NoNetworkConnectionException;
import eu.vranckaert.worktime.exceptions.network.WifiConnectionRequiredException;
import eu.vranckaert.worktime.exceptions.worktime.account.*;
import eu.vranckaert.worktime.exceptions.worktime.sync.CorruptSyncDataException;
import eu.vranckaert.worktime.exceptions.worktime.sync.SyncAlreadyBusyException;
import eu.vranckaert.worktime.exceptions.worktime.sync.SynchronizationFailedException;
import eu.vranckaert.worktime.guice.Application;
import eu.vranckaert.worktime.model.*;
import eu.vranckaert.worktime.service.AccountService;
import eu.vranckaert.worktime.service.BackupService;
import eu.vranckaert.worktime.utils.network.NetworkUtil;
import eu.vranckaert.worktime.utils.preferences.Preferences;
import eu.vranckaert.worktime.web.json.exception.GeneralWebException;

import java.util.*;

/**
 * User: DIRK VRANCKAERT
 * Date: 12/12/12
 * Time: 20:04
 */
public class AccountServiceImpl implements AccountService {
    public static final String LOG_TAG = AccountServiceImpl.class.getSimpleName();

    @Inject
    private WorkTimeWebDao workTimeWebDao;

    @Inject
    private AccountDao accountDao;

    @Inject
    private SyncHistoryDao syncHistoryDao;

    @Inject
    private ProjectDao projectDao;

    @Inject
    private TaskDao taskDao;

    @Inject
    private TimeRegistrationDao timeRegistrationDao;

    @Inject
    private SyncRemovalCacheDao syncRemovalCacheDao;

    @Inject
    private BackupService backupService;

    @Inject
    private Context context;

    public AccountServiceImpl() {}

    public AccountServiceImpl(Application application, Context context) {
        this.context = context;

        workTimeWebDao = new WorkTimeWebDaoImpl(application, context);
        accountDao = new AccountDaoImpl(context);
        syncHistoryDao = new SyncHistoryDaoImpl(context);
        projectDao = new ProjectDaoImpl(context, new SyncRemovalCacheDaoImpl(context));
        taskDao = new TaskDaoImpl(context, new SyncRemovalCacheDaoImpl(context));
        timeRegistrationDao = new TimeRegistrationDaoImpl(context, new SyncRemovalCacheDaoImpl(context));
        syncRemovalCacheDao = new SyncRemovalCacheDaoImpl(context);
        backupService = new DatabaseFileBackupServiceImpl();
    }

    @Override
    public boolean isUserLoggedIn() {
        User user = accountDao.getLoggedInUser();
        return user!=null;
    }

    @Override
    public void login(String email, String password) throws GeneralWebException, NoNetworkConnectionException, LoginCredentialsMismatchException {
        String sessionKey = workTimeWebDao.login(email, password);

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setSessionKey(sessionKey);

        accountDao.storeLoggedInUser(user);
    }

    @Override
    public void reLogin() throws GeneralWebException, NoNetworkConnectionException, LoginCredentialsMismatchException {
        User user = accountDao.getLoggedInUser();
        if (user != null) {
            login(user.getEmail(), user.getPassword());
        }
    }

    @Override
    public void register(String email, String firstName, String lastName, String password) throws GeneralWebException, NoNetworkConnectionException, RegisterEmailAlreadyInUseException, PasswordLengthValidationException, RegisterFieldRequiredException {
        String sessionKey = workTimeWebDao.register(email, firstName, lastName, password);

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setSessionKey(sessionKey);

        accountDao.storeLoggedInUser(user);
    }

    @Override
    public User getOfflineUserDate() {
        User user = accountDao.getLoggedInUser();
        if (user.isProfileComplete()) {
            return user;
        } else {
            return null;
        }
    }

    @Override
    public User loadUserData() throws UserNotLoggedInException, GeneralWebException, NoNetworkConnectionException {
        User user = accountDao.getLoggedInUser();

        User updatedUser = null;
        try {
            updatedUser = workTimeWebDao.loadProfile(user);
        } catch (UserNotLoggedInException e) {
            accountDao.delete(user);
            throw e;
        }

        if (updatedUser != null) {
            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            user.setEmail(updatedUser.getEmail());
            user.setLoggedInSince(updatedUser.getLoggedInSince());
            user.setRegisteredSince(updatedUser.getRegisteredSince());
            user.setRole(updatedUser.getRole());
            accountDao.update(user);
        }

        return updatedUser;
    }

    @Override
    public void sync() throws UserNotLoggedInException, GeneralWebException, NoNetworkConnectionException, WifiConnectionRequiredException, BackupException, SyncAlreadyBusyException, SynchronizationFailedException {
        Log.i(LOG_TAG, "Starting synchronization...");
        if (isSyncBusy()) {
            throw new SyncAlreadyBusyException();
        }

        // Save an instance of SyncHistory so we always know that a sync is going on...
        SyncHistory syncHistory = new SyncHistory();
        syncHistoryDao.save(syncHistory);

        try {
            if (Preferences.Account.syncOnWifiOnly(context) && !NetworkUtil.isConnectedToWifi(context)) {
                WifiConnectionRequiredException e = new WifiConnectionRequiredException();
                markSyncAsFailed(e);
                throw e;
            }

            if (Preferences.Account.backupBeforeSync(context)) {
                updateCurrentSyncAction(SyncHistoryAction.BACKUP);
                try {
                    backupService.backup(context);
                } catch (SDCardUnavailableException e) {
                    BackupException backupException = new BackupException(e);
                    markSyncAsFailed(backupException);
                    throw backupException;
                } catch (BackupFileCouldNotBeCreated backupFileCouldNotBeCreated) {
                    markSyncAsFailed(backupFileCouldNotBeCreated);
                    throw backupFileCouldNotBeCreated;
                } catch (BackupFileCouldNotBeWritten backupFileCouldNotBeWritten) {
                    markSyncAsFailed(backupFileCouldNotBeWritten);
                    throw backupFileCouldNotBeWritten;
                }
            }

            updateCurrentSyncAction(SyncHistoryAction.PREPARE_DATA);

            // Retrieve the logged in user
            User user = accountDao.getLoggedInUser();

            // Retrieve the conflict configuration
            String conflictConfiguration = Preferences.Account.conflictConfiguration(context);

            // Get the last successful sync date
            Date lastSuccessfulSyncDate = syncHistoryDao.getLastSuccessfulSyncDate();

            // Retrieve all time projects, tasks and registrations to be synced
            // If no sync has been done before all entities will be synced. Otherwise only those that have changed
            // since the last sync.
            List<Project> projects = null;
            List<Task> tasks = null;
            List<TimeRegistration> timeRegistrations = null;
            if (lastSuccessfulSyncDate != null) {
                // Retrieve all projects, tasks and time registrations that have been modified since the last sync or
                // for which the no syncing has been done before. This last check (if not synced before) must be done
                // because due to a server interruption it is possible that some entities will not have been synced
                // during the last sync.
                projects = projectDao.findAllModifiedAfterOrUnSynced(lastSuccessfulSyncDate);
                tasks = taskDao.findAllModifiedAfterOrUnSynced(lastSuccessfulSyncDate);
                timeRegistrations = timeRegistrationDao.findAllModifiedAfterOrUnSynced(lastSuccessfulSyncDate);

            } else {
                projects = projectDao.findAll();
                tasks = taskDao.findAll();
                timeRegistrations = timeRegistrationDao.findAll();
            }

            // Make sure all relations are correctly loaded into memory...
            for (Task task : tasks) {
                projectDao.refresh(task.getProject());
            }
            for (TimeRegistration timeRegistration : timeRegistrations) {
                taskDao.refresh(timeRegistration.getTask());
                projectDao.refresh(timeRegistration.getTask().getProject());
            }

            // Retrieve removed sync-keys
            Map<String, String> syncRemovalMap = syncRemovalCacheDao.findAllSyncKeys();

            updateCurrentSyncAction(SyncHistoryAction.SYNC_SERVER);

            List<Object> result;
            try {
                // Execute the sync on the server
                result = workTimeWebDao.sync(user, conflictConfiguration, lastSuccessfulSyncDate, projects, tasks, timeRegistrations, syncRemovalMap);
            } catch (UserNotLoggedInException e) {
                accountDao.delete(user);
                markSyncAsFailed(e);
                throw e;
            } catch (SynchronizationFailedException e) {
                markSyncAsFailed(e);
                throw e;
            } catch (CorruptSyncDataException e) {
                markSyncAsFailed(e);
                throw new RuntimeException("The data of the application seems to be corrupt!", e);
            } catch (SyncAlreadyBusyException e) {
                markSyncAsFailed(e);
                throw e;
            } catch (GeneralWebException e) {
                markSyncAsFailed(e);
                throw e;
            } catch (NoNetworkConnectionException e) {
                markSyncAsFailed(e);
                throw e;
            }

            updateCurrentSyncAction(SyncHistoryAction.SYNC_LOCAL);

            List<Project> projectsSinceLastSync = (List<Project>) result.get(0);
            List<Task> tasksSinceLastSync = (List<Task>) result.get(1);
            List<TimeRegistration> timeRegistrationsSinceLastSync = (List<TimeRegistration>) result.get(2);
            EntitySyncResult entitySyncResult = (EntitySyncResult) result.get(3);
            Map<String, String> serverSyncRemovalMap = (Map<String, String>) result.get(4);

            applySyncResult(entitySyncResult);
            checkServerEntities(projectsSinceLastSync, tasksSinceLastSync, timeRegistrationsSinceLastSync);
            removeEntities(serverSyncRemovalMap);

            // Clean up the entities that should be removed on the next sync.
            syncRemovalCacheDao.deleteAll();

            syncHistory = syncHistoryDao.getOngoingSyncHistory();
            if (syncHistory != null) {
                syncHistory.setEnded(new Date());

                if (entitySyncResult.getSyncResult().equals(SyncResult.INTERRUPTED)) {
                    syncHistory.setStatus(SyncHistoryStatus.INTERRUPTED);
                } else {
                    syncHistory.setStatus(SyncHistoryStatus.SUCCESSFUL);
                }
                syncHistory.setAction(SyncHistoryAction.DONE);

                storeStatisticalData(syncHistory, syncRemovalMap, serverSyncRemovalMap, entitySyncResult,
                        projectsSinceLastSync, tasksSinceLastSync, timeRegistrationsSinceLastSync);

                syncHistoryDao.update(syncHistory);
            }

            // Make sure that all non-synced entities so far (due to a server interruption) will be synced again on next
            // synchronisation
            if (entitySyncResult.getSyncResult().equals(SyncResult.INTERRUPTED)) {
                List<String> projectNames = new ArrayList<String>();
                for (Project project : entitySyncResult.getNonSyncedProjects()) {
                    projectNames.add(project.getName());
                }
                projectDao.setLastModified(projectNames, new Date());


                for (Task task : entitySyncResult.getNonSyncedTasks()) {
                    Project project = projectDao.findByName(task.getProject().getName());
                    Task localTask = taskDao.findByName(task.getName(), project);
                    localTask.setLastUpdated(new Date());
                    taskDao.update(localTask);
                }

                for (TimeRegistration timeRegistration : entitySyncResult.getNonSyncedTimeRegistrations()) {
                    Project project = projectDao.findByName(timeRegistration.getTask().getProject().getName());
                    Task task = taskDao.findByName(timeRegistration.getTask().getName(), project);
                    TimeRegistration localTimeRegistration = timeRegistrationDao.findByDates(timeRegistration.getStartTime(), timeRegistration.getEndTime());
                    localTimeRegistration.setLastUpdated(new Date());
                    timeRegistrationDao.update(localTimeRegistration);
                }
            }
        } catch (RuntimeException e) {
            markSyncAsFailed(e);
            throw e;
        }
    }

    private void removeEntities(Map<String, String> syncRemovalMap) {
        if (syncRemovalMap == null || syncRemovalMap.size() == 0)
            return;

        List<String> projectSyncKeys = new ArrayList<String>();
        List<String> taskSyncKeys = new ArrayList<String>();
        List<String> timeRegistrationSyncKeys = new ArrayList<String>();

        for (Map.Entry<String, String> entry : syncRemovalMap.entrySet()) {
            String syncKey = entry.getKey();
            String entityName = entry.getValue();

            if (entityName.equals("Project")) {
                projectSyncKeys.add(syncKey);
            } else if (entityName.equals("Task")) {
                taskSyncKeys.add(syncKey);
            } else if (entityName.equals("TimeRegistration")) {
                timeRegistrationSyncKeys.add(syncKey);
            }
        }

        for (String syncKey : timeRegistrationSyncKeys) {
            TimeRegistration entity = timeRegistrationDao.findBySyncKey(syncKey);
            if (entity != null)
                timeRegistrationDao.delete(entity);
        }

        for (String syncKey : taskSyncKeys) {
            Task entity = taskDao.findBySyncKey(syncKey);
            if (entity != null)
                taskDao.delete(entity);
        }

        for (String syncKey : projectSyncKeys) {
            Project entity = projectDao.findBySyncKey(syncKey);
            if (entity != null)
                projectDao.delete(entity);
        }
    }

    /**
     * Apply the sync result as it has been returned from the server. For each entity group it result in which we find
     * the original entity (that has been sent to the server by us), the synced entity (as it is stored on the server)
     * or null and the resolution (if it's accepted, merged, not accepted or no action has been performed.<br/>
     * In case an entity is merged we do a lookup locally based on the original entity and replace it's content with the
     * content of the synced entity.<br/>
     * In case the entity is not either accepted or no action has been performed for the entity we'll check if the
     * sync-key is set or not. If the sync key is not set we set it and update the entity.<br/>
     * In case the entity is not accepted the entity will be removed from the local database. If it's a time
     * registration the list of synced time registrations needs to be checked. If that list is not empty it means the
     * the just removed entity needs to be replaced with that list of time registrations. So each one of them should be
     * persisted.
     * @param entitySyncResult The synchronization result containing every single entity that has been sent to the
     *                         server.
     */
    private void applySyncResult(EntitySyncResult entitySyncResult) {
        Log.d(LOG_TAG, "Applying synchronization result...");

        for (ProjectSyncResult syncResult : entitySyncResult.getProjectSyncResults()) {
            Log.d(LOG_TAG, "Checking for project with local name " + syncResult.getProject().getName());
            Project localProject = syncResult.getProject();
            Project syncedProject = syncResult.getSyncedProject();
            switch (syncResult.getResolution()) {
                case MERGED: {
                    // The entity has been merged with an entity on the server, so we will have copy all of the
                    // synced data into the local entity.
                    Log.d(LOG_TAG, "The project has been merged...");
                    Project project = projectDao.findByName(localProject.getName());
                    updateProject(syncedProject, project);
                    break;
                }
                case NOT_ACCEPTED: {
                    // The entity has been synced before but is no longer found remotely based on the sync key so we
                    // can safely remove the entity from the database.
                    Log.d(LOG_TAG, "The project has not been accepted...");
                    Project project = projectDao.findByName(localProject.getName());
                    projectDao.delete(project);
                    break;
                }
                default: {
                    // The entity has been either accepted remotely or it already exists on the server and the
                    // contents did not have to be merged. We will only update it's syncKey!
                    Log.d(LOG_TAG, "The project has been accepted or no action was necessary...");
                    Project project = projectDao.findByName(localProject.getName());
                    if (project.getSyncKey() == null) {
                        project.setSyncKey(syncedProject.getSyncKey());
                        projectDao.update(project);
                    }
                    break;
                }
            }
        }
        for (TaskSyncResult syncResult : entitySyncResult.getTaskSyncResults()) {
            Log.d(LOG_TAG, "Checking for task with local name " + syncResult.getTask().getName());
            Task localTask = syncResult.getTask();
            Project localProject = projectDao.findByName(localTask.getProject().getName());
            Task syncedTask = syncResult.getSyncedTask();
            switch (syncResult.getResolution()) {
                case MERGED: {
                    // The entity has been merged with an entity on the server, so we will have copy all of the
                    // synced data into the local entity.
                    Log.d(LOG_TAG, "The task has been merged...");
                    Task task = taskDao.findByName(localTask.getName(), localProject);
                    updateTask(syncedTask, task);
                    break;
                }
                case NOT_ACCEPTED: {
                    // The entity has been synced before but is no longer found remotely based on the sync key so we
                    // can safely remove the entity from the database.
                    Log.d(LOG_TAG, "The task has not been accepted...");
                    Task task = taskDao.findByName(localTask.getName(), localProject);
                    taskDao.delete(task);
                    break;
                }
                default: {
                    // The entity has been either accepted remotely or it already exists on the server and the
                    // contents did not have to be merged. We will only update it's syncKey!
                    Log.d(LOG_TAG, "The task has been accepted or no action was necessary...");
                    Task task = taskDao.findByName(localTask.getName(), localProject);
                    if (task.getSyncKey() == null) {
                        task.setSyncKey(syncedTask.getSyncKey());
                        taskDao.update(task);
                    }
                    break;
                }
            }
        }
        for (TimeRegistrationSyncResult syncResult : entitySyncResult.getTimeRegistrationSyncResults()) {
            Log.d(LOG_TAG, "Checking for time registration with local start and end time (" + syncResult.getTimeRegistration().getStartTime() + ", END: " + (syncResult.getTimeRegistration().getEndTime() == null ? "NULL" : syncResult.getTimeRegistration().getEndTime()) + ")");
            TimeRegistration localTimeRegistration = syncResult.getTimeRegistration();
            TimeRegistration syncedTimeRegistration = syncResult.getSyncedTimeRegistration();
            List<TimeRegistration> syncedTimeRegistrations = syncResult.getSyncedTimeRegistrations();
            switch (syncResult.getResolution()) {
                case MERGED: {
                    // The entity has been merged with an entity on the server, so we will have copy all of the
                    // synced data into the local entity.
                    Log.d(LOG_TAG, "The time registration has been merged...");
                    TimeRegistration timeRegistration = timeRegistrationDao.findByDates(
                            localTimeRegistration.getStartTime(), localTimeRegistration.getEndTime()
                    );
                    updateTimeRegistration(syncedTimeRegistration, timeRegistration);
                    break;
                }
                case NOT_ACCEPTED: {
                    // The time registration was not accepted remotely because it interfered with other time
                    // registrations so the time registration should be removed and the 'others' should be
                    // persisted. Or the time registration was not accepted because there was not match found with
                    // the sync-key (so it must have been removed on the server) before this sync. We can then
                    // safely remove it here also.
                    Log.d(LOG_TAG, "The time registration has not been accepted...");
                    TimeRegistration timeRegistration = timeRegistrationDao.findByDates(
                            localTimeRegistration.getStartTime(), localTimeRegistration.getEndTime()
                    );
                    timeRegistrationDao.delete(timeRegistration);

                    if (syncedTimeRegistrations != null && !syncedTimeRegistrations.isEmpty()) {
                        for (TimeRegistration incomingTimeRegistration : syncedTimeRegistrations) {
                            Log.d(LOG_TAG, "Checking for other time registrations in DB with same sync key " + incomingTimeRegistration.getSyncKey());
                            TimeRegistration timeRegistrationWithSyncKey = timeRegistrationDao.findBySyncKey(incomingTimeRegistration.getSyncKey());
                            if (timeRegistrationWithSyncKey != null) {
                                timeRegistrationDao.delete(timeRegistrationWithSyncKey);
                            }
                            Log.d(LOG_TAG, "Persisting incoming time registration with sync key " + incomingTimeRegistration.getSyncKey() + " and start and end time (" + incomingTimeRegistration.getStartTime() + ", END: " + (incomingTimeRegistration.getEndTime() == null ? "NULL" : incomingTimeRegistration.getEndTime()) + ")");
                            Project localProject = projectDao.findByName(incomingTimeRegistration.getTask().getProject().getName());
                            Task task = taskDao.findByName(incomingTimeRegistration.getTask().getName(), localProject);
                            incomingTimeRegistration.setTask(task);
                            timeRegistrationDao.save(incomingTimeRegistration);
                        }
                    }
                    break;
                }
                default: {
                    // The entity has been either accepted remotely or it already exists on the server and the
                    // contents did not have to be merged. We will only update it's syncKey!
                    Log.d(LOG_TAG, "The time registration has been accepted or no action was necessary...");
                    TimeRegistration timeRegistration = timeRegistrationDao.findByDates(
                            localTimeRegistration.getStartTime(), localTimeRegistration.getEndTime()
                    );
                    if (timeRegistration.getSyncKey() == null) {
                        timeRegistration.setSyncKey(syncedTimeRegistration.getSyncKey());
                        timeRegistrationDao.update(timeRegistration);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Check the entities returned by the server and check if everything is already available locally or if any entity
     * should be updated or persisted.
     * @param projects Projects coming from the server.
     * @param tasks Tasks coming from the server.
     * @param timeRegistrations Time registrations coming from the server.
     */
    private void checkServerEntities(List<Project> projects, List<Task> tasks, List<TimeRegistration> timeRegistrations) {
        Log.d(LOG_TAG, "Check for the entities on the server that have changed since the last update to be persisted locally or use to update local information...");

        Log.d(LOG_TAG, "Checking the server-projects...");
        for (Project project : projects) {
            Log.d(LOG_TAG, "Checking project with sync key " + project.getSyncKey() + " and name " + project.getName());
            Project localProject = projectDao.findBySyncKey(project.getSyncKey());
            if (localProject == null) {
                Log.d(LOG_TAG, "No local project found based on the sync key (" + project.getSyncKey() + ")");
                localProject = projectDao.findByName(project.getName());
                if (localProject != null) {
                    Log.d(LOG_TAG, "Local project found based on the name (" + localProject.getName() + "), update the local project with the server content");
                    updateProject(project, localProject);
                } else {
                    Log.d(LOG_TAG, "No local project found based on the name, save incoming project with name " + project.getName());
                    projectDao.save(project);
                }
            } else {
                Log.d(LOG_TAG, "Local project found based on the sync key (" + localProject.getSyncKey() + "), update the local project with the server content");
                updateProject(project, localProject);
            }
        }

        Log.d(LOG_TAG, "Checking the server-tasks...");
        for (Task task : tasks) {
            Log.d(LOG_TAG, "Checking task with sync key " + task.getSyncKey() + " and name " + task.getName());
            Task localTask = taskDao.findBySyncKey(task.getSyncKey());
            if (localTask == null) {
                Log.d(LOG_TAG, "No local task found based on the sync key (" + task.getSyncKey() + ")");
                Project localProject = projectDao.findByName(task.getProject().getName());
                localTask = taskDao.findByName(task.getName(), localProject);
                if (localTask != null) {
                    Log.d(LOG_TAG, "Local task found based on the name (" + localTask.getName() + "), update the local task with the server content");
                    updateTask(task, localTask);
                } else {
                    Log.d(LOG_TAG, "No local task found based on the name, save incoming task with name " + task.getName());
                    updateTask(task, task);
                    taskDao.save(task);
                }
            } else {
                Log.d(LOG_TAG, "Local task found based on the sync key (" + localTask.getSyncKey() + "), update the local task with the server content");
                updateTask(task, localTask);
            }
        }

        Log.d(LOG_TAG, "Checking the server-time registrations...");
        for (TimeRegistration timeRegistration : timeRegistrations) {
            Log.d(LOG_TAG, "Checking time registrations with sync key " + timeRegistration.getSyncKey() + " and start and end time (START: " + timeRegistration.getStartTime() + ", END: " + (timeRegistration.getEndTime() == null ? "NULL" : timeRegistration.getEndTime()) + "), update the local time registration with the server content");
            TimeRegistration localTimeRegistration = timeRegistrationDao.findBySyncKey(timeRegistration.getSyncKey());
            if (localTimeRegistration == null) {
                Log.d(LOG_TAG, "No local time registration found based on the sync key (" + timeRegistration.getSyncKey() + ")");
                localTimeRegistration = timeRegistrationDao.findByDates(timeRegistration.getStartTime(), timeRegistration.getEndTime());
                if (localTimeRegistration != null) {
                    Log.d(LOG_TAG, "Local time registration found based on the start and end time (START: " + timeRegistration.getStartTime() + ", END: " + (timeRegistration.getEndTime() == null ? "NULL" : timeRegistration.getEndTime()) + "), update the local time registration with the server content");
                    updateTimeRegistration(timeRegistration, localTimeRegistration);
                } else {
                    Log.d(LOG_TAG, "No local time registration found based on the start and end time, save incoming time registration with start and end time (START: " + timeRegistration.getStartTime() + ", END: " + (timeRegistration.getEndTime() == null ? "NULL" : timeRegistration.getEndTime()) + ")");
                    updateTimeRegistration(timeRegistration, timeRegistration);
                    timeRegistrationDao.save(timeRegistration);
                }
            } else {
                Log.d(LOG_TAG, "Local time registration found based on the sync key (" + localTimeRegistration.getSyncKey() + "), update the local task with the server content");
                updateTimeRegistration(timeRegistration, localTimeRegistration);
            }
        }
    }

    /**
     * Updates the destination project after the contents of the source project have been copied into the destination
     * project. The id is not overwritten!
     * @param source The source project.
     * @param destination The destination project that will be updated.
     */
    private void updateProject(Project source, Project destination) {
        destination.setName(source.getName());
        destination.setComment(source.getComment());
        destination.setDefaultValue(source.isDefaultValue());
        destination.setFinished(source.isFinished());
        destination.setFlags(source.getFlags());
        destination.setOrder(source.getOrder());
        destination.setLastUpdated(source.getLastUpdated());
        destination.setSyncKey(source.getSyncKey());

        if (source.getId() != null)
            projectDao.update(destination);
    }

    /**
     * Updates the destination task after the contents of the source task have been copied into the destination task.
     * The id is not overwritten! The linked project is looked up in the database based on the project of the
     * source-task.
     * @param source The source task.
     * @param destination The destination task that will be updated.
     */
    private void updateTask(Task source, Task destination) {
        destination.setName(source.getName());
        destination.setComment(source.getComment());
        destination.setFinished(source.isFinished());
        destination.setFlags(source.getFlags());
        destination.setOrder(source.getOrder());
        destination.setLastUpdated(source.getLastUpdated());
        destination.setSyncKey(source.getSyncKey());
        Project project = projectDao.findBySyncKey(source.getProject().getSyncKey());
        if (project == null) {
            project = projectDao.findByName(source.getProject().getName());
        }
        destination.setProject(project);
        if (source.getId() != null)
            taskDao.update(destination);
    }

    /**
     * Updates the destination time registration after the contents of the source time registration have been copied
     * into the destination time registration. The id is not overwritten! The linked task is looked up in the database
     * based on the task of the source-time-registration.
     * @param source The source time registration.
     * @param destination The destination time registration that will be updated.
     */
    private void updateTimeRegistration(TimeRegistration source, TimeRegistration destination) {
        destination.setComment(source.getComment());
        destination.setFlags(source.getFlags());
        destination.setLastUpdated(source.getLastUpdated());
        destination.setSyncKey(source.getSyncKey());
        Task task = taskDao.findBySyncKey(source.getTask().getSyncKey());
        if (task == null) {
            Project localProject = projectDao.findByName(source.getTask().getProject().getName());
            task = taskDao.findByName(source.getTask().getName(), localProject);
        }
        destination.setTask(task);
        if (source.getId() != null)
            timeRegistrationDao.update(destination);
    }

    /**
     * Mark the ongoing {@link SyncHistory} as {@link SyncHistoryStatus#FAILED}. If an exception is provided the simple
     * name of the exception-class will be put in the {@link SyncHistory#failureReason} field.
     * @param e The exception that occurred or null.
     */
    private void markSyncAsFailed(Exception e) {
        SyncHistory syncHistory = syncHistoryDao.getOngoingSyncHistory();
        if (syncHistory != null && syncHistory.getStatus().equals(SyncHistoryStatus.BUSY)) {
            syncHistory.setStatus(SyncHistoryStatus.FAILED);
            syncHistory.setEnded(new Date());
            if (e != null) {
                syncHistory.setFailureReason(e.getClass().getSimpleName());
            }
            syncHistoryDao.update(syncHistory);
        }
    }

    /**
     * Updates the current action of the synchronization process.
     * @param action The action that is now going on.
     */
    private void updateCurrentSyncAction(SyncHistoryAction action) {
        SyncHistory syncHistory = syncHistoryDao.getOngoingSyncHistory();
        if (syncHistory.getStatus().equals(SyncHistoryStatus.BUSY)) {
            syncHistory.setAction(action);
            syncHistoryDao.update(syncHistory);
        }
    }

    private void storeStatisticalData(SyncHistory syncHistory, Map<String, String> outgoingSyncRemovalMap,
                                      Map<String, String> incomingSyncRemovalMap, EntitySyncResult outgoingSyncResult,
                                      List<Project> incomingProjects, List<Task> incomingTasks,
                                      List<TimeRegistration> incomingTimeRegistrations) {
        // outgoing sync removal map
        Map<String, Integer> outgoingRemovals = countRemovalsPerType(outgoingSyncRemovalMap);
        syncHistory.setNumOutgoingProjectsRemoved(outgoingRemovals.get(Project.class.getSimpleName()));
        syncHistory.setNumOutgoingTasksRemoved(outgoingRemovals.get(Task.class.getSimpleName()));
        syncHistory.setNumOutgoingTimeRegistrationsRemoved(outgoingRemovals.get(TimeRegistration.class.getSimpleName()));

        // incoming sync removal map
        Map<String, Integer> incomingRemovals = countRemovalsPerType(incomingSyncRemovalMap);
        syncHistory.setNumIncomingProjectsRemoved(incomingRemovals.get(Project.class.getSimpleName()));
        syncHistory.setNumIncomingTasksRemoved(incomingRemovals.get(Task.class.getSimpleName()));
        syncHistory.setNumIncomingTimeRegistrationsRemoved(incomingRemovals.get(TimeRegistration.class.getSimpleName()));

        // incoming project changes
        syncHistory.setNumIncomingProjectChanges(incomingProjects.size());

        // incoming task changes
        syncHistory.setNumIncomingTaskChanges(incomingTasks.size());

        // incoming time registration changes
        syncHistory.setNumIncomingTimeRegistrationChanges(incomingTimeRegistrations.size());

        // outgoing project changes
        Map<EntitySyncResolution, Integer> outgoingProjectSyncResults = countSyncsPerResolution(outgoingSyncResult.getProjectSyncResults());
        syncHistory.setNumOutgoingAcceptedProjectChanges(outgoingProjectSyncResults.get(EntitySyncResolution.ACCEPTED));
        syncHistory.setNumOutgoingMergedProjectChanges(outgoingProjectSyncResults.get(EntitySyncResolution.MERGED));
        syncHistory.setNumOutgoingNoActionProjectChanges(outgoingProjectSyncResults.get(EntitySyncResolution.NO_ACTION));
        syncHistory.setNumOutgoingNotAcceptedProjectChanges(outgoingProjectSyncResults.get(EntitySyncResolution.NOT_ACCEPTED));

        // outgoing task changes
        Map<EntitySyncResolution, Integer> outgoingTaskSyncResults = countSyncsPerResolution(outgoingSyncResult.getTaskSyncResults());
        syncHistory.setNumOutgoingAcceptedTaskChanges(outgoingTaskSyncResults.get(EntitySyncResolution.ACCEPTED));
        syncHistory.setNumOutgoingMergedTaskChanges(outgoingTaskSyncResults.get(EntitySyncResolution.MERGED));
        syncHistory.setNumOutgoingNoActionTaskChanges(outgoingTaskSyncResults.get(EntitySyncResolution.NO_ACTION));
        syncHistory.setNumOutgoingNotAcceptedTaskChanges(outgoingTaskSyncResults.get(EntitySyncResolution.NOT_ACCEPTED));

        // outgoing time registration changes
        Map<EntitySyncResolution, Integer> outgoingTimeRegistrationSyncResults = countSyncsPerResolution(outgoingSyncResult.getTimeRegistrationSyncResults());
        syncHistory.setNumOutgoingAcceptedTimeRegistrationChanges(outgoingTimeRegistrationSyncResults.get(EntitySyncResolution.ACCEPTED));
        syncHistory.setNumOutgoingMergedTimeRegistrationChanges(outgoingTimeRegistrationSyncResults.get(EntitySyncResolution.MERGED));
        syncHistory.setNumOutgoingNoActionTimeRegistrationChanges(outgoingTimeRegistrationSyncResults.get(EntitySyncResolution.NO_ACTION));
        syncHistory.setNumOutgoingNotAcceptedTimeRegistrationChanges(outgoingTimeRegistrationSyncResults.get(EntitySyncResolution.NOT_ACCEPTED));
    }

    private Map<String, Integer> countRemovalsPerType(Map<String, String> syncRemovalMap) {
        int projects = 0;
        int tasks = 0;
        int timeRegistrations = 0;

        if (syncRemovalMap != null) {
            for (Map.Entry<String, String> entry : syncRemovalMap.entrySet()) {
                String entityName = entry.getKey();
                if (entityName.equals(Project.class.getSimpleName())) {
                    projects++;
                } else if (entityName.equals(Task.class.getSimpleName())) {
                    tasks++;
                } else if (entityName.equals(TimeRegistration.class.getSimpleName())) {
                    timeRegistrations++;
                }
            }
        }

        Map<String, Integer> result = new HashMap<String, Integer>();
        result.put(Project.class.getSimpleName(), projects);
        result.put(Task.class.getSimpleName(), tasks);
        result.put(TimeRegistration.class.getSimpleName(), timeRegistrations);
        return result;
    }

    private Map<EntitySyncResolution, Integer> countSyncsPerResolution(List syncResults) {
        int accepted = 0;
        int merged = 0;
        int noAction = 0;
        int notAccepted = 0;

        for (Object object : syncResults) {
            EntitySyncResolution syncResolution = null;
            if (object instanceof ProjectSyncResult) {
                syncResolution = ((ProjectSyncResult) object).getResolution();
            } else if (object instanceof TaskSyncResult) {
                syncResolution = ((TaskSyncResult) object).getResolution();
            } else if (object instanceof TimeRegistrationSyncResult) {
                syncResolution = ((TimeRegistrationSyncResult) object).getResolution();
            }

            switch (syncResolution) {
                case ACCEPTED:
                    accepted++;
                    break;
                case MERGED:
                    merged++;
                    break;
                case NO_ACTION:
                    noAction++;
                    break;
                case NOT_ACCEPTED:
                    notAccepted++;
                    break;
            }
        }

        Map<EntitySyncResolution, Integer> result = new HashMap<EntitySyncResolution, Integer>();
        result.put(EntitySyncResolution.ACCEPTED, accepted);
        result.put(EntitySyncResolution.MERGED, merged);
        result.put(EntitySyncResolution.NO_ACTION, noAction);
        result.put(EntitySyncResolution.NOT_ACCEPTED, notAccepted);
        return result;
    }

    @Override
    public boolean isSyncBusy() {
        SyncHistory ongoingSyncHistory = syncHistoryDao.getOngoingSyncHistory();
        if (ongoingSyncHistory != null) {
            long startTime = ongoingSyncHistory.getStarted().getTime();
            if (new Date().getTime() - startTime  > 600000) { // Checks for a 10 minute timeout.
                ongoingSyncHistory.setStatus(SyncHistoryStatus.TIMED_OUT);
                ongoingSyncHistory.setEnded(new Date());
                syncHistoryDao.update(ongoingSyncHistory);
            } else {
                return true;
            }
        }

        return false;
    }

    @Override
    public SyncHistory getLastSyncHistory() {
        return syncHistoryDao.getLastSyncHistory();
    }

    @Override
    public void logout() {
        // Clear all the sync keys...
        List<TimeRegistration> timeRegistrations = timeRegistrationDao.findAll();
        for (TimeRegistration timeRegistration : timeRegistrations) {
            timeRegistration.setSyncKey(null);
            timeRegistrationDao.update(timeRegistration);
        }
        List<Project> projects = projectDao.findAll();
        for (Project project : projects) {
            project.setSyncKey(null);
            projectDao.update(project);
        }
        List<Task> tasks = taskDao.findAll();
        for (Task task : tasks) {
            task.setSyncKey(null);
            taskDao.update(task);
        }

        clearUserAppData();
    }

    @Override
    public void removeAll() {
        clearUserAppData();
    }

    @Override
    public List<SyncHistory> findAllSyncHistories() {
        return syncHistoryDao.findAll();
    }

    private void clearUserAppData() {
        // Remove all the caching data...
        syncRemovalCacheDao.deleteAll();

        // Remove all sync history
        syncHistoryDao.deleteAll();

        // Logout the current logged in user
        User user = accountDao.getLoggedInUser();
        accountDao.delete(user);
        workTimeWebDao.logout(user);
    }
}
