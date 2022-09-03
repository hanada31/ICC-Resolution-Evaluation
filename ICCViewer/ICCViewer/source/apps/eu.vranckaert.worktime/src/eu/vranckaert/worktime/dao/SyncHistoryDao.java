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

package eu.vranckaert.worktime.dao;

import eu.vranckaert.worktime.dao.generic.GenericDao;
import eu.vranckaert.worktime.model.SyncHistory;

import java.util.Date;
import java.util.List;

/**
 * User: Dirk Vranckaert
 * Date: 11/01/13
 * Time: 09:46
 */
public interface SyncHistoryDao extends GenericDao<SyncHistory, Integer> {
    /**
     * Search for a record in the {@link SyncHistory}'s that has status
     * {@link eu.vranckaert.worktime.model.SyncHistoryStatus#BUSY}.
     * @return Null if no ongoing (or busy) syncs are found, otherwise the instance of the {@link SyncHistory}.
     */
    SyncHistory getOngoingSyncHistory();

    /**
     * Get the date of the latest successful synchronization.
     * @return The {@link Date} of the last successful sync or null if none.
     */
    Date getLastSuccessfulSyncDate();

    /**
     * Get the latest sync history object.
     * @return The latest {@link SyncHistory} object or null if none.
     */
    SyncHistory getLastSyncHistory();

    @Override
    List<SyncHistory> findAll();
}
