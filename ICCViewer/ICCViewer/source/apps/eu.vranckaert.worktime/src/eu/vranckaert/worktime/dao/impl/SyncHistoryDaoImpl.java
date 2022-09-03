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

package eu.vranckaert.worktime.dao.impl;

import android.content.Context;
import android.util.Log;
import com.google.inject.Inject;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import eu.vranckaert.worktime.dao.SyncHistoryDao;
import eu.vranckaert.worktime.dao.generic.GenericDaoImpl;
import eu.vranckaert.worktime.model.SyncHistory;
import eu.vranckaert.worktime.model.SyncHistoryStatus;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * User: Dirk Vranckaert
 * Date: 11/01/13
 * Time: 09:46
 */
public class SyncHistoryDaoImpl extends GenericDaoImpl<SyncHistory, Integer> implements SyncHistoryDao {
    private static final String LOG_TAG = SyncHistoryDaoImpl.class.getSimpleName();

    @Inject
    public SyncHistoryDaoImpl(Context context) {
        super(SyncHistory.class, context);
    }

    @Override
    public SyncHistory getOngoingSyncHistory() {
        List<SyncHistory> syncHistories;
        QueryBuilder<SyncHistory, Integer> qb = dao.queryBuilder();
        try {
            qb.where().eq("status", SyncHistoryStatus.BUSY);
            PreparedQuery<SyncHistory> pq = qb.prepare();
            syncHistories = dao.query(pq);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Could not start the query... Returning null.", e);
            return null;
        }

        if(syncHistories == null || syncHistories.size() == 0) {
            return null;
        } else {
            return syncHistories.get(0);
        }
    }

    @Override
    public Date getLastSuccessfulSyncDate() {
        List<SyncHistory> syncHistories;
        QueryBuilder<SyncHistory, Integer> qb = dao.queryBuilder();
        try {
            qb.where().eq("status", SyncHistoryStatus.SUCCESSFUL);
            qb.orderBy("ended", false);
            PreparedQuery<SyncHistory> pq = qb.prepare();
            syncHistories = dao.query(pq);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Could not start the query... Returning null.", e);
            return null;
        }

        if(syncHistories == null || syncHistories.size() == 0) {
            return null;
        } else {
            return syncHistories.get(0).getEnded();
        }
    }

    @Override
    public SyncHistory getLastSyncHistory() {
        List<SyncHistory> syncHistories;
        QueryBuilder<SyncHistory, Integer> qb = dao.queryBuilder();
        try {
            qb.orderBy("started", false);
            PreparedQuery<SyncHistory> pq = qb.prepare();
            syncHistories = dao.query(pq);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Could not start the query... Returning null.", e);
            return null;
        }

        if(syncHistories == null || syncHistories.size() == 0) {
            return null;
        } else {
            return syncHistories.get(0);
        }
    }

    @Override
    public List<SyncHistory> findAll() {
        List<SyncHistory> syncHistories;
        QueryBuilder<SyncHistory, Integer> qb = dao.queryBuilder();
        try {
            qb.orderBy("started", false);
            PreparedQuery<SyncHistory> pq = qb.prepare();
            syncHistories = dao.query(pq);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Could not start the query... Returning null.", e);
            return null;
        }

        return syncHistories;
    }
}
