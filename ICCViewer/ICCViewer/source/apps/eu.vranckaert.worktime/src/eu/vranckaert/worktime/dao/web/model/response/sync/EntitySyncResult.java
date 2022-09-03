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

package eu.vranckaert.worktime.dao.web.model.response.sync;

import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.Task;
import eu.vranckaert.worktime.model.TimeRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Dirk Vranckaert
 * Date: 11/01/13
 * Time: 10:42
 */
public class EntitySyncResult {
    private List<ProjectSyncResult> projectSyncResults;
    private List<TaskSyncResult> taskSyncResults;
    private List<TimeRegistrationSyncResult> timeRegistrationSyncResults;

    private List<Project> nonSyncedProjects = new ArrayList<Project>();
    private List<Task> nonSyncedTasks = new ArrayList<Task>();
    private List<TimeRegistration> nonSyncedTimeRegistrations = new ArrayList<TimeRegistration>();

    private SyncResult syncResult;

    public List<ProjectSyncResult> getProjectSyncResults() {
        return projectSyncResults;
    }

    public void setProjectSyncResults(List<ProjectSyncResult> projectSyncResults) {
        this.projectSyncResults = projectSyncResults;
    }

    public List<TaskSyncResult> getTaskSyncResults() {
        return taskSyncResults;
    }

    public void setTaskSyncResults(List<TaskSyncResult> taskSyncResults) {
        this.taskSyncResults = taskSyncResults;
    }

    public List<TimeRegistrationSyncResult> getTimeRegistrationSyncResults() {
        return timeRegistrationSyncResults;
    }

    public void setTimeRegistrationSyncResults(List<TimeRegistrationSyncResult> timeRegistrationSyncResults) {
        this.timeRegistrationSyncResults = timeRegistrationSyncResults;
    }

    public List<Project> getNonSyncedProjects() {
        return nonSyncedProjects;
    }

    public void setNonSyncedProjects(List<Project> nonSyncedProjects) {
        this.nonSyncedProjects = nonSyncedProjects;
    }

    public List<Task> getNonSyncedTasks() {
        return nonSyncedTasks;
    }

    public void setNonSyncedTasks(List<Task> nonSyncedTasks) {
        this.nonSyncedTasks = nonSyncedTasks;
    }

    public List<TimeRegistration> getNonSyncedTimeRegistrations() {
        return nonSyncedTimeRegistrations;
    }

    public void setNonSyncedTimeRegistrations(List<TimeRegistration> nonSyncedTimeRegistrations) {
        this.nonSyncedTimeRegistrations = nonSyncedTimeRegistrations;
    }

    public SyncResult getSyncResult() {
        return syncResult;
    }

    public void setSyncResult(SyncResult syncResult) {
        this.syncResult = syncResult;
    }
}
