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

package eu.vranckaert.worktime.model;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import eu.vranckaert.worktime.enums.ExternalSystems;

import java.io.Serializable;
import java.util.Date;

/**
 * User: DIRK VRANCKAERT
 * Date: 05/02/11
 * Time: 16:50
 */
@DatabaseTable
public class TimeRegistration implements Serializable {
    @DatabaseField(generatedId = true, columnName = "id")
    private Integer id;
    @DatabaseField(columnName = "startTime", dataType = DataType.DATE_STRING)
    @Expose
    private Date startTime;
    @DatabaseField(columnName = "endTime", dataType = DataType.DATE_STRING)
    @Expose
    private Date endTime;
    @DatabaseField(columnName = "comment")
    @Expose
    private String comment;
    @DatabaseField(foreign = true, columnName = "taskId")
    @Expose
    private Task task;
    @DatabaseField
    private Long externalId;
    @DatabaseField(dataType = DataType.ENUM_STRING)
    private ExternalSystems externalSystem;
    @DatabaseField
    @Expose
    private String flags;
    @DatabaseField(columnName = "lastUpdated", dataType = DataType.DATE_STRING)
    @Expose
    private Date lastUpdated;
    @DatabaseField
    @Expose
    private String syncKey;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public boolean isOngoingTimeRegistration() {
        if (endTime == null) {
            return true;
        }
        return false;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getExternalId() {
        return externalId;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    public ExternalSystems getExternalSystem() {
        return externalSystem;
    }

    public void setExternalSystem(ExternalSystems externalSystem) {
        this.externalSystem = externalSystem;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    public TimeRegistration duplicate() {
        TimeRegistration timeRegistration = new TimeRegistration();
        timeRegistration.setId(this.getId());
        timeRegistration.setStartTime(this.getStartTime());
        timeRegistration.setEndTime(this.getEndTime());
        timeRegistration.setExternalId(this.getExternalId());
        timeRegistration.setExternalSystem(this.getExternalSystem());
        timeRegistration.setTask(this.getTask());
        timeRegistration.setComment(this.getComment());
        timeRegistration.setFlags(this.getFlags());
        timeRegistration.setSyncKey(this.getSyncKey());
        return timeRegistration;
    }

    public void clearSensitiveData() {
        this.id = null;
        this.syncKey = null;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getSyncKey() {
        return syncKey;
    }

    public void setSyncKey(String syncKey) {
        this.syncKey = syncKey;
    }

    public String toString() {
        String endTime = "";
        String comment = "";

        if (this.endTime != null) {
            endTime = this.endTime.toString();
        }
        if (this.comment != null) {
            comment = this.comment;
        }
        return "[TimeRegistration - id: " + id + " startTime: " + startTime + " endTime: " + endTime + " comment: " + comment + "]";
    }

    public boolean isModifiedAfter(TimeRegistration timeRegistration) {
        if (this.getLastUpdated().after(timeRegistration.getLastUpdated())) {
            return true;
        }

        return false;
    }
}
