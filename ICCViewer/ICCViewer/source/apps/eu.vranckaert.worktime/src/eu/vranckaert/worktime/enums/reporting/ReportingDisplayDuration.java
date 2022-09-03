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
package eu.vranckaert.worktime.enums.reporting;

/**
 * User: DIRK VRANCKAERT
 * Date: 06/10/11
 * Time: 23:18
 */
public enum ReportingDisplayDuration {
    HOUR_MINUTES_SECONDS(0),
    DAYS_HOUR_MINUTES_SECONDS_24H(1),
    DAYS_HOUR_MINUTES_SECONDS_08H(2);

    private int order;

    ReportingDisplayDuration(int order) {
        setOrder(order);
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
