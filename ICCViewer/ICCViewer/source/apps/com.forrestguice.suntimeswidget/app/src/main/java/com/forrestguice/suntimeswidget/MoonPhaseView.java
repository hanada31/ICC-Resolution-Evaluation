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

import android.content.Context;
import android.content.res.TypedArray;
//import android.os.Bundle;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.calculator.MoonPhaseDisplay;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.text.NumberFormat;

@SuppressWarnings("Convert2Diamond")
public class MoonPhaseView extends LinearLayout
{
    private SuntimesUtils utils = new SuntimesUtils();
    private boolean isRtl = false;
    private boolean centered = false;
    private boolean illumAtNoon = false;
    private boolean showPosition = false;

    private LinearLayout content;
    private TextView phaseText, illumText, azimuthText, elevationText;

    protected SuntimesMoonData data = null;  // cached

    public MoonPhaseView(Context context)
    {
        super(context);
        init(context, null);
    }

    public MoonPhaseView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        applyAttributes(context, attrs);
        init(context, attrs);
    }

    private void applyAttributes(Context context, AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MoonPhaseView, 0, 0);
        try {
            illumAtNoon = a.getBoolean(R.styleable.MoonPhaseView_illuminationAtLunarNoon, illumAtNoon);
            showPosition = a.getBoolean(R.styleable.MoonPhaseView_showPosition, false);
        } finally {
            a.recycle();
        }
    }

    private void init(Context context, AttributeSet attrs)
    {
        initLocale(context);
        themeViews(context);
        LayoutInflater.from(context).inflate(R.layout.layout_view_moonphase, this, true);

        if (attrs != null)
        {
            LayoutParams lp = generateLayoutParams(attrs);
            centered = ((lp.gravity == Gravity.CENTER) || (lp.gravity == Gravity.CENTER_HORIZONTAL));
        }

        content = (LinearLayout)findViewById(R.id.moonphase_layout);

        phaseText = (TextView)findViewById(R.id.text_info_moonphase);
        illumText = (TextView)findViewById(R.id.text_info_moonillum);
        azimuthText = (TextView)findViewById(R.id.text_info_moon_azimuth);
        elevationText = (TextView)findViewById(R.id.text_info_moon_elevation);

        if (isInEditMode())
        {
            updateViews(context, null);
        }
    }

    private int noteColor;
    private void themeViews(Context context)
    {
        int[] colorAttrs = { android.R.attr.textColorPrimary }; //, R.attr.springColor, R.attr.summerColor, R.attr.fallColor, R.attr.winterColor };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int def = R.color.transparent;
        noteColor = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
        typedArray.recycle();
    }

    public void themeViews(Context context, SuntimesTheme theme)
    {
        noteColor = theme.getTimeColor();
        int textColor = theme.getTextColor();

        illumText.setTextColor(textColor);
        azimuthText.setTextColor(textColor);
        elevationText.setTextColor(textColor);
        phaseText.setTextColor(theme.getTimeColor());

        int[] viewID = getIconViewIDs();
        Bitmap[] bitmaps = getThemedBitmaps(context, theme);
        for (int i=0; i<viewID.length; i++)
        {
            ImageView view = (ImageView)findViewById(viewID[i]);
            if (view != null && bitmaps[i] != null) {
                view.setImageBitmap(bitmaps[i]);
            }
        }
    }

    private static Bitmap[] getThemedBitmaps(Context context, SuntimesTheme theme)
    {
        int colorWaxing = theme.getMoonWaxingColor();
        int colorWaning = theme.getMoonWaningColor();
        int colorFull = theme.getMoonFullColor();
        int colorNew = theme.getMoonNewColor();

        return new Bitmap[] {
                SuntimesUtils.gradientDrawableToBitmap(context, MoonPhaseDisplay.FULL.getIcon(), colorFull, colorWaning, theme.getMoonFullStrokePixels(context)),
                SuntimesUtils.gradientDrawableToBitmap(context, MoonPhaseDisplay.NEW.getIcon(), colorNew, colorWaxing, theme.getMoonNewStrokePixels(context)),
                SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.WAXING_CRESCENT.getIcon(), colorWaxing, colorWaxing, 0),
                SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.FIRST_QUARTER.getIcon(), colorWaxing, colorWaxing, 0),
                SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.WAXING_GIBBOUS.getIcon(), colorWaxing, colorWaxing, 0),
                SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.WANING_CRESCENT.getIcon(), colorWaning, colorWaning, 0),
                SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.THIRD_QUARTER.getIcon(), colorWaning, colorWaning, 0),
                SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.WANING_GIBBOUS.getIcon(), colorWaning, colorWaning, 0)
        };
    }
    private static int[] getIconViewIDs()
    {
        return new int[] { R.id.icon_info_moonphase_full, R.id.icon_info_moonphase_new,
                R.id.icon_info_moonphase_waxing_crescent, R.id.icon_info_moonphase_waxing_quarter,
                R.id.icon_info_moonphase_waxing_gibbous, R.id.icon_info_moonphase_waning_crescent,
                R.id.icon_info_moonphase_waning_quarter, R.id.icon_info_moonphase_waning_gibbous };
    }

    private boolean tomorrowMode = false;
    public void setTomorrowMode( boolean value )
    {
        tomorrowMode = value;
    }
    public boolean isTomorrowMode()
    {
        return tomorrowMode;
    }

    public void initLocale(Context context)
    {
        isRtl = AppSettings.isLocaleRtl(context);
        SuntimesUtils.initDisplayStrings(context);
        MoonPhaseDisplay.initDisplayStrings(context);
    }

    protected void updateViews( Context context, SuntimesMoonData data )
    {
        int positionVisibility = (showPosition ? View.VISIBLE : View.GONE);
        azimuthText.setVisibility(positionVisibility);
        elevationText.setVisibility(positionVisibility);

        if (isInEditMode())
        {
            return;
        }

        this.data = data;
        if (data != null && data.isCalculated())
        {
            hideIcons();

            MoonPhaseDisplay phase = (tomorrowMode ? data.getMoonPhaseTomorrow() : data.getMoonPhaseToday());
            if (phase != null)
            {
                phaseText.setText(phase.getLongDisplayString());

                View phaseIcon = findViewById(phase.getView());
                if (phaseIcon != null) {
                    phaseIcon.setVisibility(View.VISIBLE);
                }

                /**Integer phaseColor = phaseColors.get(phase);
                if (phaseColor != null)
                {
                    phaseText.setTextColor(phaseColor);
                }*/
            }

            updateIllumination(context);
            updatePosition();

        } else {
            phaseText.setText("");
            illumText.setText("");
            azimuthText.setText("");
            elevationText.setText("");
            hideIcons();
        }
    }

    private void hideIcons()
    {
        for (MoonPhaseDisplay moonPhase : MoonPhaseDisplay.values())
        {
            View view = findViewById(moonPhase.getView());
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
    }

    public void updateIllumination(Context context)
    {
        if (data != null && data.isCalculated())
        {
            double illumination = (!illumAtNoon ? data.getMoonIlluminationNow()
                    : (tomorrowMode ? data.getMoonIlluminationTomorrow()
                    : data.getMoonIlluminationToday()));

            NumberFormat formatter = NumberFormat.getPercentInstance();
            formatter.setMinimumFractionDigits(0);
            formatter.setMaximumFractionDigits((illumAtNoon ? 0 : 1));

            String illum = formatter.format(illumination);
            String illumNote = (context == null ? illum : context.getString(R.string.moon_illumination, illum));
            SpannableString illumNoteSpan = SuntimesUtils.createColorSpan(null, illumNote, illum, noteColor);
            illumText.setText(illumNoteSpan);

        } else {
            illumText.setText("");
        }
    }

    public void updatePosition()
    {
        if (data != null && data.isCalculated())
        {
            SuntimesCalculator calculator = data.calculator();
            SuntimesCalculator.Position position = calculator.getMoonPosition(data.nowThen(data.calendar()));
            updatePosition(position);

        } else {
            updatePosition(null);
        }
    }

    public void updatePosition(SuntimesCalculator.Position position)
    {
        if (position == null)
        {
            if (azimuthText != null)
            {
                azimuthText.setText("");
                azimuthText.setContentDescription("");
            }
            if (elevationText != null)
            {
                elevationText.setText("");
                elevationText.setContentDescription("");
            }
            return;
        }

        if (azimuthText != null)
        {
            SuntimesUtils.TimeDisplayText azimuthText = utils.formatAsDirection2(position.azimuth, 2, false);
            String azimuthString = utils.formatAsDirection(azimuthText.getValue(), azimuthText.getSuffix());
            SpannableString azimuthSpan = SuntimesUtils.createRelativeSpan(null, azimuthString, azimuthText.getSuffix(), 0.7f);
            azimuthSpan = SuntimesUtils.createBoldSpan(azimuthSpan, azimuthString, azimuthText.getSuffix());
            this.azimuthText.setText(azimuthSpan);

            SuntimesUtils.TimeDisplayText azimuthDesc = utils.formatAsDirection2(position.azimuth, 2, true);
            this.azimuthText.setContentDescription(utils.formatAsDirection(azimuthDesc.getValue(), azimuthDesc.getSuffix()));
        }

        if (elevationText != null)
        {
            //int elevationColor = Color.WHITE;
            SuntimesUtils.TimeDisplayText elevationText = utils.formatAsElevation(position.elevation, 2);
            String elevationString = utils.formatAsElevation(elevationText.getValue(), elevationText.getSuffix());
            SpannableString elevationSpan = SuntimesUtils.createRelativeSpan(null, elevationString, elevationText.getSuffix(), 0.7f);
            //elevationSpan = SuntimesUtils.createColorSpan(elevationSpan, elevationString, elevationString, elevationColor);
            this.elevationText.setText(elevationSpan);
        }
    }

    public void adjustColumnWidth(int columnWidthPx)
    {
        phaseText.setMaxWidth(columnWidthPx);
    }

    /**public boolean saveState(Bundle bundle)
    {
        return true;
    }*/

    /**public void loadState(Bundle bundle) {}*/

    public void setOnClickListener( OnClickListener listener )
    {
        content.setOnClickListener(listener);
    }

    public void setOnLongClickListener( OnLongClickListener listener)
    {
        content.setOnLongClickListener(listener);
    }

}
