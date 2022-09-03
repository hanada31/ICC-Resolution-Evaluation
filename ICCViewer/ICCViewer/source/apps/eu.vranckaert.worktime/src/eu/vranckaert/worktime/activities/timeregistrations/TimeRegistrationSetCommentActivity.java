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

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.TrackerConstants;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.service.CommentHistoryService;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.service.ui.StatusBarNotificationService;
import eu.vranckaert.worktime.service.ui.WidgetService;
import eu.vranckaert.worktime.utils.context.AsyncHelper;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.string.StringUtils;
import eu.vranckaert.worktime.utils.tracker.AnalyticsTracker;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectExtra;

/**
 * Updates the comment of a {@link TimeRegistration}.
 */
public class TimeRegistrationSetCommentActivity extends RoboActivity {
    /**
     * LOG_TAG for logging
     */
    private static final String LOG_TAG = TimeRegistrationSetCommentActivity.class.getSimpleName();

    /**
     * Google Analytics Tracker
     */
    private AnalyticsTracker tracker;

    @Inject
    private TimeRegistrationService timeRegistrationService;

    @Inject
    private CommentHistoryService commentHistoryService;

    @Inject
    private StatusBarNotificationService statusBarNotificationService;

    @Inject
    private WidgetService widgetService;

    @InjectExtra(value = Constants.Extras.TIME_REGISTRATION)
    private TimeRegistration timeRegistration;

    @InjectExtra(value = Constants.Extras.TIME_REGISTRATION_COMMENT)
    private String comment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tracker = AnalyticsTracker.getInstance(getApplicationContext());

        AsyncTask threading = new AsyncTask() {

            @Override
            protected void onPreExecute() {
                showDialog(Constants.Dialog.TIME_REGISTRATION_ACTION_LOADING);
            }

            @Override
            protected Object doInBackground(Object... objects) {
                Log.d(getApplicationContext(), LOG_TAG, "Is there already a looper? " + (Looper.myLooper() != null));
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }

                timeRegistration.setComment(comment);
                tracker.trackEvent(
                        TrackerConstants.EventSources.TIME_REGISTRATION_ACTION_ACTIVITY,
                        TrackerConstants.EventActions.ADD_TR_COMMENT
                );
                timeRegistrationService.update(timeRegistration);
                widgetService.updateWidgetsForTask(timeRegistration.getTask());
                statusBarNotificationService.addOrUpdateNotification(timeRegistration);

                if (StringUtils.isNotBlank(comment)) {
                    commentHistoryService.updateLastComment(comment);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                removeDialog(Constants.Dialog.TIME_REGISTRATION_ACTION_LOADING);
                Log.d(getApplicationContext(), LOG_TAG, "Loading dialog removed from UI");
                Log.d(getApplicationContext(), LOG_TAG, "Finishing activity...");
                setResult(RESULT_OK);
                finish();
            }
        };
        AsyncHelper.start(threading);
    }

    @Override
    protected Dialog onCreateDialog(int dialogId) {
        Dialog dialog = null;
        switch (dialogId) {

            case Constants.Dialog.TIME_REGISTRATION_ACTION_LOADING: {
                Log.d(getApplicationContext(), LOG_TAG, "Creating loading dialog for executing a tr-action");
                dialog = ProgressDialog.show(
                        TimeRegistrationSetCommentActivity.this,
                        "",
                        getString(R.string.lbl_time_registration_actions_dialog_updating_time_registration),
                        true,
                        false
                );
                break;
            }
        }
        return dialog;
    }
}