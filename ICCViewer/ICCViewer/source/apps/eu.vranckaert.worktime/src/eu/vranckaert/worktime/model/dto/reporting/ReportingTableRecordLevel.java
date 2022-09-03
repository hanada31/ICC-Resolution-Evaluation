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
 * Time: 16:44
 */
public enum ReportingTableRecordLevel implements Serializable {
    LVL0, LVL1, LVL2, LVL3;

    ReportingTableRecordLevel() {}
}
