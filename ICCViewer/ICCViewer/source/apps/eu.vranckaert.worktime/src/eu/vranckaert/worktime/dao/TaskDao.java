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
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.Task;

import java.util.Date;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 05/02/11
 * Time: 16:56
 */
public interface TaskDao extends GenericDao<Task, Integer> {
    /**
     * Find all tasks linked to a certain {@link Project}.
     * @param project The project.
     * @return A list of {@link Task} instances linked to the {@link Project}.
     */
    List<Task> findTasksForProject(Project project);

    /**
     * Count the number of tasks for a project {@link Project}.
     * @param project The project.
     * @return The number of tasks found for the specified project.
     */
    int countTasksForProject(Project project);

    /**
     * Find all tasks linked to a certain {@link Project} for which the flag {@link Task#finished} is
     * {@link Boolean#FALSE}.
     * @param project The project.
     * @return A list of {@link Task} instances linked to the {@link Project} and with the flag {@link Task#finished}
     * set to {@link Boolean#FALSE}.
     */
    List<Task> findNotFinishedTasksForProject(Project project);

    /**
     * Find a specific task by name.
     * @param name The name of the task to search.
     * @param project The project of task.
     * @return The task with this specific name or null.
     */
    Task findByName(String name, Project project);

    /**
     * Find a task based on the sync-key.
     * @param syncKey The sync-key.
     * @return The task that has the provided sync-key or null.
     */
    Task findBySyncKey(String syncKey);

    /**
     * Find all {@link Task}s that have been modified after a certain date.
     * @param lastModified The date to be checked against.
     * @return A list of {@link Task}s that have modified after the specific date.
     */
    List<Task> findAllModifiedAfterOrUnSynced(Date lastModified);
}
