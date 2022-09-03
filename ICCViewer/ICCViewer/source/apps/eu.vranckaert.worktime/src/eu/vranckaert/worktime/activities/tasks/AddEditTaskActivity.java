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

package eu.vranckaert.worktime.activities.tasks;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.TextConstants;
import eu.vranckaert.worktime.constants.TrackerConstants;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.Task;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.service.TaskService;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.service.ui.StatusBarNotificationService;
import eu.vranckaert.worktime.service.ui.WidgetService;
import eu.vranckaert.worktime.utils.context.AsyncHelper;
import eu.vranckaert.worktime.utils.context.IntentUtil;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.tracker.AnalyticsTracker;
import eu.vranckaert.worktime.utils.view.actionbar.synclock.SyncLockedActivity;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

/**
 * User: DIRK VRANCKAERT
 * Date: 30/03/11
 * Time: 00:19
 */
public class AddEditTaskActivity extends SyncLockedActivity {
    private static final String LOG_TAG = AddEditTaskActivity.class.getSimpleName();

    @InjectView(R.id.project_name)
    private TextView projectName;

    @InjectView(R.id.task_name_required)
    private TextView taskNameRequiredMessage;

    @InjectView(R.id.task_name)
    private TextView taskName;

    @InjectExtra(Constants.Extras.PROJECT)
    private Project project;

    @InjectExtra(value = Constants.Extras.TASK, optional = true)
    private Task editTask;

    @Inject
    private TaskService taskService;

    @Inject
    private WidgetService widgetService;

    @Inject
    private StatusBarNotificationService statusBarNotificationService;

    @Inject
    private TimeRegistrationService timeRegistrationService;

    private AnalyticsTracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        setTitle(R.string.lbl_add_task_title);
        setDisplayHomeAsUpEnabled(true);

        tracker = AnalyticsTracker.getInstance(getApplicationContext());
        tracker.trackPageView(TrackerConstants.PageView.ADD_EDIT_TASK_ACTIVITY);

        buildUI();
    }

    private void buildUI() {
        if (!inUpdateMode()) {
            Log.d(getApplicationContext(), LOG_TAG, "Adding task for project " + project.getName());
        } else {
            Log.d(getApplicationContext(), LOG_TAG, "Editing task for project " + project.getName());
            setTitle(R.string.lbl_edit_task_title);
            taskName.setText(editTask.getName());
        }

        projectName.setText(TextConstants.SPACE + project.getName());
    }

    private void performSave() {
        if (taskName.getText().length() == 0) {
            taskNameRequiredMessage.setVisibility(View.VISIBLE);
        } else {
            taskNameRequiredMessage.setVisibility(View.GONE);
            saveOrUpdateTaskForProject(project, taskName.getText().toString());
        }
    }

    /**
     * Creates the task based on the provided project and name of the task. Before saving the task the save button is
     * removed and a progress bar is shown.
     * @param project The {@link Project} for which to create a task.
     * @param taskNameText The name of the task to create.
     */
    private void saveOrUpdateTaskForProject(final Project project, final String taskNameText) {
        AsyncTask<Void, Void, Task> task = new AsyncTask<Void, Void, Task>(){
            @Override
            protected void onPreExecute() {
                getActionBarHelper().setRefreshActionItemState(true, R.id.menu_add_task_activity_save);
            }

            @Override
            protected Task doInBackground(Void... parameters) {
                Task task;
                if (!inUpdateMode()) {
                    task = new Task();
                    task.setProject(project);
                } else {
                    task = editTask;
                }
                task.setName(taskNameText);

                if (!inUpdateMode()) {
                    taskService.save(task);
                    tracker.trackEvent(
                            TrackerConstants.EventSources.ADD_EDIT_TASK_ACTIVITY,
                            TrackerConstants.EventActions.ADD_TASK
                    );
                    Log.d(getApplicationContext(), LOG_TAG, "New task persisted");
                } else {
                    taskService.update(task);
                    tracker.trackEvent(
                            TrackerConstants.EventSources.ADD_EDIT_TASK_ACTIVITY,
                            TrackerConstants.EventActions.EDIT_TASK
                    );
                    Log.d(getApplicationContext(), LOG_TAG, "Task with id " + task.getId() + " and name " + task.getName() + " is updated");
                }

                return task;
            }

            @Override
            protected void onPostExecute(Task task) {
                if (inUpdateMode()) {
                    Log.d(getApplicationContext(), LOG_TAG, "About to update the wiget and notifications");
                    TimeRegistration tr = timeRegistrationService.getLatestTimeRegistration();
                    if (tr != null && tr.getTask().getId().equals(task.getId())) {
                        widgetService.updateAllWidgets();
                        statusBarNotificationService.addOrUpdateNotification(tr);
                    }
                }

                getActionBarHelper().setRefreshActionItemState(false, R.id.menu_add_task_activity_save);
                setResult(RESULT_OK);
                finish();
            }
        };
        AsyncHelper.start(task);
    }

    /**
     * Checks if the activity is in update mode. If not it's create mode!
     * @return {@link Boolean#TRUE} if the task is about to be updated, {@link Boolean#FALSE} if in creation mode.
     */
    private boolean inUpdateMode() {
        if (editTask == null || editTask.getId() < 0) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.ab_activity_add_task, menu);

        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                IntentUtil.goBack(AddEditTaskActivity.this);
                break;
            case R.id.menu_add_task_activity_save:
                performSave();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tracker.stopSession();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (inUpdateMode() && requestCode == Constants.IntentRequestCodes.SYNC_BLOCKING_ACTIVITY) {
            if (taskService.checkTaskExisting(editTask)) {
                if (taskService.checkReloadTask(editTask)) {
                    taskService.refresh(editTask);
                    buildUI();
                }
            } else {
                setResult(RESULT_OK);
                finish();
            }
        }
    }
}
