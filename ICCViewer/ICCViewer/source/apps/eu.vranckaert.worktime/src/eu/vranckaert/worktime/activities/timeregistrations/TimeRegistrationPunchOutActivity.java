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
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.TrackerConstants;
import eu.vranckaert.worktime.model.Task;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.service.CommentHistoryService;
import eu.vranckaert.worktime.service.TaskService;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.service.ui.StatusBarNotificationService;
import eu.vranckaert.worktime.service.ui.WidgetService;
import eu.vranckaert.worktime.utils.context.AsyncHelper;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.preferences.Preferences;
import eu.vranckaert.worktime.utils.string.StringUtils;
import eu.vranckaert.worktime.utils.tracker.AnalyticsTracker;
import eu.vranckaert.worktime.utils.view.actionbar.synclock.SyncLockedGuiceActivity;
import roboguice.inject.InjectExtra;

import java.util.Date;

/**
 * This activity ends the time registration that is passed on the extra-bundle under
 * {@link Constants.Extras#TIME_REGISTRATION}. Two optional extra-bundle parameters can be specified:<br/>
 * 1. {@link Constants.Extras#TIME_REGISTRATION_CONTINUE_WITH_NEW}:  the default value for this parameter is
 * {@link Boolean#FALSE}. If it's set to {@link Boolean#TRUE} a new {@link TimeRegistration} will be started using the
 * {@link TimeRegistrationPunchInActivity}.
 * 2. {@link Constants.Extras#WIDGET_ID}: The id of the widget from which this request is launched. The default value is
 * null. If null it will be assigned the value of {@link Constants.Others#PUNCH_BAR_WIDGET_ID}. The value of this
 * parameter is used together with the first extra-bundle parameter
 * {@link Constants.Extras#TIME_REGISTRATION_CONTINUE_WITH_NEW} to start the {@link TimeRegistrationPunchInActivity}.
 */
public class TimeRegistrationPunchOutActivity extends SyncLockedGuiceActivity {
    /**
     * LOG_TAG for logging
     */
    private static final String LOG_TAG = TimeRegistrationPunchOutActivity.class.getSimpleName();

    /**
     * Google Analytics Tracker
     */
    private AnalyticsTracker tracker;

    @Inject
    private TimeRegistrationService timeRegistrationService;

    @Inject
    private TaskService taskService;

    @Inject
    private CommentHistoryService commentHistoryService;

    @Inject
    private StatusBarNotificationService statusBarNotificationService;

    @Inject
    private WidgetService widgetService;

    @InjectExtra(value = Constants.Extras.TIME_REGISTRATION)
    private TimeRegistration timeRegistration;

    @InjectExtra(value = Constants.Extras.TIME_REGISTRATION_CONTINUE_WITH_NEW, optional = true)
    private boolean continueWithNew = false;

    @InjectExtra(value = Constants.Extras.WIDGET_ID, optional = true)
    private Integer widgetId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tracker = AnalyticsTracker.getInstance(getApplicationContext());

        AsyncTask threading = new AsyncTask() {

            @Override
            protected void onPreExecute() {
                showDialog(Constants.Dialog.LOADING_TIME_REGISTRATION_PUNCH_OUT);
            }

            @Override
            protected Object doInBackground(Object... objects) {
                Log.d(getApplicationContext(), LOG_TAG, "Is there already a looper? " + (Looper.myLooper() != null));
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }

                Date endTime = new Date();

                if (timeRegistration.getEndTime() != null) {
                    Log.w(getApplicationContext(), LOG_TAG, "Data must be corrupt, time registration is already ended! Please clear all the data through the system settings of the application!");
                    return new Object();
                } else {
                    timeRegistration.setEndTime(endTime);
                    timeRegistrationService.update(timeRegistration);

                    tracker.trackEvent(
                            TrackerConstants.EventSources.TIME_REGISTRATION_ACTION_ACTIVITY,
                            TrackerConstants.EventActions.END_TIME_REGISTRATION
                    );

                    statusBarNotificationService.removeOngoingTimeRegistrationNotification();

                    widgetService.updateAllWidgets();
                }

                if (StringUtils.isNotBlank(timeRegistration.getComment())) {
                    commentHistoryService.updateLastComment(timeRegistration.getComment());
                }

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                removeDialog(Constants.Dialog.LOADING_TIME_REGISTRATION_PUNCH_OUT);
                Log.d(getApplicationContext(), LOG_TAG, "Loading dialog removed from UI");
                if (o != null) {
                    Log.d(getApplicationContext(), LOG_TAG, "Something went wrong, the data is corrupt");
                    Toast.makeText(TimeRegistrationPunchOutActivity.this, R.string.err_time_registration_actions_dialog_corrupt_data, Toast.LENGTH_LONG).show();
                } else if (o == null) {
                    Log.d(getApplicationContext(), LOG_TAG, "Successfully ended time registration");
                    Toast.makeText(TimeRegistrationPunchOutActivity.this, R.string.msg_widget_time_reg_ended, Toast.LENGTH_LONG).show();
                }

                if (continueWithNew) {
                    if (widgetId == null) {
                        widgetId = Constants.Others.PUNCH_BAR_WIDGET_ID;
                    }
                    Intent intent = new Intent(TimeRegistrationPunchOutActivity.this, TimeRegistrationPunchInActivity.class);
                    intent.putExtra(Constants.Extras.WIDGET_ID, widgetId);
                    intent.putExtra(Constants.Extras.UPDATE_WIDGET, true);
                    startActivityForResult(intent, Constants.IntentRequestCodes.START_TIME_REGISTRATION);
                } else {
                    checkFinishTask();
                }
            }
        };
        AsyncHelper.start(threading);
    }

    /**
     * Check the preferences if the user should be requested for finishing the task or not. If positive the user will be
     * asked to. If not the activity is ended.
     */
    private void checkFinishTask() {
        boolean askFinishTask = Preferences.getWidgetEndingTimeRegistrationFinishTaskPreference(getApplicationContext());
        if (askFinishTask) {
            showDialog(Constants.Dialog.ASK_FINISH_TASK);
        } else {
            endActivity();
        }
    }

    /**
     * After a positive response from the user the specified task will be marked as finished. This will only be
     * triggered when ending a {@link TimeRegistration} and when the preference
     * {@link Preferences#getWidgetEndingTimeRegistrationFinishTaskPreference(android.content.Context)} returns
     * {@link Boolean#TRUE}.
     * @param task The {@link Task} that should be marked as finished ({@link Task#finished}).
     */
    private void finishTask(Task task) {
        task.setFinished(true);
        taskService.update(task);

        tracker.trackEvent(
                TrackerConstants.EventSources.TIME_REGISTRATION_ACTION_ACTIVITY,
                TrackerConstants.EventActions.MARK_TASK_FINISHED
        );

        endActivity();
    }

    /**
     * Ends the activity with a result {@link android.app.Activity#RESULT_OK}.
     */
    private void endActivity() {
        Log.d(getApplicationContext(), LOG_TAG, "Finishing activity...");
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected Dialog onCreateDialog(int dialogId) {
        Dialog dialog = null;
        switch(dialogId) {
            case Constants.Dialog.LOADING_TIME_REGISTRATION_PUNCH_OUT: {
                Log.d(getApplicationContext(), LOG_TAG, "Creating loading dialog for ending the active time registration");
                dialog = ProgressDialog.show(
                        TimeRegistrationPunchOutActivity.this,
                        "",
                        getString(R.string.lbl_time_registration_actions_punching_out),
                        true,
                        false
                );
                break;
            }

            case Constants.Dialog.ASK_FINISH_TASK: {
                final Task task = timeRegistration.getTask();
                taskService.refresh(task);

                AlertDialog.Builder alertRemoveAllRegs = new AlertDialog.Builder(this);
                alertRemoveAllRegs.setTitle(R.string.lbl_time_registration_punch_out_ask_finish_task_title)
                        .setMessage(getString(R.string.msg_time_registration_punch_out_ask_finish_task_title, task.getName()))
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finishTask(task);
                                removeDialog(Constants.Dialog.ASK_FINISH_TASK);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                setResult(RESULT_OK);
                                finish();
                                removeDialog(Constants.Dialog.ASK_FINISH_TASK);
                            }
                        });
                dialog = alertRemoveAllRegs.create();
                break;
            }
        }
        return dialog;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        endActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tracker.stopSession();
    }
}