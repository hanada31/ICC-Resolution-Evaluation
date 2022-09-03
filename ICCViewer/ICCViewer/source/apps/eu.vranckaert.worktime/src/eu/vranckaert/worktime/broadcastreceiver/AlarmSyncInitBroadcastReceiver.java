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
import com.google.inject.Inject;
import eu.vranckaert.worktime.activities.preferences.AccountSyncPreferencesActivity;
import eu.vranckaert.worktime.service.AccountService;
import roboguice.receiver.RoboBroadcastReceiver;

/**
 * User: Dirk Vranckaert
 * Date: 16/01/13
 * Time: 14:02
 */
public class AlarmSyncInitBroadcastReceiver extends RoboBroadcastReceiver {
    @Inject private AccountService accountService;

    @Override
    protected void handleReceive(Context context, Intent intent) {
        if (accountService.isUserLoggedIn()) {
            AccountSyncPreferencesActivity.scheduleAlarm(context, accountService);
            //AlarmUtil.setAlarmSyncCycle(context, accountService.getLastSyncHistory(), Preferences.Account.syncInterval(context));
        }
    }
}
