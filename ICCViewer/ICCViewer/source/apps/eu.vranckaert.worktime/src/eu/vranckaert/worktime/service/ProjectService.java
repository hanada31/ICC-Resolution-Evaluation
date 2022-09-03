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

import eu.vranckaert.worktime.exceptions.AtLeastOneProjectRequiredException;
import eu.vranckaert.worktime.exceptions.ProjectHasOngoingTimeRegistration;
import eu.vranckaert.worktime.exceptions.ProjectStillHasTasks;
import eu.vranckaert.worktime.model.Project;

import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 06/02/11
 * Time: 04:19
 */
public interface ProjectService {
    /**
     * Persist a new project instance.
     * @param project The {@link eu.vranckaert.worktime.model.Project} instance to persist.
     * @return The persisted instance.
     */
    Project save(Project project);

    /**
     * Find all persisted projects.
     * @return All projects.
     */
    List<Project> findAll();

    /**
     * Remove a project.
     * @param project The project to remove.
     * @param force If set to true all possible exception will be ignored and the project will be removed anyway.
     * @throws AtLeastOneProjectRequiredException If this project is the last project available this exception is
     * thrown.
     * @throws ProjectStillHasTasks If this project is linked with tasks (and possibly with time registrations) this
     * exception is thrown.
     * @throws ProjectHasOngoingTimeRegistration If the project is linked with ongoing time registrations (via it's
     * tasks).
     */
    void remove(Project project, boolean force) throws AtLeastOneProjectRequiredException, ProjectStillHasTasks, ProjectHasOngoingTimeRegistration;

    /**
     * Checks if a certain name for a project is already in use.
     * @param projectName The name to check for duplicates.
     * @return {@link Boolean#TRUE} if a project with this name already exists, {@link Boolean#FALSE} if not.
     */
    boolean isNameAlreadyUsed(String projectName);

    /**
     * Checks if a certain name for a project is already in use, excluding the name of the excludedProject. Preferred
     * use in update-mode.
     * @param projectName The name to check for duplicates.
     * @param excludedProject The project which name should be ignored during the check.
     * @return {@link Boolean#TRUE} if a project with this name already exists, {@link Boolean#FALSE} if not.
     */
    boolean isNameAlreadyUsed(String projectName, Project excludedProject);

    /**
     * Retrieve the selected project to be displayed in the widget and to which new
     * {@link eu.vranckaert.worktime.model.TimeRegistration} instances will be linked to.
     * @param widgetId The id of the widget for which the selected project should be retrieved.
     * @return The selected project. If no selected project is found for the specified widget id this method returns
     * the default project.
     */
    Project getSelectedProject(int widgetId);

    /**
     * Change the selected project to be displayed in the widget and to which new
     * {@link eu.vranckaert.worktime.model.TimeRegistration} instances will be linked. CAUTION: changing the selected
     * project for the widget does not update the widget!
     * @param widgetId The id of the widget for which the project should be changed.
     * @param project The project to be set as selected project.
     */
    void setSelectedProject(int widgetId, Project project);

    /**
     * Change the selected projected to be used in the widgets and to which new
     * {@link eu.vranckaert.worktime.model.TimeRegistration} instances will be linked. CAUTION: changing the selected
     * project for the widget does not update the widget!
     * @param fromProject The project that used to be selected.
     * @param toProject The project that should now be selected.
     * @return Returns a list of widget ids for which the project has changed.
     */
    List<Integer> changeSelectedProject(Project fromProject, Project toProject);

    /**
     * Updates an existing project.
     * @param project The project to update.
     * @return The updated project.
     */
    Project update(Project project);

    /**
     * Refreshes the project data. Should only be used when the project is expected not be loaded entirely (only the
     * id).
     * @param project The project to refresh.
     */
    void refresh(Project project);

    /**
     * Retrieve all projects that have the flag {@link Project#finished} set to {@link Boolean#FALSE}.
     * @return A list of projects filtered on the finished-flag.
     */
    List<Project> findUnfinishedProjects();

    /**
     * Change the default project upon marking a project as finished which is set to be the default project.
     * @param projectMarkedFinished The default project which is to be marked as finished.
     * @return The project that is now marked as default project.
     */
    Project changeDefaultProjectUponProjectMarkedFinished(Project projectMarkedFinished);

    /**
     * Removes all items.
     */
    void removeAll();

    /**
     * Inserts the default data in the database!
     */
    void insertDefaultProjectAndTaskData();

    /**
     * Checks if a provided project does exist or not based on a search on it's identifier. This mainly should be used
     * after a synchronization.
     * @param project The project to be checked for.
     * @return True is the project exists, false if not.
     */
    boolean checkProjectExisting(Project project);

    /**
     * Checks if a project should be reloaded (so if the version in the database is modified after the provided projects
     * last updated timestamp).
     * @param project The project to be checked for.
     * @return True is the project should be reloaded, false if not.
     */
    boolean checkReloadProject(Project project);
}
