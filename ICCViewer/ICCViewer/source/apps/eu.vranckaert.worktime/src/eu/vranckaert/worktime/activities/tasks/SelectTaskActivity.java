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

package eu.vranckaert.worktime.activities.tasks;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.google.inject.Inject;
import com.google.inject.internal.Nullable;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.comparators.task.TaskByNameComparator;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.Task;
import eu.vranckaert.worktime.service.ProjectService;
import eu.vranckaert.worktime.service.TaskService;
import eu.vranckaert.worktime.service.ui.WidgetService;
import eu.vranckaert.worktime.utils.preferences.Preferences;
import eu.vranckaert.worktime.utils.string.StringUtils;
import eu.vranckaert.worktime.utils.view.actionbar.synclock.SyncLockedGuiceActivity;
import roboguice.inject.InjectExtra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 02/03/11
 * Time: 20:56
 */
public class SelectTaskActivity extends SyncLockedGuiceActivity {
    @Inject
    private ProjectService projectService;

    @Inject
    private TaskService taskService;

    @Inject
    private WidgetService widgetService;

    @InjectExtra(value = Constants.Extras.WIDGET_ID, optional = true)
    @Nullable
    private Integer widgetId;

    @InjectExtra(value = Constants.Extras.ONLY_SELECT, optional = true)
    private boolean onlySelect = false;

    @InjectExtra(value = Constants.Extras.ENABLE_SELECT_NONE_OPTION, optional = true)
    private boolean enableSelectNoneOption = false;

    @InjectExtra(value = Constants.Extras.UPDATE_WIDGET, optional = true)
    private boolean updateWidget = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<Task> selectableTasks = loadSelectableTasks();


        if (selectableTasks.size() == 1) {
            if (Preferences.getWidgetAskForTaskSelectionIfOnlyOnePreference(SelectTaskActivity.this)) {
                showTaskSelectionDialog(selectableTasks);
            } else {
                Task task = selectableTasks.get(0);

                Intent resultValue = new Intent();
                resultValue.putExtra(Constants.Extras.TASK, task);
                setResult(RESULT_OK, resultValue);
                SelectTaskActivity.this.finish();
            }
        } else {
            showTaskSelectionDialog(selectableTasks);
        }
    }

    private List<Task> loadSelectableTasks() {
        List<Task> selectableTasks = null;
        //Find all tasks and sort by name
        if (widgetId != null) {
            Project project = projectService.getSelectedProject(widgetId);
            if (Preferences.getSelectTaskHideFinished(SelectTaskActivity.this)) {
                selectableTasks = taskService.findNotFinishedTasksForProject(project);
            } else {
                selectableTasks = taskService.findTasksForProject(project);
            }
        } else {
            selectableTasks = taskService.findAll();
        }

        return selectableTasks;
    }

    private void showTaskSelectionDialog(final List<Task> availableTasks) {
        Collections.sort(availableTasks, new TaskByNameComparator());
        Task selectedTask = null;
        int selectedTaskIndex = -1;

        List<String> tasks = new ArrayList<String>();
        for (Task task : availableTasks) {
            tasks.add(task.getName());
        }

        if (widgetId != null) {
            tasks.add(getString(R.string.lbl_widget_select_new_project_task));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.lbl_widget_title_select_task)
                .setSingleChoiceItems(
                        StringUtils.convertListToArray(tasks),
                        selectedTaskIndex,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int index) {
                                if (index != availableTasks.size()) {
                                    Task newSelectedTask = availableTasks.get(index);
                                    if (!onlySelect && widgetId != null)
                                        taskService.setSelectedTask(widgetId, newSelectedTask);

                                    if (updateWidget)
                                        widgetService.updateWidget(widgetId);

                                    Intent resultValue = new Intent();
                                    resultValue.putExtra(Constants.Extras.TASK, newSelectedTask);
                                    setResult(RESULT_OK, resultValue);
                                    SelectTaskActivity.this.finish();
                                } else {
                                    dialogInterface.dismiss();
                                    Intent intent = new Intent(SelectTaskActivity.this, AddEditTaskActivity.class);
                                    intent.putExtra(Constants.Extras.PROJECT, projectService.getSelectedProject(widgetId));
                                    startActivityForResult(intent, Constants.IntentRequestCodes.ADD_TASK);
                                }
                            }
                        }
                )
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialogInterface) {
                        setResult(RESULT_CANCELED);
                        SelectTaskActivity.this.finish();
                    }
                });

        if (enableSelectNoneOption) {
            builder.setNeutralButton(R.string.lbl_widget_select_no_task_option, new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setResult(RESULT_OK);
                    SelectTaskActivity.this.finish();
                }
            });
        }
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.IntentRequestCodes.ADD_TASK && resultCode == RESULT_OK) {
            showTaskSelectionDialog(loadSelectableTasks());
        }
    }
}
