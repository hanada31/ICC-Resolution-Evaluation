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

package eu.vranckaert.worktime.service.ui.impl;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.activities.HomeActivity;
import eu.vranckaert.worktime.activities.projects.SelectProjectActivity;
import eu.vranckaert.worktime.activities.timeregistrations.TimeRegistrationActionActivity;
import eu.vranckaert.worktime.activities.timeregistrations.TimeRegistrationPunchInActivity;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.dao.WidgetConfigurationDao;
import eu.vranckaert.worktime.dao.impl.WidgetConfigurationDaoImpl;
import eu.vranckaert.worktime.enums.timeregistration.TimeRegistrationAction;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.Task;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.model.WidgetConfiguration;
import eu.vranckaert.worktime.providers.WorkTimeWidgetProvider_2x1_ProjectTask;
import eu.vranckaert.worktime.providers.WorkTimeWidgetProvider_2x2_Project;
import eu.vranckaert.worktime.service.ProjectService;
import eu.vranckaert.worktime.service.TaskService;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.service.impl.ProjectServiceImpl;
import eu.vranckaert.worktime.service.impl.TaskServiceImpl;
import eu.vranckaert.worktime.service.impl.TimeRegistrationServiceImpl;
import eu.vranckaert.worktime.service.ui.WidgetService;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.preferences.Preferences;
import eu.vranckaert.worktime.utils.widget.WidgetUtil;
import roboguice.inject.ContextSingleton;

import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 09/02/11
 * Time: 19:13
 */
public class WidgetServiceImpl implements WidgetService {
    private static final String LOG_TAG = WidgetServiceImpl.class.getName();

    @Inject
    @ContextSingleton
    private Context ctx;

    @Inject
    private TimeRegistrationService timeRegistrationService;

    @Inject
    private TaskService taskService;

    @Inject
    private ProjectService projectService;

    @Inject
    private WidgetConfigurationDao widgetConfigurationDao;

    private RemoteViews views;

    public WidgetServiceImpl(Context ctx) {
        this.ctx = ctx;
        getServices(ctx);
        getDaos(ctx);
    }

    /**
     * Default constructor required by RoboGuice!
     */
    public WidgetServiceImpl() {}

    @Override
    public void updateAllWidgets() {
        Log.d(ctx, LOG_TAG, "Updating all widgets...");
        updateWidgets(WidgetUtil.getAllWidgetIds(ctx));
    }

    @Override
    public void updateWidgets(List<Integer> widgetIds) {
        for (int widgetId : widgetIds) {
            updateWidget(widgetId);
        }
    }

    @Override
    public void updateWidgetsForTask(Task task) {
        List<WidgetConfiguration> wcs = widgetConfigurationDao.findPerTaskId(task.getId());

        for (WidgetConfiguration wc : wcs) {
            updateWidget(wc.getWidgetId());
        }

        updateWidgetsForProject(task.getProject());
    }

    private void updateWidgetsForProject(Project project) {
        List<WidgetConfiguration> wcs = widgetConfigurationDao.findPerProjectId(project.getId());

        for (WidgetConfiguration wc : wcs) {
            updateWidget(wc.getWidgetId());
        }
    }

    @Override
    public void updateWidget(int id) {
        AppWidgetManager awm = AppWidgetManager.getInstance(ctx);
        AppWidgetProviderInfo info = awm.getAppWidgetInfo(id);
        if (info != null) {
            ComponentName componentName = info.provider;
            if (componentName.getClassName().equals(WorkTimeWidgetProvider_2x1_ProjectTask.class.getName())) {
                updateWidget2x1ProjectTask(id);
            } else if (componentName.getClassName().equals(WorkTimeWidgetProvider_2x2_Project.class.getName())) {
                updateWidget2x2Project(id);
            }
        }
    }

    /**
     * Updates the widget's content for the 2x1 widgets for a project or task.
     * @param widgetId The id of the widget to be updated.
     */
    private void updateWidget2x1ProjectTask(int widgetId) {
        Log.d(ctx, LOG_TAG, "Updating widget (2x1) with id " + widgetId);

        getViews(ctx, R.layout.worktime_appwidget_2x1_project_task);

        // Set the project-name
        WidgetConfiguration wc = widgetConfigurationDao.findById(widgetId);
        if (wc == null) {
            views.setCharSequence(R.id.widget_title, "setText", ctx.getString(R.string.loading));
            return;
        }

        if (wc.getProject() != null) {
            Project project = projectService.getSelectedProject(widgetId);
            views.setCharSequence(R.id.widget_title, "setText", project.getName());
        } else if (wc.getTask() != null) {
            Task task = taskService.getSelectedTask(widgetId);
            views.setCharSequence(R.id.widget_title, "setText", task.getName());
        } else {
            views.setCharSequence(R.id.widget_title, "setText", "Corrupt data!");
        }

        // Set the button and it's action
        setPunchButton(widgetId);

        enableWidgetOnClick(R.id.widget);


        commitView(ctx, widgetId, views, WorkTimeWidgetProvider_2x1_ProjectTask.class);
    }

    /**
     * Updates the widget's content for the 2x2 widgets for projects.
     * @param widgetId The id of the widget to be updated.
     */
    private void updateWidget2x2Project(int widgetId) {
        Log.d(ctx, LOG_TAG, "Updating widget (2x2) with id " + widgetId);

        getViews(ctx, R.layout.worktime_appwidget_2x2_project);

        //Update the selected project
        Project selectedProject = projectService.getSelectedProject(widgetId);
        views.setCharSequence(R.id.widget_projectname, "setText", selectedProject.getName());

        // Set the button and it's action
        boolean timeRegistrationStarted = setPunchButton(widgetId);

        //Enable on click for the entire widget to open the app
        enableWidgetOnClick(R.id.widget);

        //Enable on click for the widget title to open the app if a registration is just started, or to open the
        //"select project" popup to change the selected project.
        Log.d(ctx, LOG_TAG, "Couple the widget title background to an on click action.");
        if (timeRegistrationStarted) {
            Log.d(ctx, LOG_TAG, "On click opens the home activity");
            enableWidgetOnClick(R.id.widget_bgtop);
        } else {
            Log.d(ctx, LOG_TAG, "On click opens a chooser-dialog for selecting the a project");
            startBackgroundWorkActivity(ctx, R.id.widget_bgtop, SelectProjectActivity.class, null, null, widgetId);
        }

        commitView(ctx, widgetId, views, WorkTimeWidgetProvider_2x2_Project.class);
    }

    private boolean setPunchButton(int widgetId) {
        boolean ongoingTimeRegistration = false;

        WidgetConfiguration wc = widgetConfigurationDao.findById(widgetId);
        if (wc.getProject() != null) {
            Project project = projectService.getSelectedProject(widgetId);
            return setPunchButton(widgetId, project);
        } else if (wc.getTask() != null) {
            Task task = taskService.getSelectedTask(widgetId);
            return setPunchButton(widgetId, task);
        } else {
            String errorMsg = "Invalid widget configuration found for widget with id " + widgetId + ". Cause: no project or task found in the configuration, at least one of both should be available! The punch-button will not be set!";
            Log.w(ctx, LOG_TAG, errorMsg);
            throw new RuntimeException(errorMsg);
        }
    }

    /**
     * Configure the "punch in"/"punch out" button to display the correct value and handle an on click correctly.
     * @param widgetId The id of the widget for which the button needs to be configured.
     * @param project The {@link Project} configured on that widget to be used to start/end a time registration for.
     * @return {@link Boolean#TRUE} if a time registration is ongoing for this project, {@link Boolean#FALSE} if not.
     */
    private boolean setPunchButton(int widgetId, Project project) {
        boolean ongoingTimeRegistration = false;

        Long numberOfTimeRegs = timeRegistrationService.count();
        TimeRegistration lastTimeRegistration = null;
        if(numberOfTimeRegs > 0L) {
            lastTimeRegistration = timeRegistrationService.getLatestTimeRegistration();
            timeRegistrationService.fullyInitialize(lastTimeRegistration);
            Log.d(ctx, LOG_TAG, "The last time registration has ID " + lastTimeRegistration.getId());
        } else {
            Log.d(ctx, LOG_TAG, "No time registrations found yet!");
        }

        if(numberOfTimeRegs == 0L || (lastTimeRegistration != null &&
                (!lastTimeRegistration.isOngoingTimeRegistration() || !lastTimeRegistration.getTask().getProject().getId().equals(project.getId())) )) {
            Log.d(ctx, LOG_TAG, "No time registrations found yet or it's an ended time registration");
            views.setCharSequence(R.id.widget_actionbtn, "setText", ctx.getString(R.string.btn_widget_start));
            //Enable on click for the start button
            Log.d(ctx, LOG_TAG, "Couple the start button to an on click action");
            startBackgroundWorkActivity(ctx, R.id.widget_actionbtn, TimeRegistrationPunchInActivity.class, null, null, widgetId);
        } else if(lastTimeRegistration != null && lastTimeRegistration.isOngoingTimeRegistration() && lastTimeRegistration.getTask().getProject().getId().equals(project.getId())) {
            Log.d(ctx, LOG_TAG, "This is an ongoing time registration");
            views.setCharSequence(R.id.widget_actionbtn, "setText", ctx.getString(R.string.btn_widget_stop));
            //Enable on click for the stop button
            Log.d(ctx, LOG_TAG, "Couple the stop button to an on click action.");
            startBackgroundWorkActivity(ctx, R.id.widget_actionbtn, TimeRegistrationActionActivity.class, lastTimeRegistration, null, widgetId);
            ongoingTimeRegistration = true;
        }

        return ongoingTimeRegistration;
    }

    /**
     * Configure the "punch in"/"punch out" button to display the correct value and handle an on click correctly.
     * @param widgetId The id of the widget for which the button needs to be configured.
     * @param task The {@link Task} configured on that widget to be used to start/end a time registration for.
     * @return {@link Boolean#TRUE} if a time registration is ongoing for this task, {@link Boolean#FALSE} if not.
     */
    private boolean setPunchButton(int widgetId, Task task) {
        boolean ongoingTimeRegistration = false;

        Long numberOfTimeRegs = timeRegistrationService.count();
        TimeRegistration lastTimeRegistration = null;
        if(numberOfTimeRegs > 0L) {
            lastTimeRegistration = timeRegistrationService.getLatestTimeRegistration();
            timeRegistrationService.fullyInitialize(lastTimeRegistration);
            Log.d(ctx, LOG_TAG, "The last time registration has ID " + lastTimeRegistration.getId());
        } else {
            Log.d(ctx, LOG_TAG, "No time registrations found yet!");
        }

        if(numberOfTimeRegs == 0L || (lastTimeRegistration != null &&
                (!lastTimeRegistration.isOngoingTimeRegistration() || !lastTimeRegistration.getTask().getId().equals(task.getId())) )) {
            Log.d(ctx, LOG_TAG, "No time registrations found yet or it's an ended time registration");
            views.setCharSequence(R.id.widget_actionbtn, "setText", ctx.getString(R.string.btn_widget_start));
            //Enable on click for the start button
            Log.d(ctx, LOG_TAG, "Couple the start button to an on click action");
            startBackgroundWorkActivity(ctx, R.id.widget_actionbtn, TimeRegistrationPunchInActivity.class, null, task, widgetId);
        } else if(lastTimeRegistration != null && lastTimeRegistration.isOngoingTimeRegistration() && lastTimeRegistration.getTask().getId().equals(task.getId())) {
            Log.d(ctx, LOG_TAG, "This is an ongoing time registration");
            views.setCharSequence(R.id.widget_actionbtn, "setText", ctx.getString(R.string.btn_widget_stop));
            //Enable on click for the stop button
            Log.d(ctx, LOG_TAG, "Couple the stop button to an on click action.");
            startBackgroundWorkActivity(ctx, R.id.widget_actionbtn, TimeRegistrationActionActivity.class, lastTimeRegistration, null, widgetId);
            ongoingTimeRegistration = true;
        }

        return ongoingTimeRegistration;
    }

    /**
     * Enable the on click for the entire widget to open the app.
     * @param resId The resource id on which should be clicked.
     */
    private void enableWidgetOnClick(int resId) {
        Log.d(ctx, LOG_TAG, "Couple the widget background to an on click action. On click opens the home activity");
        Intent homeAppIntent = new Intent(ctx, HomeActivity.class);
        PendingIntent homeAppPendingIntent = PendingIntent.getActivity(ctx, 0, homeAppIntent, 0);
        views.setOnClickPendingIntent(resId, homeAppPendingIntent);
    }

    @Override
    public void removeWidget(int widgetId) {
        WidgetConfiguration wc = widgetConfigurationDao.findById(widgetId);
        if (wc != null) {
            widgetConfigurationDao.delete(wc);
            Log.d(ctx, LOG_TAG, "Widget configuration for widget with id " + widgetId + " has been removed");
        } else {
            Log.d(ctx, LOG_TAG, "No widget configuration found for widget-id: " + widgetId);
        }
    }

    @Override
    public WidgetConfiguration getWidgetConfiguration(int widgetId) {
        return widgetConfigurationDao.findById(widgetId);
    }

    /**
     * Starts an activity that should do something in the background after clicking a button on the widget. That doesn't
     * mean that the activity cannot ask the user for any input/choice/... It only means that the launched
     * {@link Intent} by default enables on flag: {@link Intent#FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS} which forces the
     * activity to not be shown in the recent launched apps/activities. Other flags can be defined in the method call.
     * @param ctx The widget's context.
     * @param resId The resource id of the view on the widget on which to bind the on click action.
     * @param activity The activity that will do some background processing.
     * @param timeRegistration The {@link TimeRegistration} for which an {@link Intent} should be created. If null it
     * will not be put on the {@link Intent}.
     * @param task The {@link Task} for which an {@link Intent} should be created. If null it will not be put on the
     * {@link Intent}.
     * @param widgetId The id of the widget for which an intent should be created.
     * @param extraFlags Extra flags for the activities.
     */
    private void startBackgroundWorkActivity(Context ctx, int resId, Class<? extends Activity> activity, TimeRegistration timeRegistration, Task task, int widgetId, int... extraFlags) {
        int defaultFlags = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK;

        Intent intent = new Intent(ctx, activity);
        if (timeRegistration != null)
            intent.putExtra(Constants.Extras.TIME_REGISTRATION, timeRegistration);
        if (task != null)
            intent.putExtra(Constants.Extras.TASK, task);
        intent.putExtra(Constants.Extras.WIDGET_ID, widgetId);
        intent.putExtra(Constants.Extras.UPDATE_WIDGET, true);
        intent.setFlags(defaultFlags);

        if (Preferences.getImmediatePunchOut(ctx)) {
            intent.putExtra(Constants.Extras.DEFAULT_ACTION, TimeRegistrationAction.PUNCH_OUT);
            intent.putExtra(Constants.Extras.SKIP_DIALOG, true);
        }

        if(extraFlags != null) {
            for (int flag : extraFlags) {
                if (flag != defaultFlags) {
                    intent.setFlags(flag);
                }
            }
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(resId, pendingIntent);
    }

    /**
     * Every {@link RemoteViews} that is updated should be committed!
     * @param ctx The context.
     * @param widgetId The id of the widget to be updated.
     * @param updatedView The updated {@link RemoteViews}.
     * @param clazz The implementation class of the {@link AppWidgetProvider}.
     */
    private void commitView(Context ctx, int widgetId, RemoteViews updatedView, Class clazz) {
        Log.d(ctx, LOG_TAG, "Committing update view...");
        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
        mgr.updateAppWidget(widgetId, updatedView);
        Log.d(ctx, LOG_TAG, "Updated view committed!");
    }

    /**
     * Find the views to be updated for the widget with the specified view resource id.
     * @param ctx The widget's context.
     * @param viewResId The view resource id of the widget that is going to be updated.
     */
    private void getViews(Context ctx, int viewResId) {
        this.views = new RemoteViews(ctx.getPackageName(), viewResId);
        Log.d(ctx, LOG_TAG, "I just got the view which we'll start updating!");
    }

    /**
     * Create all the required DAO instances.
     * @param ctx The widget's context.
     */
    private void getDaos(Context ctx) {
        this.widgetConfigurationDao = new WidgetConfigurationDaoImpl(ctx);
        Log.d(ctx, LOG_TAG, "DAOS are loaded...");
    }

    /**
     * Create all the required service instances.
     * @param ctx The widget's context.
     */
    private void getServices(Context ctx) {
        this.projectService = new ProjectServiceImpl(ctx);
        this.taskService = new TaskServiceImpl(ctx);
        this.timeRegistrationService = new TimeRegistrationServiceImpl(ctx);
        Log.d(ctx, LOG_TAG, "Services are loaded...");
    }
}
