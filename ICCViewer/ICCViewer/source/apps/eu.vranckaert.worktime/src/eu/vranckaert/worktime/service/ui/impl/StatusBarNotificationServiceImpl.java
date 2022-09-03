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

package eu.vranckaert.worktime.service.ui.impl;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.google.inject.Inject;
import com.jakewharton.notificationcompat2.NotificationCompat2;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.activities.HomeActivity;
import eu.vranckaert.worktime.activities.account.AccountSyncHistoryActivity;
import eu.vranckaert.worktime.activities.notifcationbar.StatusBarOthersActionHandleActivity;
import eu.vranckaert.worktime.activities.notifcationbar.StatusBarPunchOutHandleActivity;
import eu.vranckaert.worktime.activities.notifcationbar.StatusBarSplitActionHandleActivity;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.model.notification.NotificationAction;
import eu.vranckaert.worktime.service.ProjectService;
import eu.vranckaert.worktime.service.TaskService;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.service.impl.ProjectServiceImpl;
import eu.vranckaert.worktime.service.impl.TaskServiceImpl;
import eu.vranckaert.worktime.service.impl.TimeRegistrationServiceImpl;
import eu.vranckaert.worktime.service.ui.StatusBarNotificationService;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.date.DateFormat;
import eu.vranckaert.worktime.utils.date.DateUtils;
import eu.vranckaert.worktime.utils.date.TimeFormat;
import eu.vranckaert.worktime.utils.preferences.Preferences;
import eu.vranckaert.worktime.utils.string.StringUtils;

import java.util.Date;

public class StatusBarNotificationServiceImpl implements StatusBarNotificationService {
    private static final String LOG_TAG = StatusBarNotificationServiceImpl.class.getSimpleName();

    @Inject
    private Context context;

    @Inject
    private TaskService taskService;

    @Inject
    private ProjectService projectService;

    @Inject
    private TimeRegistrationService timeRegistrationService;

    public StatusBarNotificationServiceImpl(Context context) {
        this.context = context;
        getServices(context);
    }

    /**
     * Default constructor required by RoboGuice!
     */
    public StatusBarNotificationServiceImpl() {}

    @Override
    public void removeOngoingTimeRegistrationNotification() {
        //Remove the status bar notifications
        removeMessage(Constants.StatusBarNotificationIds.ONGOING_TIME_REGISTRATION_MESSAGE);
    }

    @Override
    public void addOrUpdateNotification(TimeRegistration registration) {
        Log.d(context, LOG_TAG, "Handling status bar notifications...");

        boolean showStatusBarNotifications = Preferences.getShowStatusBarNotificationsPreference(context);
        Log.d(context, LOG_TAG, "Status bar notifications enabled? " + (showStatusBarNotifications?"Yes":"No"));

        if (registration == null) {
            registration = timeRegistrationService.getLatestTimeRegistration();
            if (registration == null) {
                Log.d(context, LOG_TAG, "Cannot add a notification because no time registration is found!");
                return;
            }
        }

        if (showStatusBarNotifications && registration != null && registration.isOngoingTimeRegistration()) {
            //Create the status bar notifications
            Log.d(context, LOG_TAG, "Ongoing time registration... Refreshing the task and project...");
            taskService.refresh(registration.getTask());
            projectService.refresh(registration.getTask().getProject());

            String startTime = DateUtils.DateTimeConverter.convertDateTimeToString(registration.getStartTime(), DateFormat.MEDIUM, TimeFormat.MEDIUM, context);
            String projectName = registration.getTask().getProject().getName();
            String taskName = registration.getTask().getName();

            String title = context.getString(R.string.lbl_notif_title_ongoing_tr);
            String message = context.getString(R.string.lbl_notif_project_task_name, projectName, taskName);
            String ticker = null;

            Intent intent = new Intent(context, HomeActivity.class);

            if (registration.getId() == null) {
                // creating...
                Log.d(context, LOG_TAG, "A status bar notifications for project '" + projectName + "' and task '" + taskName + "' will be created");
                ticker = context.getString(R.string.lbl_notif_new_tr_started);
            } else {
                // updating...
                Log.d(context, LOG_TAG, "The status bar notifications for project '" + projectName + "' and task '" + taskName + "' will be updated");
                ticker = context.getString(R.string.lbl_notif_update_tr);
            }

            String bigText = context.getString(R.string.lbl_notif_big_text_project) + ": " + projectName + "\n"
                    + context.getString(R.string.lbl_notif_big_text_task) + ": " + taskName + "\n"
                    + context.getString(R.string.lbl_notif_big_text_started_at) + " " + startTime;

            if (StringUtils.isNotBlank(registration.getComment())) {
                bigText += "\n" + context.getString(R.string.lbl_notif_big_text_comment) + ": " + registration.getComment();
            }


            // Prepare the notification action buttons for jelly bean (4.1) and up!
            Intent punchOutActionIntent = new Intent(context, StatusBarPunchOutHandleActivity.class);
            punchOutActionIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            Intent splitActionIntent = new Intent(context, StatusBarSplitActionHandleActivity.class);
            splitActionIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            Intent othersActionIntent = new Intent(context, StatusBarOthersActionHandleActivity.class);
            othersActionIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            NotificationAction punchOutAction = new NotificationAction(context.getString(R.string.lbl_notif_action_punch_out), punchOutActionIntent);
            NotificationAction splitAction = new NotificationAction(context.getString(R.string.lbl_notif_action_split), splitActionIntent, Constants.IntentRequestCodes.TIME_REGISTRATION_ACTION);
            NotificationAction othersAction = new NotificationAction(context.getString(R.string.lbl_notif_action_others), othersActionIntent, Constants.IntentRequestCodes.TIME_REGISTRATION_ACTION);

            setStatusBarNotification(
                    title, message, ticker, intent, bigText, null,
                    Constants.StatusBarNotificationIds.ONGOING_TIME_REGISTRATION_MESSAGE,
                    NotificationCompat2.PRIORITY_LOW, null, true, punchOutAction, splitAction, othersAction
            );
        }
    }

    public void addStatusBarNotificationForBackup(boolean success) {

    }

    @Override
    public void addStatusBarNotificationForBackup(String backupLocation, boolean success, String text, String bigText) {
        boolean showStatusBarNotifications = Preferences.getShowStatusBarNotificationsPreference(context);

        if (!showStatusBarNotifications) {
            return;
        }

        String ticker = null;
        String title = null;
        String message = null;
        String bigTextMessage = null;
        int drawable = -1;

        if (success) {
            ticker = context.getString(R.string.msg_backup_successful_notification_ticker);
            title = context.getString(R.string.msg_backup_successful_notification_title);
            message = context.getString(R.string.msg_backup_successful_notification_message);
            bigTextMessage = context.getString(R.string.msg_backup_successful_notification_big_text_message, DateUtils.DateTimeConverter.convertDateTimeToString(new Date(), DateFormat.MEDIUM, TimeFormat.MEDIUM, context), backupLocation);
            drawable = R.drawable.backup_restore_notif_bar;
        } else {
            ticker = context.getString(R.string.msg_backup_failed_notification_ticker);
            title = context.getString(R.string.msg_backup_failed_notification_title);
            message = context.getString(R.string.msg_backup_failed_notification_message);
            bigTextMessage = context.getString(R.string.msg_backup_failed_notification_big_text_message);
            drawable = R.drawable.failure_notif_bar;
        }

        if (text != null)
            message = text;
        if (bigText != null)
            bigTextMessage = bigText;

        setStatusBarNotification(title, message, ticker, new Intent(), bigTextMessage, null, Constants.StatusBarNotificationIds.BACKUP, NotificationCompat2.PRIORITY_HIGH, drawable, false);
    }

    @Override
    public void addStatusBarNotificationForRestore(boolean success, String text, String bigText) {
        boolean showStatusBarNotifications = Preferences.getShowStatusBarNotificationsPreference(context);

        if (!showStatusBarNotifications) {
            return;
        }

        String ticker = null;
        String title = null;
        String message = null;
        String bigTextMessage = null;
        int drawable = -1;

        if (success) {
            ticker = context.getString(R.string.msg_restore_successful_notification_ticker);
            title = context.getString(R.string.msg_restore_successful_notification_title);
            message = context.getString(R.string.msg_restore_successful_notification_message);
            bigTextMessage = context.getString(R.string.msg_restore_successful_notification_big_text_message, DateUtils.DateTimeConverter.convertDateTimeToString(new Date(), DateFormat.MEDIUM, TimeFormat.MEDIUM, context));
            drawable = R.drawable.backup_restore_notif_bar;
        } else {
            ticker = context.getString(R.string.msg_restore_failed_notification_ticker);
            title = context.getString(R.string.msg_restore_failed_notification_title);
            message = context.getString(R.string.msg_restore_failed_notification_message);
            bigTextMessage = context.getString(R.string.msg_restore_failed_notification_big_text_message);
            drawable = R.drawable.failure_notif_bar;
        }

        if (text != null)
            message = text;
        if (bigText != null)
            bigTextMessage = bigText;

        setStatusBarNotification(title, message, ticker, new Intent(), bigTextMessage, null, Constants.StatusBarNotificationIds.RESTORE, NotificationCompat2.PRIORITY_HIGH, drawable, false);
    }

    @Override
    public void addStatusBarNotificationForSync(int titleResId, int smallMsgResId, int msgResId) {
        String ticker = null;
        String title = null;
        String message = null;
        String bigTextMessage = null;

        ticker = context.getString(titleResId);
        title = context.getString(titleResId);
        message = context.getString(smallMsgResId);
        bigTextMessage = context.getString(msgResId);

        Intent intent = new Intent(context, AccountSyncHistoryActivity.class);

        setStatusBarNotification(title, message, ticker, intent, bigTextMessage, title, Constants.StatusBarNotificationIds.SYNC, NotificationCompat2.PRIORITY_HIGH, null, false);
    }

    @Override
    public void removeSyncNotifications() {
        removeMessage(Constants.StatusBarNotificationIds.SYNC);
    }

    /**
     * Get an instance of the Android {@link NotificationManager}.
     * @return An instance of the {@link NotificationManager}.
     */
    private NotificationManager getNotificationManager() {
        Log.d(context, LOG_TAG, "Creating a NoticationManager instance");

        String ns = Context.NOTIFICATION_SERVICE;
        return (NotificationManager) context.getSystemService(ns);
    }

    /**
     * Remove a message form the notification bar.
     * @param id The id of the message to be removed from the notification bar. The id should be found in
     * {@link Constants.StatusBarNotificationIds};
     */
    private void removeMessage(int id) {
        Log.d(context, LOG_TAG, "Remove status bar notification messages with ID: " + id);

        NotificationManager notificationManager = getNotificationManager();
        notificationManager.cancel(id);
    }

    /**
     * Removes all messages from the notification bar.
     */
    private void removeAllMessages() {
        Log.d(context, LOG_TAG, "Remove all messages");

        NotificationManager notificationManager = getNotificationManager();
        notificationManager.cancelAll();
    }

    /**
     * Creates a new status bar notification message.
     * @param title              The title of the notification.
     * @param message            The message of the notification.
     * @param ticker             The ticker-text shown when the notification is created.
     * @param intent             The intent to be launched when the notification is selected.
     * @param bigText            The 'big-text' to be shown in the 4.1 and up OS versions.
     * @param bigContentTitle    The title to be shown when a big-text is shown in the 4.1 and up OS versions.
     * @param notificationId     The id of the notification.
     * @param priority           The priority of the notification.
     * @param iconDrawable       The icon of the notification.
     * @param fixed              If the notification can be removed by the user or not.
     * @param actions            The possible actions of type {@link NotificationAction} that be performed the
     *                           notification. The actions will only be used on 4.1 and up OS versions.
     */
    private void setStatusBarNotification(String title, String message, String ticker, Intent intent, String bigText, String bigContentTitle, int notificationId, int priority, Integer iconDrawable, boolean fixed, NotificationAction... actions) {
        int icon = R.drawable.logo_notif_bar;
        if (iconDrawable != null)
            icon = iconDrawable;

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

        final NotificationManager mgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        NotificationCompat2.Builder builder = new NotificationCompat2.Builder(context)
                .setSmallIcon(icon)
                .setTicker(ticker)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(contentIntent)
                .setPriority(priority)
                .setOngoing(fixed);

        for (NotificationAction action : actions) {
            builder.addAction(action.getDrawable(), action.getText(), PendingIntent.getActivity(context, action.getIntentRequestCode(), action.getIntent(), 0));
        }

        NotificationCompat2.BigTextStyle bigTextStyle = new NotificationCompat2.BigTextStyle(builder).bigText(bigText);
        if (bigContentTitle != null) {
            bigTextStyle.setBigContentTitle(bigContentTitle);
        }
        Notification notification = bigTextStyle.build();
//        if (fixed)
//            notification.flags |= Notification.FLAG_NO_CLEAR;
        mgr.notify(notificationId, notification);
    }

    /**
     * Create all the required service instances.
     * @param ctx The widget's context.
     */
    private void getServices(Context ctx) {
        this.timeRegistrationService = new TimeRegistrationServiceImpl(ctx);
        this.projectService = new ProjectServiceImpl(ctx);
        this.taskService = new TaskServiceImpl(ctx);
        Log.d(context, LOG_TAG, "Services ok!");
    }
}
