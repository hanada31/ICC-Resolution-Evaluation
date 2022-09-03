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

/**
 * User: DIRK VRANCKAERT
 * Date: 19/02/11
 * Time: 17:35
 */
public enum ExportCsvSeparator {
    COMMA(',', 0),
    SEMICOLON(';', 1);

    private char separator;
    private int position;

    private ExportCsvSeparator(char separator, int position) {
        this.separator = separator;
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public char getSeparator() {
        return separator;
    }

    public static ExportCsvSeparator getByIndex(int index) {
        ExportCsvSeparator[] exportCsvSeparators = ExportCsvSeparator.values();
        for (ExportCsvSeparator exportCsvSeparator : exportCsvSeparators) {
            if (exportCsvSeparator.getPosition() == index) {
                return exportCsvSeparator;
            }
        }

        return null;
    }
}
