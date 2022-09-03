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

package eu.vranckaert.worktime.activities.preferences;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.TrackerConstants;
import eu.vranckaert.worktime.service.AccountService;
import eu.vranckaert.worktime.utils.activity.GenericPreferencesActivity;
import eu.vranckaert.worktime.utils.alarm.AlarmUtil;
import eu.vranckaert.worktime.utils.preferences.Preferences;
import eu.vranckaert.worktime.utils.preferences.TimePreference;

import java.util.Date;

/**
 * User: DIRK VRANCKAERT
 * Date: 31/01/12
 * Time: 9:18
 */
public class AccountSyncPreferencesActivity extends GenericPreferencesActivity {
    private static final String LOG_TAG = AccountSyncPreferencesActivity.class.getSimpleName();

    @Inject
    private AccountService accountService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.pref_account_sync_category_title);

        final TimePreference syncIntervalFixedTimePreference = (TimePreference) getPreferenceScreen().findPreference(Constants.Preferences.Keys.ACCOUNT_SYNC_INTERVAL_FIXED_TIME);
        if (Preferences.Account.syncInterval(AccountSyncPreferencesActivity.this) / 3600000L != 24) {
            syncIntervalFixedTimePreference.setEnabled(false);
        } else {
            syncIntervalFixedTimePreference.setEnabled(true);
        }
        syncIntervalFixedTimePreference.setOnPreferenceChangeListener(new TimePreference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                scheduleAlarm(AccountSyncPreferencesActivity.this, Preferences.Account.syncInterval(AccountSyncPreferencesActivity.this) / 3600000L, new Date((Long)newValue), accountService);
                return true;
            }
        });

        final ListPreference syncIntervalPreference = (ListPreference) getPreferenceScreen().findPreference(Constants.Preferences.Keys.ACCOUNT_SYNC_INTERVAL);
        syncIntervalPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Integer newSyncInterval = Integer.parseInt(newValue.toString());

                syncIntervalFixedTimePreference.setEnabled(false);
                if (newSyncInterval == 24) {
                    syncIntervalFixedTimePreference.setEnabled(true);
                }

                scheduleAlarm(AccountSyncPreferencesActivity.this, newSyncInterval, null, accountService);

                return true;
            }
        });
    }

    private static void scheduleAlarm(Context ctx, long synchronizationInterval, Date fixedSyncTime, AccountService accountService) {
        if (synchronizationInterval == -1) {
            return;
        } else if (synchronizationInterval == 24) {
            if (fixedSyncTime == null) {
                fixedSyncTime = Preferences.Account.syncIntervalFixedTime(ctx);
            }
            AlarmUtil.setAlarmSyncCycleOnceADay(ctx, accountService.getLastSyncHistory(), fixedSyncTime);
        } else {
            long interval = synchronizationInterval * 3600000L;
            AlarmUtil.setAlarmSyncCycle(ctx, accountService.getLastSyncHistory(), interval);
        }
    }

    public static void scheduleAlarm(Context ctx, AccountService accountService) {
        scheduleAlarm(ctx, Preferences.Account.syncInterval(ctx) / 3600000L, null, accountService);
    }

    @Override
    public int getPreferenceResourceId() {
        return R.xml.preference_account_sync;
    }

    @Override
    public String getPageViewTrackerId() {
        return TrackerConstants.PageView.Preferences.ACCOUNT_SYNC_PREFERENCES;
    }
}
