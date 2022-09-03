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
import android.os.Looper;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.comparators.preferences.DatabaseBackupFileComparator;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.exceptions.SDCardUnavailableException;
import eu.vranckaert.worktime.exceptions.backup.BackupFileCouldNotBeWritten;
import eu.vranckaert.worktime.service.BackupService;
import eu.vranckaert.worktime.service.ui.StatusBarNotificationService;
import eu.vranckaert.worktime.service.ui.WidgetService;
import eu.vranckaert.worktime.utils.context.AsyncHelper;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.string.StringUtils;
import eu.vranckaert.worktime.utils.view.actionbar.synclock.SyncLockedGuiceActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 11/09/11
 * Time: 11:49
 */
public class RestoreFromSDActivity extends SyncLockedGuiceActivity {
    private static final String LOG_TAG = RestoreFromSDActivity.class.getSimpleName();
    @Inject
    private BackupService backupService;

    @Inject
    private WidgetService widgetService;

    @Inject
    private StatusBarNotificationService statusBarNotificationService;

    private File restoreFile;
    private List<File> databaseBackupFiles;
    private String error = "";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            databaseBackupFiles = backupService.getPossibleRestoreFiles(getApplicationContext());
            if (databaseBackupFiles == null || databaseBackupFiles.isEmpty()) {
                showDialog(Constants.Dialog.BACKUP_RESTORE_FILE_SEARCH_NOTHING_FOUND);
            } else {
                processDatabaseBackupFiles();
            }
        } catch (SDCardUnavailableException e) {
            showDialog(Constants.Dialog.BACKUP_RESTORE_FILE_SEARCH_NO_SD);
        }
    }

    private void processDatabaseBackupFiles() {
        Collections.sort(databaseBackupFiles, new DatabaseBackupFileComparator());

        showDialog(Constants.Dialog.BACKUP_RESTORE_FILE_SEARCH_SHOW_LIST);
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;

        switch (id) {
            case Constants.Dialog.BACKUP_RESTORE_FILE_SEARCH_NOTHING_FOUND: {
                AlertDialog.Builder alertRestoreNothingFound = new AlertDialog.Builder(this);
				alertRestoreNothingFound
						   .setMessage(getString(R.string.msg_backup_restore_no_backup_files_found))
						   .setCancelable(false)
						   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int which) {
                                   finish();
                                   dialog.cancel();
                               }
                           });
				dialog = alertRestoreNothingFound.create();
                break;
            }
            case Constants.Dialog.BACKUP_RESTORE_FILE_SEARCH_NO_SD: {
                AlertDialog.Builder alertRestoreNoSd = new AlertDialog.Builder(this);
				alertRestoreNoSd
						   .setMessage(getString(R.string.warning_msg_sd_car_unavailable))
						   .setCancelable(false)
						   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int which) {
                                   finish();
                                   dialog.cancel();
                               }
                           });
				dialog = alertRestoreNoSd.create();
                break;
            }
            case Constants.Dialog.BACKUP_RESTORE_FILE_SEARCH_SHOW_LIST: {
                List<String> fileNames = new ArrayList<String>();
                for (File file : databaseBackupFiles) {
                    Log.d(getApplicationContext(), LOG_TAG, "Filename found: " + file.getName());
                    fileNames.add(backupService.toString(RestoreFromSDActivity.this, file));
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.lbl_backup_restore_restore_backup_list_title)
                       .setSingleChoiceItems(
                               StringUtils.convertListToArray(fileNames),
                               0,
                               new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogInterface, int index) {
                                        Log.d(getApplicationContext(), LOG_TAG, "File at index " + index + " choosen.");
                                        restoreFile = databaseBackupFiles.get(index);
                                        removeDialog(Constants.Dialog.BACKUP_RESTORE_FILE_SEARCH_SHOW_LIST);
                                        showDialog(Constants.Dialog.BACKUP_RESTORE_START_QUESTION);
                                    }
                               }
                       )
                       .setOnCancelListener(new DialogInterface.OnCancelListener() {
                           public void onCancel(DialogInterface dialogInterface) {
                               Log.d(getApplicationContext(), LOG_TAG, "No backup file chosen, close the activity");
                               RestoreFromSDActivity.this.finish();
                           }
                       });
                dialog = builder.create();
                break;
            }
            case Constants.Dialog.BACKUP_RESTORE_START_QUESTION: {
                AlertDialog.Builder alertRestoreStartQuestion = new AlertDialog.Builder(this);
				alertRestoreStartQuestion
						   .setMessage(getString(R.string.msg_backup_restore_are_your_sure, restoreFile.getName()))
						   .setCancelable(false)
						   .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int which) {
                                   startRestoreProcedure();
                               }
                           })
                           .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int which) {
                                   finish();
                                   dialog.cancel();
                               }
                           });
				dialog = alertRestoreStartQuestion.create();
                break;
            }
            case Constants.Dialog.RESTORE_IN_PROGRESS: {
                dialog = ProgressDialog.show(
                        RestoreFromSDActivity.this,
                        "",
                        getString(R.string.lbl_backup_restore_restoring_backup_from_sd, restoreFile.getName()),
                        true,
                        false
                );
                break;
            }
            case Constants.Dialog.RESTORE_SUCCESS: {
                AlertDialog.Builder alertRestoreSuccess = new AlertDialog.Builder(this);
				alertRestoreSuccess
						   .setMessage(getString(R.string.msg_backup_restore_success, restoreFile.getName()))
						   .setCancelable(false)
						   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int which) {
                                   finish();
                                   dialog.cancel();
                               }
                           });
				dialog = alertRestoreSuccess.create();
                break;
            }
            case Constants.Dialog.RESTORE_ERROR: {
                AlertDialog.Builder alertRestoreError = new AlertDialog.Builder(this);
				alertRestoreError
						   .setMessage(error)
						   .setCancelable(false)
						   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int which) {
                                   finish();
                                   dialog.cancel();
                               }
                           });
				dialog = alertRestoreError.create();
                break;
            }
        }

        return dialog;
    }

    private void startRestoreProcedure() {
        AsyncTask restoreTask = new AsyncTask() {
            @Override
            protected void onPreExecute() {
                showDialog(Constants.Dialog.RESTORE_IN_PROGRESS);
            }

            @Override
            protected Object doInBackground(Object... objects) {
                Log.d(getApplicationContext(), LOG_TAG, "Is there already a looper? " + (Looper.myLooper() != null));
                if(Looper.myLooper() == null) {
                    Looper.prepare();
                }

                try {
                    backupService.restore(getApplicationContext(), restoreFile);

                    statusBarNotificationService.addStatusBarNotificationForRestore(true, null, null);
                } catch (SDCardUnavailableException e) {
                    error = getString(R.string.warning_msg_sd_car_unavailable);
                    String errorShort = getString(R.string.warning_msg_sd_car_unavailable_short);
                    statusBarNotificationService.addStatusBarNotificationForRestore(false, errorShort, error);
                } catch (BackupFileCouldNotBeWritten backupFileCouldNotBeWritten) {
                    error = getString(R.string.msg_backup_restore_writing_backup_file_not_written);
                    statusBarNotificationService.addStatusBarNotificationForRestore(false, null, null);
                }
                widgetService.updateAllWidgets();
                statusBarNotificationService.removeOngoingTimeRegistrationNotification();
                statusBarNotificationService.addOrUpdateNotification(null);

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                removeDialog(Constants.Dialog.RESTORE_IN_PROGRESS);
                if(StringUtils.isBlank(error)) {
                    showDialog(Constants.Dialog.RESTORE_SUCCESS);
                } else {
                    showDialog(Constants.Dialog.RESTORE_ERROR);
                }
            }
        };

        AsyncHelper.start(restoreTask);
    }
}