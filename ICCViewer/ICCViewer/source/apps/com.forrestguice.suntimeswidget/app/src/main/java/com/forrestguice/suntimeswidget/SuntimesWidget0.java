/**
    Copyright (C) 2014-2018 Forrest Guice
    This file is part of SuntimesWidget.

    SuntimesWidget is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesWidget is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SuntimesWidget.  If not, see <http://www.gnu.org/licenses/>.
*/ 

package com.forrestguice.suntimeswidget;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.content.Context;
import android.widget.RemoteViews;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;

import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData2;
import com.forrestguice.suntimeswidget.layouts.SunLayout;
import com.forrestguice.suntimeswidget.layouts.SunLayout_2x1_0;
import com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Widget receiver for resizable widget (that falls back to 1x1 layout).
 */
public class SuntimesWidget0 extends AppWidgetProvider
{
    public static final String SUNTIMES_WIDGET_UPDATE = "suntimes.SUNTIMES_WIDGET_UPDATE";
    public static final String SUNTIMES_THEME_UPDATE = "suntimes.SUNTIMES_THEME_UPDATE";
    public static final String SUNTIMES_ALARM_UPDATE = "suntimes.SUNTIMES_ALARM_UPDATE";

    public static final String KEY_WIDGETCLASS = "widgetClass";
    public static final String KEY_THEME = "themeName";

    public static final String TAG = "WidgetUpdate";

    protected static SuntimesUtils utils = new SuntimesUtils();

    protected int[] minSize = { 0, 0 };
    protected int[] getMinSize(Context context)
    {
        if (minSize[0] <= 0 || minSize[1] <= 0)
        {
            initMinSize(context);
        }
        return minSize;
    }
    protected void initMinSize(Context context)
    {
        minSize[0] = context.getResources().getInteger(R.integer.widget_size_minWidthDp);
        minSize[1] = context.getResources().getInteger(R.integer.widget_size_minHeightDp);
    }

    /**
     * @param context the context
     * @param appWidgetManager widget manager
     * @param appWidgetId the widgetID
     * @param newOptions a Bundle containing the new widget options
     */
    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions)
    {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        initLocale(context);
        updateWidget(context, appWidgetManager, appWidgetId);
    }

    /**
     * @param context the context
     * @param intent the intent that was received
     */
    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent)
    {
        super.onReceive(context, intent);
        initLocale(context);

        String filter = getUpdateIntentFilter();
        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        if (action != null && action.equals(filter))
        {
            String widgetClass = intent.getStringExtra(KEY_WIDGETCLASS);
            if (getClass().toString().equals(widgetClass))
            {
                int appWidgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);  // synonymous
                Log.d(TAG, "onReceive: " + filter + "(" + appWidgetID + "): " + getClass().toString());

                if (appWidgetID <= 0) {
                    updateWidgets(context);
                } else {
                    onUpdate(context, AppWidgetManager.getInstance(context), new int[]{appWidgetID});
                }
                setUpdateAlarm(context, appWidgetID);      // schedule next update
            }

        } else if (isClickAction(action)) {
            Log.d(TAG, "onReceive: ClickAction :: " + action + ":" + getClass());
            handleClickAction(context, intent);

        } else if (action != null && action.equals(AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED)) {
            Log.d(TAG, "onReceive: ACTION_APPWIDGET_OPTIONS_CHANGED :: " + getClass());

        } else if (action != null && action.equals(SUNTIMES_THEME_UPDATE)) {
            String themeName = (intent.hasExtra(KEY_THEME) ? intent.getStringExtra(KEY_THEME) : null);
            Log.d(TAG, "onReceive: SUNTIMES_THEME_UPDATE :: " + getClass() + " :: " + themeName);
            updateWidgets(context, themeName);

        } else if (action != null && action.equals(SUNTIMES_ALARM_UPDATE)) {
            Log.d(TAG, "onReceive: SUNTIMES_ALARM_UPDATE :: " + getClass());
            updateWidgets(context);
            setUpdateAlarms(context);

        } else if (action != null && action.equals("android.intent.action.TIME_SET")) {
            Log.d(TAG, "onReceive: android.intent.action.TIME_SET :: " + getClass());
            //updateWidgets(context);
            //setUpdateAlarms(context);
            // TODO: handle TIME_SET better .. when automatic/network time is enabled this thing fires /frequently/ ...

        } else if (action != null && action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            Log.d(TAG, "onReceive: ACTION_APPWIDGET_UPDATE :: " + getClass());
            if (extras != null)
            {
                int[] appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                if (appWidgetIds != null)
                {
                    for (int appWidgetId : appWidgetIds) {
                        setUpdateAlarm(context, appWidgetId);
                    }
                }
            }

        } else {
            Log.d(TAG, "onReceive: unhandled :: " + action + " :: " + getClass());
        }
    }

    public boolean isClickAction(String action)
    {
        for (String clickAction : getClickActions())
        {
            if (clickAction.equals(action))
            {
                return true;
            }
        }
        return false;
    }

    protected String[] getClickActions()
    {
        return new String[] { WidgetSettings.ActionMode.ONTAP_DONOTHING.name(),
                              WidgetSettings.ActionMode.ONTAP_UPDATE.name(),
                              WidgetSettings.ActionMode.ONTAP_LAUNCH_ACTIVITY.name(),
                              WidgetSettings.ActionMode.ONTAP_LAUNCH_CONFIG.name() };
    }

    /**
     * @param context the context
     * @param intent the click intent
     * @return true click handled, false otherwise
     */
    protected boolean handleClickAction(Context context, Intent intent)
    {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        int appWidgetId = (extras != null ? extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 0) : 0);

        if (action == null)
        {
            return false;
        }

        // OnTap: Ignore
        if (action.equals(WidgetSettings.ActionMode.ONTAP_DONOTHING.name()))
        {
            return false;
        }

        // OnTap: Update
        if (action.equals(WidgetSettings.ActionMode.ONTAP_UPDATE.name()))
        {
            updateWidget(context, AppWidgetManager.getInstance(context), appWidgetId);
            return true;
        }

        // OnTap: Launch an Activity
        if (action.equals(WidgetSettings.ActionMode.ONTAP_LAUNCH_ACTIVITY.name()))
        {
            Intent launchIntent;
            String launchClassName = WidgetSettings.loadActionLaunchPref(context, appWidgetId);
            Class<?> launchClass;
            try {
                launchClass = Class.forName(launchClassName);
                launchIntent = new Intent(context, launchClass);

            } catch (ClassNotFoundException e) {
                launchClass = getConfigClass();
                launchIntent = new Intent(context, launchClass);
                launchIntent.putExtra(WidgetSettings.ActionMode.ONTAP_LAUNCH_CONFIG.name(), true);
                Log.e(TAG, "LaunchApp :: " + launchClassName + " cannot be found! " + e.toString());
            }

            launchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);
            return true;
        }

        // OnTap: Reconfigure the Widget
        if (action.equals(WidgetSettings.ActionMode.ONTAP_LAUNCH_CONFIG.name()))
        {
            Intent configIntent = new Intent(context, getConfigClass());
            configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            configIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            configIntent.putExtra(WidgetSettings.ActionMode.ONTAP_LAUNCH_CONFIG.name(), true);
            context.startActivity(configIntent);
            return true;
        }

        //Log.w("SuntimesWidget", "Unsupported click action: " + action + " (" + appWidgetId + ")");
        return false;
    }

    /**
     * @return the activity (class) to be used when configuring this widget
     */
    protected Class getConfigClass()
    {
        return SuntimesConfigActivity0.class;
    }

    /**
     * @param context the context
     */
    public void updateWidgets(Context context)
    {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        int[] widgetIds = getWidgetIds(context, widgetManager);
        onUpdate(context, widgetManager, widgetIds);
    }
    public void updateWidgets(Context context, String themeName)
    {
        if (themeName == null)
        {
            Log.w(TAG, "updateWidgets: requested to update widgets by theme but no theme was supplied (null)... updating all");
            updateWidgets(context);
            return;
        }

        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        int[] widgetIds = getWidgetIds(context, widgetManager);
        ArrayList<Integer> filteredList = new ArrayList<>();
        for (int id : widgetIds)
        {
            String theme = WidgetSettings.loadThemeName(context, id);
            if (theme.equals(themeName))
            {
                filteredList.add(id);
            }
        }
        if (filteredList.size() > 0)
        {
            int[] filteredIds = new int[filteredList.size()];
            for (int i = 0; i < filteredIds.length; i++)
            {
                filteredIds[i] = filteredList.get(i);
            }
            onUpdate(context, widgetManager, filteredIds);
        }
    }

    public int[] getWidgetIds(Context context)
    {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        return getWidgetIds(context, widgetManager);
    }

    public int[] getWidgetIds(Context context, AppWidgetManager widgetManager)
    {
        return widgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
    }

    /**
     * @param context the context
     * @param appWidgetManager widget manager
     * @param appWidgetIds the widgetIDs that need to be updated
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        initLocale(context);
        WidgetThemes.initThemes(context);

        for (int appWidgetId : appWidgetIds)
        {
            updateWidget(context, appWidgetManager, appWidgetId);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    protected void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        SunLayout defLayout = WidgetSettings.loadSun1x1ModePref_asLayout(context, appWidgetId);
        SuntimesWidget0.updateAppWidget(context, appWidgetManager, appWidgetId, SuntimesWidget0.class, getMinSize(context), defLayout);
    }

    public void initLocale(Context context)
    {
        AppSettings.initLocale(context);
        SuntimesUtils.initDisplayStrings(context);
        WidgetSettings.TimeMode.initDisplayStrings(context);
    }

    /**
     * One or more widgets were deleted; cleanup after them.
     * @param context the context
     * @param appWidgetIds the widget ids that were deleted
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        for (int appWidgetId : appWidgetIds)
        {
            unsetUpdateAlarm(context, appWidgetId);
            WidgetSettings.deletePrefs(context, appWidgetId);
            WorldMapWidgetSettings.deletePrefs(context, appWidgetId);
        }
    }

    /**
     * Widget was enabled (called when the first widget is created).
     * @param context the context
     */
    @Override
    public void onEnabled(Context context)
    {
        super.onEnabled(context);
        //setUpdateAlarms(context);       // this call no longer makes sense..
        // The update alarms are now per appWidgetID, so now this call only triggers an update for the first widget (leaving subsequent widgets without an alarm)
        // The update alarms are now registered in onReceive( ACTION_APPWIDGET_UPDATE ) as each widget is added
    }

    /**
     * Widget was disabled (called after the last widget is deleted).
     * @param context the context
     */
    @Override
    public void onDisabled(Context context)
    {
        super.onDisabled(context);
        //unsetUpdateAlarms(context);        // this call no longer makes sense..
        // The update alarms are now per appWidgetID, so by the time this runs all widgets are deleted (too late to cleanup alarms without an appWidgetID).
        // The update alarms are now removed in onDeleted().
    }

    @TargetApi(17)
    public static int widgetCategory(AppWidgetManager appWidgetManager, int appWidgetId)
    {
        Bundle widgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
        return widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY);
    }

    protected static int[] widgetSizeDp(Context context, AppWidgetManager appWidgetManager, int appWidgetId, int[] defSize)
    {
        int[] mustFitWithinDp = {defSize[0], defSize[1]};
        //Log.d("widgetSizeDp", "0: must fit:  [" + mustFitWithinDp[0] + ", " + mustFitWithinDp[1] + "]");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            Bundle widgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
            int[]  sizePortrait = { widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH),   // dp values
                    widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) };
            int[]  sizeLandscape = { widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH),
                    widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) };

            //Log.d("widgetSizeDp", "portrait:  [" + sizePortrait[0] + ", " + sizePortrait[1] + "]");
            //Log.d("widgetSizeDp", "landscape: [" + sizeLandscape[0] + ", " + sizeLandscape[1] + "]");
            //Toast toast = Toast.makeText(context, "[" + sizePortrait[0] + ", " + sizePortrait[1] + "]; " + "[" + sizeLandscape[0] + ", " + sizeLandscape[1] + "]", Toast.LENGTH_SHORT);
            //toast.show();

            mustFitWithinDp[0] = Math.min( sizePortrait[0], sizeLandscape[0] );
            mustFitWithinDp[1] = Math.min( sizePortrait[1], sizeLandscape[1] );
            //Log.d("widgetSizeDp", "1: must fit:  [" + mustFitWithinDp[0] + ", " + mustFitWithinDp[1] + "]");
        }
        return mustFitWithinDp;
    }

    protected static int[] widgetMaxSizeDp(Context context, AppWidgetManager appWidgetManager, int appWidgetId, int[] defSize)
    {
        int[] mustFitWithinDp = {defSize[0], defSize[1]};
        //Log.d("widgetSizeDp", "0: must fit:  [" + mustFitWithinDp[0] + ", " + mustFitWithinDp[1] + "]");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            Bundle widgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
            int[]  sizePortrait = { widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH),   // dp values
                    widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) };
            int[]  sizeLandscape = { widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH),
                    widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) };

            //Log.d("widgetSizeDp", "portrait:  [" + sizePortrait[0] + ", " + sizePortrait[1] + "]");
            //Log.d("widgetSizeDp", "landscape: [" + sizeLandscape[0] + ", " + sizeLandscape[1] + "]");
            //Toast toast = Toast.makeText(context, "[" + sizePortrait[0] + ", " + sizePortrait[1] + "]; " + "[" + sizeLandscape[0] + ", " + sizeLandscape[1] + "]", Toast.LENGTH_SHORT);
            //toast.show();

            mustFitWithinDp[0] = Math.max( sizePortrait[0], sizeLandscape[0] );
            mustFitWithinDp[1] = Math.max( sizePortrait[1], sizeLandscape[1] );
            //Log.d("widgetSizeDp", "1: must fit:  [" + mustFitWithinDp[0] + ", " + mustFitWithinDp[1] + "]");
        }
        return mustFitWithinDp;
    }


    /**
     * @param context the context
     * @param appWidgetManager a reference to the AppWidgetManager
     * @param appWidgetId the widgetID
     * @return a SuntimesLayout that is appropriate for available space.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected static SunLayout getWidgetLayout( Context context, AppWidgetManager appWidgetManager, int appWidgetId, int[] defSize, SunLayout defLayout )
    {
        int[] mustFitWithinDp = widgetSizeDp(context, appWidgetManager, appWidgetId, defSize);

        SunLayout layout;
        if (WidgetSettings.loadAllowResizePref(context, appWidgetId))
        {
            int minWidth1x3 = context.getResources().getInteger(R.integer.widget_size_minWidthDp2x1);
            layout = ((mustFitWithinDp[0] >= minWidth1x3) ? new SunLayout_2x1_0()
                                                          : WidgetSettings.loadSun1x1ModePref_asLayout(context, appWidgetId));
        } else {
            layout = defLayout; // WidgetSettings.loadSun1x1ModePref_asLayout(context, appWidgetId);
        }
        //Log.d("getWidgetLayout", "layout is: " + layout);
        return layout;
    }

    /**
     * @param context the application context
     * @param appWidgetManager widget manager
     * @param appWidgetId id of widget to be updated
     */
    protected static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Class widgetClass, int[] defSize, SunLayout defLayout)
    {
        SunLayout layout = getWidgetLayout(context, appWidgetManager, appWidgetId, defSize, defLayout);
        SuntimesWidget0.updateAppWidget(context, appWidgetManager, appWidgetId, layout, widgetClass);
    }

    /**
     * @param context the context
     * @param appWidgetManager widget manager
     * @param appWidgetId id of the widget to be updated
     * @param layout a SuntimesLayout managing the views to be updated
     */
    protected static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, SunLayout layout, Class widgetClass)
    {
        SuntimesRiseSetData data = getRiseSetData(context, appWidgetId);
        data.calculate();

        boolean showSolarNoon = WidgetSettings.loadShowNoonPref(context, appWidgetId);
        if (showSolarNoon)
        {
            SuntimesRiseSetData noonData = new SuntimesRiseSetData(data);
            noonData.setTimeMode(WidgetSettings.TimeMode.NOON);
            noonData.calculate();
            data.linkData(noonData);
        }

        layout.prepareForUpdate(context, appWidgetId, data);

        RemoteViews views = layout.getViews(context);
        views.setOnClickPendingIntent(R.id.widgetframe_inner, SuntimesWidget0.clickActionIntent(context, appWidgetId, widgetClass));

        boolean showTitle = WidgetSettings.loadShowTitlePref(context, appWidgetId);
        views.setViewVisibility(R.id.text_title, showTitle ? View.VISIBLE : View.GONE);

        layout.themeViews(context, views, appWidgetId);
        layout.updateViews(context, appWidgetId, views, data);

        appWidgetManager.updateAppWidget(appWidgetId, views);

        WidgetSettings.RiseSetOrder order = WidgetSettings.loadRiseSetOrderPref(context, appWidgetId);
        if (order == WidgetSettings.RiseSetOrder.TODAY) {
            WidgetSettings.saveNextSuggestedUpdate(context, appWidgetId, -1);
            Log.d(TAG, "saveNextSuggestedUpdate: -1");

        } else {
            long soonest = SuntimesData.findSoonest(Calendar.getInstance(), data.getEvents());
            if (soonest != -1) {
                soonest += 5000;   // +5s
            }
            WidgetSettings.saveNextSuggestedUpdate(context, appWidgetId,  soonest);
            Log.d(TAG, "saveNextSuggestedUpdate: " + utils.calendarDateTimeDisplayString(context, soonest).toString());
        }
    }

    /**
     * getRiseSetData
     */
    protected static SuntimesRiseSetData getRiseSetData(Context context, int appWidgetId)
    {
        WidgetSettings.RiseSetOrder order = WidgetSettings.loadRiseSetOrderPref(context, appWidgetId);
        return (order == WidgetSettings.RiseSetOrder.TODAY)
                ? new SuntimesRiseSetData(context, appWidgetId) : new SuntimesRiseSetData2(context, appWidgetId);
    }

    /**
     * A static method for triggering an update of all widgets using ACTION_APPWIDGET_UPDATE intent;
     * triggers the onUpdate method.
     *
     * @param context the context
     * @param widgetClass the widget class (SuntimesWidget.class, SuntimesWidget1.class)
     */
    public static void triggerWidgetUpdate(Context context, Class widgetClass)
    {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        int[] ids = widgetManager.getAppWidgetIds(new ComponentName(context, widgetClass));

        Intent updateIntent = new Intent(context, widgetClass);
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(updateIntent);
    }

    /**
     * Start widget updates; register an alarm (inexactRepeating) that does not wake the device for each widget.
     * @param context the Context
     */
    protected void setUpdateAlarms( Context context )
    {
        for (int appWidgetID : getWidgetIds(context)) {
            setUpdateAlarm(context, appWidgetID);
        }
    }
    protected void unsetUpdateAlarms( Context context )
    {
        for (int appWidgetID : getWidgetIds(context)) {
            unsetUpdateAlarm(context, appWidgetID);
        }
    }

    /**
     * Start widget updates; register an alarm (inexactRepeating) that does not wake the device.
     * @param context the context
     */
    protected void setUpdateAlarm( Context context, int alarmID )
    {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null)
        {
            PendingIntent alarmIntent = getUpdateIntent(context, alarmID);
            long updateTime = getUpdateTimeMillis(context, alarmID);
            if (updateTime > 0)
            {
                if (Build.VERSION.SDK_INT < 19) {
                    alarmManager.set(AlarmManager.RTC, updateTime, alarmIntent);
                } else {
                    alarmManager.setWindow(AlarmManager.RTC, updateTime, 5 * 1000, alarmIntent);
                }
                Log.d(TAG, "setUpdateAlarm: " + utils.calendarDateTimeDisplayString(context, updateTime).toString() + " --> " + getUpdateIntentFilter() + "(" + alarmID + ") :: " + utils.timeDeltaLongDisplayString(getUpdateInterval(), true) );
            } else Log.d(TAG, "setUpdateAlarm: skipping " + alarmID);
        }
    }

    /**
     * Stop widget updates; unregisters the update alarm.
     * @param context the context
     */
    protected void unsetUpdateAlarm( Context context, int alarmID )
    {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null)
        {
            PendingIntent alarmIntent = getUpdateIntent(context, alarmID);
            alarmManager.cancel(alarmIntent);
            Log.d(TAG, "unsetUpdateAlarm: unset alarm --> " + getUpdateIntentFilter() + "(" + alarmID + ")");
        }
    }

    /**
     * @return time of update event (in millis)
     */
    protected long getUpdateTimeMillis( Context context, int appWidgetID )
    {
        long suggestedUpdateMillis = WidgetSettings.getNextSuggestedUpdate(context, appWidgetID);
        if (suggestedUpdateMillis <= 0)
        {
            return getUpdateTimeMillis(null);

        } else {
            Calendar suggestedUpdate = Calendar.getInstance();
            suggestedUpdate.setTimeInMillis(suggestedUpdateMillis);
            return getUpdateTimeMillis(suggestedUpdate);
        }
    }

    protected long getUpdateTimeMillis(Calendar suggestedUpdateTime)
    {
        Calendar updateTime = Calendar.getInstance();
        Calendar now = Calendar.getInstance();

        if (now.before(suggestedUpdateTime))
        {
            updateTime.setTimeInMillis(suggestedUpdateTime.getTimeInMillis());
            Log.d(TAG, "getUpdateTimeMillis: next update is at: " + updateTime.getTimeInMillis());

        } else {
            updateTime.set(Calendar.MILLISECOND, 0);   // reset seconds, minutes, and hours to 0
            updateTime.set(Calendar.MINUTE, 0);
            updateTime.set(Calendar.SECOND, 0);
            updateTime.set(Calendar.HOUR_OF_DAY, 0);
            updateTime.add(Calendar.DAY_OF_MONTH, 1);  // and increment the date by 1 day
            Log.d(TAG, "getUpdateTimeMillis: next update is at midnight: " + updateTime.getTimeInMillis());
        }
        return updateTime.getTimeInMillis();
    }

    /**
     * @return an AlarmManager interval; e.g. AlarmManager.INTERVAL_DAY
     */
    protected long getUpdateInterval()
    {
        return AlarmManager.INTERVAL_DAY;
    }

    /**
     * @param context the context
     * @return a SUNTIMES_WIDGET_UPDATE broadcast intent for widget alarmId (@see getUpdateAlarmId)
     */
    protected PendingIntent getUpdateIntent(Context context, int appWidgetId)
    {
        String updateFilter = getUpdateIntentFilter();
        Intent intent = new Intent(updateFilter);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(KEY_WIDGETCLASS, getClass().toString());

        /**
         * https://stackoverflow.com/questions/14029400/flag-cancel-current-or-flag-update-current
         * The discussion here suggests that FLAG_CANCEL_CURRENT doesn't work as expected, and
         * results in stale alarms (that may eventually consume all of the device's memory).
         */
        //return PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return PendingIntent.getBroadcast(context, appWidgetId, intent, 0);
    }

    /**
     * @return intent-filter name
     */
    protected String getUpdateIntentFilter()
    {
        return SuntimesWidget0.SUNTIMES_WIDGET_UPDATE;
    }

    /**
     * @param context the context
     * @param appWidgetId a widgetID
     * @param widgetClass the widget class (e.g. SuntimesWidget.class, SuntimesWidget1.class)
     * @return a PendingIntent for the widget (to be triggered when widget is clicked)
     */
    public static PendingIntent clickActionIntent(Context context, int appWidgetId, Class widgetClass)
    {
        WidgetSettings.ActionMode actionMode = WidgetSettings.loadActionModePref(context, appWidgetId);
        Intent actionIntent = new Intent(context, widgetClass);
        actionIntent.setAction(actionMode.name());
        actionIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        return PendingIntent.getBroadcast(context, appWidgetId, actionIntent, 0);
    }

}


