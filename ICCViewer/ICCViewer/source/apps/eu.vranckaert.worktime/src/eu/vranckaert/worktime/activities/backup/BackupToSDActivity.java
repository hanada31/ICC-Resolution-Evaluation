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

package eu.vranckaert.worktime.activities.backup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.exceptions.SDCardUnavailableException;
import eu.vranckaert.worktime.exceptions.backup.BackupFileCouldNotBeCreated;
import eu.vranckaert.worktime.exceptions.backup.BackupFileCouldNotBeWritten;
import eu.vranckaert.worktime.service.BackupService;
import eu.vranckaert.worktime.service.ui.StatusBarNotificationService;
import eu.vranckaert.worktime.utils.context.AsyncHelper;
import eu.vranckaert.worktime.utils.string.StringUtils;
import eu.vranckaert.worktime.utils.view.actionbar.synclock.SyncLockedGuiceActivity;

/**
 * User: DIRK VRANCKAERT
 * Date: 11/09/11
 * Time: 11:49
 */
public class BackupToSDActivity extends SyncLockedGuiceActivity {
    private static final String LOG_TAG = BackupToSDActivity.class.getSimpleName();

    @Inject
    private BackupService backupService;

    @Inject
    private StatusBarNotificationService statusBarNotificationService;

    private String result;
    private String error;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AsyncTask backupTask = new AsyncTask() {
            @Override
            protected void onPreExecute() {
                showDialog(Constants.Dialog.BACKUP_IN_PROGRESS);
            }

            @Override
            protected Object doInBackground(Object... objects) {
                try {
                    String backupLocation = backupService.backup(getApplicationContext());

                    statusBarNotificationService.addStatusBarNotificationForBackup(backupLocation, true, null, null);
                    return backupLocation;
                } catch (SDCardUnavailableException e) {
                    error = getString(R.string.warning_msg_sd_car_unavailable);
                    String errorShort = getString(R.string.warning_msg_sd_car_unavailable_short);
                    statusBarNotificationService.addStatusBarNotificationForBackup(null, false, errorShort, error);
                    return false;
                } catch (BackupFileCouldNotBeCreated backupFileCouldNotBeCreated) {
                    error = getString(R.string.msg_backup_restore_writing_backup_file_not_be_created);
                    statusBarNotificationService.addStatusBarNotificationForBackup(null, false, null, null);
                    return false;
                } catch (BackupFileCouldNotBeWritten backupFileCouldNotBeWritten) {
                    error = getString(R.string.msg_backup_restore_writing_backup_file_not_written);
                    statusBarNotificationService.addStatusBarNotificationForBackup(null, false, null, null);
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Object o) {
                removeDialog(Constants.Dialog.BACKUP_IN_PROGRESS);

                if (StringUtils.isNotBlank(error)) {
                    showDialog(Constants.Dialog.BACKUP_ERROR);
                } else {
                    result = (String) o;
                    showDialog(Constants.Dialog.BACKUP_SUCCESS);
                }
            }
        };
        AsyncHelper.start(backupTask);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;

        switch (id) {
            case Constants.Dialog.BACKUP_IN_PROGRESS: {
                dialog = ProgressDialog.show(
                        BackupToSDActivity.this,
                        "",
                        getString(R.string.lbl_backup_restore_writing_backup_sd),
                        true,
                        false
                );
                break;
            }
            case Constants.Dialog.BACKUP_SUCCESS: {
                AlertDialog.Builder alertBackupSuccess = new AlertDialog.Builder(this);
				alertBackupSuccess
						   .setMessage(getString(R.string.msg_backup_restore_writing_backup_sd_success, result))
						   .setCancelable(false)
						   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int which) {
                                   finish();
                                   dialog.cancel();
                               }
                           });
				dialog = alertBackupSuccess.create();
                break;
            }
            case Constants.Dialog.BACKUP_ERROR: {
                AlertDialog.Builder alertBackupSuccess = new AlertDialog.Builder(this);
				alertBackupSuccess
						   .setMessage(error)
						   .setCancelable(false)
						   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int which) {
                                   finish();
                                   dialog.cancel();
                               }
                           });
				dialog = alertBackupSuccess.create();
                break;
            }
        }

        return dialog;
    }


}