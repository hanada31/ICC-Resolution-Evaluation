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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.service.AccountService;
import eu.vranckaert.worktime.service.ProjectService;
import eu.vranckaert.worktime.service.TaskService;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.service.ui.StatusBarNotificationService;
import eu.vranckaert.worktime.service.ui.WidgetService;
import eu.vranckaert.worktime.utils.context.AsyncHelper;
import eu.vranckaert.worktime.utils.view.actionbar.synclock.SyncLockedGuiceActivity;

public class ResetApplicationPreferencesActivity extends SyncLockedGuiceActivity {
    @Inject
    private StatusBarNotificationService notificationService;

    @Inject
    private WidgetService widgetService;

    @Inject
    private TimeRegistrationService timeRegistrationService;

    @Inject
    private ProjectService projectService;

    @Inject
    private TaskService taskService;

    @Inject
    private AccountService accountService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showDialog(Constants.Dialog.RESET_APPLICATION_CONFIRMATION);
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;

        switch (id) {
            case Constants.Dialog.RESET_APPLICATION_CONFIRMATION: {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle(R.string.pref_reset_application_title)
                           .setMessage(R.string.pref_rest_application_confirmation_message)
                           .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialogInterface, int i) {
                                   dismissDialog(Constants.Dialog.RESET_APPLICATION_CONFIRMATION);
                                   AsyncHelper.start(new ResetApplicationTask());
                               }
                           })
                           .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialogInterface, int i) {
                                   finish();
                               }
                           })
                           .setOnCancelListener(new DialogInterface.OnCancelListener() {
                               @Override
                               public void onCancel(DialogInterface dialogInterface) {
                                   finish();
                               }
                           });

                dialog = alertDialog.create();
                break;
            }

            case Constants.Dialog.LOADING_RESET_APPLICATION: {
                dialog = ProgressDialog.show(
                        ResetApplicationPreferencesActivity.this,
                        "",
                        getString(R.string.pref_rest_application_loading_dialog_message),
                        true,
                        false
                );
                break;
            }
        }

        return dialog;
    }

    private class ResetApplicationTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            showDialog(Constants.Dialog.LOADING_RESET_APPLICATION);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            timeRegistrationService.removeAll();
            taskService.removeAll();
            projectService.removeAll();
            accountService.removeAll();

            projectService.insertDefaultProjectAndTaskData();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            removeDialog(Constants.Dialog.LOADING_RESET_APPLICATION);

            widgetService.updateAllWidgets();
            notificationService.addOrUpdateNotification(null);

            finish();
        }
    }
}
