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
import eu.vranckaert.worktime.dao.ProjectDao;
import eu.vranckaert.worktime.dao.SyncRemovalCacheDao;
import eu.vranckaert.worktime.dao.generic.GenericDaoImpl;
import eu.vranckaert.worktime.exceptions.CorruptProjectDataException;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.SyncRemovalCache;
import eu.vranckaert.worktime.utils.context.Log;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;


public class ProjectDaoImpl extends GenericDaoImpl<Project, Integer> implements ProjectDao {
    private static final String LOG_TAG = ProjectDaoImpl.class.getSimpleName();

    private SyncRemovalCacheDao syncRemovalCache;

    @Inject
    public ProjectDaoImpl(final Context context, final SyncRemovalCacheDao syncRemovalCache) {
        super(Project.class, context);
        this.syncRemovalCache = syncRemovalCache;
    }

    @Override
    public Project save(Project entity) {
        entity.setLastUpdated(new Date());
        return super.save(entity);
    }

    @Override
    public Project update(Project entity) {
        entity.setLastUpdated(new Date());
        return super.update(entity);
    }

    @Override
    public void delete(Project entity) {
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
        List<Project> entities = findAll();
        for (Project entity : entities) {
            if (entity.getSyncKey() != null) {
                if (syncRemovalCache.findById(entity.getSyncKey()) == null) {
                    SyncRemovalCache cache = new SyncRemovalCache(entity.getSyncKey(), entity.getClass().getSimpleName());
                    syncRemovalCache.save(cache);
                }
            }
        }
        super.deleteAll();
    }

    @Override


    /**
     * {@inheritDoc}
     */
    public boolean isNameAlreadyUsed(String projectName) {
        List<Project> projects = null;
        QueryBuilder<Project, Integer> qb = dao.queryBuilder();
        try {
            qb.where().eq("name", projectName);
            PreparedQuery<Project> pq = qb.prepare();
            projects = dao.query(pq);
        } catch (SQLException e) {
            Log.d(getContext(), LOG_TAG, "Could not start the query... Returning false");
            return false;
        }

        if(projects == null || projects.size() == 0) {
            Log.d(getContext(), LOG_TAG, "The name is not yet used!");
            return false;
        }
        Log.d(getContext(), LOG_TAG, "The name is already in use!");
        return true;
    }

    public Project findDefaultProject() {
        List<Project> projects = null;

        QueryBuilder<Project, Integer> qb = dao.queryBuilder();
        try {
            qb.where().eq("defaultValue", true);
            PreparedQuery<Project> pq = qb.prepare();
            projects = dao.query(pq);
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query... Returning null.", e);
            return null;
        }

        if(projects == null || projects.size() == 0 || projects.size() > 1) {
            String message = null;
            if (projects == null || projects.size() == 0) {
                message = "The project data is corrupt. No default project is found!";
            } else {
                message = "The project data is corrupt. More than one default project is found in the database!";
            }
            Log.e(getContext(), LOG_TAG, message);
            throw new CorruptProjectDataException(message);
        } else {
            return projects.get(0);
        }
    }

    @Override
    public List<Project> findProjectsOnFinishedFlag(boolean finished) {
        QueryBuilder<Project, Integer> qb = dao.queryBuilder();
        try {
            qb.where().eq("finished", finished);
            PreparedQuery<Project> pq = qb.prepare();
            return dao.query(pq);
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query... Returning null.", e);
            return null;
        }
    }

    @Override
    public Project findByName(String name) {
        List<Project> projects = null;

        QueryBuilder<Project, Integer> qb = dao.queryBuilder();
        try {
            qb.where().eq("name", name);
            PreparedQuery<Project> pq = qb.prepare();
            projects = dao.query(pq);
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query... Returning null.", e);
            return null;
        }

        if(projects == null || projects.size() == 0 || projects.size() > 1) {
            if (projects == null || projects.size() == 0) {
                return null;
            } else {
                String message = "The task data is corrupt. More than one task with the same name (" + name + ") is found in the database!";
                Log.e(getContext(), LOG_TAG, message);
                throw new CorruptProjectDataException(message);
            }
        } else {
            return projects.get(0);
        }
    }

    @Override
    public Project findBySyncKey(String syncKey) {
        List<Project> projects = null;

        QueryBuilder<Project, Integer> qb = dao.queryBuilder();
        try {
            qb.where().eq("syncKey", syncKey);
            PreparedQuery<Project> pq = qb.prepare();
            projects = dao.query(pq);
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query... Returning null.", e);
            return null;
        }

        if(projects == null || projects.size() == 0 || projects.size() > 1) {
            if (projects == null || projects.size() == 0) {
                return null;
            } else {
                String message = "The task data is corrupt. More than one task with the same syncKey (" + syncKey + ") is found in the database!";
                Log.e(getContext(), LOG_TAG, message);
                throw new CorruptProjectDataException(message);
            }
        } else {
            return projects.get(0);
        }
    }

    @Override
    public List<Project> findAllModifiedAfterOrUnSynced(Date lastModified) {
        QueryBuilder<Project, Integer> qb = dao.queryBuilder();
        try {
            Where where = qb.where();
            where.gt("lastUpdated", lastModified);
            where.or().isNull("syncKey");
            PreparedQuery<Project> pq = qb.prepare();
            return dao.query(pq);
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query... Returning null.", e);
            return null;
        }
    }

    @Override
    public void setLastModified(List<String> projectNames, Date date) {
        UpdateBuilder<Project, Integer> qb = dao.updateBuilder();

        try {
            qb.updateColumnValue("lastUpdated", date);
            qb.where().in("name", projectNames);
            PreparedUpdate<Project> pu = qb.prepare();
            dao.update(pu);
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query... Returning null.", e);
        }
    }
}
