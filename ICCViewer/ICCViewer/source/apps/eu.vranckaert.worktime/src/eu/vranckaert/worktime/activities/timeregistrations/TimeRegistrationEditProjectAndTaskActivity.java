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

package eu.vranckaert.worktime.activities.timeregistrations;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.activities.projects.AddEditProjectActivity;
import eu.vranckaert.worktime.activities.tasks.AddEditTaskActivity;
import eu.vranckaert.worktime.comparators.project.ProjectByNameComparator;
import eu.vranckaert.worktime.comparators.task.TaskByNameComparator;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.Task;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.service.ProjectService;
import eu.vranckaert.worktime.service.TaskService;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.service.ui.StatusBarNotificationService;
import eu.vranckaert.worktime.service.ui.WidgetService;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.preferences.Preferences;
import eu.vranckaert.worktime.utils.string.StringUtils;
import eu.vranckaert.worktime.utils.view.actionbar.synclock.SyncLockedGuiceActivity;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectExtra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 28/04/11
 * Time: 13:48
 */
public class TimeRegistrationEditProjectAndTaskActivity extends SyncLockedGuiceActivity {
    private static final String LOG_TAG = TimeRegistrationEditProjectAndTaskActivity.class.getSimpleName();

    @InjectExtra(Constants.Extras.TIME_REGISTRATION)
    private TimeRegistration timeRegistration;

    @Inject
    private TimeRegistrationService timeRegistrationService;

    @Inject
    private ProjectService projectService;

    @Inject
    private WidgetService widgetService;

    @Inject
    private StatusBarNotificationService statusBarNotificationService;

    @Inject
    private TaskService taskService;

    private Project newSelectedProject = null;
    private Project originalProject = null;

    private Task newSelectedTask = null;

    private List<Project> availableProjects = null;
    private List<Task> availableTasks = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        availableProjects = loadAllProjects();
        originalProject = timeRegistration.getTask().getProject();
        newSelectedProject = originalProject;
        newSelectedTask = timeRegistration.getTask();

        showDialog(Constants.Dialog.CHOOSE_SELECTED_PROJECT);
    }

    /**
     * Find all projects and sort by name.
     */
    private List<Project> loadAllProjects() {
        List<Project> availableProjects = new ArrayList<Project>();
        if (Preferences.getSelectProjectHideFinished(TimeRegistrationEditProjectAndTaskActivity.this)) {
            Log.d(getApplicationContext(), LOG_TAG, "About to load unfinished projects...");
            availableProjects = projectService.findUnfinishedProjects();
        } else {
            Log.d(getApplicationContext(), LOG_TAG, "About to load all projects...");
            availableProjects = projectService.findAll();
        }
        Log.d(getApplicationContext(), LOG_TAG, availableProjects.size() + " projects found!");
        Collections.sort(availableProjects, new ProjectByNameComparator());
        Log.d(getApplicationContext(), LOG_TAG, "All projects have been sorted, " + availableProjects.size() + " will be returned");
        return availableProjects;
    }

    private List<Task> loadAllTasks() {
        List<Task> availableTasks;
        if (Preferences.getSelectTaskHideFinished(getApplicationContext())) {
            availableTasks = taskService.findNotFinishedTasksForProject(newSelectedProject);
        } else {
            availableTasks = taskService.findTasksForProject(newSelectedProject);
        }
        Collections.sort(availableTasks, new TaskByNameComparator());
        return availableTasks;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;

        switch (id) {
            case Constants.Dialog.CHOOSE_SELECTED_PROJECT: {
                int selectedProjectIndex = -1;

                List<String> projects = new ArrayList<String>();
                for (int i=0; i<availableProjects.size(); i++) {
                    Project project = availableProjects.get(i);

                    Log.d(getApplicationContext(), LOG_TAG, "Is project " + project.getName() + " selected project? " + (newSelectedProject.getId().equals(project.getId())));
                    if (newSelectedProject.getId().equals(project.getId())) {
                        selectedProjectIndex = i;
                    }

                    projects.add(project.getName());
                    Log.d(getApplicationContext(), LOG_TAG, "Project with name " + project.getName() + " is added to the selection list");
                }

                projects.add(getString(R.string.lbl_widget_select_new_project_task));

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.lbl_widget_title_select_project)
                       .setSingleChoiceItems(
                               StringUtils.convertListToArray(projects),
                               selectedProjectIndex,
                               new DialogInterface.OnClickListener() {
                                   public void onClick(DialogInterface dialogInterface, int index) {
                                       if (index != availableProjects.size()) {
                                           newSelectedProject = availableProjects.get(index);

                                           availableTasks = loadAllTasks();

                                           removeDialog(Constants.Dialog.CHOOSE_SELECTED_PROJECT);
                                           showDialog(Constants.Dialog.CHOOSE_TASK);
                                       } else {
                                           removeDialog(Constants.Dialog.CHOOSE_SELECTED_PROJECT);
                                           Intent intent = new Intent(TimeRegistrationEditProjectAndTaskActivity.this, AddEditProjectActivity.class);
                                           startActivityForResult(intent, Constants.IntentRequestCodes.ADD_PROJECT);
                                       }
                                   }
                               }
                       )
                       .setOnCancelListener(new DialogInterface.OnCancelListener() {
                           public void onCancel(DialogInterface dialogInterface) {
                               finish();
                           }
                       });
                dialog = builder.create();
                break;
            }
            case Constants.Dialog.CHOOSE_TASK: {
                Log.d(getApplicationContext(), LOG_TAG, "Default value of selectedTask is id: " + newSelectedTask.getId() + " and name: " + newSelectedTask.getName());
                int selectedTaskIndex = -1;

                List<String> tasks = new ArrayList<String>();
                for (int i=0; i<availableTasks.size(); i++) {
                    Task task = availableTasks.get(i);
                    Log.d(getApplicationContext(), LOG_TAG, "Add task with name " + task.getName() + " to selection list");
                    tasks.add(task.getName());
                    Log.d(getApplicationContext(), LOG_TAG, "Is task " + task.getName() + " selected task? " + (task.getId().equals(newSelectedTask.getId())));
                    if (task.getId().equals(newSelectedTask.getId())) {
                        Log.d(getApplicationContext(), LOG_TAG, "Task should be default selected...");
                        selectedTaskIndex = i;
                    }
                    Log.d(getApplicationContext(), LOG_TAG, "Selected task index is: " + selectedTaskIndex);
                }

                tasks.add(getString(R.string.lbl_widget_select_new_project_task));

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.lbl_widget_title_select_task)
                       .setSingleChoiceItems(
                               StringUtils.convertListToArray(tasks),
                               selectedTaskIndex,
                               new DialogInterface.OnClickListener() {
                                   public void onClick(DialogInterface dialogInterface, int index) {
                                       if (index != availableTasks.size()) {
                                           Log.d(getApplicationContext(), LOG_TAG, "Task at index " + index + " choosen.");
                                           newSelectedTask = availableTasks.get(index);
                                           Log.d(getApplicationContext(), LOG_TAG, "About to update the time registration for task with name " + newSelectedTask.getName());
                                           removeDialog(Constants.Dialog.CHOOSE_TASK);
                                           updateTimeRegistration();
                                       } else {
                                           removeDialog(Constants.Dialog.CHOOSE_TASK);
                                           Intent intent = new Intent(TimeRegistrationEditProjectAndTaskActivity.this, AddEditTaskActivity.class);
                                           intent.putExtra(Constants.Extras.PROJECT, newSelectedProject);
                                           startActivityForResult(intent, Constants.IntentRequestCodes.ADD_TASK);
                                       }
                                   }
                               }
                       )
                       .setOnCancelListener(new DialogInterface.OnCancelListener() {
                           public void onCancel(DialogInterface dialogInterface) {
                               Log.d(getApplicationContext(), LOG_TAG, "No task choosen, close the activity");
                               removeDialog(Constants.Dialog.CHOOSE_TASK);
                               showDialog(Constants.Dialog.CHOOSE_SELECTED_PROJECT);
                           }
                       });
                dialog = builder.create();
                break;
            }
        }

        return dialog;
    }

    /**
     * Updates the time registration. If it concerns an ongoing registration, the preferences (for the selected project)
     * and the widget (displaying the selected and ongoing project) will be update.
     */
    private void updateTimeRegistration() {
        timeRegistration.setTask(newSelectedTask);
        timeRegistrationService.update(timeRegistration);

        // If the time registration is currently ongoing we have to update
        // the selected project and the notifications
        if (timeRegistration.isOngoingTimeRegistration()) {
            List<Integer> widgetIds = projectService.changeSelectedProject(originalProject, newSelectedProject);
            widgetService.updateWidgets(widgetIds);
            statusBarNotificationService.addOrUpdateNotification(timeRegistration);
        }

        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.IntentRequestCodes.ADD_PROJECT && resultCode == RESULT_OK) {
            availableProjects = loadAllProjects();
            showDialog(Constants.Dialog.CHOOSE_SELECTED_PROJECT);
        } else if (requestCode == Constants.IntentRequestCodes.ADD_TASK && resultCode == RESULT_OK) {
            availableTasks = loadAllTasks();

            showDialog(Constants.Dialog.CHOOSE_TASK);
        }
    }
}