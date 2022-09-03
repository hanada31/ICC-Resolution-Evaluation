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

package eu.vranckaert.worktime.broadcastreceiver;

import android.content.Context;
import android.content.Intent;
import eu.vranckaert.worktime.activities.timeregistrations.TimeRegistrationActionActivity;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.enums.timeregistration.TimeRegistrationAction;
import eu.vranckaert.worktime.model.TimeRegistration;
import roboguice.receiver.RoboBroadcastReceiver;

/**
 * User: Dirk Vranckaert
 * Date: 07/02/13
 * Time: 15:04
 */
public class ActionDialogBroadCastReceiver extends RoboBroadcastReceiver {
    @Override
    protected void handleReceive(Context context, Intent intent) {
        TimeRegistration timeRegistration = (TimeRegistration) intent.getExtras().get(Constants.Extras.TIME_REGISTRATION);
        TimeRegistrationAction defaultAction = (TimeRegistrationAction) intent.getExtras().get(Constants.Extras.DEFAULT_ACTION);
        Boolean skipDialog = (Boolean) intent.getExtras().get(Constants.Extras.SKIP_DIALOG);
        Boolean onlyAction = (Boolean) intent.getExtras().get(Constants.Extras.ONLY_ACTION);

        Intent actionIntent = new Intent(context, TimeRegistrationActionActivity.class);
        if (timeRegistration != null) {
            actionIntent.putExtra(Constants.Extras.TIME_REGISTRATION, timeRegistration);
        }
        if (defaultAction != null) {
            actionIntent.putExtra(Constants.Extras.DEFAULT_ACTION, defaultAction);
        }
        if (skipDialog != null) {
            actionIntent.putExtra(Constants.Extras.SKIP_DIALOG, skipDialog);
        }
        if (onlyAction != null) {
            actionIntent.putExtra(Constants.Extras.ONLY_ACTION, onlyAction);
        }
        actionIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(actionIntent);
    }
}
