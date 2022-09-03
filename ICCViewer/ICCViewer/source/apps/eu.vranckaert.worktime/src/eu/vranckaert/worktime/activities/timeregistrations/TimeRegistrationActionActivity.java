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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.activities.timeregistrations.listadapter.TimeRegistrationActionListAdapter;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.TrackerConstants;
import eu.vranckaert.worktime.enums.timeregistration.TimeRegistrationAction;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.service.BackupService;
import eu.vranckaert.worktime.service.CommentHistoryService;
import eu.vranckaert.worktime.service.TaskService;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.service.impl.CommentHistoryServiceImpl;
import eu.vranckaert.worktime.service.impl.DatabaseFileBackupServiceImpl;
import eu.vranckaert.worktime.service.impl.TaskServiceImpl;
import eu.vranckaert.worktime.service.impl.TimeRegistrationServiceImpl;
import eu.vranckaert.worktime.service.ui.StatusBarNotificationService;
import eu.vranckaert.worktime.service.ui.WidgetService;
import eu.vranckaert.worktime.service.ui.impl.StatusBarNotificationServiceImpl;
import eu.vranckaert.worktime.service.ui.impl.WidgetServiceImpl;
import eu.vranckaert.worktime.utils.context.ContextUtils;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.date.DateFormat;
import eu.vranckaert.worktime.utils.date.DateUtils;
import eu.vranckaert.worktime.utils.preferences.Preferences;
import eu.vranckaert.worktime.utils.string.StringUtils;
import eu.vranckaert.worktime.utils.tracker.AnalyticsTracker;
import eu.vranckaert.worktime.utils.view.actionbar.synclock.SyncLockedGuiceActivity;
import roboguice.activity.RoboActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 09/02/11
 * Time: 23:25
 */
public class TimeRegistrationActionActivity extends SyncLockedGuiceActivity {
    /**
     * LOG_TAG for logging
     */
    private static final String LOG_TAG = TimeRegistrationActionActivity.class.getSimpleName();

    /**
     * Services
     */
    @Inject private WidgetService widgetService;
    @Inject private StatusBarNotificationService statusBarNotificationService;
    @Inject private TimeRegistrationService timeRegistrationService;
    @Inject private CommentHistoryService commentHistoryService;
    @Inject private TaskService taskService;
    @Inject private BackupService backupService;

    /**
     * Extras
     */
    private TimeRegistration timeRegistration;
    private Integer widgetId;
    private TimeRegistrationAction defaultAction;
    private Boolean skipDialog;
    private Boolean onlyAction;

    /**
     * Google Analytics Tracker
     */
    private AnalyticsTracker tracker;

    /**
     * Vars
     */
    private Calendar removeRangeMinBoundary = null;
    private Calendar removeRangeMaxBoundary = null;
    private Button deleteRangeFromButton = null;
    private Button deleteRangeToButton = null;
    private RadioGroup deleteRadioContainer = null;
    private EditText commentEditText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadExtras();

        tracker = AnalyticsTracker.getInstance(getApplicationContext());
        Log.d(getApplicationContext(), LOG_TAG, "Started the TimeRegistration Action activity");

        if (timeRegistration == null) {
            Log.e(getApplicationContext(), LOG_TAG, "The time registration should never be null in the TimeRegistrationActionActivity!");
            throw new RuntimeException("The time registration should never be null in the TimeRegistrationActionActivity!");
        } else {
            timeRegistrationService.fullyInitialize(timeRegistration);
            Log.d(getApplicationContext(), LOG_TAG, "Launching action-activity with timeRegistration " + timeRegistration);
        }

        if (skipDialog && defaultAction != null) {
            handleTimeRegistrationAction(defaultAction);
        } else {
            showDialog(Constants.Dialog.TIME_REGISTRATION_ACTION);
        }
    }

    /**
     * Get all the extras...
     */
    private void loadExtras() {
        timeRegistration = (TimeRegistration) getIntent().getExtras().get(Constants.Extras.TIME_REGISTRATION);
        widgetId = (Integer) getIntent().getExtras().get(Constants.Extras.WIDGET_ID);

        // Extras for setting a default action
        defaultAction = (TimeRegistrationAction) getIntent().getExtras().get(Constants.Extras.DEFAULT_ACTION);
        skipDialog = (Boolean) getIntent().getExtras().get(Constants.Extras.SKIP_DIALOG);
        if (skipDialog == null) {
            skipDialog = false;
        }
        // Use this together with default action to set that can be used in the dialog (the one action will be the
        // 'default action'
        onlyAction = (Boolean) getIntent().getExtras().get(Constants.Extras.ONLY_ACTION);
        if (onlyAction == null) {
            onlyAction = false;
        }
    }

    @Override
    protected Dialog onCreateDialog(int dialogId) {
        Dialog dialog = null;
        switch (dialogId) {
            case Constants.Dialog.TIME_REGISTRATION_ACTION: {
                Log.d(getApplicationContext(), LOG_TAG, "Building the actions dialog");
                AlertDialog.Builder actionsDialog = new AlertDialog.Builder(this);

                LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

                final View layout = inflater.inflate(R.layout.dialog_time_registration_actions,
                        (ViewGroup) findViewById(R.id.dialog_layout_root));
                commentEditText = (EditText) layout.findViewById(R.id.tr_comment);

                if (timeRegistration.getComment() != null) {
                    commentEditText.setText(timeRegistration.getComment());
                }

                // Attach the button
                Button reuseComment = (Button) layout.findViewById(R.id.tr_reuse_btn);
                reuseComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String comment = commentHistoryService.findLastComment();
                        if (comment != null) {
                            commentEditText.setText(comment);
                        }
                    }
                });

                // Create the spinner content
                final List<TimeRegistrationAction> actions = TimeRegistrationAction.getTimeRegistrationActions(timeRegistration);
                final Spinner actionSpinner = (Spinner) layout.findViewById(R.id.tr_action_spinner);
                TimeRegistrationActionListAdapter actionsAdapter = getFilteredActionsAdapter(actions);
                actionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                actionSpinner.setAdapter(actionsAdapter);

                // Set default value for action...
                if (defaultAction == null) {
                    if (timeRegistration.isOngoingTimeRegistration()) {
                        actionSpinner.setSelection(
                                Preferences.getDefaultTimeRegistrationActionForOngoingTr(TimeRegistrationActionActivity.this).getOrder()
                        );
                    } else {
                        actionSpinner.setSelection(
                                Preferences.getDefaultTimeRegistrationActionForFinishedTr(TimeRegistrationActionActivity.this).getOrder()
                        );
                    }
                } else {
                    actionSpinner.setSelection(defaultAction.getOrder());
                    if (onlyAction) {
                        actionSpinner.setEnabled(false);
                    }
                }
                actionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        final TimeRegistrationAction action = TimeRegistrationAction.getByIndex(actions, position);
                        final View commentContainer = layout.findViewById(R.id.tr_comment_container);
                        final View deleteContainer = layout.findViewById(R.id.tr_delete_container);

                        commentContainer.setVisibility(View.GONE);
                        deleteContainer.setVisibility(View.GONE);

                        switch (action) {
                            case PUNCH_OUT:
                            case PUNCH_OUT_AND_START_NEXT:
                                if (Preferences.getEndingTimeRegistrationCommentPreference(getApplicationContext())) {
                                    commentContainer.setVisibility(View.VISIBLE);
                                }
                                break;
                            case SET_COMMENT:
                                commentContainer.setVisibility(View.VISIBLE);
                                break;
                            case DELETE_TIME_REGISTRATION:
                                deleteContainer.setVisibility(View.VISIBLE);
                                final TableLayout deleteRangeContainer = (TableLayout) layout.findViewById(R.id.tr_delete_range_container);
                                deleteRangeFromButton = (Button) layout.findViewById(R.id.tr_delete_range_date_from);
                                deleteRangeToButton = (Button) layout.findViewById(R.id.tr_delete_range_date_to);

                                removeRangeMinBoundary = Calendar.getInstance();
                                removeRangeMinBoundary.setTime(new Date());
                                removeRangeMaxBoundary = Calendar.getInstance();
                                removeRangeMaxBoundary.setTime(new Date());

                                updateDateRangeSelectionButtons();

                                deleteRadioContainer = (RadioGroup) layout.findViewById(R.id.tr_delete_radio_container);
                                deleteRadioContainer.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(RadioGroup radioGroup, int id) {
                                        switch (id) {
                                            case R.id.tr_delete_current: {
                                                deleteRangeContainer.setVisibility(View.GONE);
                                                break;
                                            }
                                            case R.id.tr_delete_range: {
                                                deleteRangeContainer.setVisibility(View.VISIBLE);

                                                deleteRangeFromButton.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        showDialog(Constants.Dialog.TIME_REGISTRATION_DELETE_RANGE_MIN_BOUNDARY);
                                                    }
                                                });
                                                deleteRangeToButton.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        showDialog(Constants.Dialog.TIME_REGISTRATION_DELETE_RANGE_MAX_BOUNDARY);
                                                    }
                                                });
                                                break;
                                            }
                                        }
                                    }
                                });
                                deleteRadioContainer.check(R.id.tr_delete_current);
                                break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        // NA
                    }
                });

                actionsDialog.setTitle(R.string.lbl_time_registration_actions_dialog_title_choose_action);
                actionsDialog.setCancelable(true);
                actionsDialog.setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeDialog(Constants.Dialog.TIME_REGISTRATION_ACTION);
                        ContextUtils.hideKeyboard(TimeRegistrationActionActivity.this, commentEditText);
                        TimeRegistrationAction action = TimeRegistrationAction.getByIndex(actions, actionSpinner.getSelectedItemPosition());
                        handleTimeRegistrationAction(action);
                    }
                });
                actionsDialog.setNegativeButton(android.R.string.cancel, new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(getApplicationContext(), LOG_TAG, "Cancelled ending TR when about to enter comment...");
                        removeDialog(Constants.Dialog.TIME_REGISTRATION_ACTION);
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });
                actionsDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        Log.d(getApplicationContext(), LOG_TAG, "Cancelled ending TR when about to enter comment...");
                        removeDialog(Constants.Dialog.TIME_REGISTRATION_ACTION);
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });

                actionsDialog.setView(layout);
                dialog = actionsDialog.create();

                break;
            }
            case Constants.Dialog.TIME_REGISTRATION_DELETE_LOADING: {
                Log.d(getApplicationContext(), LOG_TAG, "Creating loading dialog for deleting tr");
                dialog = ProgressDialog.show(
                        TimeRegistrationActionActivity.this,
                        "",
                        getString(R.string.lbl_time_registration_actions_dialog_removing_time_registration),
                        true,
                        false
                );
                break;
            }
            case Constants.Dialog.TIME_REGISTRATIONS_DELETE_LOADING: {
                Log.d(getApplicationContext(), LOG_TAG, "Creating loading dialog for deleting tr's");
                dialog = ProgressDialog.show(
                        TimeRegistrationActionActivity.this,
                        "",
                        getString(R.string.lbl_time_registration_actions_dialog_removing_time_registrations),
                        true,
                        false
                );
                break;
            }
            case Constants.Dialog.TIME_REGISTRATION_DELETE_RANGE_MIN_BOUNDARY: {
                // dialog code for min boundary
                Calendar temp = removeRangeMinBoundary;
                if (temp == null) {
                    temp = Calendar.getInstance();
                }
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        TimeRegistrationActionActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            public void onDateSet(DatePicker datePickerView
                                    , int year, int monthOfYear, int dayOfMonth) {
                                if (removeRangeMinBoundary == null) {
                                    removeRangeMinBoundary = Calendar.getInstance();
                                }

                                removeRangeMinBoundary.set(Calendar.YEAR, year);
                                removeRangeMinBoundary.set(Calendar.MONTH, monthOfYear);
                                removeRangeMinBoundary.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                updateDateRangeSelectionButtons();

                                if (removeRangeMaxBoundary != null && removeRangeMinBoundary != null &&
                                        removeRangeMinBoundary.after(removeRangeMaxBoundary)) {
                                    showDialog(Constants.Dialog.TIME_REGISTRATION_DELETE_RANGE_MAX_BOUNDARY);
                                }

                                removeDialog(Constants.Dialog.TIME_REGISTRATION_DELETE_RANGE_MIN_BOUNDARY);
                            }
                        },
                        temp.get(Calendar.YEAR),
                        temp.get(Calendar.MONTH),
                        temp.get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.setTitle(R.string.lbl_time_registration_actions_dialog_removing_time_registrations_range_selection_min_boundary_title);
                datePickerDialog.setButton2(getText(R.string.lbl_time_registration_actions_dialog_removing_time_registrations_range_clear_date), new DatePickerDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeRangeMinBoundary = null;
                        updateDateRangeSelectionButtons();
                        removeDialog(Constants.Dialog.TIME_REGISTRATION_DELETE_RANGE_MIN_BOUNDARY);
                    }
                });
                datePickerDialog.setButton3(getString(android.R.string.cancel), new DatePickerDialog.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeDialog(Constants.Dialog.TIME_REGISTRATION_DELETE_RANGE_MIN_BOUNDARY);
                    }
                });
                datePickerDialog.show();
                break;
            }
            case Constants.Dialog.TIME_REGISTRATION_DELETE_RANGE_MAX_BOUNDARY: {
                // dialog code for min boundary
                Calendar temp = removeRangeMaxBoundary;
                if (temp == null) {
                    temp = Calendar.getInstance();
                }
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        TimeRegistrationActionActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            public void onDateSet(DatePicker datePickerView
                                    , int year, int monthOfYear, int dayOfMonth) {
                                if (removeRangeMaxBoundary == null) {
                                    removeRangeMaxBoundary = Calendar.getInstance();
                                }

                                removeRangeMaxBoundary.set(Calendar.YEAR, year);
                                removeRangeMaxBoundary.set(Calendar.MONTH, monthOfYear);
                                removeRangeMaxBoundary.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                updateDateRangeSelectionButtons();

                                if (removeRangeMaxBoundary != null && removeRangeMinBoundary != null &&
                                        removeRangeMaxBoundary.before(removeRangeMinBoundary)) {
                                    showDialog(Constants.Dialog.TIME_REGISTRATION_DELETE_RANGE_MIN_BOUNDARY);
                                }

                                removeDialog(Constants.Dialog.TIME_REGISTRATION_DELETE_RANGE_MAX_BOUNDARY);
                            }
                        },
                        temp.get(Calendar.YEAR),
                        temp.get(Calendar.MONTH),
                        temp.get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.setTitle(R.string.lbl_time_registration_actions_dialog_removing_time_registrations_range_selection_max_boundary_title);
                datePickerDialog.setButton2(getText(R.string.lbl_time_registration_actions_dialog_removing_time_registrations_range_clear_date), new DatePickerDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeRangeMaxBoundary = null;
                        updateDateRangeSelectionButtons();
                        removeDialog(Constants.Dialog.TIME_REGISTRATION_DELETE_RANGE_MIN_BOUNDARY);
                    }
                });
                datePickerDialog.setButton3(getString(android.R.string.cancel), new DatePickerDialog.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeDialog(Constants.Dialog.TIME_REGISTRATION_DELETE_RANGE_MAX_BOUNDARY);
                    }
                });
                datePickerDialog.show();
                break;
            }
            case Constants.Dialog.TIME_REGISTRATION_DELETE_RANGE_BOUNDARY_PROBLEM: {
                AlertDialog.Builder alertDeleteBoundariesProblem = new AlertDialog.Builder(this);
                alertDeleteBoundariesProblem
                        .setMessage(R.string.msg_time_registration_actions_dialog_removing_time_registrations_range_boundary_problem)
                        .setCancelable(true)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                removeDialog(Constants.Dialog.TIME_REGISTRATION_DELETE_RANGE_BOUNDARY_PROBLEM);
                            }
                        });
                dialog = alertDeleteBoundariesProblem.create();
                break;
            }
        }
        ;
        return dialog;
    }

    private void updateDateRangeSelectionButtons() {
        if (deleteRangeFromButton != null && removeRangeMinBoundary != null)
            deleteRangeFromButton.setText(
                    DateUtils.DateTimeConverter.convertDateToString(removeRangeMinBoundary.getTime(), DateFormat.MEDIUM, TimeRegistrationActionActivity.this)
            );
        else if (deleteRangeFromButton != null)
            deleteRangeFromButton.setText(R.string.none);

        if (deleteRangeToButton != null && removeRangeMaxBoundary != null)
            deleteRangeToButton.setText(
                    DateUtils.DateTimeConverter.convertDateToString(removeRangeMaxBoundary.getTime(), DateFormat.MEDIUM, TimeRegistrationActionActivity.this)
            );
        else if (deleteRangeToButton != null)
            deleteRangeToButton.setText(R.string.none);
    }

    /**
     * Handles all the possible {@link TimeRegistrationAction}s.
     *
     * @param action          The actions to handle of type {@link TimeRegistrationAction}.
     */
    private void handleTimeRegistrationAction(TimeRegistrationAction action) {
        Log.i(getApplicationContext(), LOG_TAG, "Handling Time Registration action: " + action.toString());

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_time_registration_actions,
                (ViewGroup) findViewById(R.id.dialog_layout_root));
        final RadioButton deleteCurrentRadio = (RadioButton) layout.findViewById(R.id.tr_delete_current);
        final RadioButton deleteRangeRadio = (RadioButton) layout.findViewById(R.id.tr_delete_range);

        if (commentEditText == null) {
            commentEditText = (EditText) layout.findViewById(R.id.tr_comment);
        }

        switch (action) {
            case PUNCH_OUT:
            case PUNCH_OUT_AND_START_NEXT: {
                String comment = commentEditText.getText().toString();
                if (StringUtils.isNotBlank(comment)) {
                    Log.d(getApplicationContext(), LOG_TAG, "Time Registration will be saved with comment: " + comment);
                    timeRegistration.setComment(comment);
                    tracker.trackEvent(
                            TrackerConstants.EventSources.TIME_REGISTRATION_ACTION_ACTIVITY,
                            TrackerConstants.EventActions.ADD_TR_COMMENT
                    );
                }

                timeRegistration.setComment(comment);
                Intent intent = new Intent(this, TimeRegistrationPunchOutActivity.class);
                intent.putExtra(Constants.Extras.TIME_REGISTRATION, timeRegistration);
                intent.putExtra(Constants.Extras.TIME_REGISTRATION_CONTINUE_WITH_NEW, action == TimeRegistrationAction.PUNCH_OUT ? false : true);
                startActivityForResult(intent, Constants.IntentRequestCodes.TIME_REGISTRATION_EDIT);

                break;
            }
            case SPLIT: {
                Intent intent = new Intent(this, TimeRegistrationSplitActivity.class);
                intent.putExtra(Constants.Extras.TIME_REGISTRATION, timeRegistration);
                startActivityForResult(intent, Constants.IntentRequestCodes.TIME_REGISTRATION_EDIT);
                break;
            }
            case TIME_REGISTRATION_DETAILS: {
                TimeRegistration previousTimeRegistration = timeRegistrationService.getPreviousTimeRegistration(timeRegistration);
                timeRegistrationService.fullyInitialize(previousTimeRegistration);
                TimeRegistration nextTimeRegistration = timeRegistrationService.getNextTimeRegistration(timeRegistration);
                timeRegistrationService.fullyInitialize(nextTimeRegistration);

                Intent intent = new Intent(this, TimeRegistrationDetailActivity.class);
                intent.putExtra(Constants.Extras.TIME_REGISTRATION, timeRegistration);
                intent.putExtra(Constants.Extras.TIME_REGISTRATION_PREVIOUS, previousTimeRegistration);
                intent.putExtra(Constants.Extras.TIME_REGISTRATION_NEXT, nextTimeRegistration);
                startActivityForResult(intent, Constants.IntentRequestCodes.REGISTRATION_DETAILS);
                break;
            }
            case EDIT_STARTING_TIME: {
                TimeRegistration previousTimeRegistration = timeRegistrationService.getPreviousTimeRegistration(timeRegistration);
                timeRegistrationService.fullyInitialize(previousTimeRegistration);

                Intent intent = new Intent(this, TimeRegistrationEditStartTimeActivity.class);
                intent.putExtra(Constants.Extras.TIME_REGISTRATION, timeRegistration);
                intent.putExtra(Constants.Extras.TIME_REGISTRATION_PREVIOUS, previousTimeRegistration);
                startActivityForResult(intent, Constants.IntentRequestCodes.TIME_REGISTRATION_EDIT);
                break;
            }
            case EDIT_END_TIME: {
                TimeRegistration nextTimeRegistration = timeRegistrationService.getNextTimeRegistration(timeRegistration);
                timeRegistrationService.fullyInitialize(nextTimeRegistration);

                Intent intent = new Intent(this, TimeRegistrationEditEndTimeActivity.class);
                intent.putExtra(Constants.Extras.TIME_REGISTRATION, timeRegistration);
                intent.putExtra(Constants.Extras.TIME_REGISTRATION_NEXT, nextTimeRegistration);
                startActivityForResult(intent, Constants.IntentRequestCodes.TIME_REGISTRATION_EDIT);
                break;
            }
            case RESTART_TIME_REGISTRATION: {
                Intent intent = new Intent(this, TimeRegistrationRestartActivity.class);
                intent.putExtra(Constants.Extras.TIME_REGISTRATION, timeRegistration);
                startActivityForResult(intent, Constants.IntentRequestCodes.TIME_REGISTRATION_EDIT);

                break;
            }
            case EDIT_PROJECT_AND_TASK: {
                Intent intent = new Intent(this, TimeRegistrationEditProjectAndTaskActivity.class);
                intent.putExtra(Constants.Extras.TIME_REGISTRATION, timeRegistration);
                startActivityForResult(intent, Constants.IntentRequestCodes.TIME_REGISTRATION_EDIT);
                break;
            }
            case SET_COMMENT: {
                String comment = commentEditText.getText().toString();
                Intent intent = new Intent(this, TimeRegistrationSetCommentActivity.class);
                intent.putExtra(Constants.Extras.TIME_REGISTRATION, timeRegistration);
                intent.putExtra(Constants.Extras.TIME_REGISTRATION_COMMENT, comment);
                startActivityForResult(intent, Constants.IntentRequestCodes.TIME_REGISTRATION_EDIT);
                break;
            }
            case DELETE_TIME_REGISTRATION: {
                int checkedId = deleteRadioContainer.getCheckedRadioButtonId();

                switch (checkedId) {
                    case R.id.tr_delete_current: {
                        Log.d(getApplicationContext(), LOG_TAG, "Deleting current time registration");

                        Intent intent = new Intent(TimeRegistrationActionActivity.this, TimeRegistrationDeleteActivity.class);
                        intent.putExtra(Constants.Extras.TIME_REGISTRATION, timeRegistration);
                        startActivityForResult(intent, Constants.IntentRequestCodes.TIME_REGISTRATION_EDIT);
                        break;
                    }
                    case R.id.tr_delete_range: {
                        Log.d(getApplicationContext(), LOG_TAG, "Deleting all time registrations in range");
                        if (removeRangeMinBoundary.after(removeRangeMaxBoundary)) {
                            showDialog(Constants.Dialog.TIME_REGISTRATION_DELETE_RANGE_BOUNDARY_PROBLEM);
                        } else {
                            Intent intent = new Intent(TimeRegistrationActionActivity.this, TimeRegistrationDeleteActivity.class);
                            intent.putExtra(Constants.Extras.TIME_REGISTRATION_START_DATE, removeRangeMinBoundary != null ? removeRangeMinBoundary.getTime() : null);
                            intent.putExtra(Constants.Extras.TIME_REGISTRATION_END_DATE, removeRangeMaxBoundary != null ? removeRangeMaxBoundary.getTime() : null);
                            startActivityForResult(intent, Constants.IntentRequestCodes.TIME_REGISTRATION_EDIT);
                        }
                        break;
                    }
                }
                break;
            }
        }
    }

    /**
     * Builds the list adapter based on the allowed actions.
     *
     * @param allowedActions The allowed {@link TimeRegistrationAction}s retrieved by for a specific
     *                       {@link TimeRegistration} that can be retrieved using
     *                       {@link TimeRegistrationAction#getTimeRegistrationActions(eu.vranckaert.worktime.model.TimeRegistration)}.
     */
    private TimeRegistrationActionListAdapter getFilteredActionsAdapter(List<TimeRegistrationAction> allowedActions) {
        List<TimeRegistrationAction> allActions = Arrays.asList(TimeRegistrationAction.values());
        List<CharSequence> availableSpinnerItems = Arrays.asList(getResources().getTextArray(R.array.array_time_registration_actions_dialog_choose_action_spinner));

        List<Object> allowedElements = new ArrayList<Object>();
        for (TimeRegistrationAction action : allActions) {
            if (allowedActions.contains(action)) {
                allowedElements.add(availableSpinnerItems.get(action.getOriginalOrder()));
            }
        }

        return new TimeRegistrationActionListAdapter(TimeRegistrationActionActivity.this, allowedElements);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setResult(resultCode);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tracker.stopSession();
    }
}
