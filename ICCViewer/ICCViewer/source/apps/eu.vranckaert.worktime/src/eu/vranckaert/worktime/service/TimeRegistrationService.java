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

import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.Task;
import eu.vranckaert.worktime.model.TimeRegistration;

import java.util.Date;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 07/02/11
 * Time: 00:13
 */
public interface TimeRegistrationService {
    /**
     * Find all persisted time registrations.
     * @return All time registrations.
     */
    List<TimeRegistration> findAll();

    /**
     * Find all time registrations related to a list of tasks.
     * @param tasks The list of {@link Task} to which the time registrations has to be related to.
     * @return A list of {@link TimeRegistration} instances found for the specified list of tasks.
     */
    List<TimeRegistration> getTimeRegistrationForTasks(List<Task> tasks);

    /**
     * Find all time registrations matching the given criteria.
     * @param startDate The starting date is the lower limit of the list of {@link TimeRegistration}. Every time
     * registration must have a starting date greater than or equals to this date. This is a required value.
     * @param endDate The end date is the higher limit of the list of {@link TimeRegistration}. Every time registration
     * must have an end date lower than or equals to this date. This is a required value.
     * @param project The project to which a time registration must be linked. If this parameter is null it will be
     * ignored. If the task-parameter is not null the project will be ignored as the specified task is supposed to be
     * linked to this project.
     * @param task The task to which a time registration must be linked. If this parameter is null it will be ignored.
     * If it's not null the project-parameter will be ignored as the task is always supposed to be linked to the
     * specified project.
     * @return A list of {@link TimeRegistration} instances based on the specified criteria.
     */
    List<TimeRegistration> getTimeRegistrations(Date startDate, Date endDate, Project project, Task task);

    /**
     * Create a new instance of {@link TimeRegistration}.
     * @param timeRegistration The instance to create.
     */
    void create(TimeRegistration timeRegistration);

    /**
     * Update a time registration instance.
     * @param timeRegistration The instance to update.
     */
    void update(TimeRegistration timeRegistration);

    /**
     * Find the latest time registration. Returns <b>null</b> if no time registrations are found!
     * @return The latest time registration.
     */
    TimeRegistration getLatestTimeRegistration();

    /**
     * Removes an existing time registration.
     *
     * @param timeRegistration The registration to remove.
     */
    void remove(TimeRegistration timeRegistration);

    /**
     * Find a specific time registration.
     * @param id The unique identifier of the time registration.
     * @return Null if nothing found for the identifier, otherwise the time registration.
     */
    TimeRegistration get(Integer id);

    /**
     * Find all time registrations within a certain limit.
     * @param lowerLimit The lower limit to find the time registrations for
     * @param maxRows The maximum number of rows to be loaded
     * @return A list of time registrations of count lowerLimit - higherLimit.
     */
    List<TimeRegistration> findAll(int lowerLimit, int maxRows);

    /**
     * Count the total number of time registrations available.
     * @return The total number of time registrations.
     */
    Long count();

    /**
     * Find the time registration which comes just before the time registration provided. Comparison is done on start
     * and ending time.
     * @param timeRegistration The {@link TimeRegistration} to search the previous instance of.
     * @return The previous time registration if any. Otherwise null!
     */
    TimeRegistration getPreviousTimeRegistration(TimeRegistration timeRegistration);

    /**
     * Find the time registration which fomes just after the time registration provided. Comparison is done on start
     * and ending time.
     * @param timeRegistration The {@link TimeRegistration} to search the next instance of.
     * @return The next time registration if any. Otherwise null!
     */
    TimeRegistration getNextTimeRegistration(TimeRegistration timeRegistration);

    /**
     * Fully initialize all elements of a certain {@link TimeRegistration} instance.
     * @param timeRegistration The time registration to be fully initialized.
     */
    void fullyInitialize(TimeRegistration timeRegistration);

    /**
     * Removes all items.
     */
    void removeAll();

    /**
     * Removes all {@link TimeRegistration} instance in a certain range. If one of both (or both) arguments is null, it
     * will be ignored. So if no minBoundary is specified all time registrations will be deleted where the end date is
     * before the maxBoundary. If both arguments are null everything will be deleted as in the
     * {@link eu.vranckaert.worktime.service.TimeRegistrationService#removeAll()} method.
     * @param minBoundary The lower-boundary to check the time registrations start date against.
     * @param maxBoundary The higher-boundary to check the time registrations end date against.
     * @return The number of {@link TimeRegistration} instances that are removed permanently.
     */
    long removeAllInRange(Date minBoundary, Date maxBoundary);

    /**
     * Checks if a given time if after (>=) the start time and before (<) the end time of any time registration. So
     * checks if another time
     * @param time The time to check for.
     * @return True if the time is part of another time registration, false if not.
     */
    boolean doesInterfereWithTimeRegistration(Date time);

    /**
     * Refreshes the time registration data. Should only be used when the time registration is expected not be loaded
     * entirely (only the id).
     * @param timeRegistration The time Registration to refresh.
     */
    void refresh(TimeRegistration timeRegistration);

    /**
     * Checks if a provided time registration does exist or not based on a search on it's identifier. This mainly should
     * be used after a synchronization.
     * @param timeRegistration The time registration to be checked for.
     * @return True is the time registration exists, false if not.
     */
    boolean checkTimeRegistrationExisting(TimeRegistration timeRegistration);

    /**
     * Checks if a time registration should be reloaded (so if the version in the database is modified after the
     * provided time registration's last updated timestamp).
     * @param timeRegistration The time registration to be checked for.
     * @return True is the time registration should be reloaded, false if not.
     */
    boolean checkReloadTimeRegistration(TimeRegistration timeRegistration);
}
