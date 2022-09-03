/*
 * Copyright 2013 Dirk Vranckaert
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

package eu.vranckaert.worktime.activities.timeregistrations;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.google.inject.Inject;
import com.google.inject.internal.Nullable;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.TextConstants;
import eu.vranckaert.worktime.constants.TrackerConstants;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.service.ProjectService;
import eu.vranckaert.worktime.service.TaskService;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.service.ui.StatusBarNotificationService;
import eu.vranckaert.worktime.service.ui.WidgetService;
import eu.vranckaert.worktime.utils.context.IntentUtil;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.date.DateFormat;
import eu.vranckaert.worktime.utils.date.DateUtils;
import eu.vranckaert.worktime.utils.date.TimeFormat;
import eu.vranckaert.worktime.utils.punchbar.PunchBarUtil;
import eu.vranckaert.worktime.utils.string.StringUtils;
import eu.vranckaert.worktime.utils.tracker.AnalyticsTracker;
import eu.vranckaert.worktime.utils.view.actionbar.synclock.SyncLockedActivity;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

/**
 * User: DIRK VRANCKAERT
 * Date: 27/04/11
 * Time: 15:59
 */
public class TimeRegistrationDetailActivity extends SyncLockedActivity {
    private static final String LOG_TAG = TimeRegistrationDetailActivity.class.getSimpleName();

    @InjectView(R.id.start)
    private TextView timeRegistrationStart;
    @InjectView(R.id.end)
    private TextView timeRegistrationEnd;
    @InjectView(R.id.duration)
    private TextView timeRegistrationDuration;
    @InjectView(R.id.comment)
    private TextView timeRegistrationComment;
    @InjectView(R.id.comment_label)
    private TextView timeRegistrationCommentLabel;
    @InjectView(R.id.project)
    private TextView timeRegistrationProject;
    @InjectView(R.id.task)
    private TextView timeRegistrationTask;

    @InjectExtra(Constants.Extras.TIME_REGISTRATION)
    private TimeRegistration registration;

    @InjectExtra(Constants.Extras.TIME_REGISTRATION_PREVIOUS)
    @Nullable
    private TimeRegistration previousRegistration;

    @InjectExtra(Constants.Extras.TIME_REGISTRATION_NEXT)
    @Nullable
    private TimeRegistration nextRegistration;

    @Inject
    private TimeRegistrationService timeRegistrationService;

    @Inject
    private WidgetService widgetService;

    @Inject
    private StatusBarNotificationService statusBarNotificationService;

    @Inject
    private TaskService taskService;

    @Inject
    private ProjectService projectService;

    private boolean isUpdated = false;
    private boolean isSplit = false;
    private boolean initialLoad = true;

    private AnalyticsTracker tracker;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_details);

        setTitle(R.string.lbl_registration_details_title);
        setDisplayHomeAsUpEnabled(true);

        tracker = AnalyticsTracker.getInstance(getApplicationContext());
        tracker.trackPageView(TrackerConstants.PageView.REGISTRATIONS_DETAILS_ACTIVITY);

        updateView();
    }

    /**
     * Updates all of the layout fields...
     */
    private void updateView() {
        timeRegistrationStart.setText(
                TextConstants.SPACE +
                        DateUtils.DateTimeConverter.convertDateTimeToString(
                                registration.getStartTime(),
                                DateFormat.MEDIUM,
                                TimeFormat.MEDIUM,
                                getApplicationContext()
                        )
        );
        timeRegistrationDuration.setText(
                TextConstants.SPACE +
                DateUtils.TimeCalculator.calculatePeriod(getApplicationContext(), registration, false)
        );
        timeRegistrationProject.setText(TextConstants.SPACE + registration.getTask().getProject().getName());
        timeRegistrationTask.setText(TextConstants.SPACE + registration.getTask().getName());

        if (registration.isOngoingTimeRegistration()) {
            timeRegistrationEnd.setText(TextConstants.SPACE + getString(R.string.now));
        } else {
            timeRegistrationEnd.setVisibility(View.VISIBLE);

            timeRegistrationEnd.setText(
                    TextConstants.SPACE +
                    DateUtils.DateTimeConverter.convertDateTimeToString(
                            registration.getEndTime(),
                            DateFormat.MEDIUM,
                            TimeFormat.MEDIUM,
                            getApplicationContext()
                    )
            );
        }

        if (StringUtils.isNotBlank(registration.getComment())) {
            timeRegistrationCommentLabel.setVisibility(View.VISIBLE);
            timeRegistrationComment.setVisibility(View.VISIBLE);
            timeRegistrationComment.setText(registration.getComment());
        } else {
            timeRegistrationCommentLabel.setVisibility(View.GONE);
            timeRegistrationComment.setVisibility(View.GONE);
        }
    }

    public void onPunchButtonClick(View view) {
        PunchBarUtil.onPunchButtonClick(TimeRegistrationDetailActivity.this, timeRegistrationService);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.IntentRequestCodes.TIME_REGISTRATION_ACTION: {
                if (resultCode == RESULT_OK) {
                    Log.d(getApplicationContext(), LOG_TAG, "The time registration has been changed...");
                    isUpdated = true;
                } else if (resultCode == Constants.IntentResultCodes.RESULT_OK_SPLIT) {
                    Log.d(getApplicationContext(), LOG_TAG, "The time registration has been split!");
                    isUpdated = true;
                    isSplit = true;
                }
                if (resultCode == RESULT_OK || resultCode == Constants.IntentResultCodes.RESULT_OK_SPLIT) {
                    registration = timeRegistrationService.get(registration.getId());
                    taskService.refresh(registration.getTask());
                    projectService.refresh(registration.getTask().getProject());
                    updateView();
                }
                if (resultCode == Constants.IntentResultCodes.RESULT_DELETED) {
                    setResult(Constants.IntentResultCodes.RESULT_DELETED);
                    finish();
                }
                break;
            }
            case Constants.IntentRequestCodes.START_TIME_REGISTRATION: {
                PunchBarUtil.configurePunchBar(TimeRegistrationDetailActivity.this, timeRegistrationService, taskService, projectService);
                break;
            }
            case Constants.IntentRequestCodes.END_TIME_REGISTRATION: {
                PunchBarUtil.configurePunchBar(TimeRegistrationDetailActivity.this, timeRegistrationService, taskService, projectService);
                break;
            }
            case Constants.IntentRequestCodes.SYNC_BLOCKING_ACTIVITY: {
                if (timeRegistrationService.checkTimeRegistrationExisting(registration)) {
                    if (timeRegistrationService.checkReloadTimeRegistration(registration)) {
                        timeRegistrationService.refresh(registration);
                        updateView();
                    }
                } else {
                    setResult(Constants.IntentResultCodes.GHOST_RECORD);
                    finish();
                }
                break;
            }
        }

        if (resultCode == Constants.IntentResultCodes.RESULT_DELETED) {
            setResult(Constants.IntentResultCodes.RESULT_DELETED);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        PunchBarUtil.configurePunchBar(TimeRegistrationDetailActivity.this, timeRegistrationService, taskService, projectService);

        if (initialLoad) {
            initialLoad = false;
            return;
        }

        registration = timeRegistrationService.get(registration.getId());
        taskService.refresh(registration.getTask());
        projectService.refresh(registration.getTask().getProject());
        if (!registration.isOngoingTimeRegistration()) {
            nextRegistration = timeRegistrationService.getNextTimeRegistration(registration);
        }

        updateView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.ab_activity_time_registration_details, menu);

        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                IntentUtil.goBack(TimeRegistrationDetailActivity.this);
                break;
            case R.id.menu_time_registration_details_activity_edit:
                Intent intent = new Intent(TimeRegistrationDetailActivity.this, TimeRegistrationActionActivity.class);
                intent.putExtra(Constants.Extras.TIME_REGISTRATION, registration);
                startActivityForResult(intent, Constants.IntentRequestCodes.TIME_REGISTRATION_ACTION);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        if (isUpdated && isSplit) {
            setResult(Constants.IntentResultCodes.RESULT_OK_SPLIT);
        } else if (isUpdated) {
            setResult(RESULT_OK);
        }
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tracker.stopSession();
    }
}