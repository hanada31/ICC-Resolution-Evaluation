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

package eu.vranckaert.worktime.activities.timeregistrations;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.comparators.project.ProjectByNameComparator;
import eu.vranckaert.worktime.comparators.task.TaskByNameComparator;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.OSContants;
import eu.vranckaert.worktime.constants.TextConstants;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.Task;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.service.ProjectService;
import eu.vranckaert.worktime.service.TaskService;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.service.ui.StatusBarNotificationService;
import eu.vranckaert.worktime.service.ui.WidgetService;
import eu.vranckaert.worktime.utils.context.ContextUtils;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.date.DateFormat;
import eu.vranckaert.worktime.utils.date.DateUtils;
import eu.vranckaert.worktime.utils.date.HourPreference12Or24;
import eu.vranckaert.worktime.utils.date.TimeFormat;
import eu.vranckaert.worktime.utils.preferences.Preferences;
import eu.vranckaert.worktime.utils.string.StringUtils;
import eu.vranckaert.worktime.utils.view.actionbar.synclock.SyncLockedWizardActivity;

import java.util.*;

/**
 * User: DIRK VRANCKAERT
 * Date: 03/12/12
 * Time: 19:22
 */
public class TimeRegistrationAddActivity extends SyncLockedWizardActivity {
    private static final String LOG_TAG = TimeRegistrationAddActivity.class.getSimpleName();

    private static final int PAGE_INFO = 0;
    private static final int PAGE_ENTER_START_TIME = 1;
    private static final int PAGE_ENTER_END_TIME = 2;
    private static final int PAGE_SELECT_PROJECT_AND_TASK = 3;
    private static final int PAGE_ENTER_COMMENT = 4;
    private static final int PAGE_VALIDATE_TIME_REGISTRATION = 5;

    @Inject
    private TimeRegistrationService trService;

    @Inject
    private ProjectService projectService;

    @Inject
    private TaskService taskService;

    @Inject
    private WidgetService widgetService;

    @Inject
    private StatusBarNotificationService notificationService;

    @Inject
    private StatusBarNotificationService statusBarNotificationService;

    private int[] layouts = {
            R.layout.activity_time_registration_add_wizard_0,
            R.layout.activity_time_registration_add_wizard_1,
            R.layout.activity_time_registration_add_wizard_2,
            R.layout.activity_time_registration_add_wizard_3,
            R.layout.activity_time_registration_add_wizard_4,
            R.layout.activity_time_registration_add_wizard_5
    };
    private Calendar startTime;
    private Calendar endTime;
    private boolean isOngoing = false;
    private Project project;
    private Task task;
    private String comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.lbl_registration_add_title);

        setContentViews(layouts);

        super.setFinishButtonText(R.string.add);
        setCancelDialog(R.string.lbl_registration_add_cancel_dialog, R.string.msg_registration_add_cancel_dialog);
    }

    @Override
    protected void initialize(View view) {
        // Nothing to be done here...
    }

    @Override
    protected void afterPageChange(int currentViewIndex, int previousViewIndex, View view) {
        if (currentViewIndex < previousViewIndex) { // Means we are moving back
            switch (currentViewIndex) {
                case PAGE_ENTER_START_TIME: {
                    initDateTimePicker(startTime, R.id.time_registration_add_wizard_start_date, R.id.time_registration_add_wizard_start_time);
                    break;
                }
                case PAGE_ENTER_END_TIME: {
                    initEndTime(endTime != null ? endTime : startTime);
                    break;
                }
                case PAGE_SELECT_PROJECT_AND_TASK: {
                    initProjectAndTask(project, task);
                    break;
                }
                case PAGE_ENTER_COMMENT: {
                    if (comment != null) {
                        EditText commentEditText = (EditText) view.findViewById(R.id.registration_add_part_four_comment);
                        commentEditText.setText(comment);
                    }
                    break;
                }
            }
        } else { // Moving forward
            switch (currentViewIndex) {
                case PAGE_ENTER_START_TIME: {
                    if (startTime == null)
                        startTime = Calendar.getInstance();
                    initDateTimePicker(startTime, R.id.time_registration_add_wizard_start_date, R.id.time_registration_add_wizard_start_time);
                    break;
                }
                case PAGE_ENTER_END_TIME: {
                    initEndTime(endTime != null && startTime.before(endTime) ? endTime : startTime);
                    break;
                }
                case PAGE_SELECT_PROJECT_AND_TASK: {
                    initProjectAndTask(project, task);
                    break;
                }
                case PAGE_ENTER_COMMENT: {
                    if (comment != null) {
                        EditText commentEditText = (EditText) view.findViewById(R.id.registration_add_part_four_comment);
                        commentEditText.setText(comment);
                    }
                    break;
                }
                case PAGE_VALIDATE_TIME_REGISTRATION: {
                    TextView startTimeTextView = (TextView) view.findViewById(R.id.time_registration_add_wizard_review_start);
                    TextView endTimeTextView = (TextView) view.findViewById(R.id.time_registration_add_wizard_review_end);
                    TextView durationTextView = (TextView) view.findViewById(R.id.time_registration_add_wizard_review_duration);
                    TextView commentTextView = (TextView) view.findViewById(R.id.time_registration_add_wizard_review_comment);
                    TextView projectTextView = (TextView) view.findViewById(R.id.time_registration_add_wizard_review_project);
                    TextView taskTextView = (TextView) view.findViewById(R.id.time_registration_add_wizard_review_task);

                    startTimeTextView.setText(
                            TextConstants.SPACE + DateUtils.DateTimeConverter.convertDateTimeToString(
                                    startTime.getTime(),
                                    DateFormat.MEDIUM,
                                    TimeFormat.MEDIUM,
                                    TimeRegistrationAddActivity.this
                            )
                    );
                    if (!isOngoing) {
                        endTimeTextView.setText(
                                TextConstants.SPACE + DateUtils.DateTimeConverter.convertDateTimeToString(
                                    endTime.getTime(),
                                    DateFormat.MEDIUM,
                                    TimeFormat.MEDIUM,
                                    TimeRegistrationAddActivity.this
                                )
                        );
                    } else {
                        endTimeTextView.setText(TextConstants.SPACE + getString(R.string.now));
                    }
                    durationTextView.setText(
                            TextConstants.SPACE + DateUtils.TimeCalculator.calculatePeriod(
                                    TimeRegistrationAddActivity.this,
                                    constructTimeRegistration(),
                                    false
                            )
                    );
                    if (StringUtils.isNotBlank(comment)) {
                        commentTextView.setText(TextConstants.SPACE + comment);
                    } else {
                        commentTextView.setText(
                                TextConstants.SPACE + getString(R.string.lbl_registration_add_part_five_no_comment)
                        );
                    }
                    projectTextView.setText(TextConstants.SPACE + project.getName());
                    taskTextView.setText(TextConstants.SPACE + task.getName());
                    break;
                }
            }
        }
    }

    @Override
    public boolean beforePageChange(int currentViewIndex, int nextViewIndex, View view) {
        if (currentViewIndex < nextViewIndex) { // Means we are moving forward
            switch (currentViewIndex) {
                case PAGE_ENTER_START_TIME: {
                    startTime = getCurrentDateTimePickerValue(R.id.time_registration_add_wizard_start_date, R.id.time_registration_add_wizard_start_time);
                    return validateStartTime(startTime);
                }
                case PAGE_ENTER_END_TIME: {
                    if (!isOngoing) {
                        endTime = getCurrentDateTimePickerValue(R.id.time_registration_add_wizard_end_date, R.id.time_registration_add_wizard_end_time);
                        return validateEndTime(endTime);
                    } else {
                        return true;
                    }
                }
                case PAGE_SELECT_PROJECT_AND_TASK: {
                    return validateProjectAndTask(project, task);
                }
                case PAGE_ENTER_COMMENT: {
                    EditText commentEditText = (EditText) view.findViewById(R.id.registration_add_part_four_comment);
                    comment = commentEditText.getText().toString();
                    return true;
                }
            }
        }


        return true;
    }

    private boolean validateStartTime(Calendar startTime) {
        TextView errorView = (TextView) findViewById(R.id.time_registration_add_wizard_error);
        if (errorView != null) {
            errorView.setVisibility(View.GONE);
        }

        Calendar now = Calendar.getInstance();
        if (startTime.after(now)) {
            if (errorView != null) {
                errorView.setText(R.string.lbl_registration_add_validation_start_time_limit_now);
                errorView.setVisibility(View.VISIBLE);
            }
            return false;
        }

        if (trService.doesInterfereWithTimeRegistration(startTime.getTime())) {
            if (errorView != null) {
                errorView.setText(R.string.lbl_registration_add_validation_start_time_part_of_other);
                errorView.setVisibility(View.VISIBLE);
            }
            return false;
        }

        return true;
    }

    private boolean validateEndTime(Calendar endTime) {
        TextView errorView = (TextView) findViewById(R.id.time_registration_add_wizard_error);
        if (errorView != null) {
            errorView.setVisibility(View.GONE);
        }

        if (!validateGreaterThan(endTime, startTime)) {
            if (errorView != null) {
                errorView.setText(R.string.lbl_registration_add_validation_end_time_greater_than_start_time);
                errorView.setVisibility(View.VISIBLE);
            }
            return false;
        }

        TimeRegistration nextTimeRegistrationCriteria = new TimeRegistration();
        nextTimeRegistrationCriteria.setEndTime(startTime.getTime());
        TimeRegistration upperLimitTimeRegistration = trService.getNextTimeRegistration(nextTimeRegistrationCriteria);
        Date upperLimitDate = null;
        if (upperLimitTimeRegistration != null) {
            upperLimitDate = upperLimitTimeRegistration.getStartTime();
            Calendar upperLimit = Calendar.getInstance();
            upperLimit.setTime(upperLimitDate);

            if (!validateLowerThanOrEqualsTo(endTime, upperLimit)) {
                if (errorView != null) {
                    String error = getString(R.string.lbl_registration_add_validation_end_time_limit, DateUtils.DateTimeConverter.convertDateTimeToString(upperLimit.getTime(), DateFormat.MEDIUM, TimeFormat.MEDIUM, TimeRegistrationAddActivity.this));
                    errorView.setText(error);
                    errorView.setVisibility(View.VISIBLE);
                }
                return false;
            }
        } else {
            upperLimitDate = new Date();
            Calendar upperLimit = Calendar.getInstance();
            upperLimit.setTime(upperLimitDate);

            if (!validateLowerThanOrEqualsTo(endTime, upperLimit)) {
                if (errorView != null) {
                    errorView.setText(R.string.lbl_registration_add_validation_end_time_limit_now);
                    errorView.setVisibility(View.VISIBLE);
                }
                return false;
            }
        }

        return true;
    }

    private boolean validateProjectAndTask(Project project, Task task) {
        TextView errorView = (TextView) getActiveView().findViewById(R.id.time_registration_add_wizard_error);
        if (errorView != null) {
            errorView.setVisibility(View.GONE);
        }

        if (project == null) {
            if (errorView != null) {
                errorView.setText(R.string.lbl_registration_add_validation_project_required);
                errorView.setVisibility(View.VISIBLE);
            }
            return false;
        } else if (task == null) {
            if (errorView != null) {
                errorView.setText(R.string.lbl_registration_add_validation_task_required);
                errorView.setVisibility(View.VISIBLE);
            }
            return false;
        }

        return true;
    }

    private void initEndTime(final Calendar defaultTime) {
        TimeRegistration latestTimeRegistration = trService.getLatestTimeRegistration();

        CheckBox ongoingTr = (CheckBox)getActiveView().findViewById(R.id.time_registration_add_wizard_ongoing);
        ongoingTr.setVisibility(View.GONE);

        DatePicker datePicker = (DatePicker) getActiveView().findViewById(R.id.time_registration_add_wizard_end_date);
        TimePicker timePicker = (TimePicker) getActiveView().findViewById(R.id.time_registration_add_wizard_end_time);

        if (latestTimeRegistration != null) {
            if (latestTimeRegistration.isOngoingTimeRegistration()) {
                isOngoing = false;
            } else {
                Calendar endTimeLatest = Calendar.getInstance();
                endTimeLatest.setTime(latestTimeRegistration.getEndTime());
                if (validateGreaterThanOrEqualsTo(startTime, endTimeLatest)) {
                    ongoingTr.setVisibility(View.VISIBLE);
                }
            }
        } else {
            ongoingTr.setVisibility(View.VISIBLE);
        }

        if (isOngoing) {
            datePicker.setVisibility(View.GONE);
            timePicker.setVisibility(View.GONE);
        } else {
            datePicker.setVisibility(View.VISIBLE);
            timePicker.setVisibility(View.VISIBLE);
        }

        ongoingTr.setChecked(isOngoing);
        initDateTimePicker(defaultTime, R.id.time_registration_add_wizard_end_date, R.id.time_registration_add_wizard_end_time);

        ongoingTr.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                isOngoing = checked;
                TimeRegistrationAddActivity.this.initEndTime(defaultTime);
            }
        });
    }

    private void initProjectAndTask(Project project, Task task) {
        Button addProjectButton = (Button) getActiveView().findViewById(R.id.registration_add_part_three_project);
        Button addTaskButton = (Button) getActiveView().findViewById(R.id.registration_add_part_three_task);

        ImageView deleteProjectButton = (ImageView) getActiveView().findViewById(R.id.btn_delete_project);
        ImageView deleteTaskButton = (ImageView) getActiveView().findViewById(R.id.btn_delete_task);

        deleteProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimeRegistrationAddActivity.this.project = null;
                TimeRegistrationAddActivity.this.task = null;
                TimeRegistrationAddActivity.this.initProjectAndTask(TimeRegistrationAddActivity.this.project, TimeRegistrationAddActivity.this.task);
            }
        });

        deleteTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimeRegistrationAddActivity.this.task = null;
                TimeRegistrationAddActivity.this.initProjectAndTask(TimeRegistrationAddActivity.this.project, TimeRegistrationAddActivity.this.task);
            }
        });

        if (project == null) {
            task = null;

            addTaskButton.setEnabled(false);
            deleteProjectButton.setVisibility(View.GONE);
            deleteTaskButton.setVisibility(View.GONE);

            addProjectButton.setText(R.string.lbl_registration_add_part_three_project_none_selected);
            addTaskButton.setText(R.string.lbl_registration_add_part_three_task_none_selected);
        } else {
            addTaskButton.setEnabled(true);
            deleteProjectButton.setVisibility(View.VISIBLE);

            addProjectButton.setText(project.getName());

            if (task != null) {
                deleteTaskButton.setVisibility(View.VISIBLE);
                addTaskButton.setText(task.getName());
            } else {
                deleteTaskButton.setVisibility(View.GONE);
                addTaskButton.setText(R.string.lbl_registration_add_part_three_task_none_selected);
            }
        }

        addProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(Constants.Dialog.TIME_REGISTRATION_ADD_SELECT_PROJECT);
            }
        });
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(Constants.Dialog.TIME_REGISTRATION_ADD_SELECT_TASK);
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;

        switch (id) {
            case Constants.Dialog.TIME_REGISTRATION_ADD_SELECT_PROJECT: {
                List<Project> projectList = projectService.findAll();
                Collections.sort(projectList, new ProjectByNameComparator());

                final List<Project> availableProjects = projectList;
                List<String> projects = new ArrayList<String>();
                for (int i=0; i<availableProjects.size(); i++) {
                    Project project = availableProjects.get(i);

                    projects.add(project.getName());
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.lbl_reporting_criteria_project_dialog_title_select_project)
                        .setSingleChoiceItems(
                                StringUtils.convertListToArray(projects),
                                -1,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogInterface, int index) {
                                        TimeRegistrationAddActivity.this.project = availableProjects.get(index);
                                        TimeRegistrationAddActivity.this.task = null;
                                        initProjectAndTask(TimeRegistrationAddActivity.this.project, TimeRegistrationAddActivity.this.task);
                                        removeDialog(Constants.Dialog.TIME_REGISTRATION_ADD_SELECT_PROJECT);
                                    }
                                }
                        )
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialogInterface) {
                                removeDialog(Constants.Dialog.TIME_REGISTRATION_ADD_SELECT_PROJECT);
                            }
                        });
                dialog = builder.create();
                break;
            }
            case Constants.Dialog.TIME_REGISTRATION_ADD_SELECT_TASK: {
                List<Task> taskList;
                if (Preferences.getSelectTaskHideFinished(TimeRegistrationAddActivity.this)) {
                    taskList = taskService.findTasksForProject(TimeRegistrationAddActivity.this.project);
                } else {
                    taskList = taskService.findNotFinishedTasksForProject(TimeRegistrationAddActivity.this.project);
                }
                Collections.sort(taskList, new TaskByNameComparator());
                final List<Task> availableTasks = taskList;

                List<String> tasks = new ArrayList<String>();
                for (Task task : availableTasks) {
                    tasks.add(task.getName());
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(TimeRegistrationAddActivity.this);
                builder.setTitle(R.string.lbl_widget_title_select_task)
                        .setSingleChoiceItems(
                                StringUtils.convertListToArray(tasks),
                                -1,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogInterface, int index) {
                                        TimeRegistrationAddActivity.this.task = availableTasks.get(index);
                                        initProjectAndTask(TimeRegistrationAddActivity.this.project, TimeRegistrationAddActivity.this.task);
                                        removeDialog(Constants.Dialog.TIME_REGISTRATION_ADD_SELECT_TASK);
                                    }
                                }
                        )
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialogInterface) {
                                removeDialog(Constants.Dialog.TIME_REGISTRATION_ADD_SELECT_TASK);
                            }
                        });
                dialog = builder.create();
                break;
            }
        }

        return dialog;
    }

    /**
     * Validate a certain time against a certain limit. The validation formula is: time > limit.
     * @param time The time to be validated.
     * @param limit The limit to which the time should be validated. This is an optional parameter. If null the
     * validation will always succeed.
     * @return {@link Boolean#TRUE} if valid against the validation formula, {@link Boolean#FALSE} if not.
     */
    private boolean validateGreaterThan(final Calendar time, final Calendar limit) {
        Log.d(getApplicationContext(), LOG_TAG, "About to start validating time > limit");

        if (limit == null) {
            //No limit is defined so the time can be anything!
            Log.d(getApplicationContext(), LOG_TAG, "No limitations defined so validation is ok!");
            return true;
        }

        if (time.after(limit)) {
            Log.d(getApplicationContext(), LOG_TAG, "The time is greater than the limit, validation ok!");
            return true;
        }

        Log.d(getApplicationContext(), LOG_TAG, "The time is not greater than the limit, validation NOT ok!");
        return false;
    }

    /**
     * Validate a certain time against a certain limit. The validation formula is: time >= limit.
     * @param time The time to be validated.
     * @param limit The limit to which the time should be validated. This is an optional parameter. If null the
     * validation will always succeed.
     * @return {@link Boolean#TRUE} if valid against the validation formula, {@link Boolean#FALSE} if not.
     */
    private boolean validateGreaterThanOrEqualsTo(final Calendar time, final Calendar limit) {
        Log.d(getApplicationContext(), LOG_TAG, "About to start validating time >= limit");

        if (limit == null) {
            //No limit is defined so the time can be anything!
            Log.d(getApplicationContext(), LOG_TAG, "No limitations defined so validation is ok!");
            return true;
        }

        if (validateGreaterThan(time, limit) || validateEqualTo(time, limit)) {
            Log.d(getApplicationContext(), LOG_TAG, "The time is greater than or equal to the limit, validation ok!");
            return true;
        }

        Log.d(getApplicationContext(), LOG_TAG, "The time is not greater than or equal to the limit, validation NOT ok!");
        return false;
    }

    /**
     * Validate a certain time against a certain limit. The validation formula is: time < limit.
     * @param time The time to be validated.
     * @param limit The limit to which the time should be validated. This is an optional parameter. If null the
     * validation will always succeed.
     * @return {@link Boolean#TRUE} if valid against the validation formula, {@link Boolean#FALSE} if not.
     */
    private boolean validateLowerThan(final Calendar time, final Calendar limit) {
        Log.d(getApplicationContext(), LOG_TAG, "About to start validating time < limit");

        if (limit == null) {
            //No limit is defined so the time can be anything!
            Log.d(getApplicationContext(), LOG_TAG, "No limitations defined so validation is ok!");
            return true;
        }

        if (time.before(limit)) {
            Log.d(getApplicationContext(), LOG_TAG, "The time is lower than the limit, validation ok!");
            return true;
        }

        Log.d(getApplicationContext(), LOG_TAG, "The time is not lower than the limit, validation NOT ok!");
        return false;
    }

    /**
     * Validate a certain time against a certain limit. The validation formula is: time <= limit.
     * @param time The time to be validated.
     * @param limit The limit to which the time should be validated. This is an optional parameter. If null the
     * validation will always succeed.
     * @return {@link Boolean#TRUE} if valid against the validation formula, {@link Boolean#FALSE} if not.
     */
    private boolean validateLowerThanOrEqualsTo(final Calendar time, final Calendar limit) {
        Log.d(getApplicationContext(), LOG_TAG, "About to start validating time <= limit");

        if (limit == null) {
            //No limit is defined so the time can be anything!
            Log.d(getApplicationContext(), LOG_TAG, "No limitations defined so validation is ok!");
            return true;
        }

        if (validateLowerThan(time, limit) || validateEqualTo(time, limit)) {
            Log.d(getApplicationContext(), LOG_TAG, "The time is lower than or equal to the limit, validation ok!");
            return true;
        }

        Log.d(getApplicationContext(), LOG_TAG, "The time is not lower than or equal to the limit, validation NOT ok!");
        return false;
    }

    /**
     * Validate a certain time against a certain limit. The validation formula is: time = limit.
     * @param time The time to be validated.
     * @param limit The limit to which the time should be validated. This is an optional parameter. If null the
     * validation will always succeed.
     * @return {@link Boolean#TRUE} if valid against the validation formula, {@link Boolean#FALSE} if not.
     */
    private boolean validateEqualTo(final Calendar time, final Calendar limit) {
        Log.d(getApplicationContext(), LOG_TAG, "About to start validating time = limit");

        if (limit == null) {
            //No limit is defined so the time can be anything!
            Log.d(getApplicationContext(), LOG_TAG, "No limitations defined so validation is ok!");
            return true;
        }

        Long timeInMilis = time.getTimeInMillis();
        Long limitInMilis = limit.getTimeInMillis();

        Calendar calendarTime = Calendar.getInstance();
        calendarTime.setTimeInMillis(timeInMilis);
        calendarTime.set(Calendar.MILLISECOND, 0);
        calendarTime.set(Calendar.SECOND, 0);
        Calendar calendarLimit = Calendar.getInstance();
        calendarLimit.setTimeInMillis(limitInMilis);
        calendarLimit.set(Calendar.MILLISECOND, 0);
        calendarLimit.set(Calendar.SECOND, 0);
        if (calendarTime.getTimeInMillis() == calendarLimit.getTimeInMillis()) {
            Log.d(getApplicationContext(), LOG_TAG, "The time is equal to the limit, validation ok!");
            return true;
        }

        Log.d(getApplicationContext(), LOG_TAG, "The time is not equal to the limit, validation NOT ok!");
        return false;
    }
    /**
     * Initialize the date and time picker for a certain {@link Calendar}.
     * @param part The {@link Calendar} instance to set on the date and time picker.
     * @param datePickerId The resource id referencing an {@link DatePicker}.
     * @param timePickerId The resource id referencing an {@link TimePicker}.
     */
    private void initDateTimePicker(Calendar part, int datePickerId, int timePickerId) {
        DatePicker datePicker = (DatePicker) findViewById(datePickerId);
        TimePicker timePicker = (TimePicker) findViewById(timePickerId);

        datePicker.init(part.get(Calendar.YEAR), part.get(Calendar.MONTH), part.get(Calendar.DAY_OF_MONTH), null);
        if (ContextUtils.getAndroidApiVersion() >= OSContants.API.HONEYCOMB_3_2) {
            datePicker.setMaxDate((new Date()).getTime());
            datePicker.setCalendarViewShown(false);
            datePicker.setSpinnersShown(true);
        }

        HourPreference12Or24 preference12or24Hours = Preferences.getDisplayHour1224Format(TimeRegistrationAddActivity.this);
        timePicker.setIs24HourView(preference12or24Hours.equals(HourPreference12Or24.HOURS_24)?true:false);
        timePicker.setCurrentHour(part.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(part.get(Calendar.MINUTE));
    }

    private Calendar getCurrentDateTimePickerValue(int datePickerId, int timePickerId) {
        DatePicker datePicker = (DatePicker) getActiveView().findViewById(datePickerId);
        TimePicker timePicker = (TimePicker) getActiveView().findViewById(timePickerId);
        return getCurrentDateTimePickerValue(datePicker, timePicker);
    }

    /**
     * Store the current values in the date and time picker.
     * @return A {@link java.util.Calendar} instance with the current values of the date and time picker.
     */
    private Calendar getCurrentDateTimePickerValue(DatePicker datePicker, TimePicker timePicker) {
        //By clearing the focus we make sure that the latest value entered in the date or time picker is submitted to
        //to the date or time picker itself. Otherwise when editing the value in the time picker for example, using the
        //keyboard, and immediately pressing the next button the value is not yet changed because the focus is still on
        //view. By clearing the focus manually we make sure that the submitted value is changed to what the user wants.
        //We need to do this for both the date AND the time picker!
        clearFocusAndRemoveSoftKeyboard(datePicker);
        clearFocusAndRemoveSoftKeyboard(timePicker);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, datePicker.getYear());
        calendar.set(Calendar.MONTH, datePicker.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
        calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
        calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    @Override
    protected boolean onCancel(View view, View button) { return true; }

    private TimeRegistration constructTimeRegistration() {
        TimeRegistration timeRegistration = new TimeRegistration();
        timeRegistration.setStartTime(startTime.getTime());
        if (!isOngoing) {
            timeRegistration.setEndTime(endTime.getTime());
        }
        timeRegistration.setTask(task);
        if (comment != null) {
            timeRegistration.setComment(comment);
        }
        return timeRegistration;
    }

    @Override
    protected boolean onFinish(View view, View button) {
        TimeRegistration timeRegistration = constructTimeRegistration();
        trService.create(timeRegistration);

        if (timeRegistration.isOngoingTimeRegistration()) {
            notificationService.removeOngoingTimeRegistrationNotification();
            notificationService.addOrUpdateNotification(null);
        }

        return true;
    }

    @Override
    public void closeOnCancel(View view) {
        setResult(RESULT_CANCELED);
        super.closeOnCancel(view);
    }

    @Override
    public void closeOnFinish() {
        setResult(RESULT_OK);
        super.closeOnFinish();
    }
}
