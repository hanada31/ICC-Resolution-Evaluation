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

package eu.vranckaert.worktime.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * User: DIRK VRANCKAERT
 * Date: 11/07/12
 * Time: 18:53
 */
@DatabaseTable(tableName = "WidgetConfiguration")
public class WidgetConfiguration implements Serializable {
    @DatabaseField(id = true, generatedId = false, columnName = "id", dataType = DataType.INTEGER)
    private Integer widgetId;
    @DatabaseField(foreign = true, columnName = "projectId")
    private Project project;
    @DatabaseField(foreign = true, columnName = "taskId")
    private Task task;

    public WidgetConfiguration() {
    }

    public WidgetConfiguration(Integer widgetId) {
        this.widgetId = widgetId;
    }

    public Integer getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(Integer widgetId) {
        this.widgetId = widgetId;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
