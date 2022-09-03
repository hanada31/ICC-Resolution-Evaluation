/**
    Copyright (C) 2018 Forrest Guice
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

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class MoonWidget0ConfigActivity_2x1 extends MoonWidget0ConfigActivity
{
    public MoonWidget0ConfigActivity_2x1()
    {
        super();
    }

    @Override
    protected void initViews( Context context )
    {
        super.initViews(context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
        {
            hideOption1x1LayoutMode();
        }
    }

    @Override
    protected void updateWidgets(Context context, int[] appWidgetIds)
    {
        Intent updateIntent = new Intent(context, MoonWidget0_2x1.class);
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        sendBroadcast(updateIntent);

        //MoonWidget0_2x1.updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId, MoonWidget0_2x1.class, minWidgetSize(context), new MoonLayout_2x1_0());
    }

    @Override
    protected int[] minWidgetSize(Context context)
    {
        int minSize[] = new int[2];
        minSize[0] = context.getResources().getInteger(R.integer.widget_size_minWidthDp2x1);
        minSize[1] = context.getResources().getInteger(R.integer.widget_size_minHeightDp);
        return minSize;
    }
}
