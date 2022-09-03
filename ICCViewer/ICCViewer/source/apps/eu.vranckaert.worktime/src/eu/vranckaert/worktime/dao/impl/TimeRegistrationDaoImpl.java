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
import com.google.inject.Inject;
import com.j256.ormlite.stmt.*;
import eu.vranckaert.worktime.comparators.timeregistration.TimeRegistrationDescendingByStartdate;
import eu.vranckaert.worktime.dao.SyncRemovalCacheDao;
import eu.vranckaert.worktime.dao.TimeRegistrationDao;
import eu.vranckaert.worktime.dao.generic.GenericDaoImpl;
import eu.vranckaert.worktime.dao.utils.DatabaseHelper;
import eu.vranckaert.worktime.exceptions.CorruptTimeRegistrationDataException;
import eu.vranckaert.worktime.model.SyncRemovalCache;
import eu.vranckaert.worktime.model.Task;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.utils.context.Log;

import java.sql.SQLException;
import java.util.*;

/**
 * User: DIRK VRANCKAERT
 * Date: 05/02/11
 * Time: 17:31
 */
public class TimeRegistrationDaoImpl extends GenericDaoImpl<TimeRegistration, Integer> implements TimeRegistrationDao{
    private static final String LOG_TAG = TimeRegistrationDaoImpl.class.getSimpleName();

    private SyncRemovalCacheDao syncRemovalCache;

    @Inject
    public TimeRegistrationDaoImpl(final Context context, final SyncRemovalCacheDao syncRemovalCache) {
        super(TimeRegistration.class, context);
        this.syncRemovalCache = syncRemovalCache;
    }

    @Override
    public TimeRegistration save(TimeRegistration entity) {
        entity.setLastUpdated(new Date());
        return super.save(entity);
    }

    @Override
    public TimeRegistration update(TimeRegistration entity) {
        entity.setLastUpdated(new Date());
        return super.update(entity);
    }

    @Override
    public void delete(TimeRegistration entity) {
        if (entity.getSyncKey() != null) {
            if (syncRemovalCache.findById(entity.getSyncKey()) == null) {
                SyncRemovalCache cache = new SyncRemovalCache(entity.getSyncKey(), entity.getClass().getSimpleName());
                syncRemovalCache.save(cache);
            }
        }
        super.delete(entity);
    }

    @Override
    public void deleteAll() {
        List<TimeRegistration> entities = findAll();
        for (TimeRegistration entity : entities) {
            if (entity.getSyncKey() != null) {
                if (syncRemovalCache.findById(entity.getSyncKey()) == null) {
                    SyncRemovalCache cache = new SyncRemovalCache(entity.getSyncKey(), entity.getClass().getSimpleName());
                    syncRemovalCache.save(cache);
                }
            }
        }
        super.deleteAll();
    }

    /**
     * {@inheritDoc}
     */
    public TimeRegistration getLatestTimeRegistration() {
        List<TimeRegistration> timeRegistrations = findAll();
        if(timeRegistrations.size() > 0) {
            Collections.sort(timeRegistrations, new TimeRegistrationDescendingByStartdate());
            return timeRegistrations.get(0);
        } else {
            return null;
        }
    }

    public List<TimeRegistration> findTimeRegistrationsForTask(Task task) {
        QueryBuilder<TimeRegistration,Integer> qb = dao.queryBuilder();
        try {
            qb.where().eq("taskId", task.getId());
            PreparedQuery<TimeRegistration> pq = qb.prepare();
            return dao.query(pq);
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query...");
            throwFatalException(e);
        }

        return null;
    }

    public List<TimeRegistration> findTimeRegistrationsForTaks(List<Task> tasks) {
        List<Integer> taskIds = new ArrayList<Integer>();
        for (Task task : tasks) {
            taskIds.add(task.getId());
        }

        QueryBuilder<TimeRegistration,Integer> qb = dao.queryBuilder();
        try {
            qb.where().in("taskId", taskIds);
            PreparedQuery<TimeRegistration> pq = qb.prepare();
            return dao.query(pq);
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query...");
            throwFatalException(e);
        }

        return null;
    }

    public List<TimeRegistration> getTimeRegistrations(Date startDate, Date endDate, List<Task> tasks) {
        List<Integer> taskIds = null;
        if (tasks != null && !tasks.isEmpty()) {
            Log.d(getContext(), LOG_TAG, tasks.size() + " task(s) are taken into account while querying...");
            for (Task task : tasks) {
                if (taskIds == null) {
                    taskIds = new ArrayList<Integer>();
                }
                taskIds.add(task.getId());
            }
        }

        QueryBuilder<TimeRegistration,Integer> qb = dao.queryBuilder();


        Calendar cal = Calendar.getInstance();
        cal.setTime(endDate);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        endDate = cal.getTime();

        endDate = DatabaseHelper.convertDateToSqliteDate(endDate);
        startDate = DatabaseHelper.convertDateToSqliteDate(startDate);
        boolean includeOngoingTimeRegistration = false;
        Date now = new Date();
        if (endDate.after(now)) {
            Log.e(getContext(), LOG_TAG, "Ongoing time registration should be included in result...");
            includeOngoingTimeRegistration = true;
        }

        Where where = qb.where();
        try {
            where.ge("startTime", startDate);
            if (includeOngoingTimeRegistration) {
                Where orClause = where.lt("endTime", endDate).or().isNull("endTime");
                where.and(where, orClause);
            } else {
                where.and().le("endTime", endDate);
            }
            if (taskIds != null && !taskIds.isEmpty()) {
                where.and().in("taskId", taskIds);
            }
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not build the dates- and tasks-where-clause in the query...");
            throwFatalException(e);
        }
        qb.setWhere(where);

        try {
            PreparedQuery<TimeRegistration> pq = qb.prepare();
            Log.d(getContext(), LOG_TAG, "Prepared query: " + pq.toString());
            return dao.query(pq);
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query...");
            throwFatalException(e);
        }

        return null;
    }

    @Override
    public List<TimeRegistration> findAll(int lowerLimit, int maxRows) {
        QueryBuilder<TimeRegistration,Integer> qb = dao.queryBuilder();
        try {
            Log.d(getContext(), LOG_TAG, "The starting row for the query is " + lowerLimit);
            Log.d(getContext(), LOG_TAG, "The maximum number of rows to load is " + maxRows);
            qb.offset(Long.valueOf(lowerLimit));
            qb.limit(Long.valueOf(maxRows));
            qb.orderBy("startTime", false);
            PreparedQuery<TimeRegistration> pq = qb.prepare();
            Log.d(getContext(), LOG_TAG, pq.toString());
            return dao.query(pq);
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query...");
            throwFatalException(e);
        }
        return null;
    }

    @Override
    public TimeRegistration getPreviousTimeRegistration(TimeRegistration timeRegistration) {
        QueryBuilder<TimeRegistration,Integer> qb = dao.queryBuilder();
        try {
            qb.limit(1L);
            qb.orderBy("startTime", false);

            Where where = qb.where();
            where.le("endTime", timeRegistration.getStartTime());
            qb.setWhere(where);

            PreparedQuery<TimeRegistration> pq = qb.prepare();
            Log.d(getContext(), LOG_TAG, pq.toString());
            return dao.queryForFirst(pq);
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query...");
            throwFatalException(e);
        }
        return null;
    }

    @Override
    public TimeRegistration getNextTimeRegistration(TimeRegistration timeRegistration) {
        if (timeRegistration.getEndTime() == null) {
            return null;
        }

        QueryBuilder<TimeRegistration,Integer> qb = dao.queryBuilder();
        try {
            qb.limit(1L);
            qb.orderBy("startTime", true);

            Where where = qb.where();
            where.ge("startTime", timeRegistration.getEndTime());
            qb.setWhere(where);

            PreparedQuery<TimeRegistration> pq = qb.prepare();
            Log.d(getContext(), LOG_TAG, pq.toString());
            return dao.queryForFirst(pq);
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query...");
            throwFatalException(e);
        }

        return null;
    }

    @Override
    public long deleteAllInRange(Date minBoundary, Date maxBoundary) {
        long count = -1;
        Long countBefore = null;
        Long countAfter = null;

        DeleteBuilder<TimeRegistration,Integer> db = dao.deleteBuilder();

        try {
            countBefore = dao.countOf();

            if (minBoundary != null || maxBoundary != null) {
                Where where = db.where();
                if (minBoundary != null) {
                    where.ge("startTime", minBoundary);
                }
                if (maxBoundary != null) {
                    if (minBoundary != null) {
                        where.and().le("endTime", maxBoundary);
                    } else {
                        where.le("endTime", maxBoundary);
                    }
                }
                db.setWhere(where);
            }

            PreparedDelete<TimeRegistration> pd = db.prepare();
            Log.d(getContext(), LOG_TAG, pd.toString());
            dao.delete(pd);

            countAfter = dao.countOf();
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query...");
            throwFatalException(e);
        }

        count = countBefore - countAfter;

        Log.d(getContext(), LOG_TAG, "number of deleted records: " + count);
        return count;
    }

    @Override
    public boolean doesInterfereWithTimeRegistration(Date time) {
        if (time == null) {
            return false;
        }

        QueryBuilder<TimeRegistration,Integer> qb = dao.queryBuilder();
        try {
            qb.limit(1L);

            Where where = qb.where();
            where.le("startTime", time);
            Where orClause = where.isNull("endTime").or().gt("endTime", time);
            where.and(where, orClause);
            qb.setWhere(where);

            PreparedQuery<TimeRegistration> pq = qb.prepare();
            Log.d(getContext(), LOG_TAG, pq.toString());
            TimeRegistration tr = dao.queryForFirst(pq);
            if (tr != null) {
                return true;
            }
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query...");
            throwFatalException(e);
        }

        return false;
    }

    @Override
    public TimeRegistration findByDates(Date startDate, Date endDate) {
        List<TimeRegistration> timeRegistrations = null;
        QueryBuilder<TimeRegistration,Integer> qb = dao.queryBuilder();

        Where where = qb.where();
        try {
            where.eq("startTime", startDate);
            if (endDate != null) {
                where.and().eq("endTime", endDate);
            } else {
                where.and().isNull("endTime");
            }
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not build the dates- and tasks-where-clause in the query...");
            throwFatalException(e);
        }
        qb.setWhere(where);

        try {
            PreparedQuery<TimeRegistration> pq = qb.prepare();
            Log.d(getContext(), LOG_TAG, "Prepared query: " + pq.toString());
            timeRegistrations = dao.query(pq);
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query...");
            throwFatalException(e);
        }

        if(timeRegistrations == null || timeRegistrations.size() == 0 || timeRegistrations.size() > 1) {
            if (timeRegistrations == null || timeRegistrations.size() == 0) {
                return null;
            } else {
                String message = "The time registration data is corrupt. More than one time registration with the same start and end date is found in the database!";
                Log.e(getContext(), LOG_TAG, message);
                throw new CorruptTimeRegistrationDataException(message);
            }
        } else {
            return timeRegistrations.get(0);
        }
    }

    @Override
    public TimeRegistration findBySyncKey(String syncKey) {
        List<TimeRegistration> timeRegistrations = null;

        QueryBuilder<TimeRegistration, Integer> qb = dao.queryBuilder();
        try {
            qb.where().eq("syncKey", syncKey);
            PreparedQuery<TimeRegistration> pq = qb.prepare();
            timeRegistrations = dao.query(pq);
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query... Returning null.", e);
            return null;
        }

        if(timeRegistrations == null || timeRegistrations.size() == 0 || timeRegistrations.size() > 1) {
            if (timeRegistrations == null || timeRegistrations.size() == 0) {
                return null;
            } else {
                String message = "The time registration data is corrupt. More than one time registration with the same syncKey (" + syncKey + ") is found in the database!";
                Log.e(getContext(), LOG_TAG, message);
                throw new CorruptTimeRegistrationDataException(message);
            }
        } else {
            return timeRegistrations.get(0);
        }
    }

    @Override
    public List<TimeRegistration> findAllModifiedAfterOrUnSynced(Date lastModified) {
        QueryBuilder<TimeRegistration, Integer> qb = dao.queryBuilder();
        try {
            Where where = qb.where();
            where.gt("lastUpdated", lastModified);
            where.or().isNull("syncKey");
            PreparedQuery<TimeRegistration> pq = qb.prepare();
            return dao.query(pq);
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query... Returning null.", e);
            return null;
        }
    }
}
