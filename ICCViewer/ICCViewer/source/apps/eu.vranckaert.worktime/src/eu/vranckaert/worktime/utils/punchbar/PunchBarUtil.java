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

package eu.vranckaert.worktime.utils.punchbar;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.activities.HomeActivity;
import eu.vranckaert.worktime.activities.punchbar.PunchBarClickListener;
import eu.vranckaert.worktime.activities.timeregistrations.TimeRegistrationActionActivity;
import eu.vranckaert.worktime.activities.timeregistrations.TimeRegistrationPunchInActivity;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.enums.timeregistration.TimeRegistrationAction;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.service.ProjectService;
import eu.vranckaert.worktime.service.TaskService;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.utils.preferences.Preferences;

/**
 * User: DIRK VRANCKAERT
 * Date: 7/02/12
 * Time: 11:44
 */
public class PunchBarUtil {
    /**
     * Configure the punch bar to be shown correctly.
     *
     * @param ctx                     The activity from which the bar should be created.
     * @param timeRegistrationService A reference to the {@link TimeRegistrationService}.
     * @param taskService             A reference to the {@link TaskService}.
     * @param projectService          A reference to the {@link ProjectService}.
     */
    public static void configurePunchBar(Activity ctx, TimeRegistrationService timeRegistrationService, TaskService taskService, ProjectService projectService) {
        View bar = ctx.findViewById(R.id.punch_bar_container);
        if (Preferences.getTimeRegistrationPunchBarEnabledFromHomeScreen(ctx) &&
                (ctx.getClass().equals(HomeActivity.class) || Preferences.getTimeRegistrationPunchBarEnabledOnAllScreens(ctx))) {
            bar.setVisibility(View.VISIBLE);
        } else {
            bar.setVisibility(View.GONE);
            return;
        }

        TimeRegistration lastTimeRegistration = timeRegistrationService.getLatestTimeRegistration();

        ImageButton actionButton = (ImageButton) ctx.findViewById(R.id.punchBarActionId);
        TextView footerText = (TextView) ctx.findViewById(R.id.punch_bar_text);

        if (lastTimeRegistration != null && lastTimeRegistration.isOngoingTimeRegistration()) {
            TimeRegistration previousTimeRegistration = timeRegistrationService.getPreviousTimeRegistration(lastTimeRegistration);
            TimeRegistration nextTimeRegistration = null;
            setPunchBarClickActions(bar, new PunchBarClickListener(ctx, lastTimeRegistration, previousTimeRegistration, nextTimeRegistration));
            taskService.refresh(lastTimeRegistration.getTask());
            projectService.refresh(lastTimeRegistration.getTask().getProject());

            footerText.setText(
                    lastTimeRegistration.getTask().getProject().getName() +
                            " " + ctx.getString(R.string.dash) + " " +
                            lastTimeRegistration.getTask().getName()
            );
            actionButton.setImageResource(R.drawable.ic_stop);
        } else {
            setPunchBarClickActions(bar, null);
            footerText.setText(R.string.home_comp_start_stop_time_registration_no_ongoing);
            actionButton.setImageResource(R.drawable.ic_play);
        }
    }

    private static void setPunchBarClickActions(View punchBar, PunchBarClickListener clickListener) {
        if (clickListener != null) {
            punchBar.setClickable(true);
            punchBar.setOnClickListener(clickListener);
        } else {
            punchBar.setClickable(false);
            punchBar.setOnClickListener(null);
        }
    }

    /**
     * Handles the click on a punch in/out in the punch-bar.
     *
     * @param ctx                     The activity from which the punch in/out action is invoked.
     * @param timeRegistrationService A reference to the {@link TimeRegistrationService}.
     */
    public static void onPunchButtonClick(Activity ctx, TimeRegistrationService timeRegistrationService) {
        TimeRegistration lastTimeRegistration = timeRegistrationService.getLatestTimeRegistration();
        if (lastTimeRegistration != null && lastTimeRegistration.isOngoingTimeRegistration()) {
            Intent intent = new Intent(ctx, TimeRegistrationActionActivity.class);
            intent.putExtra(Constants.Extras.TIME_REGISTRATION, lastTimeRegistration);
            if (Preferences.getImmediatePunchOut(ctx)) {
                intent.putExtra(Constants.Extras.DEFAULT_ACTION, TimeRegistrationAction.PUNCH_OUT);
                intent.putExtra(Constants.Extras.SKIP_DIALOG, true);
            }
            ctx.startActivityForResult(intent, Constants.IntentRequestCodes.END_TIME_REGISTRATION);
        } else {
            Intent intent = new Intent(ctx, TimeRegistrationPunchInActivity.class);
            intent.putExtra(Constants.Extras.WIDGET_ID, Constants.Others.PUNCH_BAR_WIDGET_ID);
            intent.putExtra(Constants.Extras.UPDATE_WIDGET, true);
            ctx.startActivityForResult(intent, Constants.IntentRequestCodes.START_TIME_REGISTRATION);
        }
    }
}
