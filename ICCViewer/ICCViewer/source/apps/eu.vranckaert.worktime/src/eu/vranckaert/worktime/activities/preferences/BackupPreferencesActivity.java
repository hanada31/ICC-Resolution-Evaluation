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

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.widget.Toast;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.TrackerConstants;
import eu.vranckaert.worktime.exceptions.file.ExternalStorageNotAvailableException;
import eu.vranckaert.worktime.exceptions.file.ExternalStorageNotWritableException;
import eu.vranckaert.worktime.utils.activity.GenericPreferencesActivity;
import eu.vranckaert.worktime.utils.file.DirectoryPicker;
import eu.vranckaert.worktime.utils.file.FileUtil;
import eu.vranckaert.worktime.utils.preferences.Preferences;

import java.io.File;

/**
 * User: DIRK VRANCKAERT
 * Date: 31/01/12
 * Time: 9:20
 */
public class BackupPreferencesActivity extends GenericPreferencesActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.pref_backup_category_title);

        Preference backupLocation = (Preference) getPreferenceScreen().findPreference(Constants.Preferences.Keys.BACKUP_LOCATION);
        backupLocation.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    FileUtil.checkExternalStorageState();
                } catch (ExternalStorageNotWritableException e) {
                    // Not a problem in this case...
                } catch (ExternalStorageNotAvailableException e) {
                    Toast.makeText(BackupPreferencesActivity.this, R.string.warning_msg_external_storage_unavailable, Toast.LENGTH_LONG).show();
                    return false;
                }

                File currentBackupLocation = Preferences.getBackupLocation(BackupPreferencesActivity.this);
                if (!currentBackupLocation.exists() || !currentBackupLocation.isDirectory()) {
                    currentBackupLocation = FileUtil.getDefaultBackupDir();
                }

                Intent directoryPickerIntent = new Intent(BackupPreferencesActivity.this, DirectoryPicker.class);
                directoryPickerIntent.putExtra(DirectoryPicker.START_DIR, currentBackupLocation.getAbsolutePath());
                directoryPickerIntent.putExtra(DirectoryPicker.ONLY_DIRS, false);
                directoryPickerIntent.putExtra(DirectoryPicker.ALLOW_CREATE_DIR, true);
                startActivityForResult(directoryPickerIntent, DirectoryPicker.PICK_DIRECTORY);
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == DirectoryPicker.PICK_DIRECTORY && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            File selectedBackupLocation = (File) extras.get(DirectoryPicker.CHOSEN_DIRECTORY);
            Preferences.setBackupLocation(this, selectedBackupLocation);
        }
    }

    @Override
    public int getPreferenceResourceId() {
        return R.xml.preference_backup;
    }

    @Override
    public String getPageViewTrackerId() {
        return TrackerConstants.PageView.Preferences.BACKUP_PREFERENCES;
    }
}
