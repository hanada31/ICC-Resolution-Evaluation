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

package com.forrestguice.suntimeswidget.layouts;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.SuntimesUtils.TimeDisplayText;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.Calendar;

/**
 * A 1x1 layout that displays either a solstice or equinox datetime.
 */
public class SolsticeLayout_1x1_0 extends SolsticeLayout
{
    public SolsticeLayout_1x1_0()
    {
        super();
    }

    /**public SolsticeLayout_1x1_0(int layoutID)
    {
        this.layoutID = layoutID;
    }*/

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_solstice_1x1_0;
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesEquinoxSolsticeData data)
    {
        super.updateViews(context, appWidgetId, views, data);

        if (data != null && data.isCalculated())
        {
            Calendar now = Calendar.getInstance();

            WidgetSettings.TrackingMode trackingMode = WidgetSettings.loadTrackingModePref(context, appWidgetId);
            Calendar event = (trackingMode == WidgetSettings.TrackingMode.SOONEST ? data.eventCalendarUpcoming(now)
                                                                                  : data.eventCalendarClosest(now));

            if (event != null)
            {
                boolean showWeeks = WidgetSettings.loadShowWeeksPref(context, appWidgetId);
                boolean showHours = WidgetSettings.loadShowHoursPref(context, appWidgetId);
                boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);
                boolean showTimeDate = WidgetSettings.loadShowTimeDatePref(context, appWidgetId);
                boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId);

                views.setTextViewText(R.id.text_time_event_label, data.timeMode().getLongDisplayString());
                views.setViewVisibility(R.id.text_time_event_label, (showLabels ? View.VISIBLE : View.GONE));

                TimeDisplayText eventString = utils.calendarDateTimeDisplayString(context, event, showTimeDate, showSeconds);
                views.setTextViewText(R.id.text_time_event, eventString.getValue());

                int noteStringId = R.string.hence;
                if (event.before(now))
                {
                    noteStringId = R.string.ago;
                }

                String noteTime = utils.timeDeltaDisplayString(now.getTime(), event.getTime(), showWeeks, showHours).toString();
                String noteString = context.getString(noteStringId, noteTime);
                SpannableString noteSpan = (boldTime ? SuntimesUtils.createBoldColorSpan(null, noteString, noteTime, timeColor) : SuntimesUtils.createColorSpan(null, noteString, noteTime, timeColor));
                views.setTextViewText(R.id.text_time_event_note, noteSpan);

            } else {
                views.setTextViewText(R.id.text_time_event, "");
                views.setTextViewText(R.id.text_time_event_note, context.getString(R.string.feature_not_supported_by_source));
                views.setTextViewText(R.id.text_time_event_label, "");
                views.setViewVisibility(R.id.text_time_event_label, View.GONE);
            }
        } else {
            views.setTextViewText(R.id.text_time_event, "");
            views.setTextViewText(R.id.text_time_event_note, context.getString(R.string.time_loading));
            views.setTextViewText(R.id.text_time_event_label, "");
            views.setViewVisibility(R.id.text_time_event_label, View.GONE);
        }
    }

    private WidgetSettings.SolsticeEquinoxMode timeMode = WidgetSettings.SolsticeEquinoxMode.EQUINOX_VERNAL;
    private int timeColor = Color.WHITE;
    private boolean boldTime = false;

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);

        timeColor = theme.getTimeColor();
        int textColor = theme.getTextColor();
        int eventColor = theme.getSeasonColor(timeMode);
        boldTime = theme.getTimeBold();

        views.setTextColor(R.id.text_time_event_note, textColor);
        views.setTextColor(R.id.text_time_event, eventColor);
        views.setTextColor(R.id.text_time_event_label, eventColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            float textSize = theme.getTextSizeSp();
            float timeSize = theme.getTimeSizeSp();

            views.setTextViewTextSize(R.id.text_time_event_label, TypedValue.COMPLEX_UNIT_DIP, textSize);
            views.setTextViewTextSize(R.id.text_time_event_note, TypedValue.COMPLEX_UNIT_DIP, textSize);
            views.setTextViewTextSize(R.id.text_time_event, TypedValue.COMPLEX_UNIT_DIP, timeSize);
        }
    }

    @Override
    public void prepareForUpdate(SuntimesEquinoxSolsticeData data)
    {
        timeMode = data.timeMode();
    }
}
