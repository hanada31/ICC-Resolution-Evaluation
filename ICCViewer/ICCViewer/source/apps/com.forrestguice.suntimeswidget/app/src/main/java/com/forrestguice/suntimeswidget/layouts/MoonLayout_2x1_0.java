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

package com.forrestguice.suntimeswidget.layouts;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.MoonPhaseDisplay;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.text.NumberFormat;

/**
 * Moonrise / Moonset / Phase (2x1)
 */
public class MoonLayout_2x1_0 extends MoonLayout
{
    public MoonLayout_2x1_0()
    {
        super();
    }

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_moon_2x1_0;
    }

    private WidgetSettings.RiseSetOrder order = WidgetSettings.RiseSetOrder.TODAY;

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesMoonData data)
    {
        super.updateViews(context, appWidgetId, views, data);
        boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
        boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);
        updateViewsMoonRiseSetText(context, views, data, showSeconds, order);

        NumberFormat percentage = NumberFormat.getPercentInstance();
        String illum = percentage.format(data.getMoonIlluminationToday());
        String illumNote = context.getString(R.string.moon_illumination, illum);
        SpannableString illumNoteSpan = (boldTime ? SuntimesUtils.createBoldColorSpan(null, illumNote, illum, illumColor) : SuntimesUtils.createColorSpan(null, illumNote, illum, illumColor));
        views.setTextViewText(R.id.text_info_moonillum, illumNoteSpan);

        for (MoonPhaseDisplay moonPhase : MoonPhaseDisplay.values())
        {
            views.setViewVisibility(moonPhase.getView(), View.GONE);
        }

        MoonPhaseDisplay phase = data.getMoonPhaseToday();
        if (phase != null)
        {
            views.setTextViewText(R.id.text_info_moonphase, phase.getLongDisplayString());
            views.setViewVisibility(R.id.text_info_moonphase, (showLabels ? View.VISIBLE : View.GONE));
            views.setViewVisibility(phase.getView(), View.VISIBLE);

            Integer phaseColor = phaseColors.get(phase);
            if (phaseColor != null)
            {
                views.setTextColor(R.id.text_info_moonphase, phaseColor);
            }
        }
    }

    private int illumColor = Color.WHITE;
    private boolean boldTime = false;

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);
        illumColor = theme.getTimeColor();
        boldTime = theme.getTimeBold();

        themeViewsMoonPhase(context, views, theme);
        themeViewsMoonPhaseText(context, views, theme);
        themeViewsMoonPhaseIcons(context, views, theme);

        themeViewsMoonRiseSetText(context, views, theme);
        themeViewsMoonRiseSetIcons(context, views, theme);
    }

    @Override
    public void prepareForUpdate(Context context, int appWidgetId, SuntimesMoonData data)
    {
        order = WidgetSettings.loadRiseSetOrderPref(context, appWidgetId);
        this.layoutID = chooseMoonLayout(R.layout.layout_widget_moon_2x1_0, R.layout.layout_widget_moon_2x1_01, data, order);
    }
}

