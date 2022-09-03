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

package eu.vranckaert.worktime.activities.punchbar;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import eu.vranckaert.worktime.activities.timeregistrations.TimeRegistrationDetailActivity;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.model.TimeRegistration;

public class PunchBarClickListener implements View.OnClickListener {
    private Context ctx;
    private TimeRegistration ongoingTimeRegistration;
    private TimeRegistration previousTimeRegistration;
    private TimeRegistration nextTimeRegistration;

    public PunchBarClickListener(Context ctx, TimeRegistration ongoingTimeRegistration, TimeRegistration previousTimeRegistration, TimeRegistration nextTimeRegistration) {
        this.ongoingTimeRegistration = ongoingTimeRegistration;
        this.previousTimeRegistration = previousTimeRegistration;
        this.nextTimeRegistration = nextTimeRegistration;
        this.ctx = ctx;
    }

    @Override
    public void onClick(View view) {
        if (this.ongoingTimeRegistration != null && this.ongoingTimeRegistration.isOngoingTimeRegistration()) {
            Intent intent = new Intent(ctx, TimeRegistrationDetailActivity.class);
            intent.putExtra(Constants.Extras.TIME_REGISTRATION, ongoingTimeRegistration);
            intent.putExtra(Constants.Extras.TIME_REGISTRATION_PREVIOUS, previousTimeRegistration);
            intent.putExtra(Constants.Extras.TIME_REGISTRATION_NEXT, nextTimeRegistration);
            ctx.startActivity(intent);
        }
    }
}
