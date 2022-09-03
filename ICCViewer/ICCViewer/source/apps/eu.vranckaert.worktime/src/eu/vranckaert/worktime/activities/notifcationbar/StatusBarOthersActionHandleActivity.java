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

package eu.vranckaert.worktime.activities.notifcationbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.service.ui.StatusBarNotificationService;
import roboguice.activity.RoboActivity;

/**
 * User: Dirk Vranckaert
 * Date: 07/02/13
 * Time: 15:29
 */
public class StatusBarOthersActionHandleActivity extends RoboActivity {
    @Inject
    private TimeRegistrationService timeRegistrationService;

    @Inject
    private StatusBarNotificationService statusBarNotificationService;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        launchTimeRegistrationActionsDialog();
    }

    private void launchTimeRegistrationActionsDialog() {
        TimeRegistration timeRegistration = timeRegistrationService.getLatestTimeRegistration();
        if (timeRegistration != null) {
            Intent intent = new Intent();
            intent.setAction(Constants.Broadcast.TIME_REGISTRATION_ACTION_DIALOG);
            intent.putExtra(Constants.Extras.TIME_REGISTRATION, timeRegistration);
            sendBroadcast(intent);
        } else {
            Toast.makeText(this, R.string.lbl_notif_no_tr_found, Toast.LENGTH_LONG);
            statusBarNotificationService.removeOngoingTimeRegistrationNotification();
        }
        finish();
    }
}
