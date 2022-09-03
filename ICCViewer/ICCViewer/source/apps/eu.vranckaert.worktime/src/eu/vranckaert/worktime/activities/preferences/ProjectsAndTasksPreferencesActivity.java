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
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.constants.TrackerConstants;
import eu.vranckaert.worktime.utils.activity.GenericPreferencesActivity;

/**
 * User: DIRK VRANCKAERT
 * Date: 31/01/12
 * Time: 9:19
 */
public class ProjectsAndTasksPreferencesActivity extends GenericPreferencesActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.pref_projects_tasks_category_title);
    }

    @Override
    public int getPreferenceResourceId() {
        return R.xml.preference_projects_tasks;
    }

    @Override
    public String getPageViewTrackerId() {
        return TrackerConstants.PageView.Preferences.TASK_PREFERENCES;
    }
}
