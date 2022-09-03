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

package eu.vranckaert.worktime.utils.wizard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.utils.context.ContextUtils;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.string.StringUtils;
import eu.vranckaert.worktime.utils.view.actionbar.ActionBarGuiceActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 07/12/11
 * Time: 11:09
 *
 * A wizard activity shows a certain number of pages with a navigation bar at the bottom of the screen. <br/>
 * The navigation bar contains two buttons at the same time:<br/>
 * <li>Cancel and Next</li>
 * <li>Cancel and Finish</li>
 * <li>Previous and Next</li>
 * <li>Previous and Finish</li>
 * <br/>
 * To create a wizard activity, simply extend your activity from this {@link WizardActivity} and. override the
 * {@link WizardActivity#onCreate(android.os.Bundle)}. As in any other activity, the first call in this method should be
 * "super.onCreate(savedInstanceState);". Next the layouts to be loaded in the wizard (the different pages) should be
 * specified using {@link WizardActivity#setContentViews(int...)}. Pay attention that the order in which you
 * specified the layouts will be the order they will be displayed in the activity!<br/>
 * It's possible to disable the cancel and/or previous button when setting the content views using the method
 * {@link WizardActivity#setContentViews(boolean, boolean, int...)}. <b>CAUTION: When disabling 'cancel' it disables
 * both the cancel button and the back-button of the device which can cause a strange experience for the user.</b><br/>
 * <br/>
 * It is also possible to change the text on the cancel, previous, next and/or finish button. To achieve this you use
 * one of the appropriate methods (with a {@link String} value or an {@link Integer} resource id value:<br/>
 * <li>{@link WizardActivity#setCancelButtonText(String)}</li>
 * <li>{@link WizardActivity#setCancelButtonText(int)}</li>
 * <li>{@link WizardActivity#setPreviousButtonText(String)}</li>
 * <li>{@link WizardActivity#setPreviousButtonText(int)}</li>
 * <li>{@link WizardActivity#setNextButtonText(String)} </li>
 * <li>{@link WizardActivity#setNextButtonText(int)} </li>
 * <li>{@link WizardActivity#setFinishButtonText(String)}</li>
 * <li>{@link WizardActivity#setFinishButtonText(int)}</li>
 * <br/>
 * If cancel is enabled you can use the {@link WizardActivity#setCancelDialog(String, String)} method to enable a dialog
 * that will be popped up when canceling. The dialog always has a YES-NO option. 'YES' will start a cancel and so
 * leave the wizard, 'NO' will remove the dialog and stay in the wizard. There are also two variants of this method,
 * with the resource id and/or without title.<br/>
 * <br/>
 * A certain number of methods should be implemented in your activity to gain control over the wizard:<br/>
 * <li>{@link WizardActivity#initialize(android.view.View)}: When the activity is created the first wizard page is
 * loaded immediately. Here you can set certain values after initialization of the activity and/or change the UI based
 * on the provided view which maps on the first layout resource you provided.</li>
 * <li>{@link WizardActivity#beforePageChange(int, int, android.view.View)}: This method is executed when next is
 * pressed, just before the next layout is loaded. Here you should get variables from the UI and save them for later
 * use. What you don't save here is lost!</li>
 * <li>{@link WizardActivity#afterPageChange(int, int, android.view.View)}: This method let you set intial values on
 * the next loaded view or restore a view (if you go back one page for example you need to restore what the user has set
 * on it before).</li>
 * <li>{@link WizardActivity#onCancel(android.view.View, android.view.View)}: If you want to do something when the
 * activity is canceled (using the back button or the cancel button) this code should go here.</li>
 * <li>{@link WizardActivity#onFinish(android.view.View, android.view.View)}: If you want to do something when the
 * activity is finished, this code should go here. Typically here you will save some data.</li>
 * If you want to put EXTRA-parameters or a result on the intent when canceling or finishing the wizard, you should
 * override the methods {@link WizardActivity#closeOnCancel(View view)} or {@link WizardActivity#closeOnFinish()}.<br/>
 * <br/>
 * <b><u>Example:</u></b><br/>
 * public class MyWizardActivity extends WizardActivity {
 *   @Override
 *   protected void onCreate(Bundle savedInstanceState) {
 *      super.onCreate(savedInstanceState);
 *      //Set the layouts to be used in the wizard. The number of layouts is unlimited!
 *      setContentViews(R.layout.wizard_page_1, R.layout.wizard_page_2, R.layout.wizard_page_3);
 *      //If you want to disable cancel:
 *      setContentViews(true, false, R.layout.wizard_page_1, R.layout.wizard_page_2, R.layout.wizard_page_3);
 *      //And if you also want to disable the previous button:
 *      setContentViews(false, false, R.layout.wizard_page_1, R.layout.wizard_page_2, R.layout.wizard_page_3);
 *      //Now define the new texts to be used in the navigation:
 *      super.setFinishButtonText(R.string.save);
 *      super.setCancelButtonText(R.string.cancel);
 *      //Last but not least, you can enable the cancel dialog:
 *      setCancelDialog("Do you want to cancel?", "If yo cancel all your changes will be lost!");
 *   }
 *
 *   //Implement all the abstract methods for handling the wizard-behaviour.
 *
 *   //Example how to set a result when closing the activity after a cancel event.
 *   public void closeOnCancel() {
 *      Intent intent = new Intent();
 *      intent.putExtra("myData", myData);
 *      setResult(RESULT_CANCELED, intent);
 *      finish();
 *   }
 * }
 */
public abstract class WizardActivity extends ActionBarGuiceActivity {
    private static final String LOG_TAG = WizardActivity.class.getSimpleName();

    private View cancelButton;
    private View finishButton;
    private View nextButton;
    private View previousButton;

    private ViewGroup contentContainer;
    private View activeView;
    private int currentViewIndex = -1;

    private List<Integer> layoutResIDs = new ArrayList<Integer>();

    private boolean previousEnabled = true;
    private boolean cancelEnabled = true;

    private LayoutInflater layoutInflater;

    private boolean cancelDialogEnabled = false;
    private String cancelDialogTitle = null;
    private String cancelDialogMessage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.wizard);

        setDisplayHomeAsUpEnabled(true);

        layoutInflater = (LayoutInflater) WizardActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Set the layouts to be used in this wizard. The order of the layouts is important as they are shown in the order
     * specified.
     * @param previousEnabled Set to {@link Boolean#TRUE} if you want to set the previous button enabled. To
     * {@link Boolean#FALSE} if you want to disable it. Default is {@link Boolean#TRUE}.
     * @param cancelEnabled Set to {@link Boolean#TRUE} if you want to set the cancel button enabled. To
     * {@link Boolean#FALSE} if you want to disable it. Default is {@link Boolean#TRUE}.
     * @param layoutResIDs The layout resource ids to be used in this activity.
     */
    protected void setContentViews(boolean previousEnabled, boolean cancelEnabled, int... layoutResIDs) {
        setPreviousEnabled(previousEnabled);
        setCancelEnabled(cancelEnabled);
        setContentViews(layoutResIDs);
    }

    /**
     * Set the layouts to be used in this wizard. The order of the layouts is important as they are shown in the order
     * specified.
     * @param layoutResIDs The layout resource ids to be used in this activity.
     */
    protected void setContentViews(int... layoutResIDs) {
        for (int layoutResID : layoutResIDs) {
            this.layoutResIDs.add(layoutResID);
        }

        if (this.layoutResIDs.size() > 0) {
            init();
        } else {
            Log.w(getApplicationContext(), LOG_TAG, "No views have been defined for this wizard so we cannot the wizard!");
            finish();
        }
    }

    /**
     * Initializes the {@link WizardActivity}.
     */
    private void init() {
        contentContainer = (ViewGroup) findViewById(R.id.wizard_page_content_container);

        cancelButton = findViewById(R.id.wizard_navigation_container_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                clearFocusAndRemoveSoftKeyboard(button);
                cancel(button);
            }
        });

        finishButton = findViewById(R.id.wizard_navigation_container_finish);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                clearFocusAndRemoveSoftKeyboard(button);
                boolean result = onFinish(getActiveView(), button);
                if (result)
                    closeOnFinish();
            }
        });

        nextButton = findViewById(R.id.wizard_navigation_container_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                clearFocusAndRemoveSoftKeyboard(button);
                openNextPage();
            }
        });

        previousButton = findViewById(R.id.wizard_navigation_container_previous);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                clearFocusAndRemoveSoftKeyboard(button);
                openPreviousPage();
            }
        });

        openNextPage();
    }

    private void cancel(View button) {
        boolean result = onCancel(getActiveView(), button);
        if (result) {
            if (cancelDialogEnabled) {
                showCancelDialog(button);
            } else {
                closeOnCancel(button);
            }
        }
    }

    /**
     * Block of code to be executed when the next page is loaded/opened.
     */
    private void openNextPage() {
        int nextViewIndex = currentViewIndex + 1;
        changePage(currentViewIndex, nextViewIndex);
    }

    /**
     * Block of code to be executed when the previous page is loaded/opened.
     */
    private void openPreviousPage() {
        int nextViewIndex = currentViewIndex - 1;
        changePage(currentViewIndex, nextViewIndex);
    }

    /**
     * Change the wizard-page a certain page.
     * @param currentIndex The index of the current page.
     * @param nextIndex The index of the new page.
     */
    private void changePage(int currentIndex, int nextIndex) {
        scrollUp();

        boolean isInitialLoad = false;
        if (currentIndex == -1)
            isInitialLoad = true;

        boolean beforePageChangeResult = true;
        if (!isInitialLoad) {
            beforePageChangeResult = beforePageChange(currentIndex, nextIndex, getActiveView());
        }

        if (!beforePageChangeResult) {
            return;
        }

        int resId = Integer.parseInt(layoutResIDs.get(nextIndex).toString());
        activeView = layoutInflater.inflate(
                resId,
                contentContainer,
                false
        );
        contentContainer.removeAllViews();
        contentContainer.addView(activeView);

        currentViewIndex = nextIndex;
        if (isInitialLoad) {
            initialize(getActiveView());
        } else {
            afterPageChange(nextIndex, currentIndex, getActiveView());
        }

        invalidateNavigation(nextIndex);
    }

    private void scrollUp() {
        ScrollView scrollView = (ScrollView) findViewById(R.id.wizard_content_container);
        scrollView.scrollTo(0, 0);
    }

    /**
     * Invalidates the navigation UI components.
     * @param newIndex The index of the page navigating to.
     */
    private void invalidateNavigation(int newIndex) {
        Log.d(getApplicationContext(), LOG_TAG, "Invalidating navigation components");

        //If first: show cancel button
        //Else: show previous button
        if (newIndex == 0) {
            Log.d(getApplicationContext(), LOG_TAG, "Enable CANCEL, hide PREVIOUS");
            cancelButton.setVisibility(View.VISIBLE);
            previousButton.setVisibility(View.GONE);
        } else {
            Log.d(getApplicationContext(), LOG_TAG, "Hide CANCEL, enable PREVIOUS");
            cancelButton.setVisibility(View.GONE);
            previousButton.setVisibility(View.VISIBLE);
        }

        //If last: show finish button
        //Else: show next button
        if (newIndex == layoutResIDs.size()-1) {
            Log.d(getApplicationContext(), LOG_TAG, "Enable FINISH, hide NEXT");
            finishButton.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.GONE);
        } else {
            Log.d(getApplicationContext(), LOG_TAG, "Hide FINISH, enable NEXT");
            finishButton.setVisibility(View.GONE);
            nextButton.setVisibility(View.VISIBLE);
        }

        //Override witch specific settings for cancel and previous button
        Log.d(getApplicationContext(), LOG_TAG, "Override navigation components with user settings");
        if (!isCancelEnabled()) {
            Log.d(getApplicationContext(), LOG_TAG, "CANCEL should not be enabled");
            cancelButton.setVisibility(View.GONE);
        }
        if (!isPreviousEnabled()) {
            Log.d(getApplicationContext(), LOG_TAG, "PREVIOUS should not be enabled");
            previousButton.setVisibility(View.GONE);
        }
    }

    /**
     * This block of code is executed after the first view is loaded.
     * @param view The first view loaded.
     */
    protected abstract void initialize(View view);

    /**
     * This bock of code is executed before changing from one page in the wizard to another.
     * @param currentViewIndex The index of the page you are leaving (0-based).
     * @param nextViewIndex The index of the page you are going to (0-based).
     * @param view The view that is currently loaded (so the view of the page you are coming from).
     * @return If this method returns {@link Boolean#TRUE} execution will continue and the next page will be loaded. If
     * it returns {@link Boolean#FALSE} execution of the page change will be stopped and so the view will not change (in
     * case of a validation error for example).
     */
    public abstract boolean beforePageChange(int currentViewIndex, int nextViewIndex, View view);

    /**
     * This bock of code is executed before changing from one page in the wizard to another.
     * @param currentViewIndex The index of the page you are going to (0-based).
     * @param previousViewIndex The index of the page you left (0-based).
     * @param view The view that is currently loaded (so the view of the page you are going to).
     */
    protected abstract void afterPageChange(int currentViewIndex, int previousViewIndex, View view);

    /**
     * This block of code is executed when the "cancel" button is pressed. If you want to end this activity with a
     * certain result you should override the {@link WizardActivity#closeOnCancel(View view)} method.
     * @param view The view that is current loaded.
     * @param button The "cancel" button.
     * @return If this method returns {@link Boolean#TRUE} the activity will be closed. If it returns
     * {@link Boolean#FALSE} the activity will remain open (in case of an error for example).
     */
    protected abstract boolean onCancel(View view, View button);

    /**
     * This block of code is executed when the "finish" button is pressed. If you want to end this activity with a
     * certain result you should override the {@link WizardActivity#closeOnFinish()} method.
     * @param view The view that is current loaded.
     * @param button The "finish" button.
     * @return If this method returns {@link Boolean#TRUE} the activity will be closed. If it returns
     * {@link Boolean#FALSE} the activity will remain open (in case of a validation error for example).
     */
    protected abstract boolean onFinish(View view, View button);

    /**
     * Block of code to be executed when a close is performed after a cancel.
     */
    public void closeOnCancel(View view) {
        finish();
    }

    /**
     * Block of code to be executed when a close is performed after a finish.
     */
    public void closeOnFinish() {
        finish();
    }

    @Override
    public void onBackPressed() {
        if (isCancelEnabled()) {
            if (cancelDialogEnabled) {
                showCancelDialog(null);
            } else {
                closeOnCancel(null);
            }
        }
    }

    /**
     * Shows the dialog when canceling.
     */
    public void showCancelDialog(final View view) {
        AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
        if (StringUtils.isNotBlank(cancelDialogTitle)) {
            alertbox.setTitle(cancelDialogTitle);
        }
        alertbox.setMessage(cancelDialogMessage);

        alertbox.setPositiveButton(R.string.wizard_navigation_yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        closeOnCancel(view);
                    }
                });

        alertbox.setNeutralButton(R.string.wizard_navigation_no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {}
                });

        alertbox.show();
    }

    /**
     * Enable a cancel dialog popup when pressing the cancel button or the back button of the device. The dialog always
     * has the 'Yes' and 'No' button combination.
     * @param messageResId The string resource id for the message.
     */
    public void setCancelDialog(int messageResId) {
        setCancelDialog(null, getString(messageResId));
    }

    /**
     * Enable a cancel dialog popup when pressing the cancel button or the back button of the device. The dialog always
     * has the 'Yes' and 'No' button combination.
     * @param titleResId The string resource id for the title.
     * @param messageResId The string resource id for the message.
     */
    public void setCancelDialog(int titleResId, int messageResId) {
        setCancelDialog(getString(titleResId), getString(messageResId));
    }

    /**
     * Enable a cancel dialog popup when pressing the cancel button or the back button of the device. The dialog always
     * has the 'Yes' and 'No' button combination.
     * @param message The message.
     */
    public void setCancelDialog(String message) {
        setCancelDialog(null, message);
    }

    /**
     * Enable a cancel dialog popup when pressing the cancel button or the back button of the device. The dialog always
     * has the 'Yes' and 'No' button combination.
     * @param title The title.
     * @param message The message.
     */
    public void setCancelDialog(String title, String message) {
        this.cancelDialogTitle = title;
        this.cancelDialogMessage = message;
        setCancelDialogEnabled(true);
    }

    /**
     * Checks if previous is enabled or not.
     * @return True or false.
     */
    private boolean isPreviousEnabled() {
        return previousEnabled;
    }

    /**
     * Change the behaviour of the application. If set to true the previous button will be enabled. If set to false it
     * will be disabled (gone). Default is enabled!
     * @param previousEnabled True or false.
     */
    private void setPreviousEnabled(boolean previousEnabled) {
        this.previousEnabled = previousEnabled;
    }

    /**
     * Checks if cancel is enabled or not.
     * @return True or false.
     */
    private boolean isCancelEnabled() {
        return cancelEnabled;
    }

    /**
     * Change the behaviour of the application. If set to true the cancel button will be enabled. If set to false it
     * will be disabled (gone). Default is enabled!
     * @param cancelEnabled True or false.
     */
    private void setCancelEnabled(boolean cancelEnabled) {
        this.cancelEnabled = cancelEnabled;
    }

    /**
     * Check if the cancel dialog is enabled or not.
     * @return {@link Boolean#TRUE} if enabled, {@link Boolean#FALSE} if disabled.
     */
    public boolean isCancelDialogEnabled() {
        return cancelDialogEnabled;
    }

    /**
     * Set the cancel dialog to be enabled or not.
     * @param cancelDialogEnabled {@link Boolean#TRUE} to enable, {@link Boolean#FALSE} to disable.
     */
    private void setCancelDialogEnabled(boolean cancelDialogEnabled) {
        this.cancelDialogEnabled = cancelDialogEnabled;
    }

    @Override
    @Deprecated
    public void setContentView(int layoutResID) {
        Log.w(getApplicationContext(), LOG_TAG, "The content view cannot be changed. This method is deprecated for the WizardActivity!");
    }

    @Override
    @Deprecated
    public void setContentView(View view) {
        Log.w(getApplicationContext(), LOG_TAG, "The content view cannot be changed. This method is deprecated for the WizardActivity!");
    }

    @Override
    @Deprecated
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        Log.w(getApplicationContext(), LOG_TAG, "The content view cannot be changed. This method is deprecated for the WizardActivity!");
    }

    /**
     * Get the currently active view.
     * @return The currently active view. Null if the current view index is set to -1.
     */
    public View getActiveView() {
        return activeView;
    }

    /**
     * Change the default text of the button.
     * @param resId The id of the string-resource to use.
     */
    public void setPreviousButtonText(int resId) {
        String string  = getString(resId);
        Log.d(getApplicationContext(), LOG_TAG, "New text of the previous button: " + string);
        changeButtonText(previousButton, string);
    }

    /**
     * Change the default text of the button.
     * @param text The text to set.
     */
    public void setPreviousButtonText(String text) {
        Log.d(getApplicationContext(), LOG_TAG, "New text of the previous button: " + text);
        changeButtonText(previousButton, text);
    }

    /**
     * Change the default text of the button.
     * @param resId The id of the string-resource to use.
     */
    public void setCancelButtonText(int resId) {
        String string  = getString(resId);
        Log.d(getApplicationContext(), LOG_TAG, "New text of the cancel button: " + string);
        changeButtonText(cancelButton, string);
    }

    /**
     * Change the default text of the button.
     * @param text The text to set.
     */
    public void setCancelButtonText(String text) {
        Log.d(getApplicationContext(), LOG_TAG, "New text of the cancel button: " + text);
        changeButtonText(cancelButton, text);
    }

    /**
     * Change the default text of the button.
     * @param resId The id of the string-resource to use.
     */
    public void setNextButtonText(int resId) {
        String string  = getString(resId);
        Log.d(getApplicationContext(), LOG_TAG, "New text of the next button: " + string);
        changeButtonText(nextButton, string);
    }

    /**
     * Change the default text of the button.
     * @param text The text to set.
     */
    public void setNextButtonText(String text) {
        Log.d(getApplicationContext(), LOG_TAG, "New text of the next button: " + text);
        changeButtonText(nextButton, text);
    }

    /**
     * Change the default text of the button.
     * @param resId The id of the string-resource to use.
     */
    public void setFinishButtonText(int resId) {
        String string  = getString(resId);
        Log.d(getApplicationContext(), LOG_TAG, "New text of the finish button: " + string);
        changeButtonText(finishButton, string);
    }

    /**
     * Change the default text of the button.
     * @param text The text to set.
     */
    public void setFinishButtonText(String text) {
        Log.d(getApplicationContext(), LOG_TAG, "New text of the finish button: " + text);
        changeButtonText(finishButton, text);
    }

    /**
     * Change the text of a certain button.
     * @param view The {@link View} that represents a button.
     * @param text The new text to set on the button.
     */
    private void changeButtonText(View view, String text) {
        Button button = (Button) view;
        if (button == null) {
            Log.w(getApplicationContext(), LOG_TAG, "Could not cast the provided view to a button, so the text cannot be set!");
            return;
        }
        button.setText(text);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                cancel(null);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Removes the focus for a view and (if it was shown) the soft keyboard.
     * @param view The view on which to remove the focus and soft keyboard.
     */
    public void clearFocusAndRemoveSoftKeyboard(View view) {
        view.clearFocus();
        ContextUtils.hideKeyboard(this, view);
    }
}
