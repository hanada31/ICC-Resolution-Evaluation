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
import android.content.DialogInterface;
import android.os.Bundle;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.utils.preferences.Preferences;
import eu.vranckaert.worktime.utils.view.actionbar.synclock.SyncLockedGuiceActivity;
import roboguice.activity.RoboActivity;

public class ResetPreferencesActivity extends SyncLockedGuiceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showDialog(Constants.Dialog.RESET_PREFERENCES_CONFIRMATION);
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;

        switch (id) {
            case Constants.Dialog.RESET_PREFERENCES_CONFIRMATION: {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle(R.string.pref_reset_preferences_title)
                           .setMessage(R.string.pref_reset_preferences_confirmation_message)
                           .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialogInterface, int i) {
                                   dismissDialog(Constants.Dialog.RESET_PREFERENCES_CONFIRMATION);
                                   Preferences.removeAllPreferences(ResetPreferencesActivity.this);
                                   finish();
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
        }

        return dialog;
    }
}
