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

package eu.vranckaert.worktime.dao.utils;

import eu.vranckaert.worktime.utils.date.DateUtils;

import java.util.Date;

/**
 * User: DIRK VRANCKAERT
 * Date: 05/08/11
 * Time: 19:14
 */
public enum DatabaseUpgrade {
    UPGRADE1(21, new String[]{
            "alter table project add column flags " + DataTypes.TEXT + ";",
            "alter table task add column flags " + DataTypes.TEXT + ";",
            "alter table commenthistory add column flags " + DataTypes.TEXT + ";",
            "alter table timeregistration add column flags " + DataTypes.TEXT + ";",
            "alter table project add column finished " + DataTypes.BOOLEAN + " default 0;",
            "alter table task add column finished " + DataTypes.BOOLEAN + " default 0;",
    }),
    UPGRADE2(23, new String[] {
            "CREATE TABLE WidgetConfiguration " +
            "(" +
                "id " + DataTypes.INTEGER + " PRIMARY KEY, " +
                "projectId " + DataTypes.INTEGER +
            ");"
    }),
    UPGRADE3(24, new String[] {
            "ALTER TABLE WidgetConfiguration add column taskId " + DataTypes.INTEGER + ";"
    }),
    UPGRADE4(25, new String[] {
            "CREATE TABLE User " +
            "(" +
                "email " + DataTypes.TEXT + " PRIMARY KEY, " +
                "password " + DataTypes.TEXT + ", " +
                "sessionKey " + DataTypes.TEXT +
            ");"
    }),
    UPGRADE5(26, new String[] {
            "ALTER TABLE User add column firstName " + DataTypes.VARCHAR + ";",
            "ALTER TABLE User add column lastName " + DataTypes.VARCHAR + ";",
            "ALTER TABLE User add column loggedInSince " + DataTypes.VARCHAR + ";",
            "ALTER TABLE User add column registeredSince " + DataTypes.VARCHAR + ";",
            "ALTER TABLE User add column role " + DataTypes.VARCHAR + ";"
    }),
    UPGRADE6(27, new String[]{
            "CREATE TABLE SyncHistory " +
            "(" +
                "id " + DataTypes.INTEGER + " PRIMARY KEY, " +
                "started " + DataTypes.VARCHAR + ", " +
                "ended " + DataTypes.VARCHAR + ", " +
                "status " + DataTypes.VARCHAR + ", " +
                "failureReason " + DataTypes.TEXT +
            ");"
    }),
    UPGRADE7(28, new String[]{
            "ALTER TABLE project add column lastUpdated " + DataTypes.VARCHAR + ";",
            "ALTER TABLE task add column lastUpdated " + DataTypes.VARCHAR + ";",
            "ALTER TABLE timeregistration add column lastUpdated " + DataTypes.VARCHAR + ";",
            "ALTER TABLE project add column syncKey " + DataTypes.VARCHAR + ";",
            "ALTER TABLE task add column syncKey " + DataTypes.VARCHAR + ";",
            "ALTER TABLE timeregistration add column syncKey " + DataTypes.VARCHAR + ";",
            "UPDATE project SET lastUpdated = '" + DateUtils.DateTimeConverter.convertToDatabaseFormat(new Date()) + "'",
            "UPDATE task SET lastUpdated = '" + DateUtils.DateTimeConverter.convertToDatabaseFormat(new Date()) + "'",
            "UPDATE timeregistration SET lastUpdated = '" + DateUtils.DateTimeConverter.convertToDatabaseFormat(new Date()) + "'",
            "CREATE TABLE SyncRemovalCache " +
            "(" +
                "syncKey " + DataTypes.VARCHAR + " PRIMARY KEY, " +
                "entityName " + DataTypes.VARCHAR +
            ");"
    }),
    UPGRADE8(29, new String[] {
            "ALTER TABLE SyncHistory add column action " + DataTypes.VARCHAR + ";",
            "UPDATE SyncHistory SET action = 'DONE';"
    }),
    UPGRADE9(30, new String[] {
            "ALTER TABLE SyncHistory add column numOutgoingAcceptedProjectChanges " + DataTypes.INTEGER + ";",
            "ALTER TABLE SyncHistory add column numOutgoingMergedProjectChanges " + DataTypes.INTEGER + ";",
            "ALTER TABLE SyncHistory add column numOutgoingNoActionProjectChanges " + DataTypes.INTEGER + ";",
            "ALTER TABLE SyncHistory add column numOutgoingNotAcceptedProjectChanges " + DataTypes.INTEGER + ";",
            "ALTER TABLE SyncHistory add column numOutgoingAcceptedTaskChanges " + DataTypes.INTEGER + ";",
            "ALTER TABLE SyncHistory add column numOutgoingMergedTaskChanges " + DataTypes.INTEGER + ";",
            "ALTER TABLE SyncHistory add column numOutgoingNoActionTaskChanges " + DataTypes.INTEGER + ";",
            "ALTER TABLE SyncHistory add column numOutgoingNotAcceptedTaskChanges " + DataTypes.INTEGER + ";",
            "ALTER TABLE SyncHistory add column numOutgoingAcceptedTimeRegistrationChanges " + DataTypes.INTEGER + ";",
            "ALTER TABLE SyncHistory add column numOutgoingMergedTimeRegistrationChanges " + DataTypes.INTEGER + ";",
            "ALTER TABLE SyncHistory add column numOutgoingNoActionTimeRegistrationChanges " + DataTypes.INTEGER + ";",
            "ALTER TABLE SyncHistory add column numOutgoingNotAcceptedTimeRegistrationChanges " + DataTypes.INTEGER + ";",
            "ALTER TABLE SyncHistory add column numOutgoingProjectsRemoved " + DataTypes.INTEGER + ";",
            "ALTER TABLE SyncHistory add column numOutgoingTasksRemoved " + DataTypes.INTEGER + ";",
            "ALTER TABLE SyncHistory add column numOutgoingTimeRegistrationsRemoved " + DataTypes.INTEGER + ";",
            "ALTER TABLE SyncHistory add column numIncomingProjectChanges " + DataTypes.INTEGER + ";",
            "ALTER TABLE SyncHistory add column numIncomingTaskChanges " + DataTypes.INTEGER + ";",
            "ALTER TABLE SyncHistory add column numIncomingTimeRegistrationChanges " + DataTypes.INTEGER + ";",
            "ALTER TABLE SyncHistory add column numIncomingProjectsRemoved " + DataTypes.INTEGER + ";",
            "ALTER TABLE SyncHistory add column numIncomingTasksRemoved " + DataTypes.INTEGER + ";",
            "ALTER TABLE SyncHistory add column numIncomingTimeRegistrationsRemoved " + DataTypes.INTEGER + ";"
    })
    ;

    int toVersion;
    String[] sqlQueries;

    DatabaseUpgrade(int toVersion, String[] sqlQueries) {
        this.toVersion = toVersion;
        this.sqlQueries = sqlQueries;
    }

    public int getToVersion() {
        return toVersion;
    }

    public void setToVersion(int toVersion) {
        this.toVersion = toVersion;
    }

    public String[] getSqlQueries() {
        return sqlQueries;
    }

    public void setSqlQueries(String[] sqlQueries) {
        this.sqlQueries = sqlQueries;
    }

    private class DataTypes {
        private static final String SMALLINT = "SMALLINT";
        private static final String BIGINT = "BIGINT";
        private static final String INTEGER = "INTEGER";
        private static final String BOOLEAN = "SMALLINT";
        private static final String TEXT = "TEXT";
        private static final String VARCHAR = "VARCHAR";
    }
}
