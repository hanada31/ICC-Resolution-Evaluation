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

package eu.vranckaert.worktime.activities.notifcationbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.activities.timeregistrations.TimeRegistrationActionActivity;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.enums.timeregistration.TimeRegistrationAction;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.service.ui.StatusBarNotificationService;
import eu.vranckaert.worktime.utils.preferences.Preferences;
import roboguice.activity.RoboActivity;

/**
 * User: DIRK VRANCKAERT
 * Date: 27/04/11
 * Time: 21:46
 */
public class StatusBarPunchOutHandleActivity extends RoboActivity {
    @Inject
    private TimeRegistrationService timeRegistrationService;

    @Inject
    private StatusBarNotificationService statusBarNotificationService;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        launchStopTimeRegistrationActivity();
    }

    private void launchStopTimeRegistrationActivity() {
        TimeRegistration timeRegistration = timeRegistrationService.getLatestTimeRegistration();
        if (timeRegistration != null) {
            Intent intent = new Intent(getApplicationContext(), TimeRegistrationActionActivity.class);
            intent.putExtra(Constants.Extras.TIME_REGISTRATION, timeRegistration);
            intent.putExtra(Constants.Extras.DEFAULT_ACTION, TimeRegistrationAction.PUNCH_OUT);
            if (Preferences.getImmediatePunchOut(this)) {
                intent.putExtra(Constants.Extras.SKIP_DIALOG, true);
            } else {
                intent.putExtra(Constants.Extras.ONLY_ACTION, true);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent, Constants.IntentRequestCodes.TIME_REGISTRATION_ACTION);
        } else {
            Toast.makeText(this, R.string.lbl_notif_no_tr_found, Toast.LENGTH_LONG);
            statusBarNotificationService.removeOngoingTimeRegistrationNotification();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        finish();
    }
}