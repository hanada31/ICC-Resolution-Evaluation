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

import java.util.Calendar;
import java.util.Comparator;

public class GroupingTrByStartDateComparator implements Comparator<TimeRegistration> {

	public int compare(TimeRegistration tr0, TimeRegistration tr1) {
		int compareResult = 0;
		
		Calendar startTime0 = Calendar.getInstance();
		startTime0.setTime(tr0.getStartTime());
		startTime0.set(Calendar.HOUR, 0);
		startTime0.set(Calendar.HOUR_OF_DAY, 0);
		startTime0.set(Calendar.MINUTE, 0);
		startTime0.set(Calendar.SECOND, 0);
		startTime0.set(Calendar.MILLISECOND, 0);
		
		Calendar startTime1 = Calendar.getInstance();
		startTime1.setTime(tr1.getStartTime());
		startTime1.set(Calendar.HOUR, 0);
		startTime1.set(Calendar.HOUR_OF_DAY, 0);
		startTime1.set(Calendar.MINUTE, 0);
		startTime1.set(Calendar.SECOND, 0);
		startTime1.set(Calendar.MILLISECOND, 0);
		
		compareResult = startTime0.compareTo(startTime1);
		
		if (compareResult == 0) {
			compareResult = tr0.getTask().getProject().getName().compareTo(tr1.getTask().getProject().getName());
			
			if (compareResult == 0) {
				compareResult = tr0.getTask().getName().compareTo(tr1.getTask().getName());
			}
		}
		
		return compareResult;
	}

}
