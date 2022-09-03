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
 * All possible DateFormat types.
 * @author Dirk Vranckaert
 */
public enum DateFormat {
    /**
     * FULL is pretty completely specified, such as Tuesday, April 12, 1952 AD or 3:30:42pm PST.
     */
    FULL(java.text.DateFormat.FULL),
    /**
     * LONG is longer, such as January 12, 1952 or 3:30:32pm
     */
    LONG(java.text.DateFormat.LONG),
    /**
     * MEDIUM is longer, such as Jan 12, 1952
     */
    MEDIUM(java.text.DateFormat.MEDIUM),
    /**
     * SHORT is completely numeric, such as 12.13.52 or 3:30pm
     */
    SHORT(java.text.DateFormat.SHORT);

    int style;

    DateFormat(int style) {
        this.style = style;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }
}
