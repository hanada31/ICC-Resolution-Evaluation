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

package eu.vranckaert.worktime.dao.web;

import eu.vranckaert.worktime.dao.web.model.response.sync.WorkTimeSyncResponse;
import eu.vranckaert.worktime.exceptions.network.NoNetworkConnectionException;
import eu.vranckaert.worktime.exceptions.worktime.account.*;
import eu.vranckaert.worktime.exceptions.worktime.sync.CorruptSyncDataException;
import eu.vranckaert.worktime.exceptions.worktime.sync.SyncAlreadyBusyException;
import eu.vranckaert.worktime.exceptions.worktime.sync.SynchronizationFailedException;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.Task;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.model.User;
import eu.vranckaert.worktime.web.json.JsonWebService;
import eu.vranckaert.worktime.web.json.exception.GeneralWebException;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User: Dirk Vranckaert
 * Date: 12/12/12
 * Time: 11:36
 */
public interface WorkTimeWebDao extends JsonWebService {
    /**
     * Login the user with the specified email and password to the WorkTime webservice.
     * @param email The email.
     * @param password The password in plain text.
     * @return The session key of the registered user.
     * @throws NoNetworkConnectionException No working network connection is found.
     * @throws GeneralWebException Some kind of exception occurred during the web request.
     * @throws eu.vranckaert.worktime.exceptions.worktime.account.LoginCredentialsMismatchException The credentials provided are not correct and so the user is not logged
     * in!
     */
    String login(String email, String password) throws NoNetworkConnectionException, GeneralWebException, LoginCredentialsMismatchException;

    /**
     * Register a new user-account with the provided details to the WorkTime webservice.
     * @param email The email of the user.
     * @param firstName The first name of the user.
     * @param lastName The last name of the user.
     * @param password The chosen password in plain text.
     * @return The session key of the registered user.
     * @throws NoNetworkConnectionException No working network connection is found.
     * @throws GeneralWebException Some kind of exception occurred during the web request.
     * @throws eu.vranckaert.worktime.exceptions.worktime.account.RegisterEmailAlreadyInUseException If an account already exists for this email address.
     * @throws eu.vranckaert.worktime.exceptions.worktime.account.PasswordLengthValidationException If the password length is invalid (< 6 or > 30 characters).
     */
    String register(String email, String firstName, String lastName, String password) throws NoNetworkConnectionException, GeneralWebException, RegisterEmailAlreadyInUseException, PasswordLengthValidationException, RegisterFieldRequiredException;

    /**
     * Loads the profile of a certain user.
     * @param user A {@link User} object that contains at least the email address of the user and the session key with
     *             which the user has logged in.
     * @return The full user profile.
     * @throws NoNetworkConnectionException No working network connection is found.
     * @throws GeneralWebException Some kind of exception occurred during the web request.
     * @throws eu.vranckaert.worktime.exceptions.worktime.account.UserNotLoggedInException The user is not logged in, authentication failed...
     */
    User loadProfile(User user) throws NoNetworkConnectionException, GeneralWebException, UserNotLoggedInException;

    /**
     * Sync all of the provided data with the remote WorkTime server.
     * @param user                      The user for which the sync is to be executed.
     * @param conflictConfiguration     The conflict configuration as configured in the preferences (SERVER or CLIENT).
     * @param lastSuccessfulSyncDate    The date of the last successful sync (if any, otherwise null).
     * @param projects                  The list of projects to be synced with the server.
     * @param tasks                     The list of tasks to be synced with the server.
     * @param timeRegistrations         The list of time registrations to be synced with the server.
     * @param syncRemovalMap            The map of syncKeys and entity-names (Project, Task or TimeRegistration) that
     *                                  have been removed since the last sync and thus and should be removed on the
     *                                  server also.
     * @return The synchronization returns a list of different object types.<br/>
     * Each position in the list contains a well-defined object:<br/>
     * 1. The list of projects on the server since the last sync.<br/>
     * 2. The list of tasks on the server since the last sync.<br/>
     * 3. The list of {@link TimeRegistration}s on the server since the last sync.<br/>
     * 4. The sync result which contains for each entity that has been sent to the server a result of what has been done
     * with it on the server during the synchronization process.<br/>
     * 5. The map of sync-keys and entity-names that have been removed on the server since the last synchronization and
     * thus should also be removed on the client.
     * @throws NoNetworkConnectionException Throw if no network connection is available when making the call to the
     * remote server.
     * @throws GeneralWebException Thrown if anything goes wrong while calling the remote server or if anything went
     * wrong on the server internally during the call.
     * @throws UserNotLoggedInException Throw if the user is not logged in on the server or the email address and
     * session key do not match.
     * @throws SynchronizationFailedException Thrown if the synchronization failed on the server due to any kind of
     * error.
     * @throws SyncAlreadyBusyException Thrown if the synchronization is already busy on the server for this user. The
     * server timeout is set to 5 minutes.
     * @throws CorruptSyncDataException Thrown if the data sent to the server (projects, tasks and time registrations)
     * is corrupt.
     */
    List<Object> sync(User user, String conflictConfiguration, Date lastSuccessfulSyncDate, List<Project> projects, List<Task> tasks, List<TimeRegistration> timeRegistrations, Map<String, String> syncRemovalMap) throws NoNetworkConnectionException, GeneralWebException, UserNotLoggedInException, SynchronizationFailedException, SyncAlreadyBusyException, CorruptSyncDataException;

    /**
     * Logout the currently logged in user using the email and session key provided in the {@link User object}.
     * @param user The logged in user.
     */
    void logout(User user);
}
