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

import eu.vranckaert.worktime.model.TimeRegistration;

import java.util.List;

/**
 * User: Dirk Vranckaert
 * Date: 11/01/13
 * Time: 10:44
 */
public class TimeRegistrationSyncResult {
    private TimeRegistration timeRegistration;
    private TimeRegistration syncedTimeRegistration;
    private List<TimeRegistration> syncedTimeRegistrations;
    private EntitySyncResolution resolution;

    public TimeRegistration getTimeRegistration() {
        return timeRegistration;
    }

    public void setTimeRegistration(TimeRegistration timeRegistration) {
        this.timeRegistration = timeRegistration;
    }

    public TimeRegistration getSyncedTimeRegistration() {
        return syncedTimeRegistration;
    }

    public void setSyncedTimeRegistration(TimeRegistration syncedTimeRegistration) {
        this.syncedTimeRegistration = syncedTimeRegistration;
    }

    public List<TimeRegistration> getSyncedTimeRegistrations() {
        return syncedTimeRegistrations;
    }

    public void setSyncedTimeRegistrations(List<TimeRegistration> syncedTimeRegistrations) {
        this.syncedTimeRegistrations = syncedTimeRegistrations;
    }

    public EntitySyncResolution getResolution() {
        return resolution;
    }

    public void setResolution(EntitySyncResolution resolution) {
        this.resolution = resolution;
    }
}
