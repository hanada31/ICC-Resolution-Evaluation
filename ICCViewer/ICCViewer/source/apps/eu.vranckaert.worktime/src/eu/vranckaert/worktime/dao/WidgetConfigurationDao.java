/*
 * Copyright 2012 Dirk Vranckaert
 *
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
import eu.vranckaert.worktime.model.WidgetConfiguration;

import java.util.List;

public interface WidgetConfigurationDao extends GenericDao<WidgetConfiguration, Integer> {
    /**
     * Find all {@link WidgetConfiguration} instances that are stored with a specific project id.
     * @param projectId The id of the project to search the widget configuration entities for.
     * @return A list of {@link WidgetConfiguration}.
     */
    List<WidgetConfiguration> findPerProjectId(int projectId);

    /**
     * Find all {@link WidgetConfiguration} instances that are stored with a specific task id.
     * @param taskId The id of the project to search the widget configuration entities for.
     * @return A list of {@link WidgetConfiguration}.
     */
    List<WidgetConfiguration> findPerTaskId(int taskId);
}
