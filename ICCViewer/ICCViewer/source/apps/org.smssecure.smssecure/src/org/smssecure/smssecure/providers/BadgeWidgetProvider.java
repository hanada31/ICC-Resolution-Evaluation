package org.smssecure.smssecure.providers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import org.smssecure.smssecure.R;

public class BadgeWidgetProvider extends AppWidgetProvider {
  private static final String TAG = BadgeWidgetProvider.class.getSimpleName();

  public static final int MAX_COUNT = 99;

  private static BadgeWidgetProvider instance;
  private        Class               activityToLaunch = org.smssecure.smssecure.ConversationListActivity.class;
  private        Context             context;
  private        int                 unreadCount;

  public BadgeWidgetProvider() {}

  public BadgeWidgetProvider(@NonNull Context context) {
    this(context, null);
  }

  public BadgeWidgetProvider(@NonNull Context context, @Nullable Class activity) {
    this.context          = context;
    this.activityToLaunch = activity;
  }

  public Class getActivityToLaunch() {
    return activityToLaunch;
  }

  public void setActivityToLaunch(Class activity) {
    this.activityToLaunch = activity;
  }

  @Override
  public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

    for (int widgetId : appWidgetIds) {
      RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.badge_widget);

      if (unreadCount <= 0) {
        remoteViews.setViewVisibility(R.id.widget_number, View.GONE);
      } else {
        remoteViews.setViewVisibility(R.id.widget_number, View.VISIBLE);

        String displayCount = (unreadCount <= MAX_COUNT) ? String.valueOf(unreadCount) : String.valueOf(MAX_COUNT) + "+";
        remoteViews.setTextViewText(R.id.widget_number, displayCount);
      }

      remoteViews.setOnClickPendingIntent(R.id.widget_icon, getPendingIntent(context));
      appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }
  }

  public static BadgeWidgetProvider getInstance(@NonNull Context context) {
    return getInstance(context, null);
  }

  public static BadgeWidgetProvider getInstance(@NonNull Context context, @Nullable Class activity) {
    if (instance != null) return instance;
    instance = new BadgeWidgetProvider(context, activity);
    return instance;
  }

  private PendingIntent getPendingIntent(Context context) {
    Intent intent = new Intent(context, getActivityToLaunch());
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
  }

  public void updateBadge(int unreadCount) {
    Log.w(TAG, "updateBadge()");
    if (context == null) {
      Log.w(TAG, "context is null...");
      return;
    }
    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.badge_widget);

    this.unreadCount = unreadCount;
    if (unreadCount <= 0) {
      remoteViews.setViewVisibility(R.id.widget_number, View.GONE);
    } else {
      remoteViews.setViewVisibility(R.id.widget_number, View.VISIBLE);

      String displayCount = unreadCount <= BadgeWidgetProvider.MAX_COUNT ? String.valueOf(unreadCount) : String.valueOf(MAX_COUNT) + "+";
      remoteViews.setTextViewText(R.id.widget_number, displayCount);
    }

    remoteViews.setOnClickPendingIntent(R.id.widget_frame, getPendingIntent(context));

    ComponentName widget = new ComponentName(context, BadgeWidgetProvider.class);

    AppWidgetManager manager = AppWidgetManager.getInstance(context);
    manager.updateAppWidget(widget, remoteViews);
  }
}
