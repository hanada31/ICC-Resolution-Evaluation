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

package eu.vranckaert.worktime.activities.reporting;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.comparators.project.ProjectByNameComparator;
import eu.vranckaert.worktime.comparators.task.TaskByNameComparator;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.TrackerConstants;
import eu.vranckaert.worktime.enums.reporting.ReportingDataGrouping;
import eu.vranckaert.worktime.enums.reporting.ReportingDataOrder;
import eu.vranckaert.worktime.enums.reporting.ReportingDateRange;
import eu.vranckaert.worktime.enums.reporting.ReportingDisplayDuration;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.Task;
import eu.vranckaert.worktime.service.ExportService;
import eu.vranckaert.worktime.service.ProjectService;
import eu.vranckaert.worktime.service.TaskService;
import eu.vranckaert.worktime.utils.context.IntentUtil;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.date.DateConstants;
import eu.vranckaert.worktime.utils.date.DateFormat;
import eu.vranckaert.worktime.utils.date.DateUtils;
import eu.vranckaert.worktime.utils.file.CsvFilenameFilter;
import eu.vranckaert.worktime.utils.file.FileUtil;
import eu.vranckaert.worktime.utils.file.XlsFilenameFilter;
import eu.vranckaert.worktime.utils.string.StringUtils;
import eu.vranckaert.worktime.utils.tracker.AnalyticsTracker;
import eu.vranckaert.worktime.utils.view.actionbar.ActionBarGuiceActivity;
import eu.vranckaert.worktime.utils.view.actionbar.synclock.SyncLockedActivity;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

import com.google.inject.internal.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 15/09/11
 * Time: 20:28
 */
public class ReportingCriteriaActivity extends SyncLockedActivity {
    private static final String LOG_TAG = ReportingCriteriaActivity.class.getSimpleName();

    private List<ReportingDateRange> dateRanges;
    private List<ReportingDataGrouping> dataGroupings;
    private List<ReportingDisplayDuration> displayDurations;
    private List<ReportingDataOrder> dataOrders;

    private File[] allDocumentsAvailable;

    @InjectExtra(value = Constants.Extras.TIME_REGISTRATION_START_DATE, optional = true)
    @Nullable
    private Date startDate;
    @InjectExtra(value = Constants.Extras.TIME_REGISTRATION_END_DATE, optional = true)
    @Nullable
    private Date endDate;
    @InjectExtra(value = Constants.Extras.PROJECT, optional = true)
    @Nullable
    private Project project;
    @InjectExtra(value = Constants.Extras.TASK, optional = true)
    @Nullable
    private Task task;
    @InjectExtra(value = Constants.Extras.DATA_GROUPING, optional = true)
    @Nullable
    private ReportingDataGrouping dataGrouping;
    @InjectExtra(value = Constants.Extras.DATA_ORDER, optional = true)
    @Nullable
    private ReportingDataOrder dataOrder;
    @InjectExtra(value = Constants.Extras.DISPLAY_DURATION, optional = true)
    @Nullable
    private ReportingDisplayDuration displayDuration;

    private List<Project> availableProjects = null;
    private List<Task> availableTasks = null;

    @InjectView(R.id.reporting_criteria_date_range_spinner)
    private Spinner dateRangeSpinner;
    @InjectView(R.id.reporting_criteria_data_grouping_spinner)
    private Spinner dataGroupingSpinner;
    @InjectView(R.id.reporting_criteria_data_display_duration_spinner)
    private Spinner displayDurationSpinner;
    @InjectView(R.id.reporting_criteria_data_order_spinner)
    private Spinner dataOrderSpinner;
    @InjectView(R.id.reporting_criteria_date_range_start)
    private Button dateRangeStartButton;
    @InjectView(R.id.reporting_criteria_date_range_end)
    private Button dateRangeEndButton;
    @InjectView(R.id.reporting_criteria_project)
    private Button projectButton;
    @InjectView(R.id.reporting_criteria_task)
    private Button taskButton;
    @InjectView(R.id.reporting_criteria_show_finished_tasks)
    private CheckBox showFinishedTasks;
    @InjectView(R.id.btn_delete_project)
    private ImageView deleteProjectButton;
    @InjectView(R.id.btn_delete_task)
    private ImageView deleteTaskButton;

    @Inject
    private ProjectService projectService;
    @Inject
    private TaskService taskService;
    @Inject
    private ExportService exportService;

    private AnalyticsTracker tracker;

    private final DateFormat dateFormat = DateFormat.LONG;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporting_criteria);

        setTitle(R.string.lbl_reporting_criteria_title);
        setDisplayHomeAsUpEnabled(true);

        tracker = AnalyticsTracker.getInstance(getApplicationContext());
        tracker.trackPageView(TrackerConstants.PageView.REPORTING_CRITERIA_ACTIVITY);

        initializeView();
    }

    private void initializeView() {
        //Date Range spinner
        dateRanges = Arrays.asList(ReportingDateRange.values());
        Collections.sort(dateRanges, new Comparator<ReportingDateRange>() {
            public int compare(ReportingDateRange reportingDateRange, ReportingDateRange reportingDateRange1) {
                return ((Integer)reportingDateRange.getOrder()).compareTo((Integer)reportingDateRange1.getOrder());
            }
        });
        ArrayAdapter<CharSequence> dateRangeAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_reporting_criteria_date_range_spinner, android.R.layout.simple_spinner_item);
        dateRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateRangeSpinner.setAdapter(dateRangeAdapter);
        dateRangeSpinner.setSelection(ReportingDateRange.TODAY.getOrder()); //Set default value...

        dateRangeSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                updateViewOnDateRangeSpinnerSelection();
            }
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        //Date Range start button
        dateRangeStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                showDialog(Constants.Dialog.REPORTING_CRITERIA_SELECT_START_DATE);
            }
        });

        //Date Range end button
        dateRangeEndButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                showDialog(Constants.Dialog.REPORTING_CRITERIA_SELECT_END_DATE);
            }
        });

        //Project select button
        projectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                showDialog(Constants.Dialog.REPORTING_CRITERIA_SELECT_PROJECT);
            }
        });
        //Project delete button
        deleteProjectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                project = null;
                updateViewOnProjectAndTaskSelection();
            }
        });

        //Task select button
        taskButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                showDialog(Constants.Dialog.REPORTING_CRITERIA_SELECT_TASK);
            }
        });
        //Task delete button
        deleteTaskButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                task = null;
                updateViewOnProjectAndTaskSelection();
            }
        });

        //Data Grouping spinner
        dataGroupings = Arrays.asList(ReportingDataGrouping.values());
        Collections.sort(dataGroupings, new Comparator<ReportingDataGrouping>() {
            public int compare(ReportingDataGrouping reportingDataGrouping, ReportingDataGrouping reportingDataGrouping1) {
                return ((Integer) reportingDataGrouping.getOrder()).compareTo((Integer) reportingDataGrouping1.getOrder());
            }
        });
        ArrayAdapter<CharSequence> dataGroupingAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_reporting_criteria_data_grouping_spinner, android.R.layout.simple_spinner_item);
        dataGroupingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataGroupingSpinner.setAdapter(dataGroupingAdapter);
        dataGroupingSpinner.setSelection(ReportingDataGrouping.GROUPED_BY_START_DATE.getOrder()); //Set default value...
        this.dataGrouping = ReportingDataGrouping.GROUPED_BY_START_DATE;

        dataGroupingSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                ReportingDataGrouping[] dataGroupings = ReportingDataGrouping.values();
                for (ReportingDataGrouping dataGrouping : dataGroupings) {
                    if (dataGrouping.getOrder() == pos) {
                        ReportingCriteriaActivity.this.dataGrouping = dataGrouping;
                    }
                }
            }
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        //Data Order spinner
        dataOrders = Arrays.asList(ReportingDataOrder.values());
        Collections.sort(dataOrders, new Comparator<ReportingDataOrder>() {
            public int compare(ReportingDataOrder reportingDataOrder, ReportingDataOrder reportingDataOrder1) {
                return ((Integer) reportingDataOrder.getOrder()).compareTo((Integer) reportingDataOrder1.getOrder());
            }
        });
        ArrayAdapter<CharSequence> dataOrderAdapter = ArrayAdapter.createFromResource(this,
                R.array.lbl_reporting_criteria_data_order_spinner, android.R.layout.simple_spinner_item);
        dataOrderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataOrderSpinner.setAdapter(dataOrderAdapter);
        dataOrderSpinner.setSelection(ReportingDataOrder.DESC.getOrder()); //Set default value...
        this.dataOrder = ReportingDataOrder.DESC;

        dataOrderSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                ReportingDataOrder[] dataOrders = ReportingDataOrder.values();
                for (ReportingDataOrder dataOrder : dataOrders) {
                    if (dataOrder.getOrder() == pos) {
                        ReportingCriteriaActivity.this.dataOrder = dataOrder;
                    }
                }
            }
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        //Display Duration spinner
        displayDurations = Arrays.asList(ReportingDisplayDuration.values());
        Collections.sort(displayDurations, new Comparator<ReportingDisplayDuration>() {
            public int compare(ReportingDisplayDuration reportingDisplayDuration, ReportingDisplayDuration reportingDisplayDuration1) {
                return ((Integer) reportingDisplayDuration.getOrder()).compareTo((Integer) reportingDisplayDuration1.getOrder());
            }
        });
        ArrayAdapter<CharSequence> displayDurationAdapater = ArrayAdapter.createFromResource(this,
                R.array.array_reporting_criteria_data_display_duration_spinner, android.R.layout.simple_spinner_item);

        displayDurationAdapater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        displayDurationSpinner.setAdapter(displayDurationAdapater);
        displayDurationSpinner.setSelection(ReportingDisplayDuration.HOUR_MINUTES_SECONDS.getOrder()); //Set default value...
        this.displayDuration = ReportingDisplayDuration.HOUR_MINUTES_SECONDS;

        displayDurationSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                ReportingDisplayDuration[] displayDurations = ReportingDisplayDuration.values();
                for (ReportingDisplayDuration displayDuration : displayDurations) {
                    if (displayDuration.getOrder() == pos) {
                        ReportingCriteriaActivity.this.displayDuration = displayDuration;
                    }
                }
            }
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        //Handle changes...
        updateViewOnDateRangeSpinnerSelection();
        updateViewOnProjectAndTaskSelection();
    }

    private void updateViewOnDateRangeSpinnerSelection() {
        int index = dateRangeSpinner.getSelectedItemPosition();
        ReportingDateRange selectedDateRange = ReportingDateRange.getByIndex(index);

        switch (selectedDateRange) {
            case TODAY: {
                Date today  = new Date();
                startDate = today;
                endDate = today;
                String todayAsText = DateUtils.DateTimeConverter.convertDateToString(startDate, dateFormat, this);

                dateRangeStartButton.setText(todayAsText);
                dateRangeEndButton.setText(todayAsText);

                dateRangeStartButton.setEnabled(false);
                dateRangeEndButton.setEnabled(false);
                break;
            }
            case YESTERDAY: {
                Calendar yesterday = Calendar.getInstance();
                yesterday.add(Calendar.DAY_OF_MONTH, -1);
                startDate = yesterday.getTime();
                endDate = yesterday.getTime();
                String yesterdayAsText = DateUtils.DateTimeConverter.convertDateToString(startDate, dateFormat, this);

                dateRangeStartButton.setText(yesterdayAsText);
                dateRangeEndButton.setText(yesterdayAsText);

                dateRangeStartButton.setEnabled(false);
                dateRangeEndButton.setEnabled(false);
                break;
            }
            case THIS_WEEK: {
                SparseArray<Date> result = DateUtils.TimeCalculator.calculateWeekBoundaries(0, ReportingCriteriaActivity.this);
                Date firstDay = result.get(DateConstants.FIRST_DAY_OF_WEEK);
                Date lastDay = result.get(DateConstants.LAST_DAY_OF_WEEK);

                startDate = firstDay;
                endDate = lastDay;

                String strFirstDay = DateUtils.DateTimeConverter.convertDateToString(firstDay, dateFormat, this);
                String strLastDay = DateUtils.DateTimeConverter.convertDateToString(lastDay, dateFormat, this);

                dateRangeStartButton.setText(strFirstDay);
                dateRangeEndButton.setText(strLastDay);

                dateRangeStartButton.setEnabled(false);
                dateRangeEndButton.setEnabled(false);
                break;
            }
            case LAST_WEEK: {
                SparseArray<Date> result = DateUtils.TimeCalculator.calculateWeekBoundaries(-1, ReportingCriteriaActivity.this);
                Date firstDay = result.get(DateConstants.FIRST_DAY_OF_WEEK);
                Date lastDay = result.get(DateConstants.LAST_DAY_OF_WEEK);

                startDate = firstDay;
                endDate = lastDay;

                String strFirstDay = DateUtils.DateTimeConverter.convertDateToString(firstDay, dateFormat, this);
                String strLastDay = DateUtils.DateTimeConverter.convertDateToString(lastDay, dateFormat, this);

                dateRangeStartButton.setText(strFirstDay);
                dateRangeEndButton.setText(strLastDay);

                dateRangeStartButton.setEnabled(false);
                dateRangeEndButton.setEnabled(false);
                break;
            }
            case TWO_WEEKS_AGO: {
                SparseArray<Date> result = DateUtils.TimeCalculator.calculateWeekBoundaries(-2, ReportingCriteriaActivity.this);
                Date firstDay = result.get(DateConstants.FIRST_DAY_OF_WEEK);
                Date lastDay = result.get(DateConstants.LAST_DAY_OF_WEEK);

                startDate = firstDay;
                endDate = lastDay;

                String strFirstDay = DateUtils.DateTimeConverter.convertDateToString(firstDay, dateFormat, this);
                String strLastDay = DateUtils.DateTimeConverter.convertDateToString(lastDay, dateFormat, this);

                dateRangeStartButton.setText(strFirstDay);
                dateRangeEndButton.setText(strLastDay);

                dateRangeStartButton.setEnabled(false);
                dateRangeEndButton.setEnabled(false);
                break;
            }
            case THIS_MONTH: {
                Calendar thisMonthStart = Calendar.getInstance();
                thisMonthStart.set(Calendar.DAY_OF_MONTH, thisMonthStart.getActualMinimum(Calendar.DAY_OF_MONTH));

                Calendar thisMonthEnd = Calendar.getInstance();
                thisMonthEnd.set(Calendar.DAY_OF_MONTH, thisMonthEnd.getActualMaximum(Calendar.DAY_OF_MONTH));

                startDate = thisMonthStart.getTime();
                endDate = thisMonthEnd.getTime();

                String strFirstDay = DateUtils.DateTimeConverter.convertDateToString(startDate, dateFormat, this);
                String strLastDay = DateUtils.DateTimeConverter.convertDateToString(endDate, dateFormat, this);

                dateRangeStartButton.setText(strFirstDay);
                dateRangeEndButton.setText(strLastDay);

                dateRangeStartButton.setEnabled(false);
                dateRangeEndButton.setEnabled(false);
                break;
            }
            case LAST_MONTH: {
                Calendar thisMonthStart = Calendar.getInstance();
                thisMonthStart.add(Calendar.MONTH, -1);
                thisMonthStart.set(Calendar.DAY_OF_MONTH, thisMonthStart.getActualMinimum(Calendar.DAY_OF_MONTH));

                Calendar thisMonthEnd = Calendar.getInstance();
                thisMonthEnd.add(Calendar.MONTH, -1);
                thisMonthEnd.set(Calendar.DAY_OF_MONTH, thisMonthEnd.getActualMaximum(Calendar.DAY_OF_MONTH));

                startDate = thisMonthStart.getTime();
                endDate = thisMonthEnd.getTime();

                String strFirstDay = DateUtils.DateTimeConverter.convertDateToString(startDate, dateFormat, this);
                String strLastDay = DateUtils.DateTimeConverter.convertDateToString(endDate, dateFormat, this);

                dateRangeStartButton.setText(strFirstDay);
                dateRangeEndButton.setText(strLastDay);

                dateRangeStartButton.setEnabled(false);
                dateRangeEndButton.setEnabled(false);
                break;
            }
            case TWO_MONTHS_AGO: {
                Calendar thisMonthStart = Calendar.getInstance();
                thisMonthStart.add(Calendar.MONTH, -2);
                thisMonthStart.set(Calendar.DAY_OF_MONTH, thisMonthStart.getActualMinimum(Calendar.DAY_OF_MONTH));

                Calendar thisMonthEnd = Calendar.getInstance();
                thisMonthEnd.add(Calendar.MONTH, -2);
                thisMonthEnd.set(Calendar.DAY_OF_MONTH, thisMonthEnd.getActualMaximum(Calendar.DAY_OF_MONTH));

                startDate = thisMonthStart.getTime();
                endDate = thisMonthEnd.getTime();

                String strFirstDay = DateUtils.DateTimeConverter.convertDateToString(startDate, dateFormat, this);
                String strLastDay = DateUtils.DateTimeConverter.convertDateToString(endDate, dateFormat, this);

                dateRangeStartButton.setText(strFirstDay);
                dateRangeEndButton.setText(strLastDay);

                dateRangeStartButton.setEnabled(false);
                dateRangeEndButton.setEnabled(false);
                break;
            }
            case ALL_TIMES: {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(0L);
                cal.set(Calendar.YEAR, 1900);
                Date firstDay = cal.getTime();
                cal.set(Calendar.YEAR, 9999);
                Date lastDay = cal.getTime();

                startDate = firstDay;
                endDate = lastDay;

                String strFirstDay = DateUtils.DateTimeConverter.convertDateToString(firstDay, dateFormat, this);
                String strLastDay = DateUtils.DateTimeConverter.convertDateToString(lastDay, dateFormat, this);

                dateRangeStartButton.setText(strFirstDay);
                dateRangeEndButton.setText(strLastDay);

                dateRangeStartButton.setEnabled(false);
                dateRangeEndButton.setEnabled(false);
                break;
            }
            default: {
                String strStartDate = DateUtils.DateTimeConverter.convertDateToString(startDate, dateFormat, this);
                String strEndDate = DateUtils.DateTimeConverter.convertDateToString(endDate, dateFormat, this);

                dateRangeStartButton.setText(strStartDate);
                dateRangeEndButton.setText(strEndDate);

                dateRangeStartButton.setEnabled(true);
                dateRangeEndButton.setEnabled(true);
            }
        }
    }

    private void updateViewOnProjectAndTaskSelection() {
        if (project == null) {
            projectButton.setText(R.string.lbl_reporting_criteria_project_any);
            deleteProjectButton.setVisibility(View.GONE);

            taskButton.setText(R.string.lbl_reporting_criteria_task_any);
            taskButton.setEnabled(false);
            showFinishedTasks.setEnabled(false);
            task = null;
            deleteTaskButton.setVisibility(View.GONE);
        } else {
            projectButton.setText(project.getName());
            deleteProjectButton.setVisibility(View.VISIBLE);

            taskButton.setEnabled(true);
            showFinishedTasks.setEnabled(true);
            if (task == null) {
                taskButton.setText(R.string.lbl_reporting_criteria_task_any);
                deleteTaskButton.setVisibility(View.GONE);
            } else {
                taskButton.setText(task.getName());
                deleteTaskButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;

        switch (id) {
            case Constants.Dialog.REPORTING_CRITERIA_SELECT_PROJECT: {
                //Find all projects and sort by name
                availableProjects = projectService.findAll();
                Collections.sort(availableProjects, new ProjectByNameComparator());

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
                                        project = availableProjects.get(index);
                                        task = null;
                                        updateViewOnProjectAndTaskSelection();
                                        removeDialog(Constants.Dialog.REPORTING_CRITERIA_SELECT_PROJECT);
                                    }
                               }
                       )
                       .setOnCancelListener(new DialogInterface.OnCancelListener() {
                           public void onCancel(DialogInterface dialogInterface) {
                               removeDialog(Constants.Dialog.REPORTING_CRITERIA_SELECT_PROJECT);
                           }
                       });
                dialog = builder.create();
                break;
            }
            case Constants.Dialog.REPORTING_CRITERIA_SELECT_TASK: {
                if (showFinishedTasks.isChecked()) {
                    availableTasks = taskService.findTasksForProject(project);
                } else {
                    availableTasks = taskService.findNotFinishedTasksForProject(project);
                }
                Collections.sort(availableTasks, new TaskByNameComparator());

                List<String> tasks = new ArrayList<String>();
                for (Task task : availableTasks) {
                    tasks.add(task.getName());
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.lbl_widget_title_select_task)
                       .setSingleChoiceItems(
                               StringUtils.convertListToArray(tasks),
                               -1,
                               new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogInterface, int index) {
                                        task = availableTasks.get(index);
                                        updateViewOnProjectAndTaskSelection();
                                        removeDialog(Constants.Dialog.REPORTING_CRITERIA_SELECT_TASK);
                                    }
                               }
                       )
                       .setOnCancelListener(new DialogInterface.OnCancelListener() {
                           public void onCancel(DialogInterface dialogInterface) {
                               removeDialog(Constants.Dialog.REPORTING_CRITERIA_SELECT_TASK);
                           }
                       });
                dialog = builder.create();
                break;
            }
            case Constants.Dialog.REPORTING_CRITERIA_SELECT_START_DATE: {
                final Calendar startDateCal = Calendar.getInstance();
                startDateCal.setTime(startDate);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        ReportingCriteriaActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            public void onDateSet(DatePicker datePickerView
                                    , int year, int monthOfYear, int dayOfMonth) {
                                startDateCal.set(Calendar.YEAR, year);
                                startDateCal.set(Calendar.MONTH, monthOfYear);
                                startDateCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                startDate = startDateCal.getTime();
                                updateViewOnDateRangeSpinnerSelection();

                                if (startDate.after(endDate)) {
                                    showDialog(Constants.Dialog.REPORTING_CRITERIA_SELECT_END_DATE);
                                }

                                removeDialog(Constants.Dialog.REPORTING_CRITERIA_SELECT_START_DATE);
                            }
                        },
                        startDateCal.get(Calendar.YEAR),
                        startDateCal.get(Calendar.MONTH),
                        startDateCal.get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.setTitle(R.string.lbl_reporting_criteria_date_from_picker);
                datePickerDialog.setButton2(getString(android.R.string.cancel), new DatePickerDialog.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeDialog(Constants.Dialog.REPORTING_CRITERIA_SELECT_START_DATE);
                    }
                });
                dialog = datePickerDialog;
                break;
            }
            case Constants.Dialog.REPORTING_CRITERIA_SELECT_END_DATE: {
                final Calendar endDateCal = Calendar.getInstance();
                endDateCal.setTime(endDate);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        ReportingCriteriaActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            public void onDateSet(DatePicker datePickerView
                                    , int year, int monthOfYear, int dayOfMonth) {
                                endDateCal.set(Calendar.YEAR, year);
                                endDateCal.set(Calendar.MONTH, monthOfYear);
                                endDateCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                endDate = endDateCal.getTime();
                                updateViewOnDateRangeSpinnerSelection();

                                if (endDate.before(startDate)) {
                                    showDialog(Constants.Dialog.REPORTING_CRITERIA_SELECT_END_DATE_ERROR_BEFORE_START_DATE);
                                } else {
                                    removeDialog(Constants.Dialog.REPORTING_CRITERIA_SELECT_END_DATE);
                                }
                            }
                        },
                        endDateCal.get(Calendar.YEAR),
                        endDateCal.get(Calendar.MONTH),
                        endDateCal.get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.setTitle(R.string.lbl_reporting_criteria_date_till_picker);
                datePickerDialog.setButton2(getString(android.R.string.cancel), new DatePickerDialog.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeDialog(Constants.Dialog.REPORTING_CRITERIA_SELECT_START_DATE);
                    }
                });

                dialog = datePickerDialog;
                break;
            }
            case Constants.Dialog.REPORTING_CRITERIA_SELECT_END_DATE_ERROR_BEFORE_START_DATE: {
                String startDateString = DateUtils.DateTimeConverter.convertDateToString(startDate, dateFormat, this);
                AlertDialog.Builder alertValidationError = new AlertDialog.Builder(this);
				alertValidationError
                           .setTitle(R.string.lbl_reporting_criteria_date_till_picker_validation_error_title)
						   .setMessage( getString(
                                   R.string.msg_reporting_criteria_date_till_picker_validation_error_before_start_date,
                                   startDateString
                           ))
						   .setCancelable(false)
						   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int which) {
                                   dialog.cancel();
                                   showDialog(Constants.Dialog.REPORTING_CRITERIA_SELECT_END_DATE);
                               }
                           });
				dialog = alertValidationError.create();
                break;
            }
            case Constants.Dialog.REPORTING_BATCH_SHARE_NO_FILES_FOUND: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.lbl_reporting_criteria_batch_share_no_files_found_dialog_msg)
                       .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialogInterface, int i) {
                               removeDialog(Constants.Dialog.REPORTING_BATCH_SHARE_NO_FILES_FOUND);
                           }
                       });
                dialog = builder.create();
                break;
            }
            case Constants.Dialog.REPORTING_BATCH_SHARE: {
                final List<File> selectedDocuments = new ArrayList<File>();
                final File[] allDocuments = allDocumentsAvailable;

                String[] documentNames = new String[allDocuments.length];
                boolean[] checkedNames = new boolean[allDocuments.length];
                for (int i=0; i<allDocuments.length; i++) {
                    File document = allDocuments[i];
                    documentNames[i] = document.getName();
                    checkedNames[i] = Boolean.FALSE;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.lbl_reporting_criteria_batch_share_dialog_title)
                       .setMultiChoiceItems(documentNames,
                               checkedNames,
                               new DialogInterface.OnMultiChoiceClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                       Log.d(getApplicationContext(), LOG_TAG, (isChecked?"Selecting":"Unselecting") + " document on position " + which + " with title '" + allDocuments[which].getName() + "'");
                                       if (isChecked) {
                                           selectedDocuments.add(allDocuments[which]);
                                       } else {
                                           selectedDocuments.remove(allDocuments[which]);
                                       }
                                   }
                               }
                       )
                       .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialogInterface, int i) {
                               Log.d(getApplicationContext(), LOG_TAG, "Continuing to batch share with " + selectedDocuments.size() + " document(s)");
                               if (selectedDocuments.size() == 0) {
                                   Log.d(getApplicationContext(), LOG_TAG, "Showing toast message notifying the user he must select a document to share");
                                   Toast.makeText(ReportingCriteriaActivity.this, R.string.msg_reporting_criteria_batch_share_no_docs_selected, Toast.LENGTH_SHORT).show();
                                   return;
                               }
                               removeDialog(Constants.Dialog.REPORTING_BATCH_SHARE);
                               batchShareDocuments(selectedDocuments);
                           }
                       })
                       .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialogInterface, int i) {
                               Log.d(getApplicationContext(), LOG_TAG, "Stopping the batch share procedure using the cancel button");
                               removeDialog(Constants.Dialog.REPORTING_BATCH_SHARE);
                           }
                       })
                       .setOnCancelListener(new DialogInterface.OnCancelListener() {
                           public void onCancel(DialogInterface dialogInterface) {
                               Log.d(getApplicationContext(), LOG_TAG, "Stopping the batch share procedure using the device back");
                               removeDialog(Constants.Dialog.REPORTING_BATCH_SHARE);
                           }
                       });
                dialog = builder.create();
                break;
            }
        }

        return dialog;
    }

    private void batchShareDocuments(List<File> selectedDocuments) {
        Log.d(getApplicationContext(), LOG_TAG, "Sharing " + selectedDocuments.size() + " document(s)");

        IntentUtil.sendSomething(
                ReportingCriteriaActivity.this,
                R.string.lbl_reporting_criteria_batch_share_subject,
                R.string.lbl_reporting_criteria_batch_share_body,
                selectedDocuments,
                R.string.lbl_reporting_criteria_batch_share_app_chooser_title
        );

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.ab_activity_reporting_criteria, menu);

        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                IntentUtil.goBack(ReportingCriteriaActivity.this);
                break;
            case R.id.menu_reporting_criteria_activity_new:
                generateReport();
                break;
            case R.id.menu_reporting_criteria_activity_batch_share:
                shareFiles();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void generateReport() {
        if (startDate.after(endDate)) {
            showDialog(Constants.Dialog.REPORTING_CRITERIA_SELECT_END_DATE_ERROR_BEFORE_START_DATE);
            return;
        }

        tracker.trackEvent(
                TrackerConstants.EventSources.REPORTING_CRITERIA_ACTIVITY,
                TrackerConstants.EventActions.GENERATE_REPORT
        );

        Intent intent = new Intent(ReportingCriteriaActivity.this, ReportingResultActivity.class);
        intent.putExtra(Constants.Extras.TIME_REGISTRATION_START_DATE, startDate);
        intent.putExtra(Constants.Extras.TIME_REGISTRATION_END_DATE, endDate);
        intent.putExtra(Constants.Extras.PROJECT, project);
        intent.putExtra(Constants.Extras.TASK, task);
        intent.putExtra(Constants.Extras.DATA_GROUPING, dataGrouping);
        intent.putExtra(Constants.Extras.DATA_ORDER, dataOrder);
        intent.putExtra(Constants.Extras.DISPLAY_DURATION, displayDuration);
        startActivity(intent);
    }

    private void shareFiles() {
        final File[] csvDocuments = FileUtil.getExportDir(ReportingCriteriaActivity.this).listFiles(new CsvFilenameFilter());
        final File[] xlsDocuments = FileUtil.getExportDir(ReportingCriteriaActivity.this).listFiles(new XlsFilenameFilter());
        allDocumentsAvailable = new File[csvDocuments.length + xlsDocuments.length];
        System.arraycopy(csvDocuments, 0, allDocumentsAvailable, 0, csvDocuments.length);
        System.arraycopy(xlsDocuments, 0, allDocumentsAvailable, csvDocuments.length, xlsDocuments.length);

        if (allDocumentsAvailable.length > 0)
            showDialog(Constants.Dialog.REPORTING_BATCH_SHARE);
        else
            showDialog(Constants.Dialog.REPORTING_BATCH_SHARE_NO_FILES_FOUND);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tracker.stopSession();
    }
}