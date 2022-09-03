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

import java.util.Date;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 05/02/11
 * Time: 16:56
 */
public interface ProjectDao extends GenericDao<Project, Integer> {
    /**
     * Checks if a certain name for a project is already in use.
     *
     * @param projectName The name of the project to check for.
     * @return {@link Boolean#TRUE} if a project with this name already exists, {@link Boolean#FALSE} if not.
     */
    boolean isNameAlreadyUsed(String projectName);

    /**
     * Find the default {@link Project}.
     * @return The default {@link Project}.
     */
    Project findDefaultProject();

    /**
     * Retrieve all projects that have the flag {@link Project#finished} set to specified parameter.
     * @param finished The project-flag to filter on.
     * @return A list of projects filtered on the finished-flag.
     */
    List<Project> findProjectsOnFinishedFlag(boolean finished);

    /**
     * Find a specific project by name.
     * @param name The name of the project to search.
     * @return The project with this specific name or null.
     */
    Project findByName(String name);

    /**
     * Find a project based on the sync-key.
     * @param syncKey The sync-key.
     * @return The project that has the provided sync-key or null.
     */
    Project findBySyncKey(String syncKey);

    /**
     * Find all {@link Project}s that have been modified after a certain date.
     * @param lastModified The date to be checked against.
     * @return A list of {@link Project}s that have modified after the specific date.
     */
    List<Project> findAllModifiedAfterOrUnSynced(Date lastModified);

    void setLastModified(List<String> projectNames, Date date);
}
