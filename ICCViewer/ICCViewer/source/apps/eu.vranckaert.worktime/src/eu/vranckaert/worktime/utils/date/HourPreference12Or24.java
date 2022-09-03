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
package eu.vranckaert.worktime.utils.date;

/**
 * User: DIRK VRANCKAERT
 * Date: 03/08/11
 * Time: 16:47
 */
public enum HourPreference12Or24 {
	HOURS_24("24-hour"), HOURS_12("12-hour");

    String value;

    HourPreference12Or24(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static HourPreference12Or24 findHourPreference12Or24(String value) {
        HourPreference12Or24[] preferences = HourPreference12Or24.values();
        for (HourPreference12Or24 preference : preferences) {
            if (value.equals(preference.getValue())) {
                return preference;
            }
        }
        return null;
    }
}
