/*
 * Copyright 2012 Dirk Vranckaert
 *
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

package eu.vranckaert.worktime.activities.preferences;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.TrackerConstants;
import eu.vranckaert.worktime.service.ui.StatusBarNotificationService;
import eu.vranckaert.worktime.utils.activity.GenericPreferencesActivity;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.preferences.Preferences;

/**
 * User: DIRK VRANCKAERT
 * Date: 31/01/12
 * Time: 9:20
 */
public class NotificationsPreferencesActivity extends GenericPreferencesActivity {
    private static final String LOG_TAG = NotificationsPreferencesActivity.class.getSimpleName();

    @Inject
    private StatusBarNotificationService statusBarNotificationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.pref_stat_bar_notifs_category_title);

        final CheckBoxPreference chPreference = (CheckBoxPreference) getPreferenceScreen().findPreference(Constants.Preferences.Keys.SHOW_STATUS_BAR_NOTIFICATIONS_PREFERENCE);
        chPreference.setOnPreferenceChangeListener(new CheckBoxPreference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean result = (Boolean) newValue;

                Log.d(getApplicationContext(), LOG_TAG, "The newly selected value for 'Show status bar notification' is " + result);
                Preferences.setShowStatusBarNotificationsPreference(getApplicationContext(), result);
                Log.d(getApplicationContext(), LOG_TAG, "Show status bar notifications checkbox to be updated");
                chPreference.setChecked(result);
                Log.d(getApplicationContext(), LOG_TAG, "Delegate the change of the notifications to the notification bar service");
                if (result) {
                    statusBarNotificationService.addOrUpdateNotification(null);
                } else {
                    statusBarNotificationService.removeOngoingTimeRegistrationNotification();
                }

                return false;
            }
        });
    }

    @Override
    public int getPreferenceResourceId() {
        return R.xml.preference_notification;
    }

    @Override
    public String getPageViewTrackerId() {
        return TrackerConstants.PageView.Preferences.NOTIFICATIONS_PREFERENCES;
    }
}
