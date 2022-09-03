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

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.Task;
import eu.vranckaert.worktime.service.ProjectService;
import eu.vranckaert.worktime.service.TaskService;
import eu.vranckaert.worktime.service.impl.ProjectServiceImpl;
import eu.vranckaert.worktime.service.impl.TaskServiceImpl;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.view.actionbar.synclock.SyncLockedWizardActivity;
import eu.vranckaert.worktime.utils.wizard.WizardActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 7/03/12
 * Time: 7:47
 */
public class CopyProjectActivity extends SyncLockedWizardActivity {
    private static final String LOG_TAG = CopyProjectActivity.class.getSimpleName();
    
    private TaskService taskService;
    private ProjectService projectService;

    private Project originalProject;
    private Project newProject;
    private List<Task> tasksForOriginalProject;
    private List<Task> tasksForNewProject;
    private boolean copyProjectAllTasks = true;

    private static final int PROJECT_INPUT_PAGE = 1;
    private static final int SUMMARY_PAGE = 2;
    
    private int[] layouts = {
            R.layout.activity_copy_project_wizard_1,
            R.layout.activity_copy_project_wizard_2,
            R.layout.activity_copy_project_wizard_3
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.lbl_copy_project_title);

        setupServices();
        loadExtras();

        setContentViews(layouts);

        super.setFinishButtonText(R.string.add);
        setCancelDialog(R.string.lbl_copy_project_cancel_dialog, R.string.msg_copy_project_cancel_dialog);
    }

    private void setupServices() {
        taskService = new TaskServiceImpl(CopyProjectActivity.this);
        projectService = new ProjectServiceImpl(CopyProjectActivity.this);
    }

    private void loadExtras() {
        originalProject = (Project) getIntent().getExtras().get(Constants.Extras.PROJECT);
        newProject = (Project) originalProject.clone();
        tasksForOriginalProject = taskService.findTasksForProject(originalProject);
    }

    @Override
    protected void initialize(View view) {}

    @Override
    public boolean beforePageChange(int currentViewIndex, int nextViewIndex, View view) {
        switch (currentViewIndex) {
            case PROJECT_INPUT_PAGE: {
                TextView projectnameRequired = (TextView) findViewById(R.id.projectname_required);
                TextView projectnameUnique = (TextView) findViewById(R.id.projectname_unique);
                hideValidationErrors(projectnameRequired, projectnameUnique);

                EditText projectNameInput = (EditText) findViewById(R.id.projectname);
                String name = projectNameInput.getText().toString();

                EditText projectCommentInput = (EditText) findViewById(R.id.projectcomment);
                String comment = projectCommentInput.getText().toString();
                
                if (name.length() == 0) {
                    Log.d(getApplicationContext(), LOG_TAG, "A project name is required!");
                    showValidationError(projectnameRequired);
                    return false;
                } else if (checkForDuplicateProjectNames(name)) {
                    Log.d(getApplicationContext(), LOG_TAG, "A project with this name already exists... Choose another name!");
                    showValidationError(projectnameUnique);
                    return false;
                }

                newProject.setName(name);
                newProject.setComment(comment);

                if (!copyProjectAllTasks) {
                    // Remove tasks that are already finished
                    tasksForNewProject = new ArrayList<Task>();
                    for (Task task : tasksForOriginalProject) {
                        if (!task.isFinished()) {
                            tasksForNewProject.add(task);
                        }
                    }
                } else {
                    // Copy all tasks
                    tasksForNewProject = tasksForOriginalProject;
                }
                
                break;
            }
        }
        
        return true;
    }

    /**
     * Hide the validation errors.
     * @param projectnameRequired A textview representing an error.
     * @param projectnameUnique A textview representing an error.
     */
    private void hideValidationErrors(TextView projectnameRequired, TextView projectnameUnique) {
        projectnameRequired.setVisibility(View.GONE);
        projectnameUnique.setVisibility(View.GONE);
    }

    /**
     * Shows a specific validation error.
     * @param errorTextView The TextView representing the error.
     */
    private void showValidationError(TextView errorTextView) {
        errorTextView.setVisibility(View.VISIBLE);
    }

    /**
     * Checks if a certain project name is duplicate or not.
     * @param projectName The project name to check.
     * @return {@link Boolean#TRUE} if the name is already in use. {@link Boolean#FALSE} if the name is not yet used.
     */
    private boolean checkForDuplicateProjectNames(String projectName) {
        return projectService.isNameAlreadyUsed(projectName);
    }

    @Override
    protected void afterPageChange(int currentViewIndex, int previousViewIndex, View view) {
        switch (currentViewIndex) {
            case PROJECT_INPUT_PAGE: {
                EditText projectNameInput = (EditText) view.findViewById(R.id.projectname);
                EditText projectCommentInput = (EditText) view.findViewById(R.id.projectcomment);

                projectNameInput.setText(newProject.getName());
                projectCommentInput.setText(newProject.getComment());

                final CheckBox copyProjectAllTasksCheckBox = (CheckBox) findViewById(R.id.copy_project_all_tasks);
                copyProjectAllTasksCheckBox.setChecked(copyProjectAllTasks);
                if (copyProjectAllTasks) {
                    copyProjectAllTasksCheckBox.setText(R.string.lbl_copy_project_part_two_copy_all_tasks_chekced);
                } else {
                    copyProjectAllTasksCheckBox.setText(R.string.lbl_copy_project_part_two_copy_all_tasks_unchekced);
                }
                copyProjectAllTasksCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                        copyProjectAllTasks = checked;
                        if (copyProjectAllTasks) {
                            copyProjectAllTasksCheckBox.setText(R.string.lbl_copy_project_part_two_copy_all_tasks_chekced);
                        } else {
                            copyProjectAllTasksCheckBox.setText(R.string.lbl_copy_project_part_two_copy_all_tasks_unchekced);
                        }
                    }
                });

                break;
            }
            case SUMMARY_PAGE: {
                TextView oldProjectName = (TextView) findViewById(R.id.copy_project_old_project_name);
                TextView oldProjectComment = (TextView) findViewById(R.id.copy_project_old_project_comment);
                TextView newProjectName = (TextView) findViewById(R.id.copy_project_new_project_name);
                TextView newProjectComment = (TextView) findViewById(R.id.copy_project_new_project_comment);
                TextView newProjectNumberOfTasks = (TextView) findViewById(R.id.copy_project_new_project_number_of_tasks);

                oldProjectName.setText(originalProject.getName());
                oldProjectComment.setText(originalProject.getComment());
                
                newProjectName.setText(newProject.getName());
                newProjectComment.setText(newProject.getComment());
                
                newProjectNumberOfTasks.setText(getString(R.string.lbl_copy_project_part_three_summary_number_of_tasks, tasksForNewProject.size()));
                break;
            }
        }
    }

    @Override
    protected boolean onCancel(View view, View button) {
        return true;
    }

    @Override
    protected boolean onFinish(View view, View button) {
        newProject = projectService.save(newProject);
        Log.d(getApplicationContext(), LOG_TAG, "A new project has been created with id " + newProject.getId() + ". The id of the original project is " + originalProject.getId());
        if (tasksForNewProject != null) {
            for (Task task : tasksForNewProject) {
                Task newTask = (Task) task.clone();
                newTask.setProject(newProject);
                newTask = taskService.save(newTask);
                Log.d(getApplicationContext(), LOG_TAG, "A new task has been created with id " + newTask.getId() + " for project with id " + newProject.getId() + ". The id of the original task is " + task.getId());
            }
        }
        setResult(RESULT_OK);
        return true;
    }
}