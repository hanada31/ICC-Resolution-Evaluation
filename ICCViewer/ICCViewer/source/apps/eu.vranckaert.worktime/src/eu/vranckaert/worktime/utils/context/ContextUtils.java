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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.Locale;

/**
 * Context utils.
 * @author Dirk Vranckaert
 */
public class ContextUtils {
    private static final String LOG_TAG = ContextUtils.class.getSimpleName();

    /**
     * Get the current user locale.
     * @param context The context on which to search for the locale.
     * @return The locale.
     */
    public static Locale getCurrentLocale(Context context) {
        return context.getResources().getConfiguration().locale;
    }

    /**
     * Hides the soft keyboard of the device.
     * @param context The context on which a keyboard is shown.
     * @param view Some {@link View} instance on which the keyboard should be hidden.
     */
    public static void hideKeyboard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * Checks if an SD card is available in the current device.
     * @return {@link Boolean#TRUE} if SD card is available, {@link Boolean#FALSE} if no SD card is available.
     */
    public static boolean isSdCardAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Checks if data can be written to the SD card.
     * @return {@link Boolean#TRUE} if the SD card is writable. {@link Boolean#FALSE} if the SD card is not writable.
     */
    public static boolean isSdCardWritable() {
        return !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }

    /**
     * A lookup method for the name of the current version (something like 1.0.3).
     * @param ctx The context.
     * @return The current version name.
     */
    public static String getCurrentApplicationVersionName(Context ctx) {
        String name = ctx.getPackageName();
        try {
            PackageInfo info = ctx.getPackageManager().getPackageInfo(name,0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "ERROR";
        }
    }

    /**
     * A lookup method for the code of the current version.
     * @param ctx The context.
     * @return The current version code.
     */
    public static int getCurrentApplicationVersionCode(Context ctx) {
        String name = ctx.getPackageName();
        try {
            PackageInfo info = ctx.getPackageManager().getPackageInfo(name,0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    /**
     * Get the version of the Android OS the user is running the application on.
     * @return The version (api) of the Android OS.
     */
    public static int getAndroidApiVersion() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * Get the package of the application.
     * @param ctx The context.
     * @return The package of the application.
     */
    public static String getApplicationPackage(Context ctx) {
        String applicationPackageName = ctx.getApplicationInfo().packageName;
        Log.d(ctx, LOG_TAG, "The application package name is " + applicationPackageName);
        return applicationPackageName;
    }

    /**
     * Determines if the currently used version of the application is a stable build or not. Non-stable builds
     * recognized by the version code which should contain <b>unstable</b>, <b>alpha</b>, <b>beta</b> or <b>rc</b>.
     * @param ctx The context of the application.
     * @return {@link Boolean#TRUE} if the current version of the application is stable, {@link Boolean#FALSE} if not.
     */
    public static boolean isStableBuild(Context ctx) {
        String[] nonFinalBuildNames = {"unstable", "alpha", "beta", "rc"};
        String version = ContextUtils.getCurrentApplicationVersionName(ctx).toLowerCase();

        for (String nonFinalBuildName : nonFinalBuildNames) {
            if (version.contains(nonFinalBuildName)) {
                return false;
            }
        }

        return true;
    }
}
