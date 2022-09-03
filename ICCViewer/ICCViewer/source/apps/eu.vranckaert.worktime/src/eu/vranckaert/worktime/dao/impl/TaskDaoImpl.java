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
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import eu.vranckaert.worktime.dao.SyncRemovalCacheDao;
import eu.vranckaert.worktime.dao.TaskDao;
import eu.vranckaert.worktime.dao.generic.GenericDaoImpl;
import eu.vranckaert.worktime.exceptions.CorruptTaskDataException;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.SyncRemovalCache;
import eu.vranckaert.worktime.model.Task;
import eu.vranckaert.worktime.utils.context.Log;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class TaskDaoImpl extends GenericDaoImpl<Task, Integer> implements TaskDao {
    private static final String LOG_TAG = TaskDaoImpl.class.getSimpleName();

    private SyncRemovalCacheDao syncRemovalCache;

    @Inject
    public TaskDaoImpl(final Context context, final SyncRemovalCacheDao syncRemovalCache) {
        super(Task.class, context);
        this.syncRemovalCache = syncRemovalCache;
    }

    @Override
    public Task save(Task entity) {
        entity.setLastUpdated(new Date());
        return super.save(entity);
    }

    @Override
    public Task update(Task entity) {
        entity.setLastUpdated(new Date());
        return super.update(entity);
    }

    @Override
    public void delete(Task entity) {
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
        List<Task> entities = findAll();
        for (Task entity : entities) {
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
    public List<Task> findTasksForProject(Project project) {
        QueryBuilder<Task, Integer> qb = dao.queryBuilder();
        try {
            qb.where().eq("projectId", project.getId());
            PreparedQuery<Task> pq = qb.prepare();
            return dao.query(pq);
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query... Returning null.", e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public int countTasksForProject(Project project) {
        int rowCount = 0;
        List<String[]> results = null;
        try {
            GenericRawResults rawResults = dao.queryRaw("select count(*) from task where projectId = " + project.getId());
            results = rawResults.getResults();
        } catch (SQLException e) {
            throwFatalException(e);
        }

        if (results != null && results.size() > 0) {
            rowCount = Integer.parseInt(results.get(0)[0]);
        }

        Log.d(getContext(), LOG_TAG, "Rowcount: " + rowCount);

        return rowCount;
    }

    /**
     * {@inheritDoc}
     */
    public List<Task> findNotFinishedTasksForProject(Project project) {
        QueryBuilder<Task, Integer> qb = dao.queryBuilder();
        try {
            qb.where().eq("projectId", project.getId())
                    .and().eq("finished", false);
            PreparedQuery<Task> pq = qb.prepare();
            return dao.query(pq);
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query... Returning null.", e);
            return null;
        }
    }

    @Override
    public Task findByName(String name, Project project) {
        List<Task> tasks = null;

        QueryBuilder<Task, Integer> qb = dao.queryBuilder();
        try {
            qb.where().eq("name", name).and().eq("projectId", project.getId());
            PreparedQuery<Task> pq = qb.prepare();
            tasks = dao.query(pq);
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query... Returning null.", e);
            return null;
        }

        if(tasks == null || tasks.size() == 0 || tasks.size() > 1) {
            if (tasks == null || tasks.size() == 0) {
                return null;
            } else {
                String message = "The task data is corrupt. More than one task with the same name (" + name + ") is found in the database for project '" + project.getName() + "'!";
                Log.e(getContext(), LOG_TAG, message);
                throw new CorruptTaskDataException(message);
            }
        } else {
            return tasks.get(0);
        }
    }

    @Override
    public Task findBySyncKey(String syncKey) {
        List<Task> tasks = null;

        QueryBuilder<Task, Integer> qb = dao.queryBuilder();
        try {
            qb.where().eq("syncKey", syncKey);
            PreparedQuery<Task> pq = qb.prepare();
            tasks = dao.query(pq);
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query... Returning null.", e);
            return null;
        }

        if(tasks == null || tasks.size() == 0 || tasks.size() > 1) {
            if (tasks == null || tasks.size() == 0) {
                return null;
            } else {
                String message = "The task data is corrupt. More than one task with the same sync key (" + syncKey + ") is found in the database!";
                Log.e(getContext(), LOG_TAG, message);
                throw new CorruptTaskDataException(message);
            }
        } else {
            return tasks.get(0);
        }
    }

    @Override
    public List<Task> findAllModifiedAfterOrUnSynced(Date lastModified) {
        QueryBuilder<Task, Integer> qb = dao.queryBuilder();
        try {
            Where where = qb.where();
            where.gt("lastUpdated", lastModified);
            where.or().isNull("syncKey");
            PreparedQuery<Task> pq = qb.prepare();
            return dao.query(pq);
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query... Returning null.", e);
            return null;
        }
    }
}
