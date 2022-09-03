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

package eu.vranckaert.worktime.utils.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.OSContants;
import eu.vranckaert.worktime.utils.context.ContextUtils;
import eu.vranckaert.worktime.utils.context.IntentUtil;
import eu.vranckaert.worktime.utils.string.StringUtils;
import eu.vranckaert.worktime.utils.tracker.AnalyticsTracker;
import eu.vranckaert.worktime.utils.view.actionbar.ActionBarGuicePreferenceActivity;

/**
 * User: DIRK VRANCKAERT
 * Date: 31/01/12
 * Time: 9:21
 */
public abstract class GenericPreferencesActivity extends ActionBarGuicePreferenceActivity {
    private AnalyticsTracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ContextUtils.getAndroidApiVersion() < OSContants.API.HONEYCOMB_3_0) {
            requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // For compatibility with the action-bar
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        setDisplayHomeAsUpEnabled(true);

        tracker = AnalyticsTracker.getInstance(getApplicationContext());
        if (StringUtils.isNotBlank(getPageViewTrackerId())) {
            tracker.trackPageView(getPageViewTrackerId());
        }

        getPreferenceManager().setSharedPreferencesName(Constants.Preferences.PREFERENCES_NAME);

        addPreferencesFromResource(getPreferenceResourceId());
    }

    public abstract int getPreferenceResourceId();
    
    public abstract String getPageViewTrackerId();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                IntentUtil.goBack(GenericPreferencesActivity.this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tracker.stopSession();
    }
}
