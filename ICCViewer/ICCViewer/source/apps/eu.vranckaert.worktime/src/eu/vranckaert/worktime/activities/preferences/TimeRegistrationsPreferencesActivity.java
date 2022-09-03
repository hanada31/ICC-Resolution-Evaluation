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
import eu.vranckaert.worktime.service.ui.WidgetService;
import eu.vranckaert.worktime.utils.activity.GenericPreferencesActivity;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.preferences.Preferences;

/**
 * User: DIRK VRANCKAERT
 * Date: 31/01/12
 * Time: 9:18
 */
public class TimeRegistrationsPreferencesActivity extends GenericPreferencesActivity {
    private static final String LOG_TAG = TimeRegistrationsPreferencesActivity.class.getSimpleName();

    @Inject
    private WidgetService widgetService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.pref_time_registrations_category_title);

        final CheckBoxPreference immediatePunchOutCB = (CheckBoxPreference) getPreferenceScreen().findPreference(Constants.Preferences.Keys.IMMEDIATE_PUNCH_OUT);
        immediatePunchOutCB.setOnPreferenceChangeListener(new CheckBoxPreference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean result = (Boolean) newValue;

                Log.d(getApplicationContext(), LOG_TAG, "The newly selected value for 'Show status bar notification' is " + result);
                Preferences.setImmediatePunchOut(getApplicationContext(), result);
                immediatePunchOutCB.setChecked(result);
                Log.d(getApplicationContext(), LOG_TAG, "Delegate the change of the notifications to the notification bar service and widget service");
                widgetService.updateAllWidgets();

                return false;
            }
        });
    }

    @Override
    public int getPreferenceResourceId() {
        return R.xml.preference_time_registrations;
    }

    @Override
    public String getPageViewTrackerId() {
        return TrackerConstants.PageView.Preferences.TIME_REGISTRATIONS_PREFERENCES;
    }
}
