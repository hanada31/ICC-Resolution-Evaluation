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

package eu.vranckaert.worktime.constants;

/**
 * User: DIRK VRANCKAERT
 * Date: 06/02/11
 * Time: 15:22
 */
public class Constants {
    public class Dialog {
        public static final int DELETE_PROJECT_YES_NO = 0;
        public static final int DELETE_TIME_REGISTRATION_YES_NO = 1;
        public static final int DELETE_TIME_REGISTRATIONS_YES_NO = 2;
        public static final int CHOOSE_SELECTED_PROJECT = 8;
        public static final int LOADING_TIME_REGISTRATION_PUNCH_OUT = 9;
        public static final int CHOOSE_TASK = 10;
        public static final int DELETE_TASK_YES_NO = 12;
        public static final int DELETE_TIME_REGISTRATIONS_OF_TASK_YES_NO = 13;
        public static final int TIME_REGISTRATION_ACTION = 15;
        public static final int CHOOSE_DATE = 17;
        public static final int CHOOSE_TIME = 18;
        public static final int VALIDATION_DATE_LOWER_LIMIT = 19;
        public static final int VALIDATION_DATE_HIGHER_LIMIT = 20;
        public static final int WARN_TASK_NOT_FINISHED_ONGOING_TR = 21;
        public static final int ASK_FINISH_TASK = 22;
        public static final int BACKUP_IN_PROGRESS = 23;
        public static final int BACKUP_SUCCESS = 24;
        public static final int BACKUP_ERROR = 25;
        public static final int BACKUP_RESTORE_FILE_SEARCH_NOTHING_FOUND = 26;
        public static final int BACKUP_RESTORE_FILE_SEARCH_SHOW_LIST = 27;
        public static final int BACKUP_RESTORE_FILE_SEARCH_NO_SD = 28;
        public static final int RESTORE_IN_PROGRESS = 29;
        public static final int BACKUP_RESTORE_START_QUESTION = 30;
        public static final int RESTORE_SUCCESS = 31;
        public static final int RESTORE_ERROR = 32;
        public static final int BACKUP_RESTORE_DOCUMENTATION = 33;
        public static final int REPORTING_CRITERIA_SELECT_PROJECT = 34;
        public static final int REPORTING_CRITERIA_SELECT_TASK = 35;
        public static final int REPORTING_CRITERIA_SELECT_START_DATE = 36;
        public static final int REPORTING_CRITERIA_SELECT_END_DATE = 37;
        public static final int REPORTING_CRITERIA_SELECT_END_DATE_ERROR_BEFORE_START_DATE = 38;
        public static final int LOADING_REPORTING_RESULTS = 39;
        public static final int REPORTING_EXPORT_DONE = 42;
        public static final int REPORTING_EXPORT_LOADING = 43;
        public static final int REPORTING_EXPORT_UNAVAILABLE = 44;
        public static final int REPORTING_BATCH_SHARE = 45;
        public static final int ASK_FINISH_PROJECT_WITH_REMAINING_UNFINISHED_TASKS = 46;
        public static final int WARN_PROJECT_NOT_FINISHED_ONGOING_TR = 47;
        public static final int BACKUP_SEND_FILE_SEARCH_SHOW_LIST = 48;
        public static final int BACKUP_SEND_FILE_SEARCH_NOTHING_FOUND = 49;
        public static final int BACKUP_SEND_FILE_SEARCH_NO_SD = 50;
	    public static final int REPORTING_EXPORT_ERROR = 51;
        public static final int MANUAL_BACKUP_CONFIRMATION = 52;
        public static final int MANUAL_RESTORE_CONFIRMATION = 53;
        public static final int MANUAL_RESTORE_EXECUTING = 54;
        public static final int MANUAL_RESTORE_SUCCESSFUL = 55;
        public static final int MANUAL_RESTORE_ERROR = 56;
        public static final int TIME_REGISTRATION_ACTION_LOADING = 57;
        public static final int TIME_REGISTRATION_DELETE_LOADING = 58;
        public static final int REPORTING_BATCH_SHARE_NO_FILES_FOUND = 59;
        public static final int RESET_APPLICATION_CONFIRMATION = 60;
        public static final int LOADING_RESET_APPLICATION = 61;
        public static final int RESET_PREFERENCES_CONFIRMATION = 62;
        public static final int TIME_REGISTRATION_DELETE_RANGE_MIN_BOUNDARY = 63;
        public static final int TIME_REGISTRATION_DELETE_RANGE_MAX_BOUNDARY = 64;
        public static final int TIME_REGISTRATION_DELETE_RANGE_BOUNDARY_PROBLEM = 65;
        public static final int TIME_REGISTRATIONS_DELETE_LOADING = 66;
        public static final int WARN_ONGOING_TR = 67;
        public static final int DELETE_TASK_AT_LEAST_ONE_REQUIRED = 68;
        public static final int DELETE_ALL_TASKS_OF_PROJECT_YES_NO = 69;
        public static final int DELETE_ALL_TASKS_AND_TIME_REGISTRATIONS_OF_PROJECT_YES_NO = 70;
        public static final int TIME_REGISTRATION_ADD_SELECT_PROJECT = 71;
        public static final int TIME_REGISTRATION_ADD_SELECT_TASK = 72;
    }
    public class IntentRequestCodes {
        public static final int REGISTRATION_DETAILS = 1;
        public static final int TIME_REGISTRATION_ACTION = 2;
        public static final int TIME_REGISTRATION_EDIT = 3;
        public static final int START_TIME_REGISTRATION = 4;
        public static final int END_TIME_REGISTRATION = 5;
        public static final int ADD_PROJECT = 6;
        public static final int ADD_TASK = 7;
        public static final int EDIT_PROJECT = 8;
        public static final int EDIT_TASK = 9;
        public static final int PROJECT_DETAILS = 10;
        public static final int SELECT_PROJECT = 11;
        public static final int COPY_PROJECT = 12;
        public static final int SELECT_TASK = 13;
        public static final int ACCOUNT_DETAILS = 14;
        public static final int ACCOUNT_REGISTER = 15;
        public static final int SYNC_BLOCKING_ACTIVITY = 16;


        public static final int ALARM_SYNC_REPEAT = 9000;
        public static final int ALARM_SYNC_RETRY = 9001;
    }
    public class IntentResultCodes {
        public static final int RESULT_OK_SPLIT = 100;
        public static final int RESULT_DELETED = 200;
        public static final int RESULT_LOGOUT = 300;
        public static final int SYNC_COMPLETED_ERROR = 400;
        public static final int SYNC_COMPLETED_SUCCESS = 500;
        public static final int GHOST_RECORD = 666;
    }
    public class Preferences {
        public static final String PREFERENCES_NAME = "WorkTime_0001";

        public static final boolean WIDGET_ASK_FOR_TASK_SELECTION_IF_ONLY_ONE_DEFAULT_VALUE = true;
        public static final boolean ENDING_TIME_REGISTRATION_COMMENT_PREFERENCE_DEFAULT_VALUE = true;
        public static final boolean WIDGET_ENDING_TIME_REGISTRATION_FINISH_TASK_PREFERENCE_DEFAULT_VALUE = false;
        public static final boolean SHOW_STATUS_BAR_NOTIFICATIONS_PREFERENCE_DEFAULT_VALUE = true;
        public static final String DISPLAY_HOUR_12_24_FORMAT_DEFAULT_VALUE = "system-default";
        public static final boolean DISPLAY_PROJECTS_HIDE_FINISHED_DEFAULT_VALUE = true;
        public static final boolean SELECT_TASK_HIDE_FINISHED_DEFAULT_VALUE = true;
        public static final boolean SELECT_PROJECT_HIDE_FINISHED_DEFAULT_VALUE = true;
        public static final boolean DISPLAY_TASKS_HIDE_FINISHED_DEFAULT_VALUE = false;
        public static final String WEEK_STARTS_ON_DEFAULT_VALUE = "7";
        public static final String REPORTING_EXPORT_FILE_NAME_DEFAULT_VALUE = "export";
        public static final boolean TIME_REGISTRATION_AUTO_CLOSE_60S_GAP_DEFAULT_VALUE = true;
        public static final boolean TIME_REGISTRATION_PUNCH_BAR_ENABLED_FROM_HOME_SCREEN_DEFAULT_VALUE = true;
        public static final boolean TIME_REGISTRATION_PUNCH_BAR_ENABLED_ON_ALL_SCREENS_DEFAULT_VALUE = false;
        public static final int TIME_REGISTRATION_SPLIT_DEFAULT_GAP_DEFAULT_VALUE = 30;
        public static final boolean IMMEDIATE_PUNCH_OUT_DEFAULT_VALUE = false;
        public static final String ACCOUNT_SYNC_INTERVAL_DEFAULT_VALUE = "2";
        public static final String ACCOUNT_SYNC_CONFLICT_HANDLING_DEFAULT_VALUE = "SERVER";
        public static final boolean ACCOUNT_SYNC_ON_WIFI_ONLY_DEFAULT_VALUE = false;
        public static final boolean ACCOUNT_BACKUP_BEFORE_SYNC_DEFAULT_VALUE = true;
        public static final boolean ACCOUNT_SYNC_SUCCESS_NOTIFICATIONS_DEFAULT_VALUE = false;
        public static final boolean ACCOUNT_SYNC_ERROR_NOTIFICATIONS_DEFAULT_VALUE = false;
        public static final boolean ACCOUNT_SYNC_RETRY_ON_ERROR_DEFAULT_VALUE = true;
        public static final String ACCOUNT_SYNC_ERROR_NOTIFICATION_CASES_DEFAULT_VALUE = "LoginCredentialsMismatchException|GeneralWebException|BackupException|SynchronizationFailedException";
        public static final int SHOW_CASE_LAST_SHOWN_FOR_APP_VERSION_DEFAULT_VALUE = -1;

        public class Keys {
            public static final String WIDGET_ASK_FOR_TASK_SELECTION_IF_ONLY_ONE = "askForTaskSelectionIfOnlyOne";
            public static final String ENDING_TIME_REGISTRATION_COMMENT_PREFERENCE = "widgetEndingTimeRegistrationCommentPreference";
            public static final String WIDGET_ENDING_TIME_REGISTRATION_FINISH_TASK_PREFERENCE = "widgetEndingTimeRegistrationFinishTaskPreference";
            public static final String SHOW_STATUS_BAR_NOTIFICATIONS_PREFERENCE = "showStatusBarNotificationsPreference";
            public static final String DISPLAY_HOUR_12_24_FORMAT = "displayHour1224Format";
            public static final String DISPLAY_PROJECTS_HIDE_FINISHED = "displayProjectsHideFinished";
            public static final String DISPLAY_TASKS_HIDE_FINISHED = "displayTasksHideFinished";
            public static final String SELECT_TASK_HIDE_FINISHED = "selectTaskHideFinished";
            public static final String SELECT_PROJECT_HIDE_FINISHED = "selectProjectHideFinished";
            public static final String WEEK_STARTS_ON = "weekStartsOn";
            public static final String REPORTING_EXPORT_FILE_NAME = "reportingExportFileName";
            public static final String TIME_REGISTRATION_AUTO_CLOSE_60S_GAP = "timeRegistrationAutoClose60sGap";
            public static final String TIME_PRECISION = "timePrecision";
            public static final String TIME_REGISTRATION_PUNCH_BAR_ENABLED_FROM_HOME_SCREEN = "timeRegistrationPunchBarEnabledFromHomeScreen";
            public static final String TIME_REGISTRATION_PUNCH_BAR_ENABLED_ON_ALL_SCREENS = "timeRegistrationPunchBarEnabledOnAllScreens";
            public static final String TIME_REGISTRATION_SPLIT_DEFAULT_GAP = "timeRegistrationSplitDefaultGap";
            public static final String EXPORT_TYPE = "exportType";
            public static final String EXPORT_DATA = "exportData";
            public static final String EXPORT_CSV_SEPARATOR = "exportCsvSeparator";
            public static final String BACKUP_LOCATION = "backupLocation";
            public static final String TIME_REGISTRATION_DEFAULT_ACTION_ONGOING_TR = "timeRegistrationDefaultActionOngoingTr";
            public static final String TIME_REGISTRATION_DEFAULT_ACTION_FINISHED_TR = "timeRegistrationDefaultActionFinishedTr";
            public static final String IMMEDIATE_PUNCH_OUT = "immediatePunchOut";
            public static final String ACCOUNT_SYNC_INTERVAL = "accountSyncInterval";
            public static final String ACCOUNT_SYNC_INTERVAL_FIXED_TIME = "accountSyncIntervalFixedTime";
            public static final String ACCOUNT_SYNC_RETRY_ON_ERROR = "accountSyncRetryOnError";
            public static final String ACCOUNT_SYNC_CONFLICT_HANDLING = "accountSyncConflictHandling";
            public static final String ACCOUNT_SYNC_ON_WIFI_ONLY = "accountSyncOnWifiOnly";
            public static final String ACCOUNT_BACKUP_BEFORE_SYNC = "accountBackupBeforeSync";
            public static final String ACCOUNT_SYNC_SUCCESS_NOTIFICATIONS = "accountSyncSuccessShowNotifications";
            public static final String ACCOUNT_SYNC_ERROR_NOTIFICATIONS = "accountSyncErrorShowNotifications";
            public static final String ACCOUNT_SYNC_ERROR_NOTIFICATION_CASES = "accountSyncErrorShowNotificationCases";
            public static final String SHOW_CASE_LAST_SHOWN_FOR_APP_VERSION = "showCaseLastShownForAppVersion";
        }
    }
    public class Disk {
        public static final String EXPORT_DIRECTORY = "worktime";
        public static final String BACKUP_DIRECTORY = "worktime-backup";
    }
    public class Extras {
        public static final String PROJECT = "project";
        public static final String TASK = "task";
        public static final String TIME_REGISTRATION = "timeRegistration";
        public static final String TIME_REGISTRATION_CONTINUE_WITH_NEW = "timeRegistrationStartNew";
        public static final String TIME_REGISTRATION_NEXT = "timeRegistrationNext";
        public static final String TIME_REGISTRATION_PREVIOUS = "timeRegistrationPrevious";
        public static final String TIME_REGISTRATION_START_DATE = "timeRegistrationStartDate";
        public static final String TIME_REGISTRATION_END_DATE = "timeRegistrationEndDate";
        public static final String DATA_GROUPING = "dataGrouping";
        public static final String DISPLAY_DURATION = "displayDuration";
        public static final String DATA_ORDER = "dataOrder";
        public static final String EXPORT_DTO = "exportDTO";
        public static final String WIDGET_ID = "widgetId";
        public static final String ONLY_SELECT = "onlySelect";
        public static final String ENABLE_SELECT_NONE_OPTION = "enableSelectNoneOption";
        public static final String UPDATE_WIDGET = "updateWidget";
        public static final String TIME_REGISTRATION_COMMENT = "timeRegistrationComment";
        public static final String DEFAULT_ACTION = "defaultAction";
        public static final String SKIP_DIALOG = "skipDialog";
        public static final String ONLY_ACTION = "onlyAction";
    }
    public class ContentMenuItemIds {
        public static final int PROJECT_DETAILS = 1;
        public static final int PROJECT_DELETE = 2;
        public static final int PROJECT_ADD = 3;
        public static final int PROJECT_EDIT = 4;
        public static final int TASK_EDIT = 5;
        public static final int TASK_ADD = 6;
        public static final int TASK_DELETE = 7;
        public static final int TASK_MARK_UNFINISHED = 13;
        public static final int TASK_MARK_FINISHED = 14;
        public static final int TASK_MOVE = 19;
        public static final int PROJECT_MARK_FINISHED = 20;
        public static final int PROJECT_MARK_UNFINISHED = 21;
        public static final int PROJECT_COPY = 22;
        public static final int TASK_REPORTING = 23;
    }
    /**
     * This class specifies some constants which represent each message id.
     */
    public class StatusBarNotificationIds {
        public static final int ONGOING_TIME_REGISTRATION_MESSAGE = 1;
        public static final int RESTORE = 2;
        public static final int BACKUP = 3;
        public static final int SYNC = 4;
    }

    public class Others {
        /* The default id for the punch-bar */
        public static final int PUNCH_BAR_WIDGET_ID = -100;
    }

    public class Broadcast {
        public static final String TIME_REGISTRATION_ACTION_DIALOG = "eu.vranckaert.worktime.time_registration_action_dialog";
        public static final String TIME_REGISTRATION_SPLIT = "eu.vranckaert.worktime.time_registration_action_split";
    }
}
