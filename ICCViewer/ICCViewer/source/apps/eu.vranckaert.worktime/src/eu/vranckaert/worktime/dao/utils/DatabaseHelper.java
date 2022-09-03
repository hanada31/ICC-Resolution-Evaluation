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

package eu.vranckaert.worktime.dao.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.SqliteAndroidDatabaseType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.utils.context.Log;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A utility class to be used to setup and interact with a database.
 * @param <T> Entity.
 * @param <ID> ID type.
 */
public class DatabaseHelper<T, ID> extends OrmLiteSqliteOpenHelper {
    /**
     * Logging
     */
    private static final String LOG_TAG = DatabaseHelper.class.getSimpleName();

    /**
     * The database type.
     */
    private DatabaseType databaseType = new SqliteAndroidDatabaseType();

    /**
     * The context.
     */
    private Context context = null;

    private Map<String, Dao<T, ID>> daoCache = new HashMap<String, Dao<T, ID>>();

    /**
     * Create a new database helper.
     * @param context The context.
     */
    public DatabaseHelper(Context context) {
        super(context, DaoConstants.DATABASE, null, DaoConstants.VERSION);
        this.context = context;
        Log.i(context, LOG_TAG, "Installing database, databasename = " + DaoConstants.DATABASE + ", version = " + DaoConstants.VERSION);
    }

    /**
     * Create a new database helper.
     * @param context The context.
     * @param databaseName The database name.
     * @param factory The factory.
     * @param databaseVersion The database version.
     */
    public DatabaseHelper(Context context, String databaseName, SQLiteDatabase.CursorFactory factory, int databaseVersion) {
        super(context, databaseName, factory, databaseVersion);
        this.context = context;
        Log.i(context, LOG_TAG, "Installing database, databasename = " + databaseName + ", version = " + databaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            Log.d(context, LOG_TAG, "Creating the database");
            Log.d(context, LOG_TAG, "Database path: " + database.getPath());
            for(Tables table : Tables.values()) {
                TableUtils.createTable(connectionSource, table.getTableClass());
            }

            insertDefaultData(database);
        } catch (SQLException e) {
            Log.e(context, LOG_TAG, "Excpetion while creating the database", e);
            throw new RuntimeException("Excpetion while creating the database", e);
        }
    }

    public void insertDefaultData(SQLiteDatabase database) {
        int defaultProjectId = 1;

        Log.d(context, LOG_TAG, "Inserting default project");
        ContentValues projectValues = new ContentValues();
        projectValues.put("id", defaultProjectId);
        projectValues.put("name", context.getString(R.string.default_project_name));
        projectValues.put("comment", context.getString(R.string.default_project_comment));
        projectValues.put("defaultValue", true);
        projectValues.put("finished", false);
        projectValues.put("flags", "");
        database.insert("project", null, projectValues);

        Log.d(context, LOG_TAG, "Inserting default task for default project");
        ContentValues taskValues = new ContentValues();
        taskValues.put("name", context.getString(R.string.default_task_name));
        taskValues.put("comment", context.getString(R.string.default_task_comment));
        taskValues.put("projectId", defaultProjectId);
        taskValues.put("finished", false);
        taskValues.put("flags", "");
        database.insert("task", null, taskValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        if (newVersion < oldVersion) {
            Log.w(context, LOG_TAG, "Trying to install an older database over a more recent one. Not executing update...");
            Log.d(context, LOG_TAG, "Database path: " + database.getPath());
            return;
        }

        Log.d(context, LOG_TAG, "Updating the database from version " + oldVersion + " to " + newVersion);
        Log.d(context, LOG_TAG, "Database path: " + database.getPath());

        DatabaseUpgrade[] databaseUpgrades = DatabaseUpgrade.values();
        int upgradeSqlCount = 0;
        int upgradeSqlBlockCount = 0;
        for (DatabaseUpgrade databaseUpgrade : databaseUpgrades) {
            if (oldVersion < databaseUpgrade.getToVersion()) {
                String[] queries = databaseUpgrade.getSqlQueries();
                for (String query : queries) {
                    try {
                        database.execSQL(query);
                    } catch (android.database.SQLException e) {
                        Log.d(context, LOG_TAG, "Exception while executing upgrade queries (toVersion: "
                                + databaseUpgrade.getToVersion() + ") during query: " + query, e);
                        throw new RuntimeException("Exception while executing upgrade queries (toVersion: "
                                + databaseUpgrade.getToVersion() + ") during query: " + query, e);
                    }
                    Log.d(context, LOG_TAG, "Executed an upgrade query to version " + databaseUpgrade.getToVersion()
                            + " with success: " + query);
                    upgradeSqlCount++;
                }
                Log.d(context, LOG_TAG, "Upgrade queries for version " + databaseUpgrade.getToVersion()
                        + " executed with success");
                upgradeSqlBlockCount++;
            }
        }
        if (upgradeSqlCount > 0) {
            Log.d(context, LOG_TAG, "All upadate queries exected with success. Total number of upgrade queries executed: "
                    + upgradeSqlCount + " in " + upgradeSqlBlockCount);
        } else {
            Log.d(context, LOG_TAG, "No database upgrade queries where necessary!");
        }


        /* This is the old code for upgrading a database: dropping the old one and creating a new one...
        try {

            for(Tables table : Tables.values()) {
                TableUtils.dropTable(databaseType, connectionSource, table.getTableClass(), true);
            }
            onCreate(database);
        } catch (SQLException e) {
            Log.e(context, LOG_TAG, "Excpetion while updating the database from version " + oldVersion + "to " + newVersion, e);
            throw new RuntimeException("Excpetion while updating the database from version " + oldVersion + "to " + newVersion, e);
        }*/
    }

    @Override
    public void close() {
        Log.d(context, LOG_TAG, "Closing connection");
        super.close();
    }

    public static Date convertDateToSqliteDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }
}
