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

package eu.vranckaert.worktime.activities.projects;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.comparators.project.ProjectByNameComparator;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.service.ProjectService;
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
public class SelectProjectActivity extends SyncLockedGuiceActivity {
    @Inject
    private ProjectService projectService;

    @Inject
    private WidgetService widgetService;

    @InjectExtra(value = Constants.Extras.WIDGET_ID, optional = true)
    private Integer widgetId;

    @InjectExtra(value = Constants.Extras.ONLY_SELECT, optional = true)
    private boolean onlySelect = false;

    @InjectExtra(value = Constants.Extras.UPDATE_WIDGET, optional = true)
    private boolean updateWidget = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showSelectionDialog();
    }

    private void showSelectionDialog() {
        //Find all projects and sort by name
        List<Project> selectableProjects;
        if (Preferences.getSelectProjectHideFinished(SelectProjectActivity.this)) {
            selectableProjects = projectService.findUnfinishedProjects();
        } else {
            selectableProjects = projectService.findAll();
        }
        Collections.sort(selectableProjects, new ProjectByNameComparator());
        final List<Project> availableProjects = selectableProjects;

        Project selectedProject = null;
        if (widgetId != null) {
            selectedProject = projectService.getSelectedProject(widgetId);
        }
        int selectedProjectIndex = -1;

        List<String> projects = new ArrayList<String>();
        for (int i=0; i<availableProjects.size(); i++) {
            Project project = availableProjects.get(i);

            if (selectedProject != null && selectedProject.getId() == project.getId()) {
                selectedProjectIndex = i;
            }

            projects.add(project.getName());
        }

        projects.add(getString(R.string.lbl_widget_select_new_project_task));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.lbl_widget_title_select_project)
                .setSingleChoiceItems(
                    StringUtils.convertListToArray(projects),
                    selectedProjectIndex,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int index) {
                            if (index != availableProjects.size()) {
                                Project newSelectedProject = availableProjects.get(index);
                                if (!onlySelect && widgetId != null)
                                    projectService.setSelectedProject(widgetId, newSelectedProject);

                                if (updateWidget)
                                    widgetService.updateWidget(widgetId);

                                Intent resultValue = new Intent();
                                resultValue.putExtra(Constants.Extras.PROJECT, newSelectedProject);
                                setResult(RESULT_OK, resultValue);
                                finish();
                            } else {
                                dialogInterface.dismiss();
                                Intent intent = new Intent(SelectProjectActivity.this, AddEditProjectActivity.class);
                                startActivityForResult(intent, Constants.IntentRequestCodes.ADD_PROJECT);
                            }
                        }
                    }
                )
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialogInterface) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.IntentRequestCodes.ADD_PROJECT && resultCode == RESULT_OK) {
            showSelectionDialog();
        }
    }
}
