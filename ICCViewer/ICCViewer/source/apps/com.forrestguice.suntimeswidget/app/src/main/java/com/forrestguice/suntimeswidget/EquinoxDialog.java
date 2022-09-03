/**
    Copyright (C) 2017 Forrest Guice
    This file is part of SuntimesWidget.

    SuntimesWidget is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesWidget is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SuntimesWidget.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.forrestguice.suntimeswidget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeDataset;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

public class EquinoxDialog extends DialogFragment
{
    private EquinoxView equinoxView;

    private SuntimesEquinoxSolsticeDataset data;
    public void setData( SuntimesEquinoxSolsticeDataset data )
    {
        if (data != null && !data.isCalculated() && data.isImplemented())
        {
            data.calculateData();
        }
        this.data = data;
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final Activity myParent = getActivity();
        LayoutInflater inflater = myParent.getLayoutInflater();

        final ViewGroup viewGroup = null;
        View dialogContent = inflater.inflate(R.layout.layout_dialog_equinox, viewGroup);

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent);
        AlertDialog dialog = builder.create();

        equinoxView = (EquinoxView) dialogContent.findViewById(R.id.info_time_equinox);
        if (savedInstanceState != null)
        {
            Log.d("DEBUG", "EquinoxDialog onCreate (restoreState)");
            equinoxView.loadState(savedInstanceState);
        }

        dialog.setOnShowListener(onShowListener);
        themeViews(getContext());
        return dialog;
    }

    private DialogInterface.OnShowListener onShowListener = new DialogInterface.OnShowListener() {
        @Override
        public void onShow(DialogInterface dialogInterface) {
            Context context = getContext();
            if (context != null)
            {
                equinoxView.updateViews(getContext(), data);
            } else Log.w("EquinoxDialog.onShow", "null context! skipping update");
        }
    };

    private void themeViews(Context context)
    {
        if (themeOverride != null) {
            equinoxView.themeViews(context, themeOverride);
        }
    }

    private SuntimesTheme themeOverride = null;
    public void themeViews(Context context, SuntimesTheme theme)
    {
        if (theme != null) {
            themeOverride = theme;
            if (equinoxView != null) {
                themeViews(context);
            }
        }
    }

    public void updateViews()
    {
        if (equinoxView != null)
        {
            equinoxView.updateViews(getContext(), data);
            Log.d("DEBUG", "EquinoxDialog updated");
        }
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        equinoxView.saveState(outState);
        super.onSaveInstanceState(outState);
    }
}
