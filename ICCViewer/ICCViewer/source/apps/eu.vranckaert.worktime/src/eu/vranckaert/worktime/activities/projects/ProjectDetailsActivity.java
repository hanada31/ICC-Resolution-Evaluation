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

package eu.vranckaert.worktime.activities.projects;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.activities.reporting.ReportingCriteriaActivity;
import eu.vranckaert.worktime.activities.tasks.AddEditTaskActivity;
import eu.vranckaert.worktime.comparators.project.ProjectByNameComparator;
import eu.vranckaert.worktime.comparators.task.TaskByNameComparator;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.TrackerConstants;
import eu.vranckaert.worktime.enums.reporting.ReportingDisplayDuration;
import eu.vranckaert.worktime.exceptions.*;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.Task;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.service.ProjectService;
import eu.vranckaert.worktime.service.TaskService;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.service.ui.WidgetService;
import eu.vranckaert.worktime.utils.context.AsyncHelper;
import eu.vranckaert.worktime.utils.context.IntentUtil;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.date.DateUtils;
import eu.vranckaert.worktime.utils.preferences.Preferences;
import eu.vranckaert.worktime.utils.punchbar.PunchBarUtil;
import eu.vranckaert.worktime.utils.string.StringUtils;
import eu.vranckaert.worktime.utils.tracker.AnalyticsTracker;
import eu.vranckaert.worktime.utils.view.actionbar.synclock.SyncLockedListActivity;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 28/03/11
 * Time: 18:26
 */
public class ProjectDetailsActivity extends SyncLockedListActivity {
    private static final String LOG_TAG = ProjectDetailsActivity.class.getSimpleName();

    @InjectView(R.id.projectComment)
    private TextView projectComment;

    @InjectView(R.id.txt_project_details_total_time_spent)
    private TextView totalTimeSpent;

    @InjectView(R.id.txt_project_details_punch_in_count)
    private TextView punchInCount;

    @InjectExtra(Constants.Extras.PROJECT)
    private Project project;

    @Inject
    private TaskService taskService;

    @Inject
    private WidgetService widgetService;

    @Inject
    private ProjectService projectService;

    @Inject
    private TimeRegistrationService timeRegistrationService;

    private List<Task> tasksForProject;
    private List<TimeRegistration> registrationsForProject;
    private Task taskToRemove;
    private boolean projectUpdated = false;
    private Project projectToRemove = null;
    private boolean initialLoad = true;

    private AnalyticsTracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_details);

        setDisplayHomeAsUpEnabled(true);

        tracker = AnalyticsTracker.getInstance(getApplicationContext());
        tracker.trackPageView(TrackerConstants.PageView.PROJECTS_DETAILS_ACTIVITY);

        buildUI();

        registerForContextMenu(getListView());
    }

    private void buildUI() {
        Log.d(getApplicationContext(), LOG_TAG, "Project found with id " + project.getId() + " and name " + project.getName());
        setTitle(project.getName());
        if (StringUtils.isNotBlank(project.getComment())) {
            projectComment.setText(project.getComment());
        }
        loadProjectTasks(project);
    }

    private void loadProjectTasks(final Project project) {
        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object... objects) {
                Project project = (Project) objects[0];
                if (Preferences.getDisplayTasksHideFinished(getApplicationContext())) {
                    tasksForProject = taskService.findNotFinishedTasksForProject(project);
                } else {
                    tasksForProject = taskService.findTasksForProject(project);
                }
                Collections.sort(tasksForProject, new TaskByNameComparator());

                return null;
            }

            @Override
            protected void onPostExecute(Object result) {
                ManageTasksListAdapter adapter = new ManageTasksListAdapter(tasksForProject);
                adapter.notifyDataSetChanged();
                setListAdapter(adapter);

                loadProjectDetails(project);
            }
        };
        AsyncHelper.startWithParams(asyncTask, new Object[]{project});
    }

    /**
     *
     * @param menuItem
     * @param hideFinished
     */
    private void switchMenuItemFinishedProjects(MenuItem menuItem, boolean hideFinished) {
        if (hideFinished) {
            menuItem.setIcon(R.drawable.ic_navigation_accept);
            menuItem.setTitle(R.string.project_details_ab_menu_show_all_tasks);
        } else {
            menuItem.setIcon(R.drawable.ic_navigation_cancel);
            menuItem.setTitle(R.string.project_details_ab_menu_show_finished_tasks);
        }
    }

    private void loadProjectDetails(final Project project) {
        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object... objects) {
                List<Task> allTasksForProject = taskService.findTasksForProject(project);
                registrationsForProject = timeRegistrationService.getTimeRegistrationForTasks(allTasksForProject);
                String totalDuration = DateUtils.TimeCalculator.calculatePeriod(
                        ProjectDetailsActivity.this,
                        registrationsForProject,
                        ReportingDisplayDuration.HOUR_MINUTES_SECONDS
                );
                return totalDuration;
            }

            @Override
            protected void onPostExecute(Object result) {
                totalTimeSpent.setText(String.valueOf(result));
                punchInCount.setText(String.valueOf(registrationsForProject.size()));
            }
        };
        AsyncHelper.start(asyncTask);
    }

    private class ManageTasksListAdapter extends ArrayAdapter<Task> {
        private final String LOG_TAG = ManageTasksListAdapter.class.getSimpleName();
        /**
         * {@inheritDoc}
         */
        public ManageTasksListAdapter(List<Task> tasks) {
            super(ProjectDetailsActivity.this, R.layout.list_item_tasks, tasks);
            Log.d(getApplicationContext(), LOG_TAG, "Creating the manage projects list adapater");
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(getApplicationContext(), LOG_TAG, "Start rendering/recycling row " + position);
            View row;
            final Task taskToBeRendered = tasksForProject.get(position);
            Log.d(getApplicationContext(), LOG_TAG, "Got taskToBeRendered with name " + taskToBeRendered.getName());

            if (convertView == null) {
                Log.d(getApplicationContext(), LOG_TAG, "Render a new line in the list");
                row = getLayoutInflater().inflate(R.layout.list_item_tasks, parent, false);
            } else {
                Log.d(getApplicationContext(), LOG_TAG, "Recycling an existing line in the list");
                row = convertView;
            }

            updateRow(row, taskToBeRendered);

            Log.d(getApplicationContext(), LOG_TAG, "Done rendering row " + position);
            return row;
        }
    }

    private void updateRow(View row, final Task taskToBeRendered) {
        Log.d(getApplicationContext(), LOG_TAG, "Ready to update the name of the taskToBeRendered of the listitem...");
        TextView taskName = (TextView) row.findViewById(R.id.task_name_listitem);
        taskName.setText(taskToBeRendered.getName());

        Log.d(getApplicationContext(), LOG_TAG, "Ready to set the finished flag (" + taskToBeRendered.isFinished() + ") ...");
        if (taskToBeRendered.isFinished()) {
            taskName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_finished, 0, 0, 0);
        } else {
            taskName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    private void changeTaskFinished(boolean finished, int listIndex) {
        Task task = tasksForProject.get(listIndex);
        TimeRegistration timeRegistration = timeRegistrationService.getLatestTimeRegistration();
        if (timeRegistration != null) {
            taskService.refresh(timeRegistration.getTask());
            if (timeRegistration.isOngoingTimeRegistration() && timeRegistration.getTask().getId() == task.getId()) {
                showDialog(Constants.Dialog.WARN_TASK_NOT_FINISHED_ONGOING_TR);
                return;
            }
        }

        task.setFinished(finished);
        taskService.update(task);

        if (finished) {
            tracker.trackEvent(
                TrackerConstants.EventSources.PROJECT_DETAILS_ACTIVITY,
                TrackerConstants.EventActions.MARK_TASK_FINISHED
            );
        } else {
            tracker.trackEvent(
                TrackerConstants.EventSources.PROJECT_DETAILS_ACTIVITY,
                TrackerConstants.EventActions.MARK_TASK_UNFINISHED
            );
        }

        int firstVisiblePosition = getListView().getFirstVisiblePosition();
        if (Preferences.getDisplayTasksHideFinished(getApplicationContext())) {
            loadProjectTasks(project);
        } else {
            View row = getListView().getChildAt(listIndex - firstVisiblePosition);
            updateRow(row, task);
        }
    }

    /**
     * Delete a task.
     * @param task The task to delete.
     * @param askConfirmation If a confirmation should be requested to the user. If so the delete will no be executed
     * but a show dialog is called form where you have to call this method again with the askConfirmation parameter set
     * @param force If set to {@link Boolean#TRUE} all {@link eu.vranckaert.worktime.model.TimeRegistration} instances
     * linked to the task will be deleted first, then the project. If set to {@link Boolean#FALSE} nothing will
     * happen.
     */
    private void deleteTask(Task task, boolean askConfirmation, boolean force) {
        if (askConfirmation) {
            Log.d(getApplicationContext(), LOG_TAG, "Asking confirmation to remove a task");
            taskToRemove = task;
            showDialog(Constants.Dialog.DELETE_TASK_YES_NO);
        } else {
            Log.d(getApplicationContext(), LOG_TAG, "Removing a task... Are we forcing the removal? " + force);
            taskToRemove = null;
            try {
                Log.d(getApplicationContext(), LOG_TAG, "Ready to actually remove the task!");

                taskService.remove(task, force);
                tracker.trackEvent(
                    TrackerConstants.EventSources.PROJECT_DETAILS_ACTIVITY,
                    TrackerConstants.EventActions.DELETE_TASK
                );
                Log.d(getApplicationContext(), LOG_TAG, "Task removed, ready to reload tasks");
                loadProjectTasks(project);

                widgetService.updateAllWidgets();
            } catch (TaskStillInUseException e) {
                if (force) {
                    Log.d(getApplicationContext(), LOG_TAG, "Something is wrong. Forcing the time registrations to be deleted should not result"
                        + "in this exception!");
                } else {
                    taskToRemove = task;
                    showDialog(Constants.Dialog.DELETE_TIME_REGISTRATIONS_OF_TASK_YES_NO);
                }
            } catch (AtLeastOneTaskRequiredException e) {
                showDialog(Constants.Dialog.DELETE_TASK_AT_LEAST_ONE_REQUIRED);
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int dialogId) {
        Dialog dialog = null;
        switch(dialogId) {
            case Constants.Dialog.DELETE_TASK_YES_NO: {
                AlertDialog.Builder alertRemoveAllRegs = new AlertDialog.Builder(this);
				alertRemoveAllRegs.setTitle(taskToRemove.getName())
						   .setMessage(R.string.msg_delete_task_confirmation)
						   .setCancelable(false)
						   .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									deleteTask(taskToRemove, false, false);
                                    removeDialog(Constants.Dialog.DELETE_TASK_YES_NO);
								}
							})
						   .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
                                    taskToRemove = null;
									removeDialog(Constants.Dialog.DELETE_TASK_YES_NO);
								}
							});
				dialog = alertRemoveAllRegs.create();
                break;
            }
            case Constants.Dialog.DELETE_TIME_REGISTRATIONS_OF_TASK_YES_NO: {
                AlertDialog.Builder alertRemoveAllRegs = new AlertDialog.Builder(this);
				alertRemoveAllRegs.setTitle(taskToRemove.getName())
						   .setMessage(R.string.msg_delete_task_and_linked_time_registrations_confirmation)
						   .setCancelable(false)
						   .setPositiveButton(R.string.lbl_delete_all, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									deleteTask(taskToRemove, false, true);
                                    removeDialog(Constants.Dialog.DELETE_TIME_REGISTRATIONS_OF_TASK_YES_NO);
								}
							})
						   .setNegativeButton(R.string.lbl_do_nothing, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
                                    taskToRemove = null;
									removeDialog(Constants.Dialog.DELETE_TIME_REGISTRATIONS_OF_TASK_YES_NO);
								}
							});
				dialog = alertRemoveAllRegs.create();
                break;
            }
            case Constants.Dialog.DELETE_PROJECT_YES_NO: {
                AlertDialog.Builder alertRemoveAllRegs = new AlertDialog.Builder(this);
				alertRemoveAllRegs.setTitle(projectToRemove.getName())
						   .setMessage(R.string.msg_delete_project_confirmation)
						   .setCancelable(false)
						   .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									deleteProject(projectToRemove, false, false);
                                    removeDialog(Constants.Dialog.DELETE_PROJECT_YES_NO);
								}
							})
						   .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
                                    projectToRemove = null;
									removeDialog(Constants.Dialog.DELETE_PROJECT_YES_NO);
								}
							});
				dialog = alertRemoveAllRegs.create();
                break;
            }
            case Constants.Dialog.DELETE_ALL_TASKS_OF_PROJECT_YES_NO: {
                AlertDialog.Builder alertRemoveProjectNotPossible = new AlertDialog.Builder(this);
                alertRemoveProjectNotPossible.setTitle(projectToRemove.getName())
                        .setMessage(R.string.msg_delete_project_and_all_tasks)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                deleteProject(projectToRemove, false, true);
                                removeDialog(Constants.Dialog.DELETE_ALL_TASKS_OF_PROJECT_YES_NO);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                projectToRemove = null;
                                removeDialog(Constants.Dialog.DELETE_ALL_TASKS_OF_PROJECT_YES_NO);
                            }
                        });
                dialog = alertRemoveProjectNotPossible.create();
                break;
            }
            case Constants.Dialog.DELETE_ALL_TASKS_AND_TIME_REGISTRATIONS_OF_PROJECT_YES_NO: {
                AlertDialog.Builder alertRemoveProjectNotPossible = new AlertDialog.Builder(this);
                alertRemoveProjectNotPossible.setTitle(projectToRemove.getName())
                        .setMessage(R.string.msg_delete_project_and_all_tasks_and_all_time_registrations)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                deleteProject(projectToRemove, false, true);
                                removeDialog(Constants.Dialog.DELETE_ALL_TASKS_AND_TIME_REGISTRATIONS_OF_PROJECT_YES_NO);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                projectToRemove = null;
                                removeDialog(Constants.Dialog.DELETE_ALL_TASKS_AND_TIME_REGISTRATIONS_OF_PROJECT_YES_NO);
                            }
                        });
                dialog = alertRemoveProjectNotPossible.create();
                break;
            }
            case Constants.Dialog.WARN_ONGOING_TR: {
                AlertDialog.Builder ongoingTrDialog = new AlertDialog.Builder(this);
                ongoingTrDialog.setTitle(projectToRemove.getName())
                        .setMessage(R.string.msg_delete_project_ongoing_time_registration)
                        .setCancelable(true)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                projectToRemove = null;
                                removeDialog(Constants.Dialog.WARN_ONGOING_TR);
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                projectToRemove = null;
                                removeDialog(Constants.Dialog.WARN_ONGOING_TR);
                            }
                        });;
                dialog = ongoingTrDialog.create();
                break;
            }
            case Constants.Dialog.WARN_TASK_NOT_FINISHED_ONGOING_TR: {
                AlertDialog.Builder warnTaskNotFinishedOngoingTr = new AlertDialog.Builder(this);
				warnTaskNotFinishedOngoingTr
						   .setMessage(R.string.msg_mark_task_finished_not_possible_ongoing_tr)
						   .setCancelable(true)
						   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int which) {
                                   removeDialog(Constants.Dialog.WARN_TASK_NOT_FINISHED_ONGOING_TR);
                               }
                           })
                           .setOnCancelListener(new DialogInterface.OnCancelListener() {
                               @Override
                               public void onCancel(DialogInterface dialogInterface) {
                                   removeDialog(Constants.Dialog.WARN_TASK_NOT_FINISHED_ONGOING_TR);
                               }
                           });
				dialog = warnTaskNotFinishedOngoingTr.create();
                break;
            }
            case Constants.Dialog.DELETE_TASK_AT_LEAST_ONE_REQUIRED: {
                AlertDialog.Builder deleteTaskDialog = new AlertDialog.Builder(this);
                deleteTaskDialog
                        .setMessage(R.string.msg_delete_task_unavailable_one_required_per_project)
                        .setCancelable(true)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                removeDialog(Constants.Dialog.DELETE_TASK_AT_LEAST_ONE_REQUIRED);
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                removeDialog(Constants.Dialog.DELETE_TASK_AT_LEAST_ONE_REQUIRED);
                            }
                        });
                dialog = deleteTaskDialog.create();
                break;
            }
        };
        return dialog;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.IntentRequestCodes.ADD_TASK:
            case Constants.IntentRequestCodes.EDIT_TASK:
                if (resultCode == Activity.RESULT_OK) {
                    loadProjectTasks(project);
                }
                break;
            case Constants.IntentRequestCodes.EDIT_PROJECT:
                if (resultCode == Activity.RESULT_OK) {
                    project = (Project) data.getExtras().get(Constants.Extras.PROJECT);
                    setTitle(project.getName());
                    if (StringUtils.isNotBlank(project.getComment())) {
                        projectComment.setText(project.getComment());
                    } else {
                        projectComment.setText(R.string.lbl_project_details_no_comment);
                    }

                    projectUpdated = true;
                }
                break;
            case Constants.IntentRequestCodes.START_TIME_REGISTRATION:
                PunchBarUtil.configurePunchBar(ProjectDetailsActivity.this, timeRegistrationService, taskService, projectService);
                break;
            case Constants.IntentRequestCodes.END_TIME_REGISTRATION:
                PunchBarUtil.configurePunchBar(ProjectDetailsActivity.this, timeRegistrationService, taskService, projectService);
                break;
            case Constants.IntentRequestCodes.SYNC_BLOCKING_ACTIVITY:
                if (projectService.checkProjectExisting(project)) {
                    if (projectService.checkReloadProject(project)) {
                        projectService.refresh(project);
                        buildUI();
                    }
                } else {
                    setResult(RESULT_OK);
                    finish();
                }
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Log.d(getApplicationContext(), LOG_TAG, "In method onCreateContextMenu(...)");
        if (v.getId() == android.R.id.list) {
            super.onCreateContextMenu(menu, v, menuInfo);
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            int element = info.position;
            Log.d(getApplicationContext(), LOG_TAG, "Creating context menu for element " + element + " in list");
            Task taskForContext = tasksForProject.get(element);

            menu.setHeaderTitle(taskForContext.getName());

            menu.add(Menu.NONE,
                    Constants.ContentMenuItemIds.TASK_EDIT,
                    Menu.NONE,
                    R.string.lbl_tasks_menu_edit
            );

            menu.add(Menu.NONE,
                    Constants.ContentMenuItemIds.TASK_MOVE,
                    Menu.NONE,
                    R.string.lbl_tasks_menu_move
            );

            if (taskForContext.isFinished()) {
                menu.add(Menu.NONE,
                    Constants.ContentMenuItemIds.TASK_MARK_UNFINISHED,
                    Menu.NONE,
                    R.string.lbl_tasks_menu_mark_unfinished
                );
            } else {
                menu.add(Menu.NONE,
                    Constants.ContentMenuItemIds.TASK_MARK_FINISHED,
                    Menu.NONE,
                    R.string.lbl_tasks_menu_mark_finished
                );
            }

            menu.add(Menu.NONE,
                     Constants.ContentMenuItemIds.TASK_REPORTING,
                     Menu.NONE,
                     R.string.lbl_tasks_menu_reporting
            );
            menu.add(Menu.NONE,
                    Constants.ContentMenuItemIds.TASK_DELETE,
                    Menu.NONE,
                    R.string.lbl_tasks_menu_delete
            );
            menu.add(Menu.NONE,
                    Constants.ContentMenuItemIds.TASK_ADD,
                    Menu.NONE,
                    R.string.lbl_tasks_menu_add
            );
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int element = info.position;

        Task taskForContext = tasksForProject.get(element);
        switch (item.getItemId()) {
            case Constants.ContentMenuItemIds.TASK_EDIT: {
                openEditTaskActivity(taskForContext);
                break;
            }
            case Constants.ContentMenuItemIds.TASK_MOVE: {
                moveTaskFromProject(taskForContext, project);
                break;
            }
            case Constants.ContentMenuItemIds.TASK_MARK_FINISHED: {
                changeTaskFinished(true, element);
                break;
            }
            case Constants.ContentMenuItemIds.TASK_MARK_UNFINISHED: {
                changeTaskFinished(false, element);
                break;
            }
            case Constants.ContentMenuItemIds.TASK_REPORTING: {
                openReportingCriteriaActivity(project, taskForContext);
                break;
            }
            case Constants.ContentMenuItemIds.TASK_DELETE: {
                deleteTask(taskForContext, true, false);
                break;
            }
            case Constants.ContentMenuItemIds.TASK_ADD: {
                openAddTaskActivity();
                break;
            }
            default: {
                return false;
            }
        }
        return true;
    }

    /**
     * Opens the edit task. Activity starts for result!
     * @param task The task to edit.
     */
    private void openEditTaskActivity(Task task) {
        Intent intent = new Intent(ProjectDetailsActivity.this, AddEditTaskActivity.class);
        intent.putExtra(Constants.Extras.TASK, task);
        intent.putExtra(Constants.Extras.PROJECT, project);
        startActivityForResult(intent, Constants.IntentRequestCodes.EDIT_TASK);
    }

    /**
     * Opens the add task activity. Activity starts for result!
     */
    private void openAddTaskActivity() {
        Intent intent = new Intent(ProjectDetailsActivity.this, AddEditTaskActivity.class);
        intent.putExtra(Constants.Extras.PROJECT, project);
        startActivityForResult(intent, Constants.IntentRequestCodes.ADD_TASK);
    }

    /**
     * Opens the edit project activity. Activity starts for result!
     * @param project The project to edit.
     */
    private void openEditProjectActivity(Project project) {
        Intent intent = new Intent(getApplicationContext(), AddEditProjectActivity.class);
        intent.putExtra(Constants.Extras.PROJECT, project);
        startActivityForResult(intent, Constants.IntentRequestCodes.EDIT_PROJECT);
    }

    private void openReportingCriteriaActivity(Project project) {
        Intent intent = new Intent(getApplicationContext(), ReportingCriteriaActivity.class);
        intent.putExtra(Constants.Extras.PROJECT, project);
        startActivity(intent);
    }

    private void openReportingCriteriaActivity(Project project, Task task) {
        Intent intent = new Intent(getApplicationContext(), ReportingCriteriaActivity.class);
        intent.putExtra(Constants.Extras.PROJECT, project);
        intent.putExtra(Constants.Extras.TASK, task);
        startActivity(intent);
    }
    
    public void showHideFinishedTasks() {
        boolean hide = Preferences.getDisplayTasksHideFinished(ProjectDetailsActivity.this);
        Preferences.setDisplayTasksHideFinished(ProjectDetailsActivity.this, !hide);

        tasksForProject.clear();
        ManageTasksListAdapter adapter = new ManageTasksListAdapter(tasksForProject);
        adapter.notifyDataSetChanged();
        setListAdapter(adapter);

        loadProjectTasks(project);
    }

    public void onPunchButtonClick(View view) {
        PunchBarUtil.onPunchButtonClick(ProjectDetailsActivity.this, timeRegistrationService);
    }

    /**
     * Delete a project.
     * @param project The project to delete.
     * @param askConfirmation If a confirmation should be requested to the user. If so the delete will no be executed
     * but a show dialog is called form where you have to call this method again with the askConfirmation parameter set
     */
    private void deleteProject(Project project, boolean askConfirmation, boolean force) {
        if (askConfirmation) {
            Log.d(getApplicationContext(), LOG_TAG, "Asking confirmation to remove a project");
            projectToRemove = project;
            showDialog(Constants.Dialog.DELETE_PROJECT_YES_NO);
        } else {
            try {
                Log.d(getApplicationContext(), LOG_TAG, "Ready to actually remove the project!");
                projectService.remove(project, force);
                tracker.trackEvent(
                        TrackerConstants.EventSources.PROJECT_DETAILS_ACTIVITY,
                        TrackerConstants.EventActions.DELETE_PROJECT
                );
                Log.d(getApplicationContext(), LOG_TAG, "Project removed, closing it's detail activity");
                projectUpdated = true;
                projectToRemove = null;

                widgetService.updateAllWidgets();

                finish();
            } catch (AtLeastOneProjectRequiredException e) {
                Toast.makeText(ProjectDetailsActivity.this, R.string.msg_delete_project_at_least_one_required,  Toast.LENGTH_LONG).show();
            } catch (ProjectStillHasTasks e) {
                if (e.hasTimeRegistrations()) {
                    showDialog(Constants.Dialog.DELETE_ALL_TASKS_AND_TIME_REGISTRATIONS_OF_PROJECT_YES_NO);
                } else {
                    showDialog(Constants.Dialog.DELETE_ALL_TASKS_OF_PROJECT_YES_NO);
                }
            } catch (ProjectHasOngoingTimeRegistration e) {
                showDialog(Constants.Dialog.WARN_ONGOING_TR);
            }
        }
    }

    /**
     * Move a task. This will show a selection dialog where one project can be selected to move the task to. This method
     * will not perform the move itself!
     * @param task The task to be moved.
     */
    private void moveTaskFromProject(final Task task, final Project fromProject) {
        final List<Project> availableProjects = projectService.findAll();
        Collections.sort(availableProjects, new ProjectByNameComparator());
        Log.d(getApplicationContext(), LOG_TAG, availableProjects.size() + " projects found");

        List<String> projectList = new ArrayList<String>();
        int indexOfCurrentProject = -1;
        for (Project p : availableProjects) {
            if (!p.getId().equals(fromProject.getId())) {
                projectList.add(p.getName());
            } else {
                indexOfCurrentProject = availableProjects.indexOf(p);
            }
        }
        if (indexOfCurrentProject > -1) {
            availableProjects.remove(indexOfCurrentProject);
        }
        Log.d(getApplicationContext(), LOG_TAG, availableProjects.size() + " projects to choose from");
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.lbl_move_task_title)
                .setSingleChoiceItems(
                        StringUtils.convertListToArray(projectList),
                        -1,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int index) {
                                Log.d(getApplicationContext(), LOG_TAG, "Project at index " + index + " choosen.");
                                Project selectedProject = availableProjects.get(index);
                                Log.d(getApplicationContext(), LOG_TAG, "Changing task to project " + selectedProject.getName());
                                moveTaskToProject(task, fromProject, selectedProject);
                                dialogInterface.dismiss();
                            }
                        }
                )
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialogInterface) {
                        Log.d(getApplicationContext(), LOG_TAG, "No project chosen, closing the dialog");
                    }
                });
        Dialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Move a task to a certain project.
     * @param task The task to be moved.
     * @param toProject The project to which the task should be moved.
     */
    private void moveTaskToProject(Task task, Project fromProject, Project toProject) {
        Log.d(getApplicationContext(), LOG_TAG, "About to move the task away from project " + fromProject.getName() + ", to " + toProject.getName());
        task.setProject(toProject);
        taskService.update(task);
        tracker.trackEvent(
                TrackerConstants.EventSources.PROJECT_DETAILS_ACTIVITY,
                TrackerConstants.EventActions.MOVE_TASK
        );
        Log.d(getApplicationContext(), LOG_TAG, "Task has been moved!");
        loadProjectTasks(project);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.ab_activity_project_details, menu);

        MenuItem menuItem = menu.getItem(3);
        boolean hideFinished = Preferences.getDisplayTasksHideFinished(ProjectDetailsActivity.this);
        switchMenuItemFinishedProjects(menuItem, hideFinished);

        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                IntentUtil.goBack(ProjectDetailsActivity.this);
                break;
            case R.id.menu_project_details_activity_edit:
                openEditProjectActivity(project);
                break;
            case R.id.menu_project_details_activity_delete:
                deleteProject(project, true, false);
                break;
            case R.id.menu_project_details_activity_add_task:
                openAddTaskActivity();
                break;
            case R.id.menu_project_details_activity_switch_finished_tasks:
                boolean hideFinished = !Preferences.getDisplayTasksHideFinished(ProjectDetailsActivity.this);
                switchMenuItemFinishedProjects(item, hideFinished);
                Preferences.setDisplayProjectsHideFinished(ProjectDetailsActivity.this, hideFinished);
                showHideFinishedTasks();
                break;
            case R.id.menu_project_details_activity_reporting:
                openReportingCriteriaActivity(project);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PunchBarUtil.configurePunchBar(ProjectDetailsActivity.this, timeRegistrationService, taskService, projectService);

        if (initialLoad) {
            initialLoad = false;
            return;
        }

        loadProjectDetails(project);
    }

    @Override
    public void finish() {
        if (projectUpdated) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tracker.stopSession();
    }
}
