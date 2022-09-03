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

package eu.vranckaert.worktime.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * User: Dirk Vranckaert
 * Date: 11/01/13
 * Time: 09:42
 */
@DatabaseTable
public class SyncHistory {
    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField(dataType = DataType.DATE_STRING)
    private Date started;
    @DatabaseField(dataType = DataType.DATE_STRING)
    private Date ended;
    @DatabaseField(dataType = DataType.ENUM_STRING)
    private SyncHistoryStatus status;
    @DatabaseField(dataType = DataType.ENUM_STRING)
    private SyncHistoryAction action;
    @DatabaseField
    private String failureReason;

    @DatabaseField
    private Integer numOutgoingAcceptedProjectChanges;
    @DatabaseField
    private Integer numOutgoingMergedProjectChanges;
    @DatabaseField
    private Integer numOutgoingNoActionProjectChanges;
    @DatabaseField
    private Integer numOutgoingNotAcceptedProjectChanges;
    @DatabaseField
    private Integer numOutgoingAcceptedTaskChanges;
    @DatabaseField
    private Integer numOutgoingMergedTaskChanges;
    @DatabaseField
    private Integer numOutgoingNoActionTaskChanges;
    @DatabaseField
    private Integer numOutgoingNotAcceptedTaskChanges;
    @DatabaseField
    private Integer numOutgoingAcceptedTimeRegistrationChanges;
    @DatabaseField
    private Integer numOutgoingMergedTimeRegistrationChanges;
    @DatabaseField
    private Integer numOutgoingNoActionTimeRegistrationChanges;
    @DatabaseField
    private Integer numOutgoingNotAcceptedTimeRegistrationChanges;
    @DatabaseField
    private Integer numOutgoingProjectsRemoved;
    @DatabaseField
    private Integer numOutgoingTasksRemoved;
    @DatabaseField
    private Integer numOutgoingTimeRegistrationsRemoved;
    @DatabaseField
    private Integer numIncomingProjectChanges;
    @DatabaseField
    private Integer numIncomingTaskChanges;
    @DatabaseField
    private Integer numIncomingTimeRegistrationChanges;
    @DatabaseField
    private Integer numIncomingProjectsRemoved;
    @DatabaseField
    private Integer numIncomingTasksRemoved;
    @DatabaseField
    private Integer numIncomingTimeRegistrationsRemoved;

    public SyncHistory() {
        this.started = new Date();
        this.status = SyncHistoryStatus.BUSY;
        this.action = SyncHistoryAction.CHECK_DEVICE;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public Date getEnded() {
        return ended;
    }

    public void setEnded(Date ended) {
        this.ended = ended;
    }

    public SyncHistoryStatus getStatus() {
        return status;
    }

    public void setStatus(SyncHistoryStatus status) {
        this.status = status;
    }

    public SyncHistoryAction getAction() {
        return action;
    }

    public void setAction(SyncHistoryAction action) {
        this.action = action;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Integer getNumOutgoingAcceptedProjectChanges() {
        return numOutgoingAcceptedProjectChanges;
    }

    public void setNumOutgoingAcceptedProjectChanges(Integer numOutgoingAcceptedProjectChanges) {
        this.numOutgoingAcceptedProjectChanges = numOutgoingAcceptedProjectChanges;
    }

    public Integer getNumOutgoingMergedProjectChanges() {
        return numOutgoingMergedProjectChanges;
    }

    public void setNumOutgoingMergedProjectChanges(Integer numOutgoingMergedProjectChanges) {
        this.numOutgoingMergedProjectChanges = numOutgoingMergedProjectChanges;
    }

    public Integer getNumOutgoingNoActionProjectChanges() {
        return numOutgoingNoActionProjectChanges;
    }

    public void setNumOutgoingNoActionProjectChanges(Integer numOutgoingNoActionProjectChanges) {
        this.numOutgoingNoActionProjectChanges = numOutgoingNoActionProjectChanges;
    }

    public Integer getNumOutgoingNotAcceptedProjectChanges() {
        return numOutgoingNotAcceptedProjectChanges;
    }

    public void setNumOutgoingNotAcceptedProjectChanges(Integer numOutgoingNotAcceptedProjectChanges) {
        this.numOutgoingNotAcceptedProjectChanges = numOutgoingNotAcceptedProjectChanges;
    }

    public Integer getNumOutgoingAcceptedTaskChanges() {
        return numOutgoingAcceptedTaskChanges;
    }

    public void setNumOutgoingAcceptedTaskChanges(Integer numOutgoingAcceptedTaskChanges) {
        this.numOutgoingAcceptedTaskChanges = numOutgoingAcceptedTaskChanges;
    }

    public Integer getNumOutgoingMergedTaskChanges() {
        return numOutgoingMergedTaskChanges;
    }

    public void setNumOutgoingMergedTaskChanges(Integer numOutgoingMergedTaskChanges) {
        this.numOutgoingMergedTaskChanges = numOutgoingMergedTaskChanges;
    }

    public Integer getNumOutgoingNoActionTaskChanges() {
        return numOutgoingNoActionTaskChanges;
    }

    public void setNumOutgoingNoActionTaskChanges(Integer numOutgoingNoActionTaskChanges) {
        this.numOutgoingNoActionTaskChanges = numOutgoingNoActionTaskChanges;
    }

    public Integer getNumOutgoingNotAcceptedTaskChanges() {
        return numOutgoingNotAcceptedTaskChanges;
    }

    public void setNumOutgoingNotAcceptedTaskChanges(Integer numOutgoingNotAcceptedTaskChanges) {
        this.numOutgoingNotAcceptedTaskChanges = numOutgoingNotAcceptedTaskChanges;
    }

    public Integer getNumOutgoingAcceptedTimeRegistrationChanges() {
        return numOutgoingAcceptedTimeRegistrationChanges;
    }

    public void setNumOutgoingAcceptedTimeRegistrationChanges(Integer numOutgoingAcceptedTimeRegistrationChanges) {
        this.numOutgoingAcceptedTimeRegistrationChanges = numOutgoingAcceptedTimeRegistrationChanges;
    }

    public Integer getNumOutgoingMergedTimeRegistrationChanges() {
        return numOutgoingMergedTimeRegistrationChanges;
    }

    public void setNumOutgoingMergedTimeRegistrationChanges(Integer numOutgoingMergedTimeRegistrationChanges) {
        this.numOutgoingMergedTimeRegistrationChanges = numOutgoingMergedTimeRegistrationChanges;
    }

    public Integer getNumOutgoingNoActionTimeRegistrationChanges() {
        return numOutgoingNoActionTimeRegistrationChanges;
    }

    public void setNumOutgoingNoActionTimeRegistrationChanges(Integer numOutgoingNoActionTimeRegistrationChanges) {
        this.numOutgoingNoActionTimeRegistrationChanges = numOutgoingNoActionTimeRegistrationChanges;
    }

    public Integer getNumOutgoingNotAcceptedTimeRegistrationChanges() {
        return numOutgoingNotAcceptedTimeRegistrationChanges;
    }

    public void setNumOutgoingNotAcceptedTimeRegistrationChanges(Integer numOutgoingNotAcceptedTimeRegistrationChanges) {
        this.numOutgoingNotAcceptedTimeRegistrationChanges = numOutgoingNotAcceptedTimeRegistrationChanges;
    }

    public Integer getNumOutgoingProjectsRemoved() {
        return numOutgoingProjectsRemoved;
    }

    public void setNumOutgoingProjectsRemoved(Integer numOutgoingProjectsRemoved) {
        this.numOutgoingProjectsRemoved = numOutgoingProjectsRemoved;
    }

    public Integer getNumOutgoingTasksRemoved() {
        return numOutgoingTasksRemoved;
    }

    public void setNumOutgoingTasksRemoved(Integer numOutgoingTasksRemoved) {
        this.numOutgoingTasksRemoved = numOutgoingTasksRemoved;
    }

    public Integer getNumOutgoingTimeRegistrationsRemoved() {
        return numOutgoingTimeRegistrationsRemoved;
    }

    public void setNumOutgoingTimeRegistrationsRemoved(Integer numOutgoingTimeRegistrationsRemoved) {
        this.numOutgoingTimeRegistrationsRemoved = numOutgoingTimeRegistrationsRemoved;
    }

    public Integer getNumIncomingProjectChanges() {
        return numIncomingProjectChanges;
    }

    public void setNumIncomingProjectChanges(Integer numIncomingProjectChanges) {
        this.numIncomingProjectChanges = numIncomingProjectChanges;
    }

    public Integer getNumIncomingTaskChanges() {
        return numIncomingTaskChanges;
    }

    public void setNumIncomingTaskChanges(Integer numIncomingTaskChanges) {
        this.numIncomingTaskChanges = numIncomingTaskChanges;
    }

    public Integer getNumIncomingTimeRegistrationChanges() {
        return numIncomingTimeRegistrationChanges;
    }

    public void setNumIncomingTimeRegistrationChanges(Integer numIncomingTimeRegistrationChanges) {
        this.numIncomingTimeRegistrationChanges = numIncomingTimeRegistrationChanges;
    }

    public Integer getNumIncomingProjectsRemoved() {
        return numIncomingProjectsRemoved;
    }

    public void setNumIncomingProjectsRemoved(Integer numIncomingProjectsRemoved) {
        this.numIncomingProjectsRemoved = numIncomingProjectsRemoved;
    }

    public Integer getNumIncomingTasksRemoved() {
        return numIncomingTasksRemoved;
    }

    public void setNumIncomingTasksRemoved(Integer numIncomingTasksRemoved) {
        this.numIncomingTasksRemoved = numIncomingTasksRemoved;
    }

    public Integer getNumIncomingTimeRegistrationsRemoved() {
        return numIncomingTimeRegistrationsRemoved;
    }

    public void setNumIncomingTimeRegistrationsRemoved(Integer numIncomingTimeRegistrationsRemoved) {
        this.numIncomingTimeRegistrationsRemoved = numIncomingTimeRegistrationsRemoved;
    }
}
