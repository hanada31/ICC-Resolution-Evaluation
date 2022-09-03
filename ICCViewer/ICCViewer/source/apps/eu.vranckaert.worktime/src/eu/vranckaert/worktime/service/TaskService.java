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

package eu.vranckaert.worktime.service;

import eu.vranckaert.worktime.exceptions.AtLeastOneTaskRequiredException;
import eu.vranckaert.worktime.exceptions.TaskStillInUseException;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.Task;

import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 28/03/11
 * Time: 17:28
 */
public interface TaskService {
    /**
     * Find all tasks linked to a certain {@link Project}.
     * @param project The project.
     * @return A list of {@link Task} instances linked to the {@link Project}.
     */
    List<Task> findTasksForProject(Project project);

    /**
     * Find all tasks linked to a certain {@link Project} for which the flag {@link Task#finished} is
     * {@link Boolean#FALSE}.
     * @param project The project.
     * @return A list of {@link Task} instances linked to the {@link Project} and with the flag {@link Task#finished}
     * set to {@link Boolean#FALSE}.
     */
    List<Task> findNotFinishedTasksForProject(Project project);

    /**
     * Persist a new {@link Task} instance.
     * @param task The new {@link Task} to persist.
     * @return The persisted {@link Task} instance.
     */
    Task save(Task task);

    /**
     * Updates an existing {@link Task} instance.
     * @param task The {@link Task} instance to update.
     * @return The updated instance.
     */
    Task update(Task task);

    /**
     * Remove a task.
     * @param task The task to remove.
     * @param force If set to {@link Boolean#TRUE} all {@link eu.vranckaert.worktime.model.TimeRegistration} instances
     * linked to the task will be deleted first, then the tasj itself. If set to {@link Boolean#FALSE} nothing will
     * happen.
     * @throws TaskStillInUseException If the task is coupled to time registrations and the force-option is not
     * used this exception is thrown.
     * @throws AtLeastOneTaskRequiredException If the task to be deleted is the only tasks that is still related to it's
     * {@link Project} it cannot be removed.
     */
    void remove(Task task, boolean force) throws TaskStillInUseException, AtLeastOneTaskRequiredException;

    /**
     * Refreshes the task status.
     * @param task The task to refresh.
     */
    void refresh(Task task);

    /**
     * Removes all items.
     */
    void removeAll();

    /**
     * Retrieve the selected task to be displayed in the widget and to which new
     * {@link eu.vranckaert.worktime.model.TimeRegistration} instances will be linked to.
     * @param widgetId The id of the widget for which the selected task should be retrieved.
     * @return The selected task. If no selected task is found for the specified widget id this method returns the first
     * task of the default project. If no default project is found or no tasks are linked to it a
     * {@link RuntimeException} is thrown because this means the application data is corrupt!
     */
    Task getSelectedTask(int widgetId);

    /**
     * Change the selected task to be displayed in the widget and to which new
     * {@link eu.vranckaert.worktime.model.TimeRegistration} instances will be linked. CAUTION: changing the selected
     * task for the widget does not update the widget! The project-id in the corresponding
     * {@link eu.vranckaert.worktime.model.WidgetConfiguration} will be removed and the task-id will be stored.
     * @param widgetId The id of the widget for which the task should be changed.
     * @param task The task to be set as selected project.
     */
    void setSelectedTask(Integer widgetId, Task task);

    /**
     * Find all tasks in the system.
     * @return The list of {@link Task}s available.
     */
    List<Task> findAll();

    /**
     * Checks if a provided task does exist or not based on a search on it's identifier. This mainly should be used
     * after a synchronization.
     * @param task The task to be checked for.
     * @return True is the task exists, false if not.
     */
    boolean checkTaskExisting(Task task);

    /**
     * Checks if a task should be reloaded (so if the version in the database is modified after the provided tasks'
     * last updated timestamp).
     * @param task The task to be checked for.
     * @return True is the task should be reloaded, false if not.
     */
    boolean checkReloadTask(Task task);
}
