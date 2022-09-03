/*
 * Copyright © 2013 – 2016 Ricki Hirner (bitfire web engineering).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package com.etesync.syncadapter.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class ServiceDB {

    public static class Settings {
        public static final String
                _TABLE = "settings",
                NAME = "setting",
                VALUE = "value";
    }

    @Deprecated
    public static class Services {
        public static final String
                _TABLE = "services",
                ID = "_id",
                ACCOUNT_NAME = "accountName",
                SERVICE = "service";
    }

    @Deprecated
    public static class Collections {
        public static final String
                _TABLE = "collections",
                ID = "_id",
                SERVICE_ID = "serviceID",
                URL = "url",
                READ_ONLY = "readOnly",
                DISPLAY_NAME = "displayName",
                DESCRIPTION = "description",
                COLOR = "color",
                TIME_ZONE = "timezone",
                SYNC = "sync";
    }


    public static class OpenHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "services.db";
        private static final int DATABASE_VERSION = 1;

        public OpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            if (Build.VERSION.SDK_INT < 16)
                db.execSQL("PRAGMA foreign_keys=ON;");
        }

        @Override
        @RequiresApi(16)
        public void onConfigure(SQLiteDatabase db) {
            setWriteAheadLoggingEnabled(true);
            db.setForeignKeyConstraintsEnabled(true);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + Settings._TABLE + "(" +
                    Settings.NAME + " TEXT NOT NULL," +
                    Settings.VALUE + " TEXT NOT NULL" +
                    ")");
            db.execSQL("CREATE UNIQUE INDEX settings_name ON " + Settings._TABLE + " (" + Settings.NAME + ")");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // no different versions yet
        }


        public void dump(StringBuilder sb) {
            SQLiteDatabase db = getReadableDatabase();
            db.beginTransactionNonExclusive();

            // iterate through all tables
            Cursor cursorTables = db.query("sqlite_master", new String[]{"name"}, "type='table'", null, null, null, null);
            while (cursorTables.moveToNext()) {
                String table = cursorTables.getString(0);
                sb.append(table).append("\n");
                Cursor cursor = db.query(table, null, null, null, null, null, null);

                // print columns
                int cols = cursor.getColumnCount();
                sb.append("\t| ");
                for (int i = 0; i < cols; i++) {
                    sb.append(" ");
                    sb.append(cursor.getColumnName(i));
                    sb.append(" |");
                }
                sb.append("\n");

                // print rows
                while (cursor.moveToNext()) {
                    sb.append("\t| ");
                    for (int i = 0; i < cols; i++) {
                        sb.append(" ");
                        try {
                            String value = cursor.getString(i);
                            if (value != null)
                                sb.append(value
                                        .replace("\r", "<CR>")
                                        .replace("\n", "<LF>"));
                            else
                                sb.append("<null>");

                        } catch (SQLiteException e) {
                            sb.append("<unprintable>");
                        }
                        sb.append(" |");
                    }
                    sb.append("\n");
                }
                cursor.close();
                sb.append("----------\n");
            }
            cursorTables.close();
            db.endTransaction();
        }
    }
}
