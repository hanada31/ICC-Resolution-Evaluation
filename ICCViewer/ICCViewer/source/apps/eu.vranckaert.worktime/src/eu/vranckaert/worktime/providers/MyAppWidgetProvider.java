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

package eu.vranckaert.worktime.providers;

import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import eu.vranckaert.worktime.service.ui.StatusBarNotificationService;
import eu.vranckaert.worktime.service.ui.WidgetService;
import eu.vranckaert.worktime.service.ui.impl.WidgetServiceImpl;
import eu.vranckaert.worktime.utils.context.Log;

public class MyAppWidgetProvider extends AppWidgetProvider {
    private static final String LOG_TAG = MyAppWidgetProvider.class.getName();

    protected WidgetService widgetService;
    protected StatusBarNotificationService statusBarNotificationService;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(context, LOG_TAG, "RECEIVE");
        super.onReceive(context, intent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(context, LOG_TAG, "DELETED");
        widgetService = new WidgetServiceImpl(context);

        for (int id : appWidgetIds) {
            Log.d(context, LOG_TAG, "Removing widget with id " + id);
            widgetService.removeWidget(id);
        }

        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        Log.d(context, LOG_TAG, "ENABLED");
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        Log.d(context, LOG_TAG, "DISABLED");
        super.onDisabled(context);
    }
}
