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

package eu.vranckaert.worktime.enums.timeregistration;

import eu.vranckaert.worktime.model.TimeRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * This enum defines all possible actions on a {@link TimeRegistration}. It defines weather the action is possible for
 * ongoing, ended or all time registrations. and the order the of the actions to be used in the front-end. When
 * modifying the list of possible time registration actions you should also:</br>
 * <ul>
 * <li>modify the array resource
 * {@link eu.vranckaert.worktime.R.array#array_time_registration_actions_dialog_choose_action_spinner} and add/remove
 * the action in the correct position (according to the {@link TimeRegistrationAction#order} value that you defined for
 * the action).</li>
 * <li>Handle the new option in the activity method:
 * {@link eu.vranckaert.worktime.activities.timeregistrations.TimeRegistrationActionActivity#handleTimeRegistrationAction(TimeRegistrationAction, android.widget.EditText, android.widget.RadioGroup)}</li>
 * </ul>
 */
public enum TimeRegistrationAction {
    PUNCH_OUT(0, TimeRegistrationActionScope.ONGOING_TIME_REGISTRATION),
    PUNCH_OUT_AND_START_NEXT(1, TimeRegistrationActionScope.ONGOING_TIME_REGISTRATION),
    SPLIT(2, TimeRegistrationActionScope.BOTH),
    TIME_REGISTRATION_DETAILS(3, TimeRegistrationActionScope.BOTH),
    EDIT_STARTING_TIME(4, TimeRegistrationActionScope.BOTH),
    EDIT_END_TIME(5, TimeRegistrationActionScope.ENDED_TIME_REGISTRATION),
    RESTART_TIME_REGISTRATION(6, TimeRegistrationActionScope.ENDED_TIME_REGISTRATION),
    EDIT_PROJECT_AND_TASK(7, TimeRegistrationActionScope.BOTH),
    SET_COMMENT(8, TimeRegistrationActionScope.BOTH),
    DELETE_TIME_REGISTRATION(9, TimeRegistrationActionScope.BOTH);

    private int order;
    private int originalOrder;
    private TimeRegistrationActionScope scope;

    TimeRegistrationAction(int order, TimeRegistrationActionScope scope) {
        this.order = order;
        this.originalOrder = order;
        this.scope = scope;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOriginalOrder() {
        return originalOrder;
    }

    public TimeRegistrationActionScope getScope() {
        return scope;
    }

    public void setScope(TimeRegistrationActionScope scope) {
        this.scope = scope;
    }

    public static List<TimeRegistrationAction> getTimeRegistrationActions(TimeRegistration timeRegistration) {
        List<TimeRegistrationAction> actions = new ArrayList<TimeRegistrationAction>();

        for (TimeRegistrationAction action : TimeRegistrationAction.values()) {
            if (action.getScope().equals(TimeRegistrationActionScope.BOTH)) {
                actions.add(action);
            } else if (timeRegistration.isOngoingTimeRegistration() && action.getScope().equals(TimeRegistrationActionScope.ONGOING_TIME_REGISTRATION)) {
                actions.add(action);
            } else if (!timeRegistration.isOngoingTimeRegistration() && action.getScope().equals(TimeRegistrationActionScope.ENDED_TIME_REGISTRATION)) {
                actions.add(action);
            }
        }

        for (int i=0; i<actions.size(); i++) {
            actions.get(i).setOrder(i);
        }

        return actions;
    }

    public static TimeRegistrationAction getByIndex(List<TimeRegistrationAction> actions, int index) {
        for (TimeRegistrationAction action : actions) {
            if (index == action.getOrder()) {
                return action;
            }
        }
        return null;
    }
}
