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

package eu.vranckaert.worktime.activities.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import com.google.inject.Inject;
import eu.vranckaert.worktime.activities.projects.SelectProjectActivity;
import eu.vranckaert.worktime.activities.tasks.SelectTaskActivity;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.service.ui.WidgetService;
import eu.vranckaert.worktime.service.ui.impl.WidgetServiceImpl;
import roboguice.activity.RoboActivity;

public class Configuration2x1Activity extends RoboActivity {
    private Integer widgetId;

    @Inject private WidgetService widgetService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        widgetId = getIntent().getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        setResult(RESULT_CANCELED);

        Intent selectProjectIntent = new Intent(Configuration2x1Activity.this, SelectProjectActivity.class);
        selectProjectIntent.putExtra(Constants.Extras.WIDGET_ID, widgetId);
        startActivityForResult(selectProjectIntent, Constants.IntentRequestCodes.SELECT_PROJECT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.IntentRequestCodes.SELECT_PROJECT: {
                if (resultCode == RESULT_OK) {
                    Intent selectTaskIntent = new Intent(Configuration2x1Activity.this, SelectTaskActivity.class);
                    selectTaskIntent.putExtra(Constants.Extras.WIDGET_ID, widgetId);
                    selectTaskIntent.putExtra(Constants.Extras.ENABLE_SELECT_NONE_OPTION, true);
                    startActivityForResult(selectTaskIntent, Constants.IntentRequestCodes.SELECT_TASK);
                } else {
                    finish();
                }
                break;
            }
            case Constants.IntentRequestCodes.SELECT_TASK: {
                if (resultCode == RESULT_OK) {
                    widgetService.updateWidget(widgetId);

                    Intent resultValue = new Intent();
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                    setResult(RESULT_OK, resultValue);
                }
                finish();
            }
        }
    }
}
