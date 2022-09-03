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

package eu.vranckaert.worktime.service.ui;

import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.Task;
import eu.vranckaert.worktime.model.WidgetConfiguration;

import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 09/02/11
 * Time: 19:13
 */
public interface WidgetService {
    /**
     * Updates all widgets to match the current application state.
     */
    void updateAllWidgets();

    /**
     * Updates all widgets for which the id is specified in the list.
     * @param widgetIds The list of id's defining which widgets should be updated.
     */
    void updateWidgets(List<Integer> widgetIds);

    /**
     * Updates all the widgets that are configured for the specified {@link Task} and all the widgets that are
     * configured for the specified tasks' {@link Project}.
     * @param task Based on this variable a lookup is done on
     * {@link eu.vranckaert.worktime.model.WidgetConfiguration} to check which widgets are configured for this
     * {@link Task}. All related widgets will be updated.
     */
    void updateWidgetsForTask(Task task);

    /**
     * Update the widget with a certain id. This will forward the call to the method that will handle the request for
     * widgets of this size.
     * @param id The id of the widget to be updated.
     */
    void updateWidget(int id);

    /**
     * Called when a widget with a certain id is removed from the home-screen.
     * @param id The id of the widget.
     */
    void removeWidget(int id);

    /**
     * Get the {@link WidgetConfiguration} instance based on a widgetId. If the widget-id is not found in the database
     * it returns null.
     * @param widgetId The id of the widget.
     * @return The {@link WidgetConfiguration} or null if not found.
     */
    WidgetConfiguration getWidgetConfiguration(int widgetId);
}
