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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.TrackerConstants;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.service.ui.StatusBarNotificationService;
import eu.vranckaert.worktime.service.ui.WidgetService;
import eu.vranckaert.worktime.utils.context.AsyncHelper;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.date.DateUtils;
import eu.vranckaert.worktime.utils.tracker.AnalyticsTracker;
import eu.vranckaert.worktime.utils.view.actionbar.synclock.SyncLockedGuiceActivity;
import roboguice.inject.InjectExtra;

import java.util.Date;

/**
 * This activity will delete a specific {@link TimeRegistration} or all the {@link TimeRegistration}s within a certain
 * boundary. If extra-bundle parameter {@link Constants.Extras#TIME_REGISTRATION} is present this activity will delete
 * this {@link TimeRegistration}. If this parameter is null it will delete all {@link TimeRegistration}s within provided
 * boundaries ({@link Constants.Extras#TIME_REGISTRATION_START_DATE},
 * {@link Constants.Extras#TIME_REGISTRATION_END_DATE}). If both boundaries are null all the time registrations will be
 * removed. If only the min-boundary is null, all registration until the max-boundary will removed. If the max-boundary
 * is null all registrations starting from the min-boundary will be removed.<br/>
 * All widgets will be updated when removing one or more {@link TimeRegistration}s.
 */
public class TimeRegistrationDeleteActivity extends SyncLockedGuiceActivity {
    /**
     * LOG_TAG for logging
     */
    private static final String LOG_TAG = TimeRegistrationDeleteActivity.class.getSimpleName();

    /**
     * Google Analytics Tracker
     */
    private AnalyticsTracker tracker;

    @Inject
    private TimeRegistrationService timeRegistrationService;

    @Inject
    private StatusBarNotificationService statusBarNotificationService;

    @Inject
    private WidgetService widgetService;

    @InjectExtra(value = Constants.Extras.TIME_REGISTRATION, optional = true)
    private TimeRegistration timeRegistration;

    @InjectExtra(value = Constants.Extras.TIME_REGISTRATION_START_DATE, optional = true)
    private Date minBoundary;

    @InjectExtra(value = Constants.Extras.TIME_REGISTRATION_END_DATE, optional = true)
    private Date maxBoundary;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tracker = AnalyticsTracker.getInstance(getApplicationContext());

        if (timeRegistration != null) {
            showDialog(Constants.Dialog.DELETE_TIME_REGISTRATION_YES_NO);
        } else {
            showDialog(Constants.Dialog.DELETE_TIME_REGISTRATIONS_YES_NO);
        }
    }

    @Override
    protected Dialog onCreateDialog(int dialogId) {
        Dialog dialog = null;
        switch (dialogId) {
            case Constants.Dialog.DELETE_TIME_REGISTRATION_YES_NO: {
                AlertDialog.Builder alertRemoveReg = new AlertDialog.Builder(this);
                alertRemoveReg
                        .setMessage(R.string.msg_time_registration_actions_dialog_removing_time_registration_confirmation)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                removeDialog(Constants.Dialog.DELETE_TIME_REGISTRATION_YES_NO);
                                deleteTimeRegistration();
                                endActivity(Constants.IntentResultCodes.RESULT_DELETED);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                removeDialog(Constants.Dialog.DELETE_TIME_REGISTRATION_YES_NO);
                                endActivity(RESULT_CANCELED);
                            }
                        });
                dialog = alertRemoveReg.create();
                break;
            }
            case Constants.Dialog.DELETE_TIME_REGISTRATIONS_YES_NO: {
                AlertDialog.Builder alertRemoveReg = new AlertDialog.Builder(this);
                alertRemoveReg
                        .setMessage(R.string.msg_time_registration_actions_dialog_removing_time_registrations_confirmation)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                removeDialog(Constants.Dialog.DELETE_TIME_REGISTRATIONS_YES_NO);
                                deleteTimeRegistrations();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                removeDialog(Constants.Dialog.DELETE_TIME_REGISTRATIONS_YES_NO);
                                endActivity(Constants.IntentResultCodes.RESULT_DELETED);
                                finish();
                            }
                        });
                dialog = alertRemoveReg.create();
                break;
            }
        }
        return dialog;
    }

    /**
     * Deletes a {@link TimeRegistration} instance from the database.
     */
    private void deleteTimeRegistration() {
        AsyncTask threading = new AsyncTask() {

            @Override
            protected void onPreExecute() {
                showDialog(Constants.Dialog.TIME_REGISTRATION_DELETE_LOADING);
            }

            @Override
            protected Object doInBackground(Object... objects) {
                Log.d(getApplicationContext(), LOG_TAG, "Is there already a looper? " + (Looper.myLooper() != null));
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }

                timeRegistrationService.remove(timeRegistration);

                widgetService.updateAllWidgets();

                if (timeRegistration.isOngoingTimeRegistration()) {
                    statusBarNotificationService.removeOngoingTimeRegistrationNotification();
                }

                tracker.trackEvent(
                        TrackerConstants.EventSources.TIME_REGISTRATION_ACTION_ACTIVITY,
                        TrackerConstants.EventActions.DELETE_TIME_REGISTRATION
                );

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                removeDialog(Constants.Dialog.TIME_REGISTRATION_DELETE_LOADING);
                Log.d(getApplicationContext(), LOG_TAG, "Loading dialog removed from UI");
                if (o != null) {
                    Log.d(getApplicationContext(), LOG_TAG, "Something went wrong...");
                    Toast.makeText(TimeRegistrationDeleteActivity.this, R.string.err_time_registration_actions_dialog_corrupt_data, Toast.LENGTH_LONG).show();
                }

                Log.d(getApplicationContext(), LOG_TAG, "Finishing activity...");
                endActivity(Constants.IntentResultCodes.RESULT_DELETED);
            }
        };
        AsyncHelper.start(threading);
    }

    private void deleteTimeRegistrations() {
        AsyncTask<Date, Void, Long> threading = new AsyncTask<Date, Void, Long>() {
            @Override
            protected void onPreExecute() {
                showDialog(Constants.Dialog.TIME_REGISTRATIONS_DELETE_LOADING);
            }

            @Override
            protected Long doInBackground(Date... boundaries) {
                Log.d(getApplicationContext(), LOG_TAG, "Is there already a looper? " + (Looper.myLooper() != null));
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }

                Date minBoundary = boundaries[0];
                if (minBoundary != null)
                    minBoundary = DateUtils.Various.setMinTimeValueOfDay(minBoundary);
                Date maxBoundary = boundaries[1];
                if (maxBoundary != null)
                    maxBoundary = DateUtils.Various.setMaxTimeValueOfDay(maxBoundary);

                long count = timeRegistrationService.removeAllInRange(minBoundary, maxBoundary);

                widgetService.updateAllWidgets();

                TimeRegistration latestTimeRegistration = timeRegistrationService.getLatestTimeRegistration();
                if (latestTimeRegistration != null && latestTimeRegistration.isOngoingTimeRegistration()) {
                    statusBarNotificationService.addOrUpdateNotification(latestTimeRegistration);
                } else {
                    statusBarNotificationService.removeOngoingTimeRegistrationNotification();
                }

                tracker.trackEvent(
                        TrackerConstants.EventSources.TIME_REGISTRATION_ACTION_ACTIVITY,
                        TrackerConstants.EventActions.DELETE_TIME_REGISTRATIONS_IN_RANGE
                );

                return count;
            }

            @Override
            protected void onPostExecute(Long count) {
                removeDialog(Constants.Dialog.TIME_REGISTRATIONS_DELETE_LOADING);
                Log.d(getApplicationContext(), LOG_TAG, "Loading dialog removed from UI");

                String message = "";
                if (count == 1l) {
                    message = getString(R.string.msg_time_registration_actions_dialog_removing_time_registrations_range_done_single, count);
                } else {
                    message = getString(R.string.msg_time_registration_actions_dialog_removing_time_registrations_range_done_multiple, count);
                }
                Toast.makeText(TimeRegistrationDeleteActivity.this, message, Toast.LENGTH_LONG).show();

                Log.d(getApplicationContext(), LOG_TAG, "Finishing activity...");
                endActivity(Constants.IntentResultCodes.RESULT_DELETED);
            }
        };
        AsyncHelper.startWithParams(threading, new Date[]{minBoundary, maxBoundary});
    }

    /**
     * Ends the activity with the specified result.
     *
     * @param result The result for the activity
     */
    private void endActivity(int result) {
        Log.d(getApplicationContext(), LOG_TAG, "Finishing activity...");
        setResult(result);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tracker.stopSession();
    }
}