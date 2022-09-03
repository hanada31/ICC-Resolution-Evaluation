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

package eu.vranckaert.worktime.utils.view.actionbar;

import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import roboguice.activity.RoboActivity;

/**
 * User: DIRK VRANCKAERT
 * Date: 13/04/12
 * Time: 9:01
 */
public class ActionBarGuiceActivity extends RoboActivity {
    final ActionBarHelper mActionBarHelper = ActionBarHelper.createInstance(this);

    /**
     * Returns the {@link ActionBarHelper} for this activity.
     */
    protected ActionBarHelper getActionBarHelper() {
        return mActionBarHelper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MenuInflater getMenuInflater() {
        return mActionBarHelper.getMenuInflater(super.getMenuInflater());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBarHelper.onCreate(savedInstanceState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActionBarHelper.onPostCreate(savedInstanceState);
    }

    /**
     * Base action bar-aware implementation for
     * {@link android.app.Activity#onCreateOptionsMenu(android.view.Menu)}.
     * <p/>
     * Note: marking menu items as invisible/visible is not currently supported.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean retValue = false;
        retValue |= mActionBarHelper.onCreateOptionsMenu(menu);
        retValue |= super.onCreateOptionsMenu(menu);
        return retValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        mActionBarHelper.onTitleChanged(title, color);
        super.onTitleChanged(title, color);
    }

    /**
     * Enables or disables the home button to be used for up-navigation.
     *
     * @param upEnabled If the home button should be enabled as up-navigation or not.
     */
    public void setDisplayHomeAsUpEnabled(boolean upEnabled) {
        if (Build.VERSION.SDK_INT >= 11) { //Compatible since honeycomb...
            ActionBar ab = getActionBar();
            ab.setDisplayHomeAsUpEnabled(upEnabled);
        }
    }

    ;
}
