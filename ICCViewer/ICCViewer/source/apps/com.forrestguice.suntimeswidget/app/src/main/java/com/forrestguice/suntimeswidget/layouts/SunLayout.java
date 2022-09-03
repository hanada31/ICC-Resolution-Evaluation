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
import android.widget.RemoteViews;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;

public abstract class SunLayout extends SuntimesLayout
{
    /**
     * Called by widget before themeViews and updateViews to give the layout obj an opportunity to
     * modify its state based on the supplied data.
     * @param data the data object (should be the same as supplied to updateViews)
     */
    public void prepareForUpdate(Context context, int appWidgetId, SuntimesRiseSetData data)
    {
        // EMPTY
    }

    /**
     * Apply the provided data to the RemoteViews this layout knows about.
     * @param context the android application context
     * @param appWidgetId the android widget ID to update
     * @param views the RemoteViews to apply the data to
     * @param data the data object to apply to the views
     */
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesRiseSetData data)
    {
        // update title
        String titlePattern = WidgetSettings.loadTitleTextPref(context, appWidgetId);
        String titleText = utils.displayStringForTitlePattern(context, titlePattern, data);
        CharSequence title = (boldTitle ? SuntimesUtils.createBoldSpan(null, titleText, titleText) : titleText);
        views.setTextViewText(R.id.text_title, title);
        //Log.v("DEBUG", "title text: " + titleText);
    }

    protected void updateViewsSunRiseSetText(Context context, RemoteViews views, SuntimesRiseSetData data, boolean showSeconds, WidgetSettings.RiseSetOrder order)
    {
        if (order == WidgetSettings.RiseSetOrder.TODAY)
        {
            updateViewsSunriseText(context, views, data.sunriseCalendarToday(), showSeconds);
            updateViewsSunsetText(context, views, data.sunsetCalendarToday(), showSeconds);

        } else {
            Calendar now = data.now();
            Calendar sunriseToday = data.sunriseCalendarToday();
            Calendar sunsetToday = data.sunsetCalendarToday();

            if (now.before(sunriseToday))      // in the wee hours
            {
                updateViewsSunriseText(context, views, data.sunriseCalendar(1), showSeconds);  // sunrise today
                updateViewsSunsetText(context, views, data.sunsetCalendar(0), showSeconds);    // sunset yesterday

            } else if (now.before(sunsetToday)) {       // during the day
                updateViewsSunriseText(context, views, data.sunriseCalendar(1), showSeconds);  // sunrise today
                updateViewsSunsetText(context, views, data.sunsetCalendar(1), showSeconds);    // sunset today

            } else {                          // night; the day is over (but "tomorrow" has yet to arrive)
                updateViewsSunsetText(context, views, data.sunsetCalendar(1), showSeconds);    // sunset today
                updateViewsSunriseText(context, views, data.sunriseCalendar(2), showSeconds);  // sunrise tomorrow
            }
        }

    }

    protected void updateViewsSunriseText(Context context, RemoteViews views, Calendar event, boolean showSeconds)
    {
        SuntimesUtils.TimeDisplayText sunriseText = utils.calendarTimeShortDisplayString(context, event, showSeconds);
        String sunriseString = sunriseText.getValue();
        CharSequence sunrise = (boldTime ? SuntimesUtils.createBoldSpan(null, sunriseString, sunriseString) : sunriseString);
        views.setTextViewText(R.id.text_time_rise, sunrise);
        views.setTextViewText(R.id.text_time_rise_suffix, sunriseText.getSuffix());
    }

    protected void updateViewsSunsetText(Context context, RemoteViews views, Calendar event, boolean showSeconds)
    {
        SuntimesUtils.TimeDisplayText sunsetText = utils.calendarTimeShortDisplayString(context, event, showSeconds);
        String sunsetString = sunsetText.getValue();
        CharSequence sunset = (boldTime ? SuntimesUtils.createBoldSpan(null, sunsetString, sunsetString) : sunsetString);
        views.setTextViewText(R.id.text_time_set, sunset);
        views.setTextViewText(R.id.text_time_set_suffix, sunsetText.getSuffix());
    }

    protected void updateViewsNoonText(Context context, RemoteViews views, Calendar event, boolean showSeconds)
    {
        SuntimesUtils.TimeDisplayText noonText = utils.calendarTimeShortDisplayString(context, event, showSeconds);
        String noonString = noonText.getValue();
        CharSequence noon = (boldTime ? SuntimesUtils.createBoldSpan(null, noonString, noonString) : noonString);
        views.setTextViewText(R.id.text_time_noon, noon);
        views.setTextViewText(R.id.text_time_noon_suffix, noonText.getSuffix());
    }

    protected int chooseSunLayout(int layout1, int layout2, SuntimesRiseSetData data, WidgetSettings.RiseSetOrder order)
    {
        switch (order)
        {
            case LASTNEXT:
                Calendar now = data.now();
                if (now.before(data.sunriseCalendarToday())) {
                    return layout2;   // last sunset, next sunrise

                } else if (now.before(data.sunsetCalendarToday())) {
                    return layout1;   // last sunrise, next sunset

                } else {
                    return layout2;   // last sunset, next sunrise
                }

            case TODAY:
            default:
                return layout1;
        }
    }

}
