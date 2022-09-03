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

import eu.vranckaert.worktime.dao.web.model.base.response.WorkTimeResponse;
import eu.vranckaert.worktime.dao.web.model.exception.sync.CorruptDataJSONException;
import eu.vranckaert.worktime.dao.web.model.exception.sync.SynchronisationLockedJSONException;
import eu.vranckaert.worktime.dao.web.model.exception.sync.SyncronisationFailedJSONException;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.Task;
import eu.vranckaert.worktime.model.TimeRegistration;

import java.util.List;
import java.util.Map;

/**
 * User: Dirk Vranckaert
 * Date: 11/01/13
 * Time: 10:38
 */
public class WorkTimeSyncResponse extends WorkTimeResponse {
    private SyncronisationFailedJSONException syncronisationFailedJSONException;
    private SynchronisationLockedJSONException synchronisationLockedJSONException;
    private CorruptDataJSONException corruptDataJSONException;
    private EntitySyncResult syncResult;
    private List<Project> projectsSinceLastSync;
    private List<Task> tasksSinceLastSync;
    private List<TimeRegistration> timeRegistrationsSinceLastSync;
    private Map<String, String> syncRemovalMap;

    public SyncronisationFailedJSONException getSyncronisationFailedJSONException() {
        return syncronisationFailedJSONException;
    }

    public void setSyncronisationFailedJSONException(SyncronisationFailedJSONException syncronisationFailedJSONException) {
        this.syncronisationFailedJSONException = syncronisationFailedJSONException;
    }

    public SynchronisationLockedJSONException getSynchronisationLockedJSONException() {
        return synchronisationLockedJSONException;
    }

    public void setSynchronisationLockedJSONException(SynchronisationLockedJSONException synchronisationLockedJSONException) {
        this.synchronisationLockedJSONException = synchronisationLockedJSONException;
    }

    public CorruptDataJSONException getCorruptDataJSONException() {
        return corruptDataJSONException;
    }

    public void setCorruptDataJSONException(CorruptDataJSONException corruptDataJSONException) {
        this.corruptDataJSONException = corruptDataJSONException;
    }

    public EntitySyncResult getSyncResult() {
        return syncResult;
    }

    public void setSyncResult(EntitySyncResult syncResult) {
        this.syncResult = syncResult;
    }

    public List<Project> getProjectsSinceLastSync() {
        return projectsSinceLastSync;
    }

    public void setProjectsSinceLastSync(List<Project> projectsSinceLastSync) {
        this.projectsSinceLastSync = projectsSinceLastSync;
    }

    public List<Task> getTasksSinceLastSync() {
        return tasksSinceLastSync;
    }

    public void setTasksSinceLastSync(List<Task> tasksSinceLastSync) {
        this.tasksSinceLastSync = tasksSinceLastSync;
    }

    public List<TimeRegistration> getTimeRegistrationsSinceLastSync() {
        return timeRegistrationsSinceLastSync;
    }

    public void setTimeRegistrationsSinceLastSync(List<TimeRegistration> timeRegistrationsSinceLastSync) {
        this.timeRegistrationsSinceLastSync = timeRegistrationsSinceLastSync;
    }

    public Map<String, String> getSyncRemovalMap() {
        return syncRemovalMap;
    }

    public void setSyncRemovalMap(Map<String, String> syncRemovalMap) {
        this.syncRemovalMap = syncRemovalMap;
    }
}
