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

package eu.vranckaert.worktime.dao.web.model.request.sync;

import com.google.gson.annotations.Expose;
import eu.vranckaert.worktime.dao.web.model.base.request.AuthenticatedUserRequest;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.Task;
import eu.vranckaert.worktime.model.TimeRegistration;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User: Dirk Vranckaert
 * Date: 11/01/13
 * Time: 10:29
 */
public class WorkTimeSyncRequest extends AuthenticatedUserRequest {
    @Expose
    private Date lastSuccessfulSyncDate;
    @Expose
    private String conflictConfiguration;
    @Expose
    private List<Project> projects;
    @Expose
    private List<Task> tasks;
    @Expose
    private List<TimeRegistration> timeRegistrations;
    @Expose
    private Map<String, String> syncRemovalMap;

    public Date getLastSuccessfulSyncDate() {
        return lastSuccessfulSyncDate;
    }

    public void setLastSuccessfulSyncDate(Date lastSuccessfulSyncDate) {
        this.lastSuccessfulSyncDate = lastSuccessfulSyncDate;
    }

    public String getConflictConfiguration() {
        return conflictConfiguration;
    }

    public void setConflictConfiguration(String conflictConfiguration) {
        this.conflictConfiguration = conflictConfiguration;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<TimeRegistration> getTimeRegistrations() {
        return timeRegistrations;
    }

    public void setTimeRegistrations(List<TimeRegistration> timeRegistrations) {
        this.timeRegistrations = timeRegistrations;
    }

    public Map<String, String> getSyncRemovalMap() {
        return syncRemovalMap;
    }

    public void setSyncRemovalMap(Map<String, String> syncRemovalMap) {
        this.syncRemovalMap = syncRemovalMap;
    }
}
