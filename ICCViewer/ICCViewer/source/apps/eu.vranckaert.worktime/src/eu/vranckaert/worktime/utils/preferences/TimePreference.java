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

package eu.vranckaert.worktime.utils.preferences;

import android.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * User: Dirk Vranckaert
 * Date: 11/02/13
 * Time: 11:39
 */
public class TimePreference extends DialogPreference {
    private Calendar calendar;
    private TimePicker picker = null;

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTimePreference();
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initTimePreference();
    }

    private void initTimePreference() {
        setPositiveButtonText(R.string.ok);
        setNegativeButtonText(R.string.cancel);
        calendar = new GregorianCalendar();
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());
        return (picker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        picker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        picker.setCurrentMinute(calendar.get(Calendar.MINUTE));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            calendar.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
            calendar.set(Calendar.MINUTE, picker.getCurrentMinute());

            setSummary(getSummary());
            if (callChangeListener(calendar.getTimeInMillis())) {
                persistLong(calendar.getTimeInMillis());
                notifyChanged();
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if (restoreValue) {
            if (defaultValue == null) {
                calendar.setTimeInMillis(getPersistedLong(System.currentTimeMillis()));
            } else {
                calendar.setTimeInMillis(Long.parseLong(getPersistedString((String) defaultValue)));
            }
        } else {
            if (defaultValue == null) {
                calendar.setTimeInMillis(System.currentTimeMillis());
            } else {
                calendar.setTimeInMillis(Long.parseLong((String) defaultValue));
            }
        }
    }
}