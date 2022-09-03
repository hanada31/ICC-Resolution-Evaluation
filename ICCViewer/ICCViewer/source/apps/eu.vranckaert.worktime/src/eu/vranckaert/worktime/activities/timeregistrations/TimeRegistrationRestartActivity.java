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

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.service.ui.StatusBarNotificationService;
import eu.vranckaert.worktime.service.ui.WidgetService;
import eu.vranckaert.worktime.utils.context.AsyncHelper;
import eu.vranckaert.worktime.utils.context.Log;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectExtra;

/**
 * Resets the end date of a {@link TimeRegistration} so that the
 * {@link eu.vranckaert.worktime.model.TimeRegistration#isOngoingTimeRegistration()} is {@link Boolean#TRUE} again.
 */
public class TimeRegistrationRestartActivity extends RoboActivity {
    /**
     * LOG_TAG for logging
     */
    private static final String LOG_TAG = TimeRegistrationRestartActivity.class.getSimpleName();

    @Inject
    private TimeRegistrationService timeRegistrationService;

    @Inject
    private StatusBarNotificationService statusBarNotificationService;

    @Inject
    private WidgetService widgetService;

    @InjectExtra(Constants.Extras.TIME_REGISTRATION)
    private TimeRegistration timeRegistration;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

                if (timeRegistrationService.getLatestTimeRegistration().getId().equals(timeRegistration.getId())) {
                    timeRegistration.setEndTime(null);
                    timeRegistrationService.update(timeRegistration);

                    widgetService.updateAllWidgets();

                    statusBarNotificationService.addOrUpdateNotification(timeRegistration);

                    return null;
                } else {
                    return -1;
                }
            }

            @Override
            protected void onPostExecute(Object o) {
                removeDialog(Constants.Dialog.TIME_REGISTRATION_ACTION_LOADING);
                Log.d(getApplicationContext(), LOG_TAG, "Loading dialog removed from UI");
                if (o != null) {
                    Log.d(getApplicationContext(), LOG_TAG, "Could not restart time registration because it's not the latest!");
                    Toast.makeText(TimeRegistrationRestartActivity.this, R.string.err_time_registration_restart_not_possible_only_latest_time_registration, Toast.LENGTH_LONG).show();
                }

                Log.d(getApplicationContext(), LOG_TAG, "Finishing activity...");
                setResult(RESULT_OK);
                finish();
            }
        };
        AsyncHelper.start(threading);
    }
}