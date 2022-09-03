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

package eu.vranckaert.worktime.activities.timeregistrations.listadapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.activities.timeregistrations.TimeRegistrationListActivity;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.date.DateFormat;
import eu.vranckaert.worktime.utils.date.DateUtils;
import eu.vranckaert.worktime.utils.date.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * The list adapater private inner-class used to display the manage projects list.
 */
public class TimeRegistrationsListAdapter extends ArrayAdapter<TimeRegistration> {
    private final String LOG_TAG = TimeRegistrationsListAdapter.class.getSimpleName();

    private TimeRegistrationListActivity ctx;
    private List<TimeRegistration> timeRegistrations;
    /**
     * {@inheritDoc}
     */
    public TimeRegistrationsListAdapter(TimeRegistrationListActivity ctx, List<TimeRegistration> timeRegistrations) {
        super(ctx, R.layout.list_item_time_registrations, timeRegistrations);
        Log.d(ctx, LOG_TAG, "Creating the time registrations list adapter");

        this.ctx = ctx;
        this.timeRegistrations = timeRegistrations;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(ctx, LOG_TAG, "Start rendering/recycling row " + position);
        View row = null;
        final TimeRegistration tr = timeRegistrations.get(position);

        if (tr.getId().equals(ctx.loadExtraTimeRegistration.getId())) {
            row = ctx.getLayoutInflater().inflate(R.layout.list_item_time_registrations_load_more, parent, false);
            return row;
        }

        Log.d(ctx, LOG_TAG, "Got time registration with startDate " +
                DateUtils.DateTimeConverter.convertDateTimeToString(tr.getStartTime(),
                        DateFormat.FULL,
                        TimeFormat.MEDIUM,
                        ctx));

        if (convertView == null) {
            Log.d(ctx, LOG_TAG, "Render a new line in the list");
            row = ctx.getLayoutInflater().inflate(R.layout.list_item_time_registrations, parent, false);
        } else {
            Log.d(ctx, LOG_TAG, "Recycling an existing line in the list");
            row = convertView;

            if ((TextView) row.findViewById(R.id.time_registration_list_day_of_month) == null) {
                row = ctx.getLayoutInflater().inflate(R.layout.list_item_time_registrations, parent, false);
            }
        }

        Log.d(ctx, LOG_TAG, "Ready to update the TR list itme...");

        TextView dayOfMonth = (TextView) row.findViewById(R.id.time_registration_list_day_of_month);
        TextView dayOfWeek = (TextView) row.findViewById(R.id.time_registration_list_day_of_week);
        TextView monthAndYear = (TextView) row.findViewById(R.id.time_registration_list_month_and_year);
        TextView hours = (TextView) row.findViewById(R.id.time_registration_list_hours);
        TextView duration = (TextView) row.findViewById(R.id.time_registration_list_duration);
        TextView projectAndTask = (TextView) row.findViewById(R.id.time_registration_list_project_task);

        dayOfMonth.setText(getDayOfMonth(tr.getStartTime()));
        dayOfWeek.setText(getDayOfWeek(tr.getStartTime()));
        monthAndYear.setText(getMonthAndYear(tr.getStartTime()));
        hours.setText(getHours(tr.getStartTime(), tr.getEndTime()));
        duration.setText(DateUtils.TimeCalculator.calculatePeriod(ctx.getApplicationContext(), tr, true));
        projectAndTask.setText(getProjectAndTask(tr));

        Log.d(ctx, LOG_TAG, "Done rendering row " + position);
        return row;
    }

    private String getDayOfMonth(Date day) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        return sdf.format(day);
    }

    private String getDayOfWeek(Date day) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        return sdf.format(day);
    }

    private String getMonthAndYear(Date day) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMMM yyyy");
        return sdf.format(day);
    }

    private String getHours(Date startTime, Date endTime) {
        String start = DateUtils.DateTimeConverter.convertTimeToString(startTime, TimeFormat.MEDIUM, getContext());

        String result = start + " - ";

        if (endTime == null) {
            result += "...";
        } else {
            String end = DateUtils.DateTimeConverter.convertTimeToString(endTime, TimeFormat.MEDIUM, getContext());
            result += end;
        }

        return result;
    }

    private String getProjectAndTask(TimeRegistration timeRegistration) {
        return timeRegistration.getTask().getProject().getName() + " - " + timeRegistration.getTask().getName();
    }

    public void refill(List<TimeRegistration> timeRegistrations) {
        this.timeRegistrations.clear();
        this.timeRegistrations.addAll(timeRegistrations);
        notifyDataSetChanged();
    }
}