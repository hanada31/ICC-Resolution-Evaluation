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

package eu.vranckaert.worktime.constants;

/**
 * User: DIRK VRANCKAERT
 * Date: 17/08/11
 * Time: 17:33
 */
public class TrackerConstants {
    public class PageView {
        public static final String ABOUT_ACTIVITY = "aboutActivity";
        public static final String HOME_ACTIVITY = "homeActivity";
        public static final String MANAGE_PROJECTS_ACTIVITY = "manageProjectsActivity";
        public static final String PREFERENCES_ACTIVITY = "preferencesActivity";
        public static final String TIME_REGISTRATIONS_ACTIVITY = "timeRegistrationsActivity";
        public static final String PROJECTS_DETAILS_ACTIVITY = "projectsDetailsActivity";
        public static final String REGISTRATIONS_DETAILS_ACTIVITY = "registrationsDetailsActivity";
        public static final String ADD_EDIT_PROJECT_ACTIVITY = "addEditProjectActivity";
        public static final String ADD_EDIT_TASK_ACTIVITY = "addEditTaskActivity";
        public static final String REPORTING_CRITERIA_ACTIVITY = "reportingCriteriaActivity";
        public static final String REPORTING_RESULT_ACTIVITY = "reportingResultActivity";
        public static final String ACCOUNT_LOGIN_ACTIVITY = "accountLoginActivity";
        public static final String ACCOUNT_REGISTER_ACTIVITY = "accountRegisterActivity";
        public static final String ACCOUNT_DETAILS_ACTIVITY = "accountDetailsActivity";

        public class Preferences {
            public static final String BACKUP_PREFERENCES = "backupPreferences";
            public static final String DATE_TIME_PREFERENCES = "dateTimePreferences";
            public static final String TIME_REGISTRATIONS_PREFERENCES = "timeRegistrationsPreferences";
            public static final String TASK_PREFERENCES = "taskPreferences";
            public static final String WIDGET_PREFERENCES = "widgetPreferences";
            public static final String NOTIFICATIONS_PREFERENCES = "notificationsPreferences";
            public static final String ACCOUNT_SYNC_PREFERENCES = "accountSynchronisationPreferences";
        }
    }

    public class EventSources {
        public static final String PROJECT_DETAILS_ACTIVITY = "projectDetailsActivity";
        public static final String MANAGE_PROJECTS_ACTIVITY = "manageProjectsActivity";
        public static final String TIME_REGISTRATION_ACTION_ACTIVITY = "timeRegistrationActionActivity";
        public static final String START_TIME_REGISTRATION_ACTIVITY = "startTimeRegistrationActivity";
        public static final String ADD_EDIT_PROJECT_ACTIVITY = "addEditProjectActivity";
        public static final String REGISTRATION_DETAILS_ACTIVITY = "registrationDetailsActivity";
        public static final String TIME_REGISTRATIONS_ACTIVITY = "timeRegistrationsActivity";
        public static final String ADD_EDIT_TASK_ACTIVITY = "addEditTaskActivity";
        public static final String REPORTING_CRITERIA_ACTIVITY = "reportingCriteriaActivity";
        public static final String REPORTING_RESULT_ACTIVITY = "reportingResultActivity";
    }

    public class EventActions {
        public static final String ADD_PROJECT = "addProject";
        public static final String EDIT_PROJECT = "editProject";
        public static final String DELETE_PROJECT = "deleteProject";
        public static final String ADD_TASK = "addTask";
        public static final String EDIT_TASK = "editTask";
        public static final String DELETE_TASK = "deleteTask";
        public static final String MARK_TASK_FINISHED = "markTaskFinished";
        public static final String MARK_TASK_UNFINISHED = "markTaskUnfinished";
        public static final String START_TIME_REGISTRATION = "startTimeRegistration";
        public static final String END_TIME_REGISTRATION = "endTimeRegistration";
        public static final String DELETE_TIME_REGISTRATION = "deleteTimeRegistration";
        public static final String DELETE_TIME_REGISTRATIONS_IN_RANGE = "deleteTimeRegistrationsInRange";
        public static final String ADD_TR_COMMENT = "addTrComment";
        public static final String EDIT_TR_COMMENT = "editTrComment";
        public static final String EDIT_TR_END_TIME = "editTrEndTime";
        public static final String EDIT_TR_START_TIME = "editTrStartTime";
        public static final String EDIT_TR_PROJECT_AND_TASK = "editTrProjectAndTask";
        public static final String RESTART_TIME_REGISTRATION = "restartTimeRegistration";
        public static final String GENERATE_REPORT = "generateReport";
        public static final String EXPORT_RESULT = "exportResult";
        public static final String MOVE_TASK = "moveTask";
    }
}
