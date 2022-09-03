/**
    Copyright (C) 2014-2018 Forrest Guice
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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import android.text.SpannableString;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmClockActivity;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;

import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class AlarmDialog extends DialogFragment
{
    public static final String KEY_ALARM_TYPE = "alarmdialog_alarmtype";
    public static final AlarmClockItem.AlarmType DEF_ALARM_TYPE = AlarmClockItem.AlarmType.ALARM;

    public static final String KEY_DIALOGTITLE = "alarmdialog_title";

    public static final String PREF_KEY_ALARM_LASTCHOICE = "alarmdialog_lastchoice";
    public static final SolarEvents PREF_DEF_ALARM_LASTCHOICE = SolarEvents.SUNRISE;

    protected static final SuntimesUtils utils = new SuntimesUtils();

    /**
     * The appWidgetID used when saving/loading choice to prefs (main app uses 0).
     */
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    public int getAppWidgetId() { return appWidgetId; }
    public void setAppWidgetId(int value) { appWidgetId = value; }

    private AlarmClockItem.AlarmType type = DEF_ALARM_TYPE;
    public AlarmClockItem.AlarmType getType() {
        return type;
    }
    public void setType(AlarmClockItem.AlarmType type) {
        this.type = type;
    }

    private String dialogTitle = null;
    public void setDialogTitle( String title ) {
        dialogTitle = title;
    }

    /**
     * The supporting datasets.
     */
    private SuntimesRiseSetDataset dataset;
    private SuntimesMoonData moondata;
    public SuntimesRiseSetDataset getData() { return dataset; }
    public SuntimesMoonData getMoonData()
    {
        return moondata;
    }
    public void setData(Context context, SuntimesRiseSetDataset dataset, SuntimesMoonData moondata)
    {
        this.dataset = dataset;
        this.moondata = moondata;
        updateAdapter(context);
        setChoice(choice);
    }

    public void updateAdapter(Context context)
    {
        adapter = SolarEvents.createAdapter(context);
        if (dataset != null)
        {
            boolean supportsGoldBlue = dataset.calculatorMode().hasRequestedFeature(SuntimesCalculator.FEATURE_GOLDBLUE);
            if (!supportsGoldBlue)
            {
                adapter.remove(SolarEvents.MORNING_BLUE8);
                adapter.remove(SolarEvents.MORNING_BLUE4);
                adapter.remove(SolarEvents.EVENING_BLUE4);
                adapter.remove(SolarEvents.EVENING_BLUE8);
                adapter.remove(SolarEvents.MORNING_GOLDEN);
                adapter.remove(SolarEvents.EVENING_GOLDEN);
            }

            boolean supportsMoon = moondata != null && moondata.calculatorMode().hasRequestedFeature(SuntimesCalculator.FEATURE_MOON);
            if (!supportsMoon)
            {
                adapter.remove(SolarEvents.MOONRISE);
                adapter.remove(SolarEvents.MOONSET);
            }
        }

        if (spinner_scheduleMode != null)
        {
            spinner_scheduleMode.setAdapter(adapter);
        }
    }

    private ArrayAdapter<SolarEvents> adapter = null;

    /**
     * The user's alarm choice.
     */
    private SolarEvents choice = null;
    public void setChoice( SolarEvents choice )
    {
        if (choice != null)
        {
            this.choice = choice;
            if (spinner_scheduleMode != null)
            {
                SpinnerAdapter adapter = spinner_scheduleMode.getAdapter();
                if (adapter != null)
                {
                    for (int i = 0; i < adapter.getCount(); i++)
                    {
                        SolarEvents event = (SolarEvents) adapter.getItem(i);
                        if (event.equals(choice))
                        {
                            spinner_scheduleMode.setSelection(i);
                            break;
                        }
                    }
                }
            }
        }
    }
    public SolarEvents getChoice() { return choice; }

    /**
     * @param savedInstanceState a Bundle containing dialog state
     * @return an AlarmDialog ready to be shown
     */
    @SuppressWarnings({"deprecation","RestrictedApi"})
    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final Activity myParent = getActivity();
        LayoutInflater inflater = myParent.getLayoutInflater();
        @SuppressLint("InflateParams")
        View dialogContent = inflater.inflate(R.layout.layout_dialog_schedalarm, null);

        Resources r = getResources();
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());

        String titleString = (dialogTitle != null) ? dialogTitle
                : myParent.getString(R.string.configAction_setAlarm);

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent, 0, padding, 0, 0);
        builder.setTitle(titleString);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, myParent.getString(R.string.schedalarm_dialog_cancel),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();

                        if (onCanceled != null)
                        {
                            onCanceled.onClick(dialog, which);
                        }
                    }
                }
        );

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, myParent.getString(R.string.schedalarm_dialog_ok),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        saveSettings(myParent);
                        dialog.dismiss();

                        if (onAccepted != null)
                        {
                            onAccepted.onClick(dialog, which);
                        }
                    }
                }
        );

        initViews(myParent, dialogContent);
        if (savedInstanceState != null)
        {
            //Log.d("DEBUG", "AlarmDialog onCreate (restoreState)");
            loadSettings(savedInstanceState);

        } else {
            //Log.d("DEBUG", "AlarmDialog onCreate (newState)");
            loadSettings(myParent);
        }
        return dialog;
    }

    /**
     * @param outState a Bundle used to save state
     */
    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        //Log.d("DEBUG", "AlarmDialog onSaveInstanceState");
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    /**
     *
     */
    private Spinner spinner_scheduleMode;
    private TextView txt_note;
    private ImageView icon_note;
    private TextView txt_location;

    protected void initViews( final Context context, View dialogContent )
    {
        initColors(context);
        SuntimesUtils.initDisplayStrings(context);
        WidgetSettings.initDisplayStrings(context);
        SolarEvents.initDisplayStrings(context);

        icon_note = (ImageView) dialogContent.findViewById(R.id.appwidget_schedalarm_note_icon);
        icon_note.setVisibility(View.GONE);

        txt_note = (TextView) dialogContent.findViewById(R.id.appwidget_schedalarm_note);
        txt_note.setText("");

        txt_location = (TextView) dialogContent.findViewById(R.id.appwidget_schedalarm_location);
        if (txt_location != null) {
            txt_location.setText("");
        }

        spinner_scheduleMode = (Spinner) dialogContent.findViewById(R.id.appwidget_schedalarm_mode);
        if (adapter != null)
        {
            spinner_scheduleMode.setAdapter(adapter);
        }

        TextView txt_modeLabel = (TextView) dialogContent.findViewById(R.id.appwidget_schedalarm_mode_label);
        if (txt_modeLabel != null) {
            txt_modeLabel.setText(type == AlarmClockItem.AlarmType.NOTIFICATION ? getString(R.string.configLabel_schednotify_mode) : getString(R.string.configLabel_schedalarm_mode) );
        }

        spinner_scheduleMode.setOnItemSelectedListener(
                new Spinner.OnItemSelectedListener()
                {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                    {
                        updateLocationLabel(context, txt_location, dataset.location());

                        choice = (SolarEvents)spinner_scheduleMode.getSelectedItem();
                        Calendar now0 = dataset.nowThen(dataset.calendar());
                        Calendar alarmCalendar = getCalendarForAlarmChoice(choice, now0);
                        if (alarmCalendar != null)
                        {
                            Calendar now = dataset.now();
                            if (now.after(alarmCalendar))      // getCalendarForAlarmChoice should return a datetime in the future..
                            {                                      // but supposing it doesn't (due to user defined date) then adjust alarmTime to be today
                                alarmCalendar = SuntimesData.nowThen(alarmCalendar, now);  // and if that is also past, adjust alarmTime to be tomorrow
                                if (now.after(alarmCalendar))
                                {
                                    Calendar tomorrow = (Calendar)now.clone();
                                    tomorrow.add(Calendar.DAY_OF_YEAR, 1);
                                    alarmCalendar = SuntimesData.nowThen(alarmCalendar, tomorrow);
                                }
                            }

                            String timeString =" " + utils.timeDeltaDisplayString(now.getTime(), alarmCalendar.getTime()).getValue() + " ";
                            String noteString = context.getString(R.string.schedalarm_dialog_note, timeString);
                            txt_note.setText(SuntimesUtils.createBoldColorSpan(null, noteString, timeString, color_textTimeDelta));
                            icon_note.setVisibility(View.GONE);

                            String modeDescription = (type == AlarmClockItem.AlarmType.NOTIFICATION) ? context.getString(R.string.configLabel_schednotify_mode) : context.getString(R.string.configLabel_schedalarm_mode);
                            SuntimesUtils.announceForAccessibility(txt_note,  modeDescription + " " + choice.getLongDisplayString() + ", " + txt_note.getText());

                        } else {
                            String timeString = " " + choice.getLongDisplayString() + " ";
                            String noteString = context.getString(R.string.schedalarm_dialog_note2, timeString);
                            txt_note.setText(SuntimesUtils.createBoldColorSpan(null, noteString, timeString, color_textTimeDelta));
                            icon_note.setVisibility(View.VISIBLE);
                            SuntimesUtils.announceForAccessibility(txt_note, choice.getLongDisplayString() + ", " + txt_note.getText());
                        }
                    }

                    public void onNothingSelected(AdapterView<?> parent)
                    {
                    }
                }
        );
    }

    private int color_textTimeDelta;
    private void initColors(Context context)
    {
        int[] colorAttrs = { android.R.attr.textColorPrimary };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int def = Color.WHITE;

        color_textTimeDelta = ContextCompat.getColor(context, typedArray.getResourceId(0, def));

        typedArray.recycle();
    }

    protected void loadSettings(Context context)
    {
        loadSettings(context, false);
    }
    protected void loadSettings(Context context, boolean overwriteCurrent)
    {
        if (overwriteCurrent || choice == null)
        {
            SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
            String choiceString = prefs.getString(PREF_KEY_ALARM_LASTCHOICE, PREF_DEF_ALARM_LASTCHOICE.name());
            try
            {
                choice = SolarEvents.valueOf(choiceString);
            } catch (IllegalArgumentException e) {
                choice = PREF_DEF_ALARM_LASTCHOICE;
            }
        }
        setChoice(choice);
    }
    protected void loadSettings(Bundle bundle)
    {
        dialogTitle = bundle.getString(KEY_DIALOGTITLE);

        String choiceString = bundle.getString(PREF_KEY_ALARM_LASTCHOICE);
        if (choiceString != null)
        {
            try {
                choice = SolarEvents.valueOf(choiceString);
            } catch (IllegalArgumentException e) {
                choice = PREF_DEF_ALARM_LASTCHOICE;
            }
        } else {
            choice = PREF_DEF_ALARM_LASTCHOICE;
        }
        setChoice(choice);

        String typeString = bundle.getString(KEY_ALARM_TYPE);
        if (typeString != null)
        {
            try {
                type = AlarmClockItem.AlarmType.valueOf(typeString);
            } catch (IllegalArgumentException e) {
                type = DEF_ALARM_TYPE;
            }
        } else {
            type = DEF_ALARM_TYPE;
        }
    }

    /**
     * Save alarm choice to prefs.
     * @param context a context used to access shared prefs
     */
    protected void saveSettings(Context context)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        prefs.putString(PREF_KEY_ALARM_LASTCHOICE, choice.name());
        prefs.apply();
    }

    /**
     * Save alarm choice to bundle.
     * @param bundle state persisted to this bundle
     */
    protected void saveSettings(Bundle bundle)
    {
        bundle.putString(KEY_DIALOGTITLE, dialogTitle);
        bundle.putString(KEY_ALARM_TYPE, type.name());
        bundle.putString(PREF_KEY_ALARM_LASTCHOICE, choice.name());
    }

    /**
     * Dialog accepted listener.
     */
    private DialogInterface.OnClickListener onAccepted = null;
    public void setOnAcceptedListener( DialogInterface.OnClickListener listener )
    {
        onAccepted = listener;
    }

    /**
     * Dialog cancelled listener.
     */
    private DialogInterface.OnClickListener onCanceled = null;
    public void setOnCanceledListener( DialogInterface.OnClickListener listener )
    {
        onCanceled = listener;
    }

    /**
     * @param choice a SolarEvent "alarm choice"
     * @param now a Calendar representing "right now"
     * @return a Calendar representing the alarm selection
     */
    public Calendar getCalendarForAlarmChoice( SolarEvents choice, Calendar now )
    {
        Date time = now.getTime();
        Calendar calendar = null;
        switch (choice)
        {
            case MOONRISE:
                if (moondata != null) {
                    calendar = moondata.moonriseCalendarToday();
                    if (calendar == null || time.after(calendar.getTime()))
                    {
                        calendar = moondata.moonriseCalendarTomorrow();
                    }
                }
                break;
            case MOONSET:
                if (moondata != null) {
                    calendar = moondata.moonsetCalendarToday();
                    if (calendar == null || time.after(calendar.getTime()))
                    {
                        calendar = moondata.moonsetCalendarTomorrow();
                    }
                }
                break;
            case MORNING_ASTRONOMICAL:
                calendar = dataset.dataAstro.sunriseCalendarToday();
                if (calendar == null || time.after(calendar.getTime()))
                {
                    calendar = dataset.dataAstro.sunriseCalendarOther();
                }
                break;
            case MORNING_NAUTICAL:
                calendar = dataset.dataNautical.sunriseCalendarToday();
                if (calendar == null || time.after(calendar.getTime()))
                {
                    calendar = dataset.dataNautical.sunriseCalendarOther();
                }
                break;
            case MORNING_BLUE8:
                calendar = dataset.dataBlue8.sunriseCalendarToday();
                if (calendar == null || time.after(calendar.getTime()))
                {
                    calendar = dataset.dataBlue8.sunriseCalendarOther();
                }
                break;
            case MORNING_CIVIL:
                calendar = dataset.dataCivil.sunriseCalendarToday();
                if (calendar == null || time.after(calendar.getTime()))
                {
                    calendar = dataset.dataCivil.sunriseCalendarOther();
                }
                break;
            case MORNING_BLUE4:
                calendar = dataset.dataBlue4.sunriseCalendarToday();
                if (calendar == null || time.after(calendar.getTime()))
                {
                    calendar = dataset.dataBlue4.sunriseCalendarOther();
                }
                break;
            case MORNING_GOLDEN:
                calendar = dataset.dataGold.sunriseCalendarToday();
                if (calendar == null || time.after(calendar.getTime()))
                {
                    calendar = dataset.dataGold.sunriseCalendarOther();
                }
                break;
            case NOON:
                calendar = dataset.dataNoon.sunriseCalendarToday();
                if (calendar == null || time.after(calendar.getTime()))
                {
                    calendar = dataset.dataNoon.sunriseCalendarOther();
                }
                break;
            case SUNSET:
                calendar = dataset.dataActual.sunsetCalendarToday();
                if (calendar == null || time.after(calendar.getTime()))
                {
                    calendar = dataset.dataActual.sunsetCalendarOther();
                }
                break;
            case EVENING_GOLDEN:
                calendar = dataset.dataGold.sunsetCalendarToday();
                if (calendar == null || time.after(calendar.getTime()))
                {
                    calendar = dataset.dataGold.sunsetCalendarOther();
                }
                break;
            case EVENING_BLUE4:
                calendar = dataset.dataBlue4.sunsetCalendarToday();
                if (calendar == null || time.after(calendar.getTime()))
                {
                    calendar = dataset.dataBlue4.sunsetCalendarOther();
                }
                break;
            case EVENING_CIVIL:
                calendar = dataset.dataCivil.sunsetCalendarToday();
                if (calendar == null || time.after(calendar.getTime()))
                {
                    calendar = dataset.dataCivil.sunsetCalendarOther();
                }
                break;
            case EVENING_BLUE8:
                calendar = dataset.dataBlue8.sunsetCalendarToday();
                if (calendar == null || time.after(calendar.getTime()))
                {
                    calendar = dataset.dataBlue8.sunsetCalendarOther();
                }
                break;
            case EVENING_NAUTICAL:
                calendar = dataset.dataNautical.sunsetCalendarToday();
                if (calendar == null || time.after(calendar.getTime()))
                {
                    calendar = dataset.dataNautical.sunsetCalendarOther();
                }
                break;
            case EVENING_ASTRONOMICAL:
                calendar = dataset.dataAstro.sunsetCalendarToday();
                if (calendar == null || time.after(calendar.getTime()))
                {
                    calendar = dataset.dataAstro.sunsetCalendarOther();
                }
                break;
            case SUNRISE:
            default:
                calendar = dataset.dataActual.sunriseCalendarToday();
                if (calendar == null || time.after(calendar.getTime()))
                {
                    calendar = dataset.dataActual.sunriseCalendarOther();
                }
                break;
        }
        return calendar;
    }

    /**
     * Schedule the selected alarm on click.
     */
    public DialogInterface.OnClickListener scheduleAlarmClickListener = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialogInterface, int i)
        {
            Context context = getContext();
            if (context != null)
            {
                SolarEvents choice = getChoice();
                Calendar now = dataset.nowThen(dataset.calendar());
                Calendar calendar = getCalendarForAlarmChoice(choice, now);

                if (calendar != null)
                {
                    DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
                    String alarmLabel = context.getString(R.string.schedalarm_labelformat, choice.getShortDisplayString(), dateFormat.format(calendar.getTime()));
                    AlarmDialog.scheduleAlarm(getActivity(), alarmLabel, calendar, choice);

                } else {
                    String alarmErrorTxt = getString(R.string.schedalarm_dialog_error) + "\n" + getString(R.string.schedalarm_dialog_note2, choice.getLongDisplayString());
                    Toast alarmError = Toast.makeText(getActivity(), alarmErrorTxt, Toast.LENGTH_LONG);
                    alarmError.show();
                }
            }
        }
    };

    public static void scheduleAlarm(Activity context, String label, Calendar calendar, SolarEvents event)
    {
        if (calendar == null)
            return;

        Calendar alarm = new GregorianCalendar(TimeZone.getDefault());
        alarm.setTimeInMillis(calendar.getTimeInMillis());
        int hour = alarm.get(Calendar.HOUR_OF_DAY);
        int minutes = alarm.get(Calendar.MINUTE);

        Intent alarmIntent = new Intent(AlarmClock.ACTION_SET_ALARM);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //alarmIntent.putExtra(AlarmClock.EXTRA_MESSAGE, label);
        alarmIntent.putExtra(AlarmClock.EXTRA_HOUR, hour);
        alarmIntent.putExtra(AlarmClock.EXTRA_MINUTES, minutes);
        alarmIntent.putExtra(AlarmClockActivity.EXTRA_SOLAREVENT, event.name());

        if (alarmIntent.resolveActivity(context.getPackageManager()) != null)
        {
            context.startActivity(alarmIntent);
        }
    }

    /**
     * @param context a context used to start the "show alarm" intent
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void showAlarms(Activity context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            Intent alarmsIntent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
            alarmsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (alarmsIntent.resolveActivity(context.getPackageManager()) != null)
            {
                context.startActivity(alarmsIntent);
            }
        }
    }

    public static boolean updateLocationLabel(Context context, TextView text_location, Location location)
    {
        if (text_location != null)
        {
            if (location != null)
            {
                String coordString = context.getString(R.string.location_format_latlon, location.getLatitude(), location.getLongitude());
                String labelString = location.getLabel();
                String displayString = labelString + "\n" + coordString;
                SpannableString displayText = SuntimesUtils.createBoldSpan(null, displayString, labelString);
                displayText = SuntimesUtils.createRelativeSpan(displayText, displayString, coordString, 0.75f);
                text_location.setText(displayText);
                return true;

            } else {
                text_location.setText("");
                return false;
            }
        } else return false;
    }

}