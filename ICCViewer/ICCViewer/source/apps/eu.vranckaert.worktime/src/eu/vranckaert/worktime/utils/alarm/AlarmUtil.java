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

package eu.vranckaert.worktime.utils.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import eu.vranckaert.worktime.activities.account.AccountSyncService;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.model.SyncHistory;

import java.util.Calendar;
import java.util.Date;

/**
 * User: Dirk Vranckaert
 * Date: 16/01/13
 * Time: 13:10
 */
public class AlarmUtil {
    private static final String LOG_TAG = AlarmUtil.class.getSimpleName();

    private static AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    private static PendingIntent getSyncOperation(Context context, int requestCode) {
        Intent intent = new Intent(context, AccountSyncService.class);
        PendingIntent operation = PendingIntent.getService(context, requestCode, intent, 0);
        return operation;
    }

    /**
     * Remove all planned synchronization alarms.
     * @param context The context.
     */
    public static void removeAllSyncAlarms(Context context) {
        getAlarmManager(context).cancel(getSyncOperation(context, Constants.IntentRequestCodes.ALARM_SYNC_REPEAT));
        getAlarmManager(context).cancel(getSyncOperation(context, Constants.IntentRequestCodes.ALARM_SYNC_RETRY));
        Log.i(LOG_TAG, "The alarm sync cycle has been removed");
    }

    /**
     * Schedule the alarms for the automated synchronization process. If the last sync history object is null the next
     * synchronization will happen in 5 minutes. Otherwise the synchronization will check if (according to the settings)
     * it needs to synchronize within five minutes or if the interval can be determined from the last end date of the
     * last synchronization.
     * @param context         The context.
     * @param lastSyncHistory The last sync history, if none null.
     * @param syncInterval    The synchronization interval in milliseconds.
     */
    public static void setAlarmSyncCycle(Context context, SyncHistory lastSyncHistory, long syncInterval) {
        removeAllSyncAlarms(context);

        if (syncInterval == -1)
            return;

        long fiveMinutes = 5 * 60000;

        // Only use this for debugging purpose
//        if (!ContextUtils.isStableBuild(context)) {
//            syncInterval = 60000; // Every minute...
//            fiveMinutes = 30000; // 30 seconds...
//        }

        long nextSync = 1;
        if (lastSyncHistory == null) {
            nextSync = fiveMinutes;
        } else {
            long intervalFromLastSync = (new Date()).getTime() - lastSyncHistory.getStarted().getTime();
            if (intervalFromLastSync >= syncInterval) {
                nextSync = fiveMinutes;
            } else {
                nextSync = syncInterval - intervalFromLastSync;
            }
        }

        Log.i(LOG_TAG, "Alarm scheduled to go off in " + nextSync + " milliseconds and to be repeated in " + syncInterval + " milliseconds.");

        nextSync = (new Date().getTime()) + nextSync;

        getAlarmManager(context).setRepeating(AlarmManager.RTC_WAKEUP, nextSync, syncInterval, getSyncOperation(context, Constants.IntentRequestCodes.ALARM_SYNC_REPEAT));
    }

    public static void setAlarmSyncCycleOnceADay(Context context, SyncHistory lastSyncHistory, Date fixedSyncTime) {
        removeAllSyncAlarms(context);

        long syncInterval = 24 * 3600000L;
        long fiveMinutes = 5 * 60000;

        long nextSync = 1;

        Calendar fixedSyncTimeCal = Calendar.getInstance();
        fixedSyncTimeCal.setTime(fixedSyncTime);

        Calendar syncTime = Calendar.getInstance();
        syncTime.set(Calendar.HOUR_OF_DAY, fixedSyncTimeCal.get(Calendar.HOUR_OF_DAY));
        syncTime.set(Calendar.HOUR, fixedSyncTimeCal.get(Calendar.HOUR));
        syncTime.set(Calendar.AM_PM, fixedSyncTimeCal.get(Calendar.AM_PM));
        syncTime.set(Calendar.MINUTE, fixedSyncTimeCal.get(Calendar.MINUTE));

        Calendar now = Calendar.getInstance();

        if (now.after(syncTime) && lastSyncHistory != null && (now.getTimeInMillis() - lastSyncHistory.getStarted().getTime() > syncInterval) ) {
            nextSync = fiveMinutes;
        } else {
            nextSync = syncTime.getTimeInMillis() - now.getTimeInMillis();
        }

        if (nextSync < fiveMinutes) {
            nextSync = fiveMinutes;
        }

        Log.i(LOG_TAG, "Alarm scheduled to go off in " + nextSync + " milliseconds and to be repeated every " + syncInterval + " milliseconds.");

        nextSync = (new Date().getTime()) + nextSync;

        getAlarmManager(context).setRepeating(AlarmManager.RTC_WAKEUP, nextSync, syncInterval, getSyncOperation(context, Constants.IntentRequestCodes.ALARM_SYNC_REPEAT));
    }

    /**
     * Add syncrhonization alarm that will be triggered within five minutes
     * @param context The context.
     */
    public static void addAlarmSyncInFiveMinutes(Context context) {
        Calendar syncTime = Calendar.getInstance();
        syncTime.add(Calendar.MINUTE, 5);

        getAlarmManager(context).set(AlarmManager.RTC_WAKEUP, syncTime.getTime().getTime(), getSyncOperation(context, Constants.IntentRequestCodes.ALARM_SYNC_RETRY));
    }
}
