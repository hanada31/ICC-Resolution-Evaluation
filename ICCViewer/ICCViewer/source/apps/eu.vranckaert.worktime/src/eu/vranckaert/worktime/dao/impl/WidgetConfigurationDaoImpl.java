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
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import eu.vranckaert.worktime.dao.WidgetConfigurationDao;
import eu.vranckaert.worktime.dao.generic.GenericDaoImpl;
import eu.vranckaert.worktime.model.WidgetConfiguration;
import eu.vranckaert.worktime.utils.context.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WidgetConfigurationDaoImpl extends GenericDaoImpl<WidgetConfiguration, Integer> implements WidgetConfigurationDao {
    private static final String LOG_TAG = WidgetConfigurationDaoImpl.class.getSimpleName();

    @Inject
    public WidgetConfigurationDaoImpl(final Context context) {
        super(WidgetConfiguration.class, context);
    }

    @Override
    public List<WidgetConfiguration> findPerProjectId(int projectId) {
        QueryBuilder<WidgetConfiguration, Integer> qb = dao.queryBuilder();
        try {
            qb.where().eq("projectId", projectId);
            PreparedQuery<WidgetConfiguration> pq = qb.prepare();
            return dao.query(pq);
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query... Returning empty list.", e);
            return new ArrayList<WidgetConfiguration>();
        }
    }

    @Override
    public List<WidgetConfiguration> findPerTaskId(int taskId) {
        QueryBuilder<WidgetConfiguration, Integer> qb = dao.queryBuilder();
        try {
            qb.where().eq("taskId", taskId);
            PreparedQuery<WidgetConfiguration> pq = qb.prepare();
            return dao.query(pq);
        } catch (SQLException e) {
            Log.e(getContext(), LOG_TAG, "Could not start the query... Returning empty list.", e);
            return new ArrayList<WidgetConfiguration>();
        }
    }
}
