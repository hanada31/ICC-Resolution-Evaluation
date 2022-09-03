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

package eu.vranckaert.worktime.utils.date;

import android.content.Context;
import android.util.SparseArray;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.enums.reporting.ReportingDisplayDuration;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.utils.context.ContextUtils;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.preferences.Preferences;
import eu.vranckaert.worktime.utils.preferences.TimePrecisionPreference;
import eu.vranckaert.worktime.utils.string.StringUtils;
import org.joda.time.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Date utils.
 * @author Dirk Vranckaert
 */
public class DateUtils {
    private static final String LOG_TAG = DateUtils.class.getSimpleName();

    /**
     * Contains all methods that will convert date and/or time to string.
     */
    public static class DateTimeConverter {
        /**
         * Converts a date to an entire date-time-string based on the users locale in the context.
         * @param date The date to convert.
         * @param dateFormat The date format to use.
         * @param timeFormat The time format to use.
         * @param context The context in which the locale is stored.
         * @return The formatted string.
         */
        public static final String convertDateTimeToString(Date date, DateFormat dateFormat, TimeFormat timeFormat, Context context) {
            Locale locale = ContextUtils.getCurrentLocale(context);
            return convertDateToString(date, dateFormat, locale)
                    + " "
                    + convertTimeToString(date, timeFormat, context);
        }
        
        /**
         * Converts a certain date to a date-string based on the users locale in the context.
         * @param date The date to convert.
         * @param format The date format to use.
         * @param context The context in which the locale is stored.
         * @return The formatted string.
         */
        public static final String convertDateToString(Date date, DateFormat format, Context context) {
            Locale locale = ContextUtils.getCurrentLocale(context);
            return convertDateToString(date, format, locale);
        }

        /**
         * Converts a certain date to a date-string based on the users locale.
         * @param date The date to convert.
         * @param format The date format to use.
         * @param locale The users locale.
         * @return The formatted string.
         */
        public static final String convertDateToString(Date date, DateFormat format, Locale locale) {
            java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance(format.getStyle(), locale);
            return dateFormat.format(date);
        }

        /**
         * Converts a certain date to a time-string based on the users locale in the context.
         * @param date The date to convert.
         * @param format The date format to use.
         * @return The formatted string.
         */
        public static final String convertTimeToString(Date date, TimeFormat format, Context context) {
            HourPreference12Or24 preference = Preferences.getDisplayHour1224Format(context);
            return convertTimeToString(context, date, format, preference);
        }

        /**
         * Converts a certain date to a time-string based on the users locale.
         * @param ctx The context.
         * @param date The date to convert.
         * @param format The date format to use.
         * @param preference The hour preference, 12-hour or 24-hours based.
         * @return The formatted string.
         */
        public static final String convertTimeToString(Context ctx, Date date, TimeFormat format, HourPreference12Or24 preference) {
            String separator = ":";
            String seconds = "ss";
            String minutes = "mm";
            String hours24 = "HH";
            String hours12 = "hh";
            String amPmMarker = " a";

            String dateFormat = null;

            switch(format) {
                case MEDIUM: {
                    TimePrecisionPreference timePrecisionPreference = Preferences.getTimePrecision(ctx);
                    switch (timePrecisionPreference) {
                        case SECOND: {
                            dateFormat = separator + minutes + separator + seconds;
                            break;
                        }
                        case MINUTE: {
                            dateFormat = separator + minutes;
                            break;
                        }
                    }
                    break;
                }
                case SHORT: {
                    dateFormat = separator + minutes;
                    break;
                }
            }

            switch(preference) {
                case HOURS_12: {
                    dateFormat = hours12 + dateFormat + amPmMarker;
                    break;
                }
                case HOURS_24: {
                    dateFormat = hours24 + dateFormat;
                    break;
                }
            }

            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            return sdf.format(date);
        }

        /**
         * Convert a date to a string including the year, month and day.
         * @param date The {@link java.util.Date} to convert.
         * @return The converted date in year, month and day.
         */
        private static String getYearMonthDayAsString(Date date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            String yearMonthDay = "";
            int year = cal.get(Calendar.YEAR);
            int month = (cal.get(Calendar.MONTH) + 1);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            yearMonthDay += year;
            if (month < 10) {
                yearMonthDay += "0";
            }
            yearMonthDay += month;
            if (day < 10) {
                yearMonthDay += "0";
            }
            yearMonthDay += day;

            return yearMonthDay;
        }

        /**
         * Convert a date to a string including the hour, minute and seconds.
         * @param date The {@link java.util.Date} to convert.
         * @return The converted date in hours, minutes and seconds.
         */
        private static String getHourMinuteSecondAsString(Date date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            String hourMinuteSeconds = "";
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            int second = cal.get(Calendar.SECOND);

            if (hour < 10) {
                hourMinuteSeconds += "0";
            }
            hourMinuteSeconds += hour;
            if (minute < 10) {
                hourMinuteSeconds += "0";
            }
            hourMinuteSeconds += minute;
            if (second < 10) {
                hourMinuteSeconds += "0";
            }
            hourMinuteSeconds += second;

            return hourMinuteSeconds;
        }

        /**
         * Builds a string with a date time representation that can be used to make something unique based on the current
         * time.
         * @return A string with the date and time representation like 20110912231845-PM. The format is:<br/>
         *         YYYYMMDDHHMMSS-AMPM<br/>
         *         YYYY = four number representing the year
         *         MM = two numbers representing the month (1-based)
         *         DD = two numbers representing the day of the month (1-based)
         *         HH = two numbers representing the hour
         *         MM = two numbers representing the minute
         *         SS = two numbers representing the seconds
         *         AMPM = 1 number represting AM or PM (AM = 0, PM = 1)
         */
        public static String getUniqueTimestampString() {
            Date date = new Date();

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            return getYearMonthDayAsString(date)
                    + getHourMinuteSecondAsString(date)
                    + "-" + cal.get(Calendar.AM_PM);
        }

        public static String convertToDatabaseFormat(Date date) {
            //2013-01-11 13:23:19.000280
            java.text.DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
            String result = df.format(date);
            return result;
        }
    }

    /**
     * Contains all methods that calculates with time.
     */
    public static class TimeCalculator {
        /**
         * Calculates the time ({@link org.joda.time.Interval}) between two dates. If the startDate is not before the
         * endDate the dates will be swapped. The preferred time precision will also be applied on the start and end
         * date (so milliseconds and seconds can be set to zero).
         * @param ctx The context.
         * @param startDate The start date for the interval.
         * @param endDate The ending date for the interval.
         * @return The {@link org.joda.time.Interval} between the two dates.
         */
        public static final Interval calculateInterval(Context ctx, Date startDate, Date endDate) {
            Calendar start = TimePrecision.applyTimePrecisionCalendar(ctx, startDate);

            Calendar end = TimePrecision.applyTimePrecisionCalendar(ctx, endDate);

            if(end.before(start)) {
                Calendar swap = start;
                start = end;
                end = swap;
            }

            Interval interval = new Interval(start.getTime().getTime(), end.getTime().getTime());

            return interval;
        }

        /**
         * Calculates the time ({@link org.joda.time.Interval}) between two dates. If the startDate is not before the
         * endDate the dates will be swapped. No time precision will be applied so the interval will be calculated
         * on the exact dates specified.
         * @param ctx The context.
         * @param startDate The start date for the interval.
         * @param endDate The ending date for the interval.
         * @return The {@link org.joda.time.Interval} between the two dates.
         */
        public static final Interval calculateExactInterval(Context ctx, Date startDate, Date endDate) {
            Calendar start = Calendar.getInstance();
            start.setTime(startDate);

            Calendar end = Calendar.getInstance();
            end.setTime(endDate);

            if(end.before(start)) {
                Calendar swap = start;
                start = end;
                end = swap;
            }

            Interval interval = new Interval(start.getTime().getTime(), end.getTime().getTime());

            return interval;
        }

        /**
         * Calculates the time ({@link org.joda.time.Duration}) between two dates. If the startDate is not before the
         * endDate the dates will be swapped. Time prevision will be applied on the the start and end dates (so
         * milliseconds and seconds can be set to zero).
         * @param ctx The context.
         * @param startDate The start date for the interval.
         * @param endDate The ending date for the interval.
         * @return The {@link org.joda.time.Duration} between the two dates.
         */
        public static final Duration calculateDuration(Context ctx, Date startDate, Date endDate) {
            Interval interval = calculateInterval(ctx, startDate, endDate);
            Duration duration = interval.toDuration();
            return duration;
        }

        /**
         * Calculates the time ({@link org.joda.time.Duration}) between two dates. If the startDate is not before the
         * endDate the dates will be swapped.
         * @param ctx The context.
         * @param startDate The start date for the interval.
         * @param endDate The ending date for the interval.
         * @return The {@link org.joda.time.Duration} between the two dates.
         */
        public static final Duration calculateExactDuration(Context ctx, Date startDate, Date endDate) {
            Interval interval = calculateExactInterval(ctx, startDate, endDate);
            Duration duration = interval.toDuration();
            return duration;
        }

        /**
         * Calculates the time ({@link org.joda.time.Period}) between two dates. If the startDate is not before the endDate the dates
         * will be swapped.
         * @param ctx The context.
         * @param startDate The start date for the interval.
         * @param endDate The ending date for the interval.
         * @param periodType The type of period ({@link org.joda.time.PeriodType}) to return.
         * @return The {@link org.joda.time.Period} between the two dates.
         */
        public static final Period calculatePeriod(Context ctx, Date startDate, Date endDate, PeriodType periodType) {
            Interval interval = calculateInterval(ctx, startDate, endDate);
            Period period = interval.toPeriod(periodType);
            return period;
        }

        /**
         * Calculates the time between the start and end time of a {@link eu.vranckaert.worktime.model.TimeRegistration}
         * in hours, minutes and seconds (long-text: "5 hours, 36 minutes, 33 seconds")
         * @param ctx The context.
         * @param registration The {@link eu.vranckaert.worktime.model.TimeRegistration} instance.
         * @param shortNotation If the notation should be short or long. For a short notations minutes will be like 'm',
         *                      for a long notation it will be like 'minutes'.
         * @return The formatted string that represents the time between start and end time.
         */
        public static final String calculatePeriod(Context ctx, TimeRegistration registration, boolean shortNotation) {
            Period period = null;
            if (registration.isOngoingTimeRegistration()) {
                period = calculatePeriod(ctx, registration.getStartTime(), new Date(), PeriodType.time());
            } else {
                period = calculatePeriod(ctx, registration.getStartTime(), registration.getEndTime(), PeriodType.time());
            }

            int hours = period.getHours();
            int minutes = period.getMinutes();
            int seconds = period.getSeconds();

            String hoursString = "";
            String minutesString = "";
            String secondsString = "";
            String periodString = "";

            String hoursRes = " " + ctx.getString(R.string.hours);
            String minuteRes = " " + ctx.getString(R.string.minutes);
            String secondRes = " " + ctx.getString(R.string.seconds);
            String seperatorRes = ", ";

            if (shortNotation) {
                hoursRes = ctx.getString(R.string.hoursShort);
                minuteRes = ctx.getString(R.string.minutesShort);
                secondRes = ctx.getString(R.string.secondsShort);
                seperatorRes = " ";
            }
            
            TimePrecisionPreference preference = Preferences.getTimePrecision(ctx);
            switch (preference) {
                case SECOND: {
                    hoursString = hours + hoursRes + seperatorRes;
                    minutesString = minutes + minuteRes + seperatorRes;
                    secondsString = seconds + secondRes;
                    if (hours > 0) {
                        periodString = hoursString + minutesString + secondsString;
                    } else if (minutes > 0) {
                        periodString = minutesString + secondsString;
                    } else {
                        periodString = secondsString;
                    }
                    break;
                }
                case MINUTE: {
                    hoursString = hours + hoursRes + seperatorRes;
                    minutesString = minutes + minuteRes;
                    if (hours > 0) {
                        periodString = hoursString + minutesString;
                    } else {
                        periodString = minutesString;
                    }
                    break;
                }
            }
            return periodString;
        }

        /**
         * Calculates the time between the start and end time of a list of
         * {@link eu.vranckaert.worktime.model.TimeRegistration} instances.
         * @param ctx           The context.
         * @param registrations A list of {@link eu.vranckaert.worktime.model.TimeRegistration} instances.
         * @return The {@link Period} instance that represents the time between the first and last time registration of
         *         the list.
         */
        public static final Period calculatePeriod(Context ctx, List<TimeRegistration> registrations) {
            Long duration = 0L;

            Log.d(ctx, LOG_TAG, "Calculating period for " + registrations.size() + " TR's...");
            for (TimeRegistration registration : registrations) {
                Duration regDuration = null;
                if (registration.isOngoingTimeRegistration()) {
                    regDuration = calculateDuration(ctx, registration.getStartTime(), new Date());
                } else {
                    regDuration = calculateDuration(ctx, registration.getStartTime(), registration.getEndTime());
                }
                Log.d(ctx, LOG_TAG, "Calculated duration: " + regDuration);
                Log.d(ctx, LOG_TAG, "About to add milis: " + regDuration.getMillis());
                duration += regDuration.getMillis();
                Log.d(ctx, LOG_TAG, "Total duration with new calcuation added: " + duration);
            }
            Log.d(ctx, LOG_TAG, "Total duration calculated: " + duration);
            Duration totalDuration = new Duration(duration);
            Log.d(ctx, LOG_TAG, "Total duration created from milis: " + totalDuration);
            Period period = totalDuration.toPeriod(PeriodType.time());
            Log.d(ctx, LOG_TAG,  "Total period: " + period);
            return period;
        }

        /**
         * Calculates the time between the start and end time of a list of {@link eu.vranckaert.worktime.model.TimeRegistration} instances in days,
         * hours, minutes and seconds (short-text: 1d 4h 13m 0s)
         *
         * @param ctx The context.
         * @param registrations A list of {@link eu.vranckaert.worktime.model.TimeRegistration} instances.
         * @param displayDuration The format that defines the output.
         * @return The formatted string that represents the sum of the duration for each {@link eu.vranckaert.worktime.model.TimeRegistration}.
         */
        public static final String calculatePeriod(Context ctx, List<TimeRegistration> registrations, ReportingDisplayDuration displayDuration) {
            Period period = calculatePeriod(ctx, registrations);

            int days = 0;
            int hours = period.getHours();
            int minutes = period.getMinutes();
            int seconds = period.getSeconds();

            switch (displayDuration) {
                case DAYS_HOUR_MINUTES_SECONDS_24H: {
                    days = hours/24;
                    if (days > 0) {
                        hours = hours - (days * 24);
                    }
                    break;
                }
                case DAYS_HOUR_MINUTES_SECONDS_08H: {
                    days = hours/8;
                    if (days > 0) {
                        hours = hours - (days * 8);
                    }
                    break;
                }
            }

            Log.d(ctx, LOG_TAG, days + "d " + hours + "h " + minutes + "m " + seconds + "s");

            String daysString = "";
            String hoursString = "";
            String minutesString = "";
            String secondsString = "";
            String periodString = "";

            TimePrecisionPreference preference = Preferences.getTimePrecision(ctx);
            switch (preference) {
                case SECOND: {
                    daysString = StringUtils.leftPad(String.valueOf(days), "0", 2) + ctx.getString(R.string.daysShort) + " ";
                    hoursString = StringUtils.leftPad(String.valueOf(hours), "0", 2) + ctx.getString(R.string.hoursShort) + " ";
                    minutesString = StringUtils.leftPad(String.valueOf(minutes), "0", 2) + ctx.getString(R.string.minutesShort) + " ";
                    secondsString = StringUtils.leftPad(String.valueOf(seconds), "0", 2) + ctx.getString(R.string.secondsShort);
                    if (days > 0) {
                        periodString = daysString + hoursString + minutesString + secondsString;
                    } else if (hours > 0) {
                        periodString = hoursString + minutesString + secondsString;
                    } else if (minutes > 0) {
                        periodString = minutesString + secondsString;
                    } else {
                        periodString = secondsString;
                    }
                    break;
                }
                case MINUTE: {
                    daysString = StringUtils.leftPad(String.valueOf(days), "0", 2) + ctx.getString(R.string.daysShort) + " ";
                    hoursString = StringUtils.leftPad(String.valueOf(hours), "0", 2) + ctx.getString(R.string.hoursShort) + " ";
                    minutesString = StringUtils.leftPad(String.valueOf(minutes), "0", 2) + ctx.getString(R.string.minutesShort) + " ";
                    if (days > 0) {
                        periodString = daysString + hoursString + minutesString;
                    } else if (hours > 0) {
                        periodString = hoursString + minutesString;
                    } else {
                        periodString = minutesString;
                    }
                    break;
                }
            }

            periodString = periodString.trim();

            return periodString;
        }

        /**
         * Calculate the duration between two times and return it in a well represented text format.
         * @param ctx The context.
         * @param startDate The start date for the interval.
         * @param endDate The end date for the interval.
         * @param periodType The type of period (year-month-days-time or just time etc).
         * @return A well formatted text containing the duration between the two times.
         */
        public static final String calculateDuration(Context ctx, Date startDate, Date endDate, PeriodType periodType) {
            Calendar start = Calendar.getInstance();
            start.setTime(startDate);

            Calendar end = Calendar.getInstance();
            end.setTime(endDate);

            if(end.before(start)) {
                Calendar swap = start;
                start = end;
                end = swap;
            }

            Interval interval = new Interval(start.getTime().getTime(), end.getTime().getTime());
            Duration duration = interval.toDuration();
            Period period = duration.toPeriod(periodType);

            int years = period.getYears();
            int months = period.getMonths();
            int days = period.getDays();
            int hours = period.getHours();
            int minutes = period.getMinutes();
            int seconds = period.getSeconds();

            Log.d(ctx, LOG_TAG, years + "y " + months + "m " + days + "d " + hours + "h " + minutes + "m " + seconds + "s");

            String yearsString = "";
            String monthsString = "";
            String daysString = "";
            String hoursString = "";
            String minutesString = "";
            String secondsString = "";

            String periodString = "";

            PeriodType.time();
            PeriodType.dayTime();
            PeriodType.yearMonthDay();
            PeriodType.yearMonthDayTime();

            int startValue = 0;

            if (periodType == PeriodType.time()) {
                hoursString = StringUtils.leftPad(String.valueOf(hours), "0", 2) + ctx.getString(R.string.hoursShort) + " ";
                minutesString = StringUtils.leftPad(String.valueOf(minutes), "0", 2) + ctx.getString(R.string.minutesShort) + " ";
                secondsString = StringUtils.leftPad(String.valueOf(seconds), "0", 2) + ctx.getString(R.string.secondsShort) + " ";

                if (hours > 0) {
                    startValue = hours;
                    periodString = hoursString + " " + minutesString + " " + secondsString;
                } else if (minutes > 0) {
                    startValue = minutes;
                    periodString = minutesString + " " + secondsString;
                } else {
                    startValue = seconds;
                    periodString = secondsString;
                }
            } else if (periodType == PeriodType.dayTime()) {
                daysString = StringUtils.leftPad(String.valueOf(days), "0", 2) + ctx.getString(R.string.daysShort) + " ";
                hoursString = StringUtils.leftPad(String.valueOf(hours), "0", 2) + ctx.getString(R.string.hoursShort) + " ";
                minutesString = StringUtils.leftPad(String.valueOf(minutes), "0", 2) + ctx.getString(R.string.minutesShort) + " ";

                if (days > 0) {
                    startValue = days;
                    periodString = daysString +  " " + hoursString + " " + minutesString;
                } else if (hours > 0) {
                    startValue = hours;
                    periodString = hoursString + " " + minutesString;
                } else {
                    startValue = minutes;
                    periodString = minutesString;
                }
            } else if (periodType == PeriodType.yearMonthDay()) {
                yearsString = StringUtils.leftPad(String.valueOf(years), "0", 2) + ctx.getString(R.string.yearsShort) + " ";
                monthsString = StringUtils.leftPad(String.valueOf(months), "0", 2) + ctx.getString(R.string.monthsShort) + " ";
                daysString = StringUtils.leftPad(String.valueOf(days), "0", 2) + ctx.getString(R.string.daysShort) + " ";

                if (years > 0) {
                    startValue = years;
                    periodString = yearsString + " " + monthsString + " " + daysString;
                } else if (months > 0) {
                    startValue = months;
                    periodString = monthsString + " " + daysString;
                } else {
                    startValue = days;
                    periodString = daysString;
                }
            } else if (periodType == PeriodType.yearMonthDayTime()) {
                yearsString = StringUtils.leftPad(String.valueOf(years), "0", 2) + ctx.getString(R.string.yearsShort) + " ";
                monthsString = StringUtils.leftPad(String.valueOf(months), "0", 2) + ctx.getString(R.string.monthsShort) + " ";
                daysString = StringUtils.leftPad(String.valueOf(days), "0", 2) + ctx.getString(R.string.daysShort) + " ";
                hoursString = StringUtils.leftPad(String.valueOf(hours), "0", 2) + ctx.getString(R.string.hoursShort) + " ";
                minutesString = StringUtils.leftPad(String.valueOf(minutes), "0", 2) + ctx.getString(R.string.minutesShort) + " ";
                secondsString = StringUtils.leftPad(String.valueOf(seconds), "0", 2) + ctx.getString(R.string.secondsShort) + " ";

                if (years > 0) {
                    startValue = years;
                    periodString = yearsString + " " + monthsString + " " + daysString + " " + hoursString + " " + minutesString + " " + secondsString;
                } else if (months > 0) {
                    startValue = months;
                    periodString = monthsString + " " + daysString + " " + hoursString + " " + minutesString + " " + secondsString;
                } else if (days > 0) {
                    startValue = days;
                    periodString = daysString + " " + hoursString + " " + minutesString + " " + secondsString;
                } else if (hours > 0) {
                    startValue = hours;
                    periodString = hoursString + " " + minutesString + " " + secondsString;
                } else if (minutes > 0) {
                    startValue = minutes;
                    periodString = minutesString + " " + secondsString;
                } else {
                    startValue = seconds;
                    periodString = secondsString;
                }
            }

            if (startValue < 10) {
                periodString = periodString.substring(1);
            }

            periodString = periodString.trim();

            return periodString;
        }

        /**
         * Calculates the boundaries of a week (the date the week is starting and ending on), based on the week
         * difference that is provided. If zero it will be the current week boundaries. The week difference can be zero,
         * negative or positive. A week difference of -1 for example will calculate the week boundaries for last week.
         * The method will take into account the preference for what day the week should start on.
         * @param weekDiff The week difference to calculate the boundaries from.
         * @param ctx The context.
         * @return A map containing two keys: {@link DateConstants#FIRST_DAY_OF_WEEK) and
         * {@link DateConstants#LAST_DAY_OF_WEEK).
         */
        public static SparseArray<Date> calculateWeekBoundaries(final int weekDiff, final Context ctx) {
            return calculateWeekBoundaries(weekDiff, new Date(), ctx);
        }

        /**
         * Calculates the boundaries of a week (the date the week is starting and ending on), based on the week
         * difference that is provided. If zero it will be the week boundaries of the specified date. The week
         * difference can be zero, negative or positive. A week difference of -1 for example will calculate the week
         * boundaries for the week before the specified date. The method will take into account the preference for what
         * day the week should start on.
         * @param weekDiff The week difference to calculate the boundaries from.
         * @param date The date to start calculating from.
         * @param ctx The context.
         * @return A {@link SparseArray} containing two keys: {@link DateConstants#FIRST_DAY_OF_WEEK) and
         * {@link DateConstants#LAST_DAY_OF_WEEK).
         */
        public static SparseArray<Date> calculateWeekBoundaries(final int weekDiff, final Date date, final Context ctx) {
            int weekStartsOn = Preferences.getWeekStartsOn(ctx);

            Date dateWithWeeks = addWeeksToDate(date, weekDiff);
            
            LocalDate startDate = new LocalDate(dateWithWeeks);
            LocalDate firstDayOfWeek = new LocalDate(dateWithWeeks);

            firstDayOfWeek = firstDayOfWeek.withDayOfWeek(weekStartsOn);
            if (firstDayOfWeek.isAfter(startDate)) {
                DateTimeFieldType weekOfWeekyear = DateTimeFieldType.weekOfWeekyear();
                int weekOfYear = firstDayOfWeek.get(weekOfWeekyear);
                int newWeekOfYear = weekOfYear - 1;
                if (newWeekOfYear <= 0) {
                    newWeekOfYear = 52 - newWeekOfYear;
                    firstDayOfWeek = firstDayOfWeek.withYear(firstDayOfWeek.getYear()-1);
                }
                firstDayOfWeek = firstDayOfWeek.withWeekOfWeekyear(newWeekOfYear);
                firstDayOfWeek = firstDayOfWeek.withDayOfWeek(weekStartsOn);
            }

            Calendar lastDayOfWeek = Calendar.getInstance();
            lastDayOfWeek.setTime(addWeeksToDate(firstDayOfWeek.toDate(), 1));
            lastDayOfWeek.add(Calendar.DAY_OF_YEAR, -1);

            SparseArray<Date> result = new SparseArray<Date>();

            result.put(DateConstants.FIRST_DAY_OF_WEEK, firstDayOfWeek.toDate());
            result.put(DateConstants.LAST_DAY_OF_WEEK, lastDayOfWeek.getTime());

            return result;
        }

        /**
         * Add an amount of weeks to a certain date. If amount of weeks to add is negative it will be subtracted.
         * @param date The {@link Date} instance to calculate on.
         * @param weekDiff The number of weeks to add to the provided date.
         * @return The {@link Date} with the weeks added or subtracted.
         */
        private static Date addWeeksToDate(Date date, int weekDiff) {
            int daysInOneWeek = Calendar.getInstance().getMaximum(Calendar.DAY_OF_WEEK);
            int daysDiff = weekDiff * daysInOneWeek;
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DAY_OF_YEAR, daysDiff);

            return cal.getTime();
        }

        /**
         * Calculates the point in time between two given dates. The first argument should be before the second, if not
         * they will be swapped!
         * @param date1 The first date.
         * @param date2 The second date.
         * @return The point in time that represents the middle of the two given dates.
         */
        public static Date calculateMiddle(Date date1, Date date2) {
            if (date2.before(date1)) {
                Date tmp = date1;
                date1 = date2;
                date2 = tmp;
            }
            long timeInMillis = date1.getTime() + date2.getTime();
            long middle = timeInMillis / 2;
            Date middleDate = new Date(middle);
            return middleDate;
        }
    }

    /**
     * Contains all system specific methods.
     */
    public static class System {
        /**
         * Find out if a 24 hours clock is preferred or not. The check will be done based on the user's locale.
         * @param context The context to find the uers's locale.
         * @return {@link Boolean#TRUE} if the 24 hours format is preferred. {@link Boolean#FALSE} if the AM/PM notation
         * is preferred.
         */
        public static boolean is24HourClock(Context context) {
            Locale locale = ContextUtils.getCurrentLocale(context);
            return is24HourClock(locale);
        }

        /**
         * Find out, based on the user's locale, if a 24 hours clock is preferred or not.
         * @param locale The user's locale.
         * @return {@link Boolean#TRUE} if the 24 hours format is preferred. {@link Boolean#FALSE} if the AM/PM notation
         * is preferred.
         */
        public static boolean is24HourClock(Locale locale) {
            java.text.DateFormat dateFormat = java.text.DateFormat.getTimeInstance(java.text.DateFormat.FULL, locale);
            String t = dateFormat.format(new Date());

            java.text.DateFormat stdFormat = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT, Locale.US);
            java.text.DateFormat localeFormat = java.text.DateFormat.getTimeInstance(java.text.DateFormat.LONG, locale);
            String check = "";
            try {
                check = localeFormat.format(stdFormat.parse("7:00 PM"));
            } catch (ParseException ignore) {
                return false;
            }
            boolean is24HourClock = check.contains("19");

            return is24HourClock;
        }
    }

    /**
     * Contains various date and time methods.
     */
    public static class Various {
        /**
         * Reset the time-part of a date to midnight (00:00:00.0000).
         * @param date The date to reset.
         * @return The time reset to midnight.
         */
        public static Date setMinTimeValueOfDay(Date date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        }

        /**
         * Reset the time-part of a date to just before midnight (which is the maximum time-value of day:
         * 23:59:59.9999).
         * @param date The date to reset.
         * @return The time reset to the maximum time-value of day.
         */
        public static Date setMaxTimeValueOfDay(Date date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            return cal.getTime();
        }
    }

    /**
     * Containing some methods to determine the time precision to be used in this utility class.
     */
    private static class TimePrecision {
        /**
         * Apply the user's selected {@link TimePrecisionPreference} on the provided {@link Date}.
         * @param ctx The context to search for the preference setting.
         * @param date The {@link Date} to which the the {@link TimePrecisionPreference} must be applied.
         * @return The {@link Date} with the applied {@link TimePrecisionPreference}.
         */
        private static Date applyTimePrecision(Context ctx, Date date) {
            return applyTimePrecisionCalendar(ctx, date).getTime();
        }

        /**
         * Apply the user's selected {@link TimePrecisionPreference} on the provided {@link Calendar}.
         * @param ctx The context to search for the preference setting.
         * @param cal The {@link Calendar} to which the the {@link TimePrecisionPreference} must be applied.
         * @return The {@link Date} with the applied {@link TimePrecisionPreference}.
         */
        private static Date applyTimePrecision(Context ctx, Calendar cal) {
            return applyTimePrecisionCalendar(ctx, cal).getTime();
        }

        /**
         * Apply the user's selected {@link TimePrecisionPreference} on the provided {@link Date}.
         * @param ctx The context to search for the preference setting.
         * @param date The {@link Date} to which the the {@link TimePrecisionPreference} must be applied.
         * @return The {@link Calendar} with the applied {@link TimePrecisionPreference}.
         */
        private static Calendar applyTimePrecisionCalendar(Context ctx, Date date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return applyTimePrecisionCalendar(ctx, cal);
        }

        /**
         * Apply the user's selected {@link TimePrecisionPreference} on the provided {@link Calendar}.
         * @param ctx The context to search for the preference setting.
         * @param cal The {@link Calendar} to which the the {@link TimePrecisionPreference} must be applied.
         * @return The {@link Calendar} with the applied {@link TimePrecisionPreference}.
         */
        private static Calendar applyTimePrecisionCalendar(Context ctx, Calendar cal) {
            TimePrecisionPreference preference = Preferences.getTimePrecision(ctx);
            
            switch (preference) {
                case SECOND:
                    cal.set(Calendar.MILLISECOND, 0);
                    break;
                case MINUTE:
                    cal.set(Calendar.MILLISECOND, 0);
                    cal.set(Calendar.SECOND, 0);
                    break;
            }
            
            return cal;
        }
    }

}
