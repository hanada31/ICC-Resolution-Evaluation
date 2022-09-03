/*
 * Copyright (c) 2018.
 *
 * This file is part of MoneyWallet.
 *
 * MoneyWallet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MoneyWallet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MoneyWallet.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.oriondev.moneywallet.ui.fragment.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * Created by andrea on 03/04/18.
 */
public abstract class MultiPanelAppBarItemFragment extends MultiPanelAppBarFragment {

    private SecondaryPanelFragment mSecondaryFragment;

    @Override
    protected void onCreateSecondaryPanel(LayoutInflater inflater, @NonNull ViewGroup secondaryPanel, @Nullable Bundle savedInstanceState) {
        FragmentManager fragmentManager = getChildFragmentManager();
        String fragmentTag = getSecondaryFragmentTag();
        mSecondaryFragment = (SecondaryPanelFragment) fragmentManager.findFragmentByTag(fragmentTag);
        if (mSecondaryFragment != null) {
            fragmentManager.beginTransaction().show(mSecondaryFragment).commitNow();
        } else {
            mSecondaryFragment = onCreateSecondaryPanel();
            fragmentManager.beginTransaction()
                    .replace(secondaryPanel.getId(), mSecondaryFragment, fragmentTag)
                    .commitNow();
        }
    }

    protected abstract SecondaryPanelFragment onCreateSecondaryPanel();

    protected abstract String getSecondaryFragmentTag();

    public void showItemId(long id) {
        if (mSecondaryFragment != null) {
            mSecondaryFragment.showItemId(id);
        }
    }
}