/**
    Copyright (C) 2017-2018 Forrest Guice
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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

/**
 * Solstice / Equinox widget config activity.
 */
@SuppressWarnings("Convert2Diamond")
public class SolsticeWidget0ConfigActivity extends SuntimesConfigActivity0
{
    public SolsticeWidget0ConfigActivity()
    {
        super();
    }

    @Override
    protected void initViews( Context context )
    {
        super.initViews(context);
        setConfigActivityTitle(getString(R.string.configLabel_solsticewidget0));
        showOptionRiseSetOrder(false);
        hideOptionUseAltitude();
        hideOptionCompareAgainst();
        hideOption1x1LayoutMode();
        showOptionWeeks(true);
        showOptionHours(true);
        showOptionTimeDate(true);
        showOptionLabels(true);
        showOptionShowNoon(false);
        disableOptionAllowResize();
        showOptionTrackingMode(true);
        showOptionTimeModeOverride(true);
        showDataSource(false);  // temporarily hidden; atm all entries point to same implementation (false choice)
    }

    @Override
    protected void updateWidgets(Context context, int[] appWidgetIds)
    {
        Intent updateIntent = new Intent(context, SolsticeWidget0.class);
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        sendBroadcast(updateIntent);

        //AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        //SolsticeWidget0.updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    @Override
    protected int getAboutIconID()
    {
        return R.mipmap.ic_launcher;
    }

    @Override
    protected void initTimeMode( Context context )
    {
        if (spinner_timeMode != null)
        {
            ArrayAdapter<WidgetSettings.SolsticeEquinoxMode> spinner_timeModeAdapter;
            spinner_timeModeAdapter = new ArrayAdapter<WidgetSettings.SolsticeEquinoxMode>(this, R.layout.layout_listitem_oneline, WidgetSettings.SolsticeEquinoxMode.values() );
            spinner_timeModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_timeMode.setAdapter(spinner_timeModeAdapter);
        }

        if (button_timeModeHelp != null)
        {
            button_timeModeHelp.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    HelpDialog helpDialog = new HelpDialog();
                    helpDialog.setContent(getString(R.string.help_general_timeMode2));
                    helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
                }
            });
            button_timeModeHelp.setEnabled(false);           // disabled/hidden until txt provided
            button_timeModeHelp.setVisibility(View.GONE);
        }

        if (checkbox_timeModeOverride != null)
        {
            checkbox_timeModeOverride.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    if (spinner_timeMode != null)
                    {
                        spinner_timeMode.setEnabled(!isChecked);
                    }
                }
            });
        }
    }

    @Override
    protected SuntimesCalculatorDescriptor[] supportingCalculators()
    {
        return SuntimesCalculatorDescriptor.values(this, requiredFeatures);
    }
    private static int[] requiredFeatures = new int[] { SuntimesCalculator.FEATURE_SOLSTICE };

    @Override
    protected void loadTimeMode(Context context)
    {
        WidgetSettings.SolsticeEquinoxMode timeMode = WidgetSettings.loadTimeMode2Pref(context, appWidgetId);
        spinner_timeMode.setSelection(timeMode.ordinal());
    }

    @Override
    protected void saveTimeMode(Context context)
    {
        final WidgetSettings.SolsticeEquinoxMode[] timeModes = WidgetSettings.SolsticeEquinoxMode.values();
        WidgetSettings.SolsticeEquinoxMode timeMode = timeModes[ spinner_timeMode.getSelectedItemPosition()];
        WidgetSettings.saveTimeMode2Pref(context, appWidgetId, timeMode);
    }

    public static final boolean DEF_SHOWTITLE = false;
    public static final String DEF_TITLETEXT = "%M";

    @Override
    protected void loadTitleSettings(Context context)
    {
        boolean showTitle = WidgetSettings.loadShowTitlePref(context, appWidgetId, DEF_SHOWTITLE);
        checkbox_showTitle.setChecked(showTitle);
        setTitleTextEnabled(showTitle);

        String titleText = WidgetSettings.loadTitleTextPref(context, appWidgetId, DEF_TITLETEXT);
        text_titleText.setText(titleText);
    }

}
