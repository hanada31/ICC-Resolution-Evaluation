/*
 * Copyright 2013 Dirk Vranckaert
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

package eu.vranckaert.worktime.dao.utils;

import eu.vranckaert.worktime.model.*;

/**
 * User: DIRK VRANCKAERT
 * Date: 05/02/11
 * Time: 16:35
 */
public enum Tables {
    TIMEREGISTRATION(TimeRegistration.class),
    PROJECT(Project.class),
    TASK(Task.class),
    COMMENT_HISTORY(CommentHistory.class),
    WIDGET_CONFIGURATION(WidgetConfiguration.class),
    USER(User.class),
    SYNC_HISTORY(SyncHistory.class),
    SYNC_REMOVAL_CACHE(SyncRemovalCache.class);

    Tables(Class tableClass) {
        this.tableClass = tableClass;
    }

    private Class tableClass;

    public Class getTableClass() {
        return tableClass;
    }

    public void setTableClass(Class tableClass) {
        this.tableClass = tableClass;
    }
}
