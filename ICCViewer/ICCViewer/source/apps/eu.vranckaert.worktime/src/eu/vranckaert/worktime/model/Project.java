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
 * Time: 16:52
 */
@DatabaseTable
public class Project implements Serializable, Cloneable {
    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField
    @Expose
    private String name;
    @DatabaseField
    @Expose
    private String comment;
    @DatabaseField(defaultValue = "0")
    @Expose
    private Integer order;
    @DatabaseField(dataType = DataType.BOOLEAN, defaultValue = "false")
    @Expose
    private boolean defaultValue;
    @DatabaseField
    private Long externalId;
    @DatabaseField(dataType = DataType.ENUM_STRING)
    private ExternalSystems externalSystem;
    @DatabaseField
    @Expose
    private String flags;
    @DatabaseField(dataType = DataType.BOOLEAN, defaultValue = "false")
    @Expose
    private boolean finished;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public boolean isDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(boolean defaultValue) {
        this.defaultValue = defaultValue;
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

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
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

    @Override
    public Object clone() {
        Project clone = new Project();
        clone.setName(this.name);
        clone.setComment(this.comment);
        clone.setDefaultValue(false);
        clone.setFinished(this.finished);
        clone.setFlags(this.flags);
        clone.setOrder(this.order);
        return clone;
    }

    public boolean isModifiedAfter(Project project) {
        if (this.getLastUpdated().after(project.getLastUpdated())) {
            return true;
        }

        return false;
    }
}
