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

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import eu.vranckaert.worktime.service.ui.impl.StatusBarNotificationServiceImpl;
import eu.vranckaert.worktime.service.ui.impl.WidgetServiceImpl;
import eu.vranckaert.worktime.utils.context.Log;

/**
 * User: DIRK VRANCKAERT
 * Date: 07/02/11
 * Time: 20:58
 */
public class WorkTimeWidgetProvider_2x2_Project extends MyAppWidgetProvider {
    private static final String LOG_TAG = WorkTimeWidgetProvider_2x2_Project.class.getName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(context, LOG_TAG, "UPDATE");
        Log.d(context, LOG_TAG, "Number of widgets found: " + appWidgetIds.length);

        widgetService = new WidgetServiceImpl(context);
        statusBarNotificationService = new StatusBarNotificationServiceImpl(context);

        for(int appWidgetId : appWidgetIds) {
            AppWidgetProviderInfo widgetProviderInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
            Log.d(context, LOG_TAG, "STARTING FOR WIDGET ID: " + appWidgetId);
            if (widgetProviderInfo != null && widgetProviderInfo.provider != null)
                Log.d(context, LOG_TAG, "PROVIDER: " + widgetProviderInfo.provider.toString());

            widgetService.updateWidget(appWidgetId);
        }

        statusBarNotificationService.addOrUpdateNotification(null);
    }
}
