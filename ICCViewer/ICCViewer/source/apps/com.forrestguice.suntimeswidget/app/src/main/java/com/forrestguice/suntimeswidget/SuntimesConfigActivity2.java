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
import android.widget.ArrayAdapter;

import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * ConfigActivity for SunPosition widgets (SuntimesWidget2)
 */
public class SuntimesConfigActivity2 extends SuntimesConfigActivity0
{
    public SuntimesConfigActivity2()
    {
        super();
    }

    @Override
    protected void initViews( Context context )
    {
        super.initViews(context);
        setConfigActivityTitle(getString(R.string.configLabel_title2));
        hideOptionShowSeconds();
        showOptionRiseSetOrder(false);
        hideOptionCompareAgainst();
        showTimeMode(false);
        showOptionShowNoon(false);
        showOptionLabels(true);
        showOption3x2LayoutMode(true);
    }

    @Override
    protected void initLocale(Context context)
    {
        super.initLocale(context);
        WorldMapWidgetSettings.initDisplayStrings(context);
    }

    /**@Override
    protected void loadAppearanceSettings(Context context)
    {
        super.loadAppearanceSettings(context);
    }*/

    @Override
    protected WidgetSettings.ActionMode defaultActionMode()
    {
        return WidgetSettings.ActionMode.ONTAP_UPDATE;
    }

    @Override
    protected SuntimesCalculatorDescriptor[] supportingCalculators()
    {
        return SuntimesCalculatorDescriptor.values(this, requiredFeatures);
    }
    private static int[] requiredFeatures = new int[] { SuntimesCalculator.FEATURE_POSITION };

    @Override
    protected void updateWidgets(Context context, int[] appWidgetIds)
    {
        Intent updateIntent = new Intent(context, SuntimesWidget2.class);
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        sendBroadcast(updateIntent);

        //SunPosLayout defLayout = new SunPosLayout_1X1_0();
        //SuntimesWidget2.updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId, SuntimesWidget2.class, minWidgetSize(context), defLayout);
    }

    @Override
    protected void loadShowLabels(Context context)
    {
        checkbox_showLabels.setChecked(WidgetSettings.loadShowLabelsPref(context, appWidgetId, true));
    }

    @Override
    protected void initWidgetMode1x1(Context context)
    {
        if (spinner_1x1mode != null)
        {
            ArrayAdapter<WidgetSettings.WidgetModeSunPos1x1> adapter = new ArrayAdapter<WidgetSettings.WidgetModeSunPos1x1>(this, R.layout.layout_listitem_oneline, WidgetSettings.WidgetModeSunPos1x1.values());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_1x1mode.setAdapter(adapter);
        }
    }

    @Override
    protected void saveWidgetMode1x1(Context context)
    {
        final WidgetSettings.WidgetModeSunPos1x1[] modes = WidgetSettings.WidgetModeSunPos1x1.values();
        WidgetSettings.WidgetModeSunPos1x1 mode = modes[spinner_1x1mode.getSelectedItemPosition()];
        WidgetSettings.saveSunPos1x1ModePref(context, appWidgetId, mode);
        //Log.d("DEBUG", "Saved mode: " + mode.name());
    }

    @Override
    protected void loadWidgetMode1x1(Context context)
    {
        WidgetSettings.WidgetModeSunPos1x1 mode1x1 = WidgetSettings.loadSunPos1x1ModePref(context, appWidgetId);
        spinner_1x1mode.setSelection(mode1x1.ordinal());
    }

    @Override
    protected void initWidgetMode3x2(Context context)
    {
        if (spinner_3x2mode != null)
        {
            ArrayList<WorldMapWidgetSettings.WorldMapWidgetMode> modes = new ArrayList<>(Arrays.asList(WorldMapWidgetSettings.WorldMapWidgetMode.values()));
            modes.remove(WorldMapWidgetSettings.WorldMapWidgetMode.EQUIAZIMUTHAL_SIMPLE);
            ArrayAdapter<WorldMapWidgetSettings.WorldMapWidgetMode> adapter = new ArrayAdapter<>(this, R.layout.layout_listitem_oneline, modes);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_3x2mode.setAdapter(adapter);
        }
    }

    @Override
    protected void saveWidgetMode3x2(Context context)
    {
        if (spinner_3x2mode != null)
        {
            WorldMapWidgetSettings.WorldMapWidgetMode mode = (WorldMapWidgetSettings.WorldMapWidgetMode) spinner_3x2mode.getSelectedItem();
            WorldMapWidgetSettings.saveSunPosMapModePref(context, appWidgetId, mode);
        }
    }

    @Override
    protected void loadWidgetMode3x2(Context context)
    {
        if (spinner_3x2mode != null)
        {
            WorldMapWidgetSettings.WorldMapWidgetMode mode = WorldMapWidgetSettings.loadSunPosMapModePref(context, appWidgetId);
            spinner_3x2mode.setSelection(mode.ordinal());
        }
    }

}
