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

import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.OSContants;
import eu.vranckaert.worktime.model.TimeRegistration;
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
import eu.vranckaert.worktime.utils.view.actionbar.synclock.SyncLockedWizardActivity;
import org.joda.time.Duration;

import java.util.Calendar;
import java.util.Date;

/**
 * User: DIRK VRANCKAERT
 * Date: 07/12/11
 * Time: 07:17
 */
public class TimeRegistrationSplitActivity extends SyncLockedWizardActivity {
    private static final String LOG_TAG = TimeRegistrationSplitActivity.class.getSimpleName();

    @Inject
    private TimeRegistrationService trService;

    @Inject
    private WidgetService widgetService;

    @Inject
    private StatusBarNotificationService statusBarNotificationService;

    private TimeRegistration originalTimeRegistration;
    private int defaultSplitGap;

    private Calendar endPart1;
    private Calendar startPart2;

    private Calendar lowerLimitPart1;
    private Calendar higherLimitPart1;
    private Calendar lowerLimitPart2;
    private Calendar higherLimitPart2;

    private DatePicker datePicker;
    private TimePicker timePicker;

    private int[] layouts = {
            R.layout.activity_time_registration_split_wizard_1,
            R.layout.activity_time_registration_split_wizard_2,
            R.layout.activity_time_registration_split_wizard_3
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.lbl_registration_split_title);

        loadExtras();
        validateOriginalTimeRegistration();
        setInitialDataForTimeRegistrationParts();

        setContentViews(layouts);

        defaultSplitGap = Preferences.getTimeRegistrationSplitDefaultGap(TimeRegistrationSplitActivity.this);

        super.setFinishButtonText(R.string.save);
        setCancelDialog(R.string.lbl_registration_split_cancel_dialog, R.string.msg_registration_split_cancel_dialog);
    }

    /**
     * Load all the extra parameters needed.
     */
    private void loadExtras() {
        originalTimeRegistration = (TimeRegistration) getIntent().getExtras().get(Constants.Extras.TIME_REGISTRATION);
        Log.d(getApplicationContext(), LOG_TAG, "Received time registration " + originalTimeRegistration.getId());
    }

    /**
     * Validate the time registration that will be split. The minimum duration of the registration needs to be 2 minutes
     * or the registration cannot be split.
     */
    private void validateOriginalTimeRegistration() {
        Date endTime = originalTimeRegistration.isOngoingTimeRegistration() ? new Date() : originalTimeRegistration.getEndTime();
        Duration duration = DateUtils.TimeCalculator.calculateDuration(
                TimeRegistrationSplitActivity.this,
                originalTimeRegistration.getStartTime(),
                endTime
        );
        long durationMinutes = duration.getStandardMinutes();
        if (durationMinutes < 2L) {
            Log.e(getApplicationContext(), LOG_TAG, "The duration of the registration is less than 2 minutes so the registration cannot be split!");
            Toast.makeText(
                    TimeRegistrationSplitActivity.this,
                    R.string.lbl_registration_split_validation_original_time_registration,
                    Toast.LENGTH_LONG
            ).show();
            finish();
        }
    }

    /**
     * Set an initial split value. The validation rules specify that the end of part 1 and the start of part 2 may be
     * equal so we set them to the same value, which is the end of the original time registration minus 1 minute.
     */
    private void setInitialDataForTimeRegistrationParts() {
        Log.d(getApplicationContext(), LOG_TAG, "Setting initial data for the different parts");

        endPart1 = Calendar.getInstance();
        startPart2 = Calendar.getInstance();

        Date endTime = new Date();
        if (!originalTimeRegistration.isOngoingTimeRegistration()) {
            endTime = originalTimeRegistration.getEndTime();
        }
        Date middlePoint = DateUtils.TimeCalculator.calculateMiddle(originalTimeRegistration.getStartTime(), endTime);

        endPart1.setTime(middlePoint);
        Calendar defaultStartPart2 = Calendar.getInstance();
        defaultStartPart2.setTime(endTime);
        defaultStartPart2.set(Calendar.SECOND, 0);
        defaultStartPart2.set(Calendar.MILLISECOND, 0);
        defaultStartPart2.add(Calendar.MINUTE, -1);
        startPart2.setTime(defaultStartPart2.getTime());

        Log.d(getApplicationContext(), LOG_TAG, "The default value for the end of part 1 is: " +
                DateUtils.DateTimeConverter.convertDateTimeToString(endPart1.getTime(), DateFormat.MEDIUM, TimeFormat.SHORT, TimeRegistrationSplitActivity.this));
        Log.d(getApplicationContext(), LOG_TAG, "The default value for the start of part 2 is: " +
                DateUtils.DateTimeConverter.convertDateTimeToString(startPart2.getTime(), DateFormat.MEDIUM, TimeFormat.SHORT, TimeRegistrationSplitActivity.this));
    }

    /**
     * Calculates the lower and higher limits for both part 1 and part 2.
     */
    private void calculateLimits() {
        Log.d(getApplicationContext(), LOG_TAG, "Calculating limits for part 1");

        lowerLimitPart1 = Calendar.getInstance();
        lowerLimitPart1.setTime(originalTimeRegistration.getStartTime());
        lowerLimitPart1.set(Calendar.MILLISECOND, 0);
        lowerLimitPart1.set(Calendar.SECOND, 0);

        higherLimitPart1 = Calendar.getInstance();
        higherLimitPart1.setTime(startPart2.getTime());

        Log.d(getApplicationContext(), LOG_TAG, "Lower limit part 1: " + DateUtils.DateTimeConverter.convertDateTimeToString(lowerLimitPart1.getTime(), DateFormat.MEDIUM, TimeFormat.SHORT, TimeRegistrationSplitActivity.this));
        Log.d(getApplicationContext(), LOG_TAG, "Higher limit part 1: " + DateUtils.DateTimeConverter.convertDateTimeToString(higherLimitPart1.getTime(), DateFormat.MEDIUM, TimeFormat.SHORT, TimeRegistrationSplitActivity.this));

        Log.d(getApplicationContext(), LOG_TAG, "Calculating limits for part 2");

        lowerLimitPart2 = Calendar.getInstance();
        lowerLimitPart2.setTime(endPart1.getTime());

        higherLimitPart2 = Calendar.getInstance();
        Date originalEndTime = new Date();
        if (!originalTimeRegistration.isOngoingTimeRegistration()) {
            originalEndTime = originalTimeRegistration.getEndTime();
        }
        higherLimitPart2.setTime(originalEndTime);
        higherLimitPart2.set(Calendar.MILLISECOND, 0);
        higherLimitPart2.set(Calendar.SECOND, 0);

        Log.d(getApplicationContext(), LOG_TAG, "Lower limit part 2: " + DateUtils.DateTimeConverter.convertDateTimeToString(lowerLimitPart2.getTime(), DateFormat.MEDIUM, TimeFormat.SHORT, TimeRegistrationSplitActivity.this));
        Log.d(getApplicationContext(), LOG_TAG, "Higher limit part 2: " + DateUtils.DateTimeConverter.convertDateTimeToString(higherLimitPart2.getTime(), DateFormat.MEDIUM, TimeFormat.SHORT, TimeRegistrationSplitActivity.this));
    }

    @Override
    protected void initialize(View view) {
        initDateTimePicker(
                endPart1,
                R.id.time_registration_split_wizard_end_date,
                R.id.time_registration_split_wizard_end_time
        );
    }

    @Override
    public boolean beforePageChange(int currentViewIndex, int nextViewIndex, View view) {
        TextView errorTextView = (TextView) view.findViewById(R.id.time_registration_split_wizard_error);
        if (errorTextView != null) {
            errorTextView.setVisibility(View.GONE);
        }

        switch (currentViewIndex) {
            case 0: {
                calculateLimits();

                Calendar tmpPart = getCurrentDateTimePickerValue();
                boolean validationGreaterThan = validateGreaterThan(tmpPart, lowerLimitPart1);
                boolean validationLowerThanOrEqualsTo = validateLowerThanOrEqualsTo(tmpPart, higherLimitPart1);

                if (!validationGreaterThan) {
                    errorTextView.setText(
                        getString(R.string.lbl_registration_split_validation_greater_than,
                                DateUtils.DateTimeConverter.convertDateTimeToString(
                                        lowerLimitPart1.getTime(), DateFormat.MEDIUM, TimeFormat.SHORT, getApplicationContext()
                                )
                        )
                    );
                    errorTextView.setVisibility(View.VISIBLE);
                    return false;
                } else if (!validationLowerThanOrEqualsTo) {
                    errorTextView.setText(
                        getString(R.string.lbl_registration_split_validation_less_than_equal_to,
                                DateUtils.DateTimeConverter.convertDateTimeToString(
                                        higherLimitPart1.getTime(), DateFormat.MEDIUM, TimeFormat.SHORT, getApplicationContext()
                                )
                        )
                    );
                    errorTextView.setVisibility(View.VISIBLE);
                    return false;
                }

                endPart1 = tmpPart;

                // Add a default gap of x minutes between the two parts
                startPart2.setTimeInMillis(endPart1.getTimeInMillis());
                startPart2.add(Calendar.MINUTE, defaultSplitGap);

                break;
            }
            case 1: {
                if (nextViewIndex < currentViewIndex) {
                    return true;
                }

                calculateLimits();

                Calendar tmpPart = getCurrentDateTimePickerValue();
                boolean validationGreaterThanOrEqualsTo = validateGreaterThanOrEqualsTo(tmpPart, lowerLimitPart2);
                boolean validationLowerThan = validateLowerThan(tmpPart, higherLimitPart2);

                if (!validationGreaterThanOrEqualsTo) {
                    errorTextView.setText(
                        getString(R.string.lbl_registration_split_validation_greater_than_equal_to,
                                DateUtils.DateTimeConverter.convertDateTimeToString(
                                        lowerLimitPart2.getTime(), DateFormat.MEDIUM, TimeFormat.SHORT, getApplicationContext()
                                )
                        )
                    );
                    errorTextView.setVisibility(View.VISIBLE);
                    return false;
                } else if (!validationLowerThan) {
                    errorTextView.setText(
                        getString(R.string.lbl_registration_split_validation_less_than,
                                DateUtils.DateTimeConverter.convertDateTimeToString(
                                        higherLimitPart2.getTime(), DateFormat.MEDIUM, TimeFormat.SHORT, getApplicationContext()
                                )
                        )
                    );
                    errorTextView.setVisibility(View.VISIBLE);
                    return false;
                }

                startPart2 = tmpPart;
                break;
            }
        }
        return true;
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
     * Store the current values in the date and time picker.
     * @return A {@link Calendar} instance with the current values of the date and time picker.
     */
    private Calendar getCurrentDateTimePickerValue() {
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
    protected void afterPageChange(int currentViewIndex, int previousViewIndex, View view) {
        switch (currentViewIndex) {
            case 0:
                initDateTimePicker(
                        endPart1,
                        R.id.time_registration_split_wizard_end_date,
                        R.id.time_registration_split_wizard_end_time
                );
                break;
            case 1:
                initDateTimePicker(
                        startPart2,
                        R.id.time_registration_split_wizard_start_date,
                        R.id.time_registration_split_wizard_start_time
                );
                break;
            case 2:
                TextView tr1Start = (TextView) findViewById(R.id.time_registration_split_tr1_start);
                TextView tr1End = (TextView) findViewById(R.id.time_registration_split_tr1_end);
                TextView tr1Duration = (TextView) findViewById(R.id.time_registration_split_tr1_duration);

                TextView tr2Start = (TextView) findViewById(R.id.time_registration_split_tr2_start);
                TextView tr2End = (TextView) findViewById(R.id.time_registration_split_tr2_end);
                TextView tr2Duration = (TextView) findViewById(R.id.time_registration_split_tr2_duration);

                TextView gap = (TextView) findViewById(R.id.time_registration_split_gap);

                tr1Start.setText(
                        DateUtils.DateTimeConverter.convertDateTimeToString(
                                originalTimeRegistration.getStartTime(),
                                DateFormat.MEDIUM,
                                TimeFormat.SHORT,
                                TimeRegistrationSplitActivity.this
                        )
                );
                tr1End.setText(
                        DateUtils.DateTimeConverter.convertDateTimeToString(
                                endPart1.getTime(),
                                DateFormat.MEDIUM,
                                TimeFormat.SHORT,
                                TimeRegistrationSplitActivity.this
                        )
                );

                tr2Start.setText(
                        DateUtils.DateTimeConverter.convertDateTimeToString(
                                startPart2.getTime(),
                                DateFormat.MEDIUM,
                                TimeFormat.SHORT,
                                TimeRegistrationSplitActivity.this
                        )
                );
                Date endTime = new Date();
                if (!originalTimeRegistration.isOngoingTimeRegistration()) {
                    endTime = originalTimeRegistration.getEndTime();
                }
                tr2End.setText(
                        DateUtils.DateTimeConverter.convertDateTimeToString(
                                endTime,
                                DateFormat.MEDIUM,
                                TimeFormat.SHORT,
                                TimeRegistrationSplitActivity.this
                        )
                );

                // Duration of TR1
                TimeRegistration tmpTr = new TimeRegistration();
                tmpTr.setStartTime(originalTimeRegistration.getStartTime());
                tmpTr.setEndTime(endPart1.getTime());
                tr1Duration.setText(
                        DateUtils.TimeCalculator.calculatePeriod(TimeRegistrationSplitActivity.this, tmpTr, false)
                );
                // Duration of TR2
                tmpTr.setStartTime(startPart2.getTime());
                tmpTr.setEndTime(originalTimeRegistration.getEndTime());
                tr2Duration.setText(
                        DateUtils.TimeCalculator.calculatePeriod(TimeRegistrationSplitActivity.this, tmpTr, false)
                );
                // Duration between TR1 and TR2 (= GAP)
                tmpTr.setStartTime(endPart1.getTime());
                tmpTr.setEndTime(startPart2.getTime());
                gap.setText(
                        DateUtils.TimeCalculator.calculatePeriod(TimeRegistrationSplitActivity.this, tmpTr, false)
                );
                break;
        }
    }

    @Override
    protected boolean onCancel(View view, View button) { return true; }

    @Override
    protected boolean onFinish(View view, View button) {
        TimeRegistration part1 = createTimeRegistrationForPart(originalTimeRegistration, originalTimeRegistration.getStartTime(), endPart1.getTime());
        TimeRegistration part2 = createTimeRegistrationForPart(originalTimeRegistration, startPart2.getTime(), originalTimeRegistration.getEndTime());

        // Make sure the id and sync key are removed from part 2 as this will be seen as the new TR.
        part2.clearSensitiveData();

        trService.update(part1);
        trService.create(part2);

        widgetService.updateAllWidgets();
        statusBarNotificationService.addOrUpdateNotification(part2);

        return true;
    }

    private TimeRegistration createTimeRegistrationForPart(TimeRegistration timeRegistrationBase, Date startTime, Date endTime) {
        TimeRegistration timeRegistration = timeRegistrationBase.duplicate();
        timeRegistration.setStartTime(startTime);
        timeRegistration.setEndTime(endTime);
        return timeRegistration;
    }

    /**
     * Initialize the date and time picker for a certain {@link Calendar}.
     * @param part The {@link Calendar} instance to set on the date and time picker.
     * @param datePickerId The resource id referencing an {@link DatePicker}.
     * @param timePickerId The resource id referencing an {@link TimePicker}.
     */
    private void initDateTimePicker(Calendar part, int datePickerId, int timePickerId) {
        datePicker = (DatePicker) findViewById(datePickerId);
        timePicker = (TimePicker) findViewById(timePickerId);

        datePicker.init(part.get(Calendar.YEAR), part.get(Calendar.MONTH), part.get(Calendar.DAY_OF_MONTH), null);
        if (ContextUtils.getAndroidApiVersion() >= OSContants.API.HONEYCOMB_3_2) {
            datePicker.setMaxDate((new Date()).getTime());
            datePicker.setCalendarViewShown(true);
            datePicker.setSpinnersShown(false);
        }

        HourPreference12Or24 preference12or24Hours = Preferences.getDisplayHour1224Format(TimeRegistrationSplitActivity.this);
        timePicker.setIs24HourView(preference12or24Hours.equals(HourPreference12Or24.HOURS_24)?true:false);
        timePicker.setCurrentHour(part.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(part.get(Calendar.MINUTE));
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
