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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import eu.vranckaert.worktime.activities.HomeActivity;
import eu.vranckaert.worktime.utils.string.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 06/02/11
 * Time: 04:40
 */
public class IntentUtil {
    private static final String LOG_TAG = IntentUtil.class.getSimpleName();

    /**
     * Navigate to the home screen.
     * @param ctx The context.
     */
    public static void goHome(Context ctx) {
        goBack(ctx, HomeActivity.class);
    }

    /**
     * Navigate back to a previous activity.
     * @param ctx The context.
     * @param activityClass The Class of the activity navigating back to!
     */
    public static void goBack(Context ctx, Class activityClass) {
        Intent intent = new Intent(ctx, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ctx.startActivity(intent);
    }

    /**
     * Navigate back to the previous activity by ending the current one.
     * @param ctx The activity that you are navigating away from.
     */
    public static void goBack(Activity ctx) {
        ctx.finish();
    }

    /**
     * Send something with a single file attached. Using this method you can specify the subject, body and application
     * chooser title using the string resource id's.
     * @param activity The activity starting the action from.
     * @param subjectId The subject string resource id. Value lower than or equals to 0 will be ignored!
     * @param bodyId The body string resource id. Value lower than or equals to 0 will be ignored!
     * @param file The file attach.
     * @param chooserTitleId The application chooser's title string resource id. Value lower than or equals to 0 will be
     * ignored!
     */
    public static void sendSomething(Activity activity, int subjectId, int bodyId, File file, int chooserTitleId) {
        String subject = "";
        String body = "";
        String chooserTitle = "";
        
        if (subjectId > 0) {
            subject = activity.getString(subjectId);
        }
        if (bodyId > 0) {
            body = activity.getString(bodyId);
        }
        if (chooserTitleId > 0) {
            chooserTitle = activity.getString(chooserTitleId);
        }
        
        sendSomething(
                activity,
                subject,
                body,
                file,
                chooserTitle
        );
    }

    /**
     * Send something with a single file attached.
     * @param activity The activity starting the action from.
     * @param subject The subject string.
     * @param body The body string.
     * @param file The file attach.
     * @param chooserTitle The application chooser's title string.
     */
    public static void sendSomething(Activity activity, String subject, String body, File file, String chooserTitle) {
        List<File> files = new ArrayList<File>();
        files.add(file);
        sendSomething(activity, subject, body, files, chooserTitle);
    }

    /**
     * Send something with a bunch of files attached. Using this method you can specify the subject, body and application
     * chooser title using the string resource id's.
     * @param activity The activity starting the action from.
     * @param subjectId The subject string resource id. Value lower than or equals to 0 will be ignored!
     * @param bodyId The body string resource id. Value lower than or equals to 0 will be ignored!
     * @param files The files to attach.
     * @param chooserTitleId The application chooser's title string resource id. Value lower than or equals to 0 will be
     * ignored!
     */
    public static void sendSomething(Activity activity, int subjectId, int bodyId, List<File> files, int chooserTitleId) {
        String subject = "";
        String body = "";
        String chooserTitle = "";

        if (subjectId > 0) {
            subject = activity.getString(subjectId);
        }
        if (bodyId > 0) {
            body = activity.getString(bodyId);
        }
        if (chooserTitleId > 0) {
            chooserTitle = activity.getString(chooserTitleId);
        }

        sendSomething(
                activity,
                subject,
                body,
                files,
                chooserTitle
        );
    }

    /**
     * Send something with a bunch of files attached. If only one file is attached it will be handled as a mail with a
     * single attachment. If no files are attached it will be handled as a simple mail.
     * @param activity The activity starting the action from.
     * @param subject The subject string.
     * @param body The body string.
     * @param files The files to attach. Can be null or of count 0 if you do not want to attach any file.
     * @param chooserTitle The application chooser's title string.
     */
    public static void sendSomething(Activity activity, String subject, String body, List<File> files, String chooserTitle) {
        Log.d(activity, LOG_TAG, "About to send something...");
        Log.d(activity, LOG_TAG, "At least one attachment included? " + (files.size()>0?"Yes":"No"));

        String action = Intent.ACTION_SEND_MULTIPLE;
        if (files != null && files.size() > 1) {
            Log.d(activity, LOG_TAG, "More than one attachment included");
        }

        Intent emailIntent = new Intent(action);
        emailIntent.setType("text/plain");
        if (StringUtils.isNotBlank(subject)) {
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (StringUtils.isNotBlank(body)) {
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        }

        if(files != null) {
            Log.d(activity, LOG_TAG, "Adding multiple files...");
            ArrayList<Uri> uris = new ArrayList<Uri>();
            for (File file : files) {
                Uri uri = Uri.fromFile(file);
                uris.add(uri);
                emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
            }
            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        }

        Log.d(activity, LOG_TAG, "Launching share, application chooser if needed!");
        activity.startActivity(Intent.createChooser(emailIntent, chooserTitle));
    }

    /**
     * Get an extra-parameter from an activity-intent.
     * @param activity The activity.
     * @param key The key for the extra-parameter to look for.
     * @return The object found as extra-parameter. If not found this will return null.
     */
    public static Object getExtra(Activity activity, String key) {
        Object extra = null;

        if (activity != null && activity.getIntent() != null && activity.getIntent().getExtras() != null) {
            extra = activity.getIntent().getExtras().get(key);
        }

        return extra;
    }
}
