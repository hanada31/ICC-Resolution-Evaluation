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

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.comparators.reporting.TimeRegistrationByProjectNameAscComparator;
import eu.vranckaert.worktime.comparators.reporting.TimeRegistrationByProjectNameDescComparator;
import eu.vranckaert.worktime.comparators.reporting.TimeRegistrationByStartDateAscComparator;
import eu.vranckaert.worktime.comparators.reporting.TimeRegistrationByStartDateDescComparator;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.TrackerConstants;
import eu.vranckaert.worktime.enums.reporting.ReportingDataGrouping;
import eu.vranckaert.worktime.enums.reporting.ReportingDataOrder;
import eu.vranckaert.worktime.enums.reporting.ReportingDisplayDuration;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.Task;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.model.dto.export.ExportDTO;
import eu.vranckaert.worktime.model.dto.reporting.ReportingTableRecord;
import eu.vranckaert.worktime.model.dto.reporting.ReportingTableRecordLevel;
import eu.vranckaert.worktime.model.dto.reporting.datalevels.ReportingDataLvl0;
import eu.vranckaert.worktime.model.dto.reporting.datalevels.ReportingDataLvl1;
import eu.vranckaert.worktime.model.dto.reporting.datalevels.ReportingDataLvl2;
import eu.vranckaert.worktime.service.ProjectService;
import eu.vranckaert.worktime.service.TaskService;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.utils.context.AsyncHelper;
import eu.vranckaert.worktime.utils.context.IntentUtil;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.date.DateFormat;
import eu.vranckaert.worktime.utils.date.DateUtils;
import eu.vranckaert.worktime.utils.tracker.AnalyticsTracker;
import eu.vranckaert.worktime.utils.view.actionbar.ActionBarGuiceActivity;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

import com.google.inject.internal.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 24/09/11
 * Time: 20:03
 */
public class ReportingResultActivity extends ActionBarGuiceActivity {
    private static final String LOG_TAG = ReportingResultActivity.class.getSimpleName();

    @Inject
    private TimeRegistrationService timeRegistrationService;

    @Inject
    private ProjectService projectService;

    @Inject
    private TaskService taskService;

    @InjectExtra(value= Constants.Extras.TIME_REGISTRATION_START_DATE)
    private Date startDate;
    @InjectExtra(value= Constants.Extras.TIME_REGISTRATION_END_DATE)
    private Date endDate;
    @InjectExtra(value = Constants.Extras.PROJECT, optional = true)
    @Nullable
    private Project project;
    @InjectExtra(value = Constants.Extras.TASK, optional = true)
    @Nullable
    private Task task;
    @InjectExtra(value = Constants.Extras.DATA_GROUPING)
    private ReportingDataGrouping dataGrouping;
    @InjectExtra(value = Constants.Extras.DATA_ORDER, optional = true)
    @Nullable
    private ReportingDataOrder dataOrder;
    @InjectExtra(value = Constants.Extras.DISPLAY_DURATION)
    private ReportingDisplayDuration displayDuration;

    @InjectView(R.id.reporting_result_includes_ongoing_tr_label)
    private TextView resultIncludesOngoingTrsLabel;

    @InjectView(R.id.reporting_result_table)
    private TableLayout resultTable;

    private AnalyticsTracker tracker;

    private List<TimeRegistration> timeRegistrations = new ArrayList<TimeRegistration>();
    private List<ReportingTableRecord> tableRecords = new ArrayList<ReportingTableRecord>();
    private List<ReportingDataLvl0> reportingDataLevels = new ArrayList<ReportingDataLvl0>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporting_result);

        setTitle(R.string.lbl_reporting_result_title);
        setDisplayHomeAsUpEnabled(true);

        tracker = AnalyticsTracker.getInstance(getApplicationContext());
        tracker.trackPageView(TrackerConstants.PageView.REPORTING_RESULT_ACTIVITY);

        initializeView();
    }

    private void initializeView() {
        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected void onPreExecute() {
                showDialog(Constants.Dialog.LOADING_REPORTING_RESULTS);
            }

            @Override
            protected Object doInBackground(Object... objects) {
                timeRegistrations = timeRegistrationService
                    .getTimeRegistrations(startDate, endDate, project, task);

                //Make sure all the time registration details are loaded (task and project)
                for (TimeRegistration timeRegistration : timeRegistrations) {
                    taskService.refresh(timeRegistration.getTask());
                    projectService.refresh(timeRegistration.getTask().getProject());
                }

                //Apply the data order on the time registrations
                switch (dataGrouping) {
                    case GROUPED_BY_START_DATE: {
                        if (dataOrder.equals(ReportingDataOrder.ASC)) {
                            Log.d(getApplicationContext(), LOG_TAG, "Ordering time registrations ASC on START-DATE");
                            Collections.sort(timeRegistrations, new TimeRegistrationByStartDateAscComparator());
                        } else {
                            Log.d(getApplicationContext(), LOG_TAG, "Ordering time registrations DESC on START-DATE");
                            Collections.sort(timeRegistrations, new TimeRegistrationByStartDateDescComparator());
                        }
                        break;
                    }
                    case GROUPED_BY_PROJECT: {
                        if (dataOrder.equals(ReportingDataOrder.ASC)) {
                            Log.d(getApplicationContext(), LOG_TAG, "Ordering time registrations ASC on PROJECT-NAME");
                            Collections.sort(timeRegistrations, new TimeRegistrationByProjectNameAscComparator());
                        } else {
                            Log.d(getApplicationContext(), LOG_TAG, "Ordering time registrations DESC on PROJECT-NAME");
                            Collections.sort(timeRegistrations, new TimeRegistrationByProjectNameDescComparator());
                        }
                        break;
                    }
                }
                Log.d(getApplicationContext(), LOG_TAG, "Number of time registrations found: " + timeRegistrations.size());
                tableRecords = buildTableRecords(timeRegistrations, dataGrouping);
                return tableRecords;
            }

            @Override
            protected void onPostExecute(Object o) {
                removeDialog(Constants.Dialog.LOADING_REPORTING_RESULTS);
                List<ReportingTableRecord> tableRecords = (List<ReportingTableRecord>)o;
                buildTable(tableRecords);
            }
        };
        AsyncHelper.start(asyncTask);
    }

    private void buildTable(List<ReportingTableRecord> tableRecords) {
        for (ReportingTableRecord record : tableRecords) {
            TableRow row = new TableRow(ReportingResultActivity.this);
            TextView recordTotalCol1 = new TextView(ReportingResultActivity.this);
            recordTotalCol1.setText(record.getColumn1());
            TextView recordTotalCol2 = new TextView(ReportingResultActivity.this);
            recordTotalCol2.setText(record.getColumn2());
            TextView recordTotalCol3 = new TextView(ReportingResultActivity.this);
            recordTotalCol3.setText(record.getColumn3());
            TextView recordTotalCol4 = new TextView(ReportingResultActivity.this);
            recordTotalCol4.setText(record.getColumnTotal());
            recordTotalCol4.setGravity(Gravity.RIGHT);

            row.addView(recordTotalCol1);
            row.addView(recordTotalCol2);
            row.addView(recordTotalCol3);
            row.addView(recordTotalCol4);

            Log.d(getApplicationContext(), LOG_TAG, "Level of record: " + record.getLevel());
            switch(record.getLevel()) {
                case LVL0: {
                    Log.d(getApplicationContext(), LOG_TAG, "Setting color for row for record level 0");
                    row.setBackgroundResource(R.color.table_record_lvl_n_0);
                    break;
                }
                case LVL1: {
                    Log.d(getApplicationContext(), LOG_TAG, "Setting color for row for record level 1");
                    row.setBackgroundResource(R.color.table_record_lvl_n_1);
                    break;
                }
                case LVL2: {
                    Log.d(getApplicationContext(), LOG_TAG, "Setting color for row for record level 2");
                    row.setBackgroundResource(R.color.table_record_lvl_n_2);
                    break;
                }
                case LVL3: {
                    Log.d(getApplicationContext(), LOG_TAG, "Setting color for row for record level 3");
                    row.setBackgroundResource(R.color.table_record_lvl_n_3);
                    break;
                }
            }

            resultTable.addView(row);

            if (record.isOngoingTr()) {
                resultIncludesOngoingTrsLabel.setVisibility(View.VISIBLE);
            }
        }
    }

    private List<ReportingTableRecord> buildTableRecords(List<TimeRegistration> timeRegistrations, ReportingDataGrouping reportingDataGrouping) {
        List<ReportingTableRecord> tableRecords = new ArrayList<ReportingTableRecord>();

        ReportingTableRecord totalRecord = new ReportingTableRecord();
        String totalDuration = DateUtils.TimeCalculator.calculatePeriod(ReportingResultActivity.this, timeRegistrations, displayDuration);
        totalRecord.setColumn1(getText(R.string.lbl_reporting_results_table_total).toString());
        totalRecord.setColumnTotal(totalDuration);
        totalRecord.setLevel(ReportingTableRecordLevel.LVL0);
        tableRecords.add(totalRecord);

        reportingDataLevels = buildReportingDataLevels(timeRegistrations, reportingDataGrouping);

        for (ReportingDataLvl0 lvl0 : reportingDataLevels) {
        	ReportingTableRecord lvl0Record = new ReportingTableRecord();
        	lvl0Record.setColumn1(String.valueOf(lvl0.getKey()));
        	lvl0Record.setColumnTotal(DateUtils.TimeCalculator.calculatePeriod(ReportingResultActivity.this, lvl0.getTimeRegistrations(), displayDuration));
            lvl0Record.setLevel(ReportingTableRecordLevel.LVL1);
        	tableRecords.add(lvl0Record);
        	for (ReportingDataLvl1 lvl1 : lvl0.getReportingDataLvl1()) {
        		ReportingTableRecord lvl1Record = new ReportingTableRecord();
            	lvl1Record.setColumn2(String.valueOf(lvl1.getKey()));
            	lvl1Record.setColumnTotal(DateUtils.TimeCalculator.calculatePeriod(ReportingResultActivity.this, lvl1.getTimeRegistrations(), displayDuration));
                lvl1Record.setLevel(ReportingTableRecordLevel.LVL2);
            	tableRecords.add(lvl1Record);
            	for (ReportingDataLvl2 lvl2 : lvl1.getReportingDataLvl2()) {
            		ReportingTableRecord lvl2Record = new ReportingTableRecord();
                	lvl2Record.setColumn3(String.valueOf(lvl2.getKey()));
                	lvl2Record.setColumnTotal(DateUtils.TimeCalculator.calculatePeriod(ReportingResultActivity.this, lvl2.getTimeRegistrations(), displayDuration));
                    lvl2Record.setLevel(ReportingTableRecordLevel.LVL3);
                	tableRecords.add(lvl2Record);
                	for (TimeRegistration timeRegistration : lvl2.getTimeRegistrations()) {
                		if (timeRegistration.isOngoingTimeRegistration()) {
                            totalRecord.setOngoingTr(true);
                            break;
                        }
                	}
            	}
        	}
        }

        return tableRecords;
    }

    private List<ReportingDataLvl0> buildReportingDataLevels(List<TimeRegistration> timeRegistrations,
			ReportingDataGrouping reportingDataGrouping) {
    	List<ReportingDataLvl0> reportingDataLevels = new ArrayList<ReportingDataLvl0>();

    	switch (reportingDataGrouping) {
			case GROUPED_BY_START_DATE: {
				reportingDataLevels = groupByStartDate(timeRegistrations);
				break;
			}
			case GROUPED_BY_PROJECT: {
				reportingDataLevels = groupByProject(timeRegistrations);
				break;
			}
    	}
		return reportingDataLevels;
	}

    private List<ReportingDataLvl0> groupByStartDate(List<TimeRegistration> timeRegistrations) {
    	List<ReportingDataLvl0> reportingDataLevels = new ArrayList<ReportingDataLvl0>();

    	for (TimeRegistration tr : timeRegistrations) {
    		//Check for start date
    		Date startTime = DateUtils.Various.setMinTimeValueOfDay(tr.getStartTime());
    		ReportingDataLvl0 dateLvl = new ReportingDataLvl0(DateUtils.DateTimeConverter.convertDateToString(startTime, DateFormat.SHORT, ReportingResultActivity.this));
    		int dateLvlIndex = reportingDataLevels.indexOf(dateLvl);
    		if (dateLvlIndex > -1) {
    			dateLvl = reportingDataLevels.get(dateLvlIndex);
    		} else {
    			reportingDataLevels.add(dateLvl);
    		}

    		//Check for project
    		ReportingDataLvl1 projectLvl = new ReportingDataLvl1(tr.getTask().getProject().getName());
    		int projectLvlIndex = dateLvl.getReportingDataLvl1().indexOf(projectLvl);
    		if (projectLvlIndex > -1) {
    			projectLvl = dateLvl.getReportingDataLvl1().get(projectLvlIndex);
    		} else {
    			dateLvl.getReportingDataLvl1().add(projectLvl);
    		}

    		//Check for task
    		ReportingDataLvl2 taskLvl = new ReportingDataLvl2(tr.getTask().getName());
    		int taskLvlIndex = projectLvl.getReportingDataLvl2().indexOf(taskLvl);
    		if (taskLvlIndex > -1) {
    			taskLvl = projectLvl.getReportingDataLvl2().get(taskLvlIndex);
    		} else {
    			projectLvl.getReportingDataLvl2().add(taskLvl);
    		}

    		//Add TR to task level
    		dateLvl.addTimeRegistration(tr);
    		projectLvl.addTimeRegistration(tr);
    		taskLvl.addTimeRegistration(tr);
    	}

    	return reportingDataLevels;
    }

    private List<ReportingDataLvl0> groupByProject(List<TimeRegistration> timeRegistrations) {
    	List<ReportingDataLvl0> reportingDataLevels = new ArrayList<ReportingDataLvl0>();

    	for (TimeRegistration tr : timeRegistrations) {
    		//Check for project
			ReportingDataLvl0 projectLvl = new ReportingDataLvl0(tr.getTask().getProject().getName());
    		int projectLvlIndex = reportingDataLevels.indexOf(projectLvl);
    		if (projectLvlIndex > -1) {
    			projectLvl = reportingDataLevels.get(projectLvlIndex);
    		} else {
    			reportingDataLevels.add(projectLvl);
    		}

    		//Check for task
    		ReportingDataLvl1 taskLvl = new ReportingDataLvl1(tr.getTask().getName());
    		int taskLvlIndex = projectLvl.getReportingDataLvl1().indexOf(taskLvl);
    		if (taskLvlIndex > -1) {
    			taskLvl = projectLvl.getReportingDataLvl1().get(taskLvlIndex);
    		} else {
    			projectLvl.getReportingDataLvl1().add(taskLvl);
    		}

    		//Check for start date
    		Date startTime = DateUtils.Various.setMinTimeValueOfDay(tr.getStartTime());
            ReportingDataLvl2 dateLvl = new ReportingDataLvl2(DateUtils.DateTimeConverter.convertDateToString(startTime, DateFormat.SHORT, ReportingResultActivity.this));
    		int dateLvlIndex = taskLvl.getReportingDataLvl2().indexOf(dateLvl);;
    		if (dateLvlIndex > -1) {
    			dateLvl = taskLvl.getReportingDataLvl2().get(dateLvlIndex);
    		} else {
    			taskLvl.getReportingDataLvl2().add(dateLvl);
    		}

    		//Add TR to task level
    		projectLvl.addTimeRegistration(tr);
    		taskLvl.addTimeRegistration(tr);
    		dateLvl.addTimeRegistration(tr);
    	}

    	return reportingDataLevels;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Log.d(getApplicationContext(), LOG_TAG, "Received request to create dialog with id " + id);
        Dialog dialog = null;
        switch(id) {
            case Constants.Dialog.LOADING_REPORTING_RESULTS: {
                dialog = ProgressDialog.show(
                        ReportingResultActivity.this,
                        "",
                        getText(R.string.lbl_reporting_result_loading_dialog),
                        true,
                        false
                );
                break;
            }
            default:
                Log.e(getApplicationContext(), LOG_TAG, "Dialog id " + id + " is not supported in this activity!");
        }
        return dialog;
    }

    private void save() {
        tracker.trackEvent(
                TrackerConstants.EventSources.REPORTING_RESULT_ACTIVITY,
                TrackerConstants.EventActions.EXPORT_RESULT
        );

        if (timeRegistrations == null) {
            timeRegistrations = new ArrayList<TimeRegistration>();
        }
        if (tableRecords == null) {
            tableRecords = new ArrayList<ReportingTableRecord>();
        }

        ExportDTO exportDto = new ExportDTO();
        exportDto.setTimeRegistrations(timeRegistrations);
        exportDto.setTableRecords(tableRecords);
        exportDto.setReportingDataLevels(reportingDataLevels);

        Intent intent = new Intent(ReportingResultActivity.this, ReportingExportActivity.class);
        intent.putExtra(Constants.Extras.EXPORT_DTO, exportDto);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.ab_activity_reporting_result, menu);

        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                IntentUtil.goBack(ReportingResultActivity.this);
                break;
            case R.id.menu_reporting_result_save:
                save();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tracker.stopSession();
    }
}