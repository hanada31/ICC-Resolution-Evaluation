/*
 * Copyright 2012 Dirk Vranckaert
 *
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

package eu.vranckaert.worktime.model.dto.reporting.datalevels;

import eu.vranckaert.worktime.model.TimeRegistration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReportingDataLvl0 implements Serializable {
	private Object key;
	private List<ReportingDataLvl1> reportingDataLvl1 = new ArrayList<ReportingDataLvl1>();
	private List<TimeRegistration> timeRegistrations;

	public ReportingDataLvl0() {
		super();
	}

	public ReportingDataLvl0(Object key) {
		super();
		this.key = key;
	}

	public Object getKey() {
		return key;
	}

	public void setKey(Object key) {
		this.key = key;
	}

	public List<ReportingDataLvl1> getReportingDataLvl1() {
		return reportingDataLvl1;
	}

	public void setReportingDataLvl1(List<ReportingDataLvl1> reportingDataLvl1) {
		this.reportingDataLvl1 = reportingDataLvl1;
	}

	public List<TimeRegistration> getTimeRegistrations() {
		return timeRegistrations;
	}

	public void setTimeRegistrations(List<TimeRegistration> timeRegistrations) {
		this.timeRegistrations = timeRegistrations;
	}
	
	public void addTimeRegistration(TimeRegistration timeRegistration) {
		if (timeRegistrations == null) {
			timeRegistrations = new ArrayList<TimeRegistration>();
		}
		
		timeRegistrations.add(timeRegistration);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReportingDataLvl0 other = (ReportingDataLvl0) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}
}
