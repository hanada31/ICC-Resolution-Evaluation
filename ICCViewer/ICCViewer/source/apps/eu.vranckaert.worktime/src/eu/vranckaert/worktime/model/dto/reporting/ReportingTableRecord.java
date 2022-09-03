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
package eu.vranckaert.worktime.model.dto.reporting;

import java.io.Serializable;

/**
 * User: DIRK VRANCKAERT
 * Date: 01/10/11
 * Time: 16:41
 */
public class ReportingTableRecord implements Serializable {
    private String column1;
    private String column2;
    private String column3;
    private String columnTotal;

    private boolean isOngoingTr;

    private ReportingTableRecordLevel level;

    public ReportingTableRecord() {}

    public ReportingTableRecord(String column1, String column2, String column3, String columnTotal, ReportingTableRecordLevel level) {
        this.column1 = column1;
        this.column2 = column2;
        this.column3 = column3;
        this.columnTotal = columnTotal;
        this.level = level;
    }

    public String getColumn1() {
        return column1;
    }

    public void setColumn1(String column1) {
        this.column1 = column1;
    }

    public String getColumn2() {
        return column2;
    }

    public void setColumn2(String column2) {
        this.column2 = column2;
    }

    public String getColumn3() {
        return column3;
    }

    public void setColumn3(String column3) {
        this.column3 = column3;
    }

    public String getColumnTotal() {
        return columnTotal;
    }

    public void setColumnTotal(String columnTotal) {
        this.columnTotal = columnTotal;
    }

    public boolean isOngoingTr() {
        return isOngoingTr;
    }

    public void setOngoingTr(boolean ongoingTr) {
        isOngoingTr = ongoingTr;
    }

    public ReportingTableRecordLevel getLevel() {
        return level;
    }

    public void setLevel(ReportingTableRecordLevel level) {
        this.level = level;
    }
}

