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
package eu.vranckaert.worktime.comparators.timeregistration;

import eu.vranckaert.worktime.model.TimeRegistration;

import java.util.Comparator;

/**
 * User: DIRK VRANCKAERT
 * Date: 07/02/11
 * Time: 01:02
 */
public class TimeRegistrationDescendingByStartdate implements Comparator<TimeRegistration> {
    public int compare(TimeRegistration timeRegistration1, TimeRegistration timeRegistration2) {
        return timeRegistration2.getStartTime().compareTo(timeRegistration1.getStartTime());
    }
}
