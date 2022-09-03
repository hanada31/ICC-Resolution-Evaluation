/*
 *  Copyright 2011 Dirk Vranckaert
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.vranckaert.worktime.utils.preferences;

import android.content.Context;
import eu.vranckaert.worktime.R;

/**
 * The preference utility containing the string resource id and the value of the preference. It also contains a boolean
 * per value to determine if it's the default value.
 * 
 * User: DIRK VRANCKAERT
 * Date: 21/12/11
 * Time: 8:04
 */
public enum TimePrecisionPreference {
    SECOND(R.string.pref_date_and_time_time_registrations_precision_option_seconds, "1", false),
    MINUTE(R.string.pref_date_and_time_time_registrations_precision_option_minutes, "2", true);
    
    TimePrecisionPreference(int stringResId, String value, boolean defaultOption) {
        this.stringResId = stringResId;
        this.value = value;
        this.defaultOption = defaultOption;
    }

    private int stringResId;
    private String value;
    private boolean defaultOption;

    public int getStringResId() {
        return stringResId;
    }

    public String getValue() {
        return value;
    }

    public boolean isDefaultOption() {
        return defaultOption;
    }

    /**
     * Find the default value for this preference. This will loop over all options, the first one with the default flag
     * will be used. If no default option is defined, it's the the first option that is used as the default. If the list
     * of options is empty this method will return null.
     * @return The value (as a {@link String}) of the first found default option.
     */
    public static String getDefaultValue() {
        TimePrecisionPreference[] options = TimePrecisionPreference.values();
        for (TimePrecisionPreference option : options) {
            if (option.isDefaultOption()) {
                return option.getValue();
            }
        }

        //No default option is defined! Returning the first option!
        if (options.length > 0) {
            return options[0].getValue();
        } else {
            //No options hav been defined at all!
            return null;
        }
    }

    /**
     * Return a list of {@link CharSequence}s that are retrieved from the {@link Context}, based on the provided
     * string resource id.
     * @param ctx The context.
     * @return The list of {@link CharSequence} values.
     */
    public static CharSequence[] getEntries(Context ctx) {
        TimePrecisionPreference[] options = TimePrecisionPreference.values();
        CharSequence[] entries = new CharSequence[options.length];

        for (int i=0; i<options.length; i++) {
            entries[i] = ctx.getText(options[i].getStringResId());
        }

        return entries;
    }

    /**
     * Return a list of {@link CharSequence}s that represent the value of each option.
     * @return The list of {@link CharSequence} values.
     */
    public static CharSequence[] getEntryValues() {
        TimePrecisionPreference[] options = TimePrecisionPreference.values();
        CharSequence[] entryValues = new CharSequence[options.length];

        for (int i=0; i<options.length; i++) {
            entryValues[i] = options[i].getValue();
        }

        return entryValues;
    }

    /**
     * Find the preference for a certain value. If the value is not found in the list of preferences it returns the
     * first preference it can find.
     * @param value The value to search the preference for.
     * @return The {@link TimePrecisionPreference}.
     */
    public static TimePrecisionPreference getPreferenceForValue(String value) {
        TimePrecisionPreference[] options = TimePrecisionPreference.values();
        for (TimePrecisionPreference option : options) {
            if (option.getValue().equals(value)) {
                return option;
            }
        }
        //No option with the specified value is found! Returning the first option!
        if (options.length > 0) {
            return options[0];
        } else {
            //No options hav been defined at all!
            return null;
        }
    }
}
