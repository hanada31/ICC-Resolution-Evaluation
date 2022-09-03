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

package eu.vranckaert.worktime.enums.export;

public enum ExportData {
    REPORT(0), RAW_DATA(1);

    ExportData(int position) {
        this.position = position;
    }

    private int position;

    public int getPosition() {
        return position;
    }

    public static ExportData getByIndex(int index) {
        ExportData[] exportDatas = ExportData.values();
        for (ExportData exportData : exportDatas) {
            if (exportData.getPosition() == index) {
                return exportData;
            }
        }

        return null;
    }
}
