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

package eu.vranckaert.worktime.utils.context;

import android.content.Context;

/**
 * This class is used for sending log output.<br/>
 * Generally used logging levels are LogLevel.INFO, LogLevel#DEBUG, LogLevel#WARN and LogLevel#ERROR.<br/>
 * A check is performed on execution time to check the phase of the current executed build. This is done in order to not
 * have logging enabled for certain (much-used) levels and thus to not spam the end-user's log. The logging levels that
 * are disabled when running a stable build (provided through the Android Play Store) are:<br/>
 * <li>LogLevel#VERBOSE</li>
 * <li>LogLevel#INFO</li>
 * <li>LogLevel#DEBUG</li>
 */
public class Log {
    
    public static void v(Context ctx, String tag, String message) {
        if (!ContextUtils.isStableBuild(ctx))
            android.util.Log.v(tag, message);
    }

    public static void d(Context ctx, String tag, String message) {
        if (!ContextUtils.isStableBuild(ctx))
            android.util.Log.d(tag, message);
    }

    public static void d(Context ctx, String tag, String message, Throwable e) {
        if (!ContextUtils.isStableBuild(ctx))
            android.util.Log.d(tag, message, e);
    }

    public static void i(Context ctx, String tag, String message) {
        if (!ContextUtils.isStableBuild(ctx))
            android.util.Log.i(tag, message);
    }

    public static void i(Context ctx, String tag, String message, Throwable e) {
        if (!ContextUtils.isStableBuild(ctx))
            android.util.Log.i(tag, message, e);
    }

    public static void w(Context ctx, String tag, String message) {
        android.util.Log.w(tag, message);
    }

    public static void w(Context ctx, String tag, String message, Throwable e) {
        android.util.Log.w(tag, message, e);
    }

    public static void e(Context ctx, String tag, String message) {
        android.util.Log.e(tag, message);
    }

    public static void e(Context ctx, String tag, String message, Throwable e) {
        android.util.Log.e(tag, message, e);
    }

    public static void wtf(Context ctx, String tag, String message) {
        android.util.Log.wtf(tag, message);
    }

    public static void wtf(Context ctx, String tag, String message, Throwable e) {
        android.util.Log.wtf(tag, message, e);
    }
}
