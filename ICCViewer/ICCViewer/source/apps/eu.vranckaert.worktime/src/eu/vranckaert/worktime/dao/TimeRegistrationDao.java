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
import eu.vranckaert.worktime.model.Task;
import eu.vranckaert.worktime.model.TimeRegistration;

import java.util.Date;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 05/02/11
 * Time: 17:31
 */
public interface TimeRegistrationDao extends GenericDao<TimeRegistration, Integer> {
    /**
     * Find the latest time registration. Returns <b>null</b> if no time regstrations are found!
     * @return The latest time registration.
     */
    TimeRegistration getLatestTimeRegistration();

    /**
     * Find all time registrations bound to one specific task.
     * @param task The {@link Task}.
     * @return The list of {@link TimeRegistration} for that task.
     */
    List<TimeRegistration> findTimeRegistrationsForTask(Task task);

    /**
     * Find all time registrations bound to one of the specified tasks.
     * @param tasks The list of {@link Task}.
     * @return The list of {@link TimeRegistration} for the specified tasks.
     */
    List<TimeRegistration> findTimeRegistrationsForTaks(List<Task> tasks);

        /**
     * Find all time registrations matching the given criteria.
     * @param startDate The starting date is the lower limit of the list of {@link TimeRegistration}. Every time
     * registration must have a starting date greater than or equals to this date. This is a required value.
     * @param endDate The end date is the higher limit of the list of {@link TimeRegistration}. Every time registration
     * must have an end date lower than or equals to this date. This is a required value.
     * @param tasks A list of tasks to which the time registrations have to be linked. If the list is null or empty the
     * parameter will be ignored.
     * @return A list of {@link TimeRegistration} instances based on the specified criteria.
     */
    List<TimeRegistration> getTimeRegistrations(Date startDate, Date endDate, List<Task> tasks);

    /**
     * Find all time registrations within a certain limit.
     * @param lowerLimit The lower limit to find the time registrations for
     * @param maxRows The maximum number of rows to be loaded
     * @return A list of time registrations.
     */
    List<TimeRegistration> findAll(int lowerLimit, int maxRows);

    /**
     * Find the time registration which comes just before the time registration provided. Comparison is done on start
     * and ending time.
     * @param timeRegistration The {@link TimeRegistration} to search the previous instance of.
     * @return The previous time registration if any. Otherwise null;
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
     * Removes all {@link TimeRegistration} instance in a certain range. If one of both (or both) arguments is null, it
     * will be ignored. So if no minBoundary is specified all time registrations will be deleted where the end date is
     * before the maxBoundary. If both arguments are null everything will be deleted as in the
     * {@link eu.vranckaert.worktime.dao.TimeRegistrationDao#deleteAll()} method.
     * @param minBoundary The lower-boundary to check the time registrations start date against.
     * @param maxBoundary The higher-boundary to check the time registrations end date against.
     * @return The number of {@link TimeRegistration} instances that are removed permanently.
     */
    long deleteAllInRange(Date minBoundary, Date maxBoundary);


    /**
     * Checks if a given time if after (>=) the start time and before (<) the end time of any time registration. So
     * checks if another time
     * @param time The time to check for.
     * @return True if the time is part of another time registration, false if not.
     */
    boolean doesInterfereWithTimeRegistration(Date time);

    /**
     * Find a time registration that starts and ends on a specific time.
     * @param startDate The starting time of the time registration to look for.
     * @param endDate The ending time of the time registration to look for. Can be null if looking for an ongoing time
     *                registration.
     * @return The time registration that matches these dates. Null if nothing found.
     */
    TimeRegistration findByDates(Date startDate, Date endDate);

    /**
     * Find a time registration based on the sync-key.
     * @param syncKey The sync-key.
     * @return The time registration that has the provided sync-key or null.
     */
    TimeRegistration findBySyncKey(String syncKey);

    /**
     * Find all {@link TimeRegistration}s that have been modified after a certain date.
     * @param lastModified The date to be checked against.
     * @return A list of {@link TimeRegistration}s that have modified after the specific date.
     */
    List<TimeRegistration> findAllModifiedAfterOrUnSynced(Date lastModified);
}
