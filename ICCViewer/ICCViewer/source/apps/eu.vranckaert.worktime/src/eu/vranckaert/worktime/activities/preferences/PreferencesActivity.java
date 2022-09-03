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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.view.MenuItem;
import android.view.Window;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.activities.account.AccountProfileActivity;
import eu.vranckaert.worktime.activities.account.AccountLoginActivity;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.OSContants;
import eu.vranckaert.worktime.constants.TrackerConstants;
import eu.vranckaert.worktime.service.AccountService;
import eu.vranckaert.worktime.utils.context.ContextUtils;
import eu.vranckaert.worktime.utils.context.IntentUtil;
import eu.vranckaert.worktime.utils.file.FileUtil;
import eu.vranckaert.worktime.utils.preferences.Preferences;
import eu.vranckaert.worktime.utils.string.StringUtils;
import eu.vranckaert.worktime.utils.tracker.AnalyticsTracker;
import eu.vranckaert.worktime.utils.view.actionbar.ActionBarGuicePreferenceActivity;
import roboguice.activity.RoboPreferenceActivity;

/**
 * User: DIRK VRANCKAERT
 * Date: 05/02/11
 * Time: 19:09
 */
public class PreferencesActivity extends ActionBarGuicePreferenceActivity {
    private static final String LOG_TAG = PreferencesActivity.class.getSimpleName();

    private AnalyticsTracker tracker;

    @Inject
    private AccountService accountService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ContextUtils.getAndroidApiVersion() < OSContants.API.HONEYCOMB_3_0) {
            requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // For compatibility with the action-bar
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        setTitle(R.string.lbl_preferences_title);
        setDisplayHomeAsUpEnabled(true);

        tracker = AnalyticsTracker.getInstance(getApplicationContext());
        tracker.trackPageView(TrackerConstants.PageView.PREFERENCES_ACTIVITY);

        configurePreferences(PreferencesActivity.this);
        createPreferences(PreferencesActivity.this);

        // In order to be able to set a default value for the backup location it needs to be already stored in the preferences.
        // The following code checks if the preference already exists, if not it sets the default value for the preference.
        SharedPreferences sp = PreferencesActivity.this.getSharedPreferences(Constants.Preferences.PREFERENCES_NAME, Activity.MODE_PRIVATE);
        String backupLocation = sp.getString(
                Constants.Preferences.Keys.BACKUP_LOCATION,
                null
        );
        if (StringUtils.isBlank(backupLocation)) {
            Preferences.setBackupLocation(PreferencesActivity.this, FileUtil.getDefaultBackupDir());
        }
    }

    private void configurePreferences(RoboPreferenceActivity ctx) {
        ctx.getPreferenceManager().setSharedPreferencesName(Constants.Preferences.PREFERENCES_NAME);
    }

    private void createPreferences(RoboPreferenceActivity ctx) {
        PreferenceScreen preferences = ctx.getPreferenceManager().createPreferenceScreen(ctx);
        setPreferenceScreen(preferences);

        //Category TIME REGISTRATIONS
        createCategoryButton(ctx, preferences, R.string.pref_time_registrations_category_title, TimeRegistrationsPreferencesActivity.class);

        //Category PROJECTS AND TASKS
        createCategoryButton(ctx, preferences, R.string.pref_projects_tasks_category_title, ProjectsAndTasksPreferencesActivity.class);

        //Category DATE AND TIME
        createCategoryButton(ctx, preferences, R.string.pref_date_and_time_category_title, DateTimePreferencesActivity.class);

        //Category NOTIFICATIONS
        createCategoryButton(ctx, preferences, R.string.pref_stat_bar_notifs_category_title, NotificationsPreferencesActivity.class);

        //Category ACCOUNT SYNC
        Preference accountSyncPref = createCategoryButton(ctx, preferences, R.string.pref_account_sync_category_title, AccountSyncPreferencesActivity.class);
        if (!accountService.isUserLoggedIn()){
            accountSyncPref.setEnabled(false);
        }

        //Category BACKUP AND RESTORE
        createCategoryButton(ctx, preferences, R.string.pref_backup_category_title, BackupPreferencesActivity.class);

        //Category RESET APPLICATION
        Preference resetAppItem = new Preference(ctx);
        resetAppItem.setTitle(R.string.pref_reset_application_title);
        resetAppItem.setSummary(R.string.pref_reset_application_summary);
        preferences.addPreference(resetAppItem);
        resetAppItem.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(PreferencesActivity.this, ResetApplicationPreferencesActivity.class);
                startActivity(intent);
                return true;
            }
        });

        //Option RESET ALL PREFERENCES
        Preference resetItem = new Preference(ctx);
        resetItem.setTitle(R.string.pref_reset_category_title);
        resetItem.setSummary(R.string.pref_reset_category_summary);
        preferences.addPreference(resetItem);
        resetItem.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(PreferencesActivity.this, ResetPreferencesActivity.class);
                startActivity(intent);
                return true;
            }
        });
    }

    private Preference createCategoryButton(final Context ctx, final PreferenceScreen preferences,
                                      final int textResId, final Class preferenceActivity) {
        Preference preferencesItem = new Preference(ctx);
        preferencesItem.setTitle(textResId);
        preferences.addPreference(preferencesItem);
        preferencesItem.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(PreferencesActivity.this, preferenceActivity);
                startActivity(intent);
                return true;
            }
        });
        return preferencesItem;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                IntentUtil.goHome(PreferencesActivity.this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tracker.stopSession();
    }
}
