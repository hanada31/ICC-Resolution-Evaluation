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

package eu.vranckaert.worktime.model.dto.export;

import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.model.dto.reporting.ReportingTableRecord;
import eu.vranckaert.worktime.model.dto.reporting.datalevels.ReportingDataLvl0;

import java.io.Serializable;
import java.util.List;

public class ExportDTO implements Serializable {
    private List<TimeRegistration> timeRegistrations;
    private List<ReportingTableRecord> tableRecords;
    private List<ReportingDataLvl0> reportingDataLevels;

    public List<TimeRegistration> getTimeRegistrations() {
        return timeRegistrations;
    }

    public void setTimeRegistrations(List<TimeRegistration> timeRegistrations) {
        this.timeRegistrations = timeRegistrations;
    }

    public List<ReportingTableRecord> getTableRecords() {
        return tableRecords;
    }

    public void setTableRecords(List<ReportingTableRecord> tableRecords) {
        this.tableRecords = tableRecords;
    }

    public List<ReportingDataLvl0> getReportingDataLevels() {
        return reportingDataLevels;
    }

    public void setReportingDataLevels(List<ReportingDataLvl0> reportingDataLevels) {
        this.reportingDataLevels = reportingDataLevels;
    }
}
