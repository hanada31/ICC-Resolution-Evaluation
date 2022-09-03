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

package eu.vranckaert.worktime.utils.view.actionbar;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

/**
 * User: DIRK VRANCKAERT
 * Date: 13/04/12
 * Time: 9:01
 */
public abstract class ActionBarHelper {
    protected Activity mActivity;

    /**
     * Factory method for creating {@link eu.vranckaert.worktime.utils.view.actionbar.ActionBarHelper} objects for a
     * given activity. Depending on which device the app is running, either a basic helper or
     * Honeycomb-specific helper will be returned.
     */
    public static ActionBarHelper createInstance(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return new ActionBarHelperICS(activity);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return new ActionBarHelperHoneycomb(activity);
        } else {
            return new ActionBarHelperBase(activity);
        }
    }

    protected ActionBarHelper(Activity activity) {
        mActivity = activity;
    }

    /**
     * Action bar helper code to be run in {@link android.app.Activity#onCreate(android.os.Bundle)}.
     */
    public void onCreate(Bundle savedInstanceState) {
    }

    /**
     * Action bar helper code to be run in {@link android.app.Activity#onPostCreate(android.os.Bundle)}.
     */
    public void onPostCreate(Bundle savedInstanceState) {
    }

    /**
     * Action bar helper code to be run in {@link android.app.Activity#onCreateOptionsMenu(android.view.Menu)}.
     * <p/>
     * NOTE: Setting the visibility of menu items in <em>menu</em> is not currently supported.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    /**
     * Action bar helper code to be run in {@link android.app.Activity#onTitleChanged(CharSequence, int)}.
     */
    protected void onTitleChanged(CharSequence title, int color) {
    }

    /**
     * Sets the indeterminate loading state.
     * (where the item ID was menu_refresh).
     */
    public abstract void setRefreshActionItemState(boolean refreshing, int menuItemResId);

    /**
     * Sets/removes the loading indicator without replacing another menu item.
     * @param loading If the loading indicator should be enabled or disabled.
     */
    public abstract void setLoadingIndicator(boolean loading);

    /**
     * Enables or disables the home button.
     */
    public abstract void setHomeButtonEnabled(boolean enabled);

    /**
     * Returns a {@link android.view.MenuInflater} for use when inflating menus. The implementation of this
     * method in {@link ActionBarHelperBase} returns a wrapped menu inflater that can read
     * action bar metadata from a menu resource pre-Honeycomb.
     */
    public MenuInflater getMenuInflater(MenuInflater superMenuInflater) {
        return superMenuInflater;
    }
}
