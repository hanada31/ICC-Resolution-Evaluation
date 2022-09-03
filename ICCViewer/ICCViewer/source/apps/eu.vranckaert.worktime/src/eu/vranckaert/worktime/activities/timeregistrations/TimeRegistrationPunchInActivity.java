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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.activities.projects.SelectProjectActivity;
import eu.vranckaert.worktime.activities.tasks.SelectTaskActivity;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.TrackerConstants;
import eu.vranckaert.worktime.model.Task;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.model.WidgetConfiguration;
import eu.vranckaert.worktime.service.BackupService;
import eu.vranckaert.worktime.service.ProjectService;
import eu.vranckaert.worktime.service.TaskService;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.service.ui.StatusBarNotificationService;
import eu.vranckaert.worktime.service.ui.WidgetService;
import eu.vranckaert.worktime.utils.context.AsyncHelper;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.date.DateUtils;
import eu.vranckaert.worktime.utils.preferences.Preferences;
import eu.vranckaert.worktime.utils.tracker.AnalyticsTracker;
import eu.vranckaert.worktime.utils.view.actionbar.synclock.SyncLockedGuiceActivity;
import org.joda.time.Duration;
import roboguice.inject.InjectExtra;

import com.google.inject.internal.Nullable;
import java.util.Date;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 09/02/11
 * Time: 23:25
 */
public class TimeRegistrationPunchInActivity extends SyncLockedGuiceActivity {
    private static final String LOG_TAG = TimeRegistrationPunchInActivity.class.getSimpleName();

    @Inject
    private WidgetService widgetService;

    @Inject
    private StatusBarNotificationService statusBarNotificationService;

    @Inject
    private TimeRegistrationService timeRegistrationService;

    @Inject
    private ProjectService projectService;

    @Inject
    private TaskService taskService;

    @Inject
    private BackupService backupService;

    @InjectExtra(value = Constants.Extras.WIDGET_ID, optional = true)
    @Nullable
    private Integer widgetId;

    @InjectExtra(value = Constants.Extras.UPDATE_WIDGET, optional = true)
    private boolean updateWidget = false;

    private List<Task> availableTasks;

    private AnalyticsTracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tracker = AnalyticsTracker.getInstance(getApplicationContext());
        Log.d(getApplicationContext(), LOG_TAG, "Started the START TimeRegistration acitivity");

        TimeRegistration latestTimeRegistration = timeRegistrationService.getLatestTimeRegistration();
        if (latestTimeRegistration != null && latestTimeRegistration.isOngoingTimeRegistration()) {
            showDialog(Constants.Dialog.WARN_ONGOING_TR);
            return;
        }

        // Get the widget id from the extra-bundel and search for the widget configuration.
        if (widgetId == null) {
            showTaskChooser();
        } else if (widgetId == Constants.Others.PUNCH_BAR_WIDGET_ID) {
            projectService.getSelectedProject(widgetId); // just to make sure punch-bar widget configuration exists!
            showProjectChooser();
        } else {
            WidgetConfiguration wc = widgetService.getWidgetConfiguration(widgetId);

            if (wc.getTask() != null) {
                Task task = taskService.getSelectedTask(widgetId);
                createNewTimeRegistration(task);
            } else if (wc.getProject() != null) {
                showTaskChooser();
            }
        }
    }
    
    private void showProjectChooser() {
        Intent intent = new Intent(TimeRegistrationPunchInActivity.this, SelectProjectActivity.class);
        intent.putExtra(Constants.Extras.WIDGET_ID, widgetId);
        startActivityForResult(intent, Constants.IntentRequestCodes.SELECT_PROJECT);
    }
    
    private void showTaskChooser() {
        Intent intent = new Intent(TimeRegistrationPunchInActivity.this, SelectTaskActivity.class);
        intent.putExtra(Constants.Extras.WIDGET_ID, widgetId);
        intent.putExtra(Constants.Extras.ONLY_SELECT, true);
        startActivityForResult(intent, Constants.IntentRequestCodes.SELECT_TASK);
    }

    private void createNewTimeRegistration(final Task selectedTask) {
        removeDialog(Constants.Dialog.CHOOSE_TASK);

        AsyncTask threading = new AsyncTask() {

            @Override
            protected void onPreExecute() {
                showDialog(Constants.Dialog.LOADING_TIME_REGISTRATION_PUNCH_OUT);
            }

            @Override
            protected Object doInBackground(Object... objects) {
                Log.d(getApplicationContext(), LOG_TAG, "Is there already a looper? " + (Looper.myLooper() != null));
                if(Looper.myLooper() == null) {
                    Looper.prepare();
                }

                Date startTime = new Date();

                TimeRegistration newTr = new TimeRegistration();
                newTr.setTask(selectedTask);
                newTr.setStartTime(startTime);

                /*
                 * Issue 61
                 * If the start time of registration, and the end time of the previous registration, have a difference
                 * off less than 60 seconds, we start the time registration at the same time the previous one is ended.
                 * This is to prevent gaps in the time registrations that should be modified manual. This is default
                 * configured to happen (defined in the preferences).
                 */
                if (Preferences.getTimeRegistrationsAutoClose60sGap(TimeRegistrationPunchInActivity.this)) {
                    Log.d(getApplicationContext(), LOG_TAG, "Check for gap between this new time registration and the previous one");
                    TimeRegistration previousTimeRegistration = timeRegistrationService.getPreviousTimeRegistration(newTr);
                    if (previousTimeRegistration != null) {
                        Log.d(getApplicationContext(), LOG_TAG, "The previous time registrations ended on " + previousTimeRegistration.getEndTime());
                        Log.d(getApplicationContext(), LOG_TAG, "The new time registration starts on " + newTr.getStartTime());
                        Duration duration = DateUtils.TimeCalculator.calculateExactDuration(
                                TimeRegistrationPunchInActivity.this,
                                newTr.getStartTime(),
                                previousTimeRegistration.getEndTime()
                        );
                        Log.d(getApplicationContext(), LOG_TAG, "The duration between the previous end time and the new start time is " + duration);
                        long durationMillis = duration.getMillis();
                        Log.d(getApplicationContext(), LOG_TAG, "The duration in milliseconds is " + durationMillis);
                        if (durationMillis < 60000) {
                            Log.d(getApplicationContext(), LOG_TAG, "Gap is less than 60 seconds, setting start time to end time of previous registration");
                            newTr.setStartTime(previousTimeRegistration.getEndTime());
                        }
                    }
                }

                statusBarNotificationService.addOrUpdateNotification(newTr);
                timeRegistrationService.create(newTr);

                tracker.trackEvent(
                        TrackerConstants.EventSources.START_TIME_REGISTRATION_ACTIVITY,
                        TrackerConstants.EventActions.START_TIME_REGISTRATION
                );

                projectService.refresh(selectedTask.getProject());
                if (updateWidget)
                    widgetService.updateWidgetsForTask(newTr.getTask());

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                removeDialog(Constants.Dialog.LOADING_TIME_REGISTRATION_PUNCH_OUT);
                Toast.makeText(TimeRegistrationPunchInActivity.this, R.string.msg_widget_time_reg_created, Toast.LENGTH_LONG).show();
                finish();
            }
        };
        AsyncHelper.start(threading);
    }

    @Override
    protected Dialog onCreateDialog(int dialogId) {
        Dialog dialog = null;
        switch(dialogId) {
            case Constants.Dialog.WARN_ONGOING_TR: {
                AlertDialog.Builder alertOngoingTR = new AlertDialog.Builder(this);
                alertOngoingTR.setMessage(R.string.msg_already_ongoing_time_registration)
                        .setCancelable(true)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                removeDialog(Constants.Dialog.WARN_ONGOING_TR);
                                TimeRegistrationPunchInActivity.this.finish();
                            }
                        })
                        .setOnCancelListener(new Dialog.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                removeDialog(Constants.Dialog.WARN_ONGOING_TR);
                                TimeRegistrationPunchInActivity.this.finish();
                            }
                        });
                dialog = alertOngoingTR.create();
                break;
            }
            case Constants.Dialog.LOADING_TIME_REGISTRATION_PUNCH_OUT: {
                Log.d(getApplicationContext(), LOG_TAG, "Creating loading dialog for starting a new time registration");
                dialog = ProgressDialog.show(
                        TimeRegistrationPunchInActivity.this,
                        "",
                        getString(R.string.lbl_punching_in),
                        true,
                        false
                );
                break;
            }
        };
        return dialog;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.IntentRequestCodes.SELECT_PROJECT: {
                if (resultCode == RESULT_OK) {
                    showTaskChooser();
                } else {
                    finish();
                }
                break;
            }
            case Constants.IntentRequestCodes.SELECT_TASK: {
                if (resultCode == RESULT_OK) {
                    Task selectedTask = (Task) data.getExtras().get(Constants.Extras.TASK);
                    createNewTimeRegistration(selectedTask);
                } else {
                    finish();
                }
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tracker.stopSession();
    }
}
