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

package eu.vranckaert.worktime.utils.context;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import eu.vranckaert.worktime.constants.Constants;

public class BackupAgent extends BackupAgentHelper {
    private static final String LOG_TAG = BackupAgent.class.getSimpleName();

    private static final String PREFERENCES_BACKUP_KEY = "preferences";

    @Override
    public void onCreate() {
        // Backup the preferences
        SharedPreferencesBackupHelper prefHelper = new SharedPreferencesBackupHelper(this, Constants.Preferences.PREFERENCES_NAME);
        addHelper(PREFERENCES_BACKUP_KEY, prefHelper);
    }
}
