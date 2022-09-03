/**
    Copyright (C) 2014-2019 Forrest Guice
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
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.forrestguice.suntimeswidget.calculator.SuntimesClockData;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeData;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.WidgetThemeListActivity;

import java.util.ArrayList;

public class SuntimesWidgetListActivity extends AppCompatActivity
{
    private static final String DIALOGTAG_HELP = "help";
    private static final String DIALOGTAG_ABOUT = "about";

    private static final String KEY_LISTVIEW_TOP = "widgetlisttop";
    private static final String KEY_LISTVIEW_INDEX = "widgetlistindex";

    private ActionBar actionBar;
    private ListView widgetList;
    private static final SuntimesUtils utils = new SuntimesUtils();

    public SuntimesWidgetListActivity()
    {
        super();
    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        Context context = AppSettings.initLocale(newBase);
        super.attachBaseContext(context);
    }

    /**
     * OnCreate: the Activity initially created
     * @param icicle a Bundle containing saved state
     */
    @Override
    public void onCreate(Bundle icicle)
    {
        setTheme(AppSettings.loadTheme(this));
        super.onCreate(icicle);
        SuntimesUtils.initDisplayStrings(this);

        setResult(RESULT_CANCELED);
        setContentView(R.layout.layout_activity_widgetlist);
        initViews(this);
        updateWidgetAlarms(this);
    }

    /**
     * OnStart: the Activity becomes visible
     */
    @Override
    public void onStart()
    {
        super.onStart();
        updateViews(this);
    }

    /**
     * OnResume: the user is now interacting w/ the Activity (running state)
     */
    @Override
    public void onResume()
    {
        super.onResume();
    }

    /**
     * OnPause: the user about to interact w/ another Activity
     */
    @Override
    public void onPause()
    {
        super.onPause();
    }

    /**
     * OnStop: the Activity no longer visible
     */
    @Override
    public void onStop()
    {
        super.onStop();
    }

    /**
     * OnDestroy: the activity destroyed
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }


    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState(outState);
        saveListViewPosition(outState);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedState)
    {
        super.onRestoreInstanceState(savedState);
        restoreListViewPosition(savedState);
    }

    /**
     * ..based on stack overflow answer by ian
     * https://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
     */
    private void saveListViewPosition( Bundle outState)
    {
        int i = widgetList.getFirstVisiblePosition();
        outState.putInt(KEY_LISTVIEW_INDEX, i);

        int top = 0;
        View firstItem = widgetList.getChildAt(0);
        if (firstItem != null)
        {
            top = firstItem.getTop() - widgetList.getPaddingTop();
        }
        outState.putInt(KEY_LISTVIEW_TOP, top);
    }

    private void restoreListViewPosition(@NonNull Bundle savedState )
    {
        int i = savedState.getInt(KEY_LISTVIEW_INDEX, -1);
        if (i >= 0)
        {
            int top = savedState.getInt(KEY_LISTVIEW_TOP, 0);
            widgetList.setSelectionFromTop(i, top);
        }
    }

    /**
     * initialize ui/views
     * @param context a context used to access resources
     */
    protected void initViews(Context context)
    {
        SuntimesUtils.initDisplayStrings(context);

        Toolbar menuBar = (Toolbar) findViewById(R.id.app_menubar);
        setSupportActionBar(menuBar);
        actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        widgetList = (ListView)findViewById(R.id.widgetList);
        widgetList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                WidgetListItem widgetItem = (WidgetListItem) widgetList.getAdapter().getItem(position);
                reconfigureWidget(widgetItem);
            }
        });

        View widgetListEmpty = findViewById(android.R.id.empty);
        widgetListEmpty.setOnClickListener(onEmptyViewClick);
        widgetList.setEmptyView(widgetListEmpty);
    }

    /**
     * onEmptyViewClick
     */
    private View.OnClickListener onEmptyViewClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showHelp();
        }
    };

    /**
     * updateViews
     * @param context context
     */
    protected void updateViews(Context context)
    {
        widgetList.setAdapter(WidgetListAdapter.createWidgetListAdapter(context));
    }

    /**
     * showHelp
     */
    protected void showHelp()
    {
        HelpDialog helpDialog = new HelpDialog();
        helpDialog.setContent(getString(R.string.help_widgetlist));
        helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
    }

    /**
     * showAbout
     */
    protected void showAbout()
    {
        AboutDialog aboutDialog = new AboutDialog();
        aboutDialog.show(getSupportFragmentManager(), DIALOGTAG_ABOUT);
    }

    /**
     * launchThemeEditor
     */
    protected void launchThemeEditor(Context context)
    {
        Intent configThemesIntent = new Intent(context, WidgetThemeListActivity.class);
        configThemesIntent.putExtra(WidgetThemeListActivity.PARAM_NOSELECT, true);
        startActivity(configThemesIntent);
    }

    /**
     * @param widget a WidgetListItem (referencing some widget id)
     */
    protected void reconfigureWidget(WidgetListItem widget)
    {
        Intent configIntent = new Intent(this, widget.getConfigClass());
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget.getWidgetId());
        configIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        configIntent.putExtra(WidgetSettings.ActionMode.ONTAP_LAUNCH_CONFIG.name(), true);
        startActivity(configIntent);
    }

    /**
     * updateWidgetAlarms
     * @param context context
     */
    public static void updateWidgetAlarms(Context context)
    {
        Intent updateIntent = new Intent();
        updateIntent.setAction(SuntimesWidget0.SUNTIMES_ALARM_UPDATE);
        context.sendBroadcast(updateIntent);
    }

    /**
     * ListItem representing a running widget; specifies appWidgetId, and configuration activity.
     */
    public static class WidgetListItem
    {
        private final int appWidgetId;
        private final int icon;
        private final String title;
        private final String summary;
        private final Class configClass;

        public WidgetListItem( int appWidgetId, int icon, String title, String summary, Class configClass )
        {
            this.appWidgetId = appWidgetId;
            this.configClass = configClass;
            this.icon = icon;
            this.title = title;
            this.summary = summary;
        }

        public int getWidgetId()
        {
            return appWidgetId;
        }

        public Class getConfigClass()
        {
            return configClass;
        }

        public int getIcon()
        {
            return icon;
        }

        public String getTitle()
        {
            return title;
        }

        public String getSummary()
        {
            return summary;
        }

        public String toString()
        {
            return getTitle();
        }
    }

    /**
     * A ListAdapter of WidgetListItems.
     */
    @SuppressWarnings("Convert2Diamond")
    public static class WidgetListAdapter extends ArrayAdapter<WidgetListItem>
    {
        private Context context;
        private ArrayList<WidgetListItem> widgets;

        public WidgetListAdapter(Context context, ArrayList<WidgetListItem> widgets)
        {
            super(context, R.layout.layout_listitem_widgets, widgets);
            this.context = context;
            this.widgets = widgets;
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
        {
            return widgetItemView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent)
        {
            return widgetItemView(position, convertView, parent);
        }

        private View widgetItemView(int position, View convertView, @NonNull ViewGroup parent)
        {
            View view = convertView;
            if (convertView == null)
            {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(R.layout.layout_listitem_widgets, parent, false);
            }

            WidgetListItem item = widgets.get(position);

            ImageView icon = (ImageView) view.findViewById(android.R.id.icon1);
            icon.setImageResource(item.getIcon());

            TextView text = (TextView) view.findViewById(android.R.id.text1);
            text.setText(item.getTitle());

            TextView text2 = (TextView) view.findViewById(android.R.id.text2);
            text2.setText(item.getSummary());

            TextView text3 = (TextView) view.findViewById(R.id.text3);
            if (text3 != null)
            {
                text3.setText(String.format("%s", item.getWidgetId()));
            }

            return view;
        }

        public static WidgetListItem createWidgetListItem(Context context, int appWidgetId, AppWidgetManager widgetManager, SuntimesData data, String widgetTitle, String type) throws ClassNotFoundException
        {
            AppWidgetProviderInfo info = widgetManager.getAppWidgetInfo(appWidgetId);
            String title = context.getString(R.string.configLabel_widgetList_itemTitle, widgetTitle);
            String source = ((data.calculatorMode() == null) ? "def" : data.calculatorMode().getName());
            String summary = context.getString(R.string.configLabel_widgetList_itemSummaryPattern, type, source);
            return new WidgetListItem(appWidgetId, info.icon, title, summary, Class.forName(info.configure.getClassName()) );
        }

        public static ArrayList<WidgetListItem> createWidgetListItems(Context context, AppWidgetManager widgetManager, Class widgetClass, String titlePattern)
        {
            ArrayList<WidgetListItem> items = new ArrayList<WidgetListItem>();
            int[] ids = widgetManager.getAppWidgetIds(new ComponentName(context, widgetClass));
            for (int id : ids)
            {
                try {
                    SuntimesData data;
                    String widgetTitle;
                    String widgetType = getWidgetName(context, widgetClass);

                    if (widgetClass == SolsticeWidget0.class)
                    {
                        SuntimesEquinoxSolsticeData data0 =  new SuntimesEquinoxSolsticeData(context, id);
                        widgetTitle = utils.displayStringForTitlePattern(context, titlePattern, data0);
                        data = data0;

                    } else if (widgetClass == MoonWidget0.class || widgetClass == MoonWidget0_2x1.class || widgetClass == MoonWidget0_3x1.class) {
                        SuntimesMoonData data0 =  new SuntimesMoonData(context, id, "moon");
                        widgetTitle = utils.displayStringForTitlePattern(context, titlePattern, data0);
                        data = data0;

                    } else if (widgetClass ==ClockWidget0.class || widgetClass == ClockWidget0_3x1.class) {
                        SuntimesClockData data0 = new SuntimesClockData(context, id);
                        widgetTitle = utils.displayStringForTitlePattern(context, titlePattern, data0);
                        data = data0;

                    } else {
                        SuntimesRiseSetData data0 = new SuntimesRiseSetData(context, id);
                        widgetTitle = utils.displayStringForTitlePattern(context, titlePattern, data0);
                        data = data0;
                    }

                    items.add(createWidgetListItem(context, id, widgetManager, data, widgetTitle, widgetType));
                } catch (ClassNotFoundException e) {
                    Log.e("WidgetListActivity", "configuration class for widget " + id + " missing.");
                }
            }
            return items;
        }

        public static WidgetListAdapter createWidgetListAdapter(Context context)
        {
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
            ArrayList<WidgetListItem> items = new ArrayList<WidgetListItem>();

            String titlePattern0 = context.getString(R.string.configLabel_widgetList_itemTitlePattern);
            items.addAll(createWidgetListItems(context, widgetManager, SuntimesWidget0.class, titlePattern0));
            items.addAll(createWidgetListItems(context, widgetManager, SuntimesWidget0_2x1.class, titlePattern0));
            items.addAll(createWidgetListItems(context, widgetManager, SuntimesWidget1.class, titlePattern0));
            items.addAll(createWidgetListItems(context, widgetManager, SolsticeWidget0.class, titlePattern0));

            String titlePattern1 = context.getString(R.string.configLabel_widgetList_itemTitlePattern1);
            items.addAll(createWidgetListItems(context, widgetManager, MoonWidget0.class, titlePattern1));
            items.addAll(createWidgetListItems(context, widgetManager, MoonWidget0_2x1.class, titlePattern1));
            items.addAll(createWidgetListItems(context, widgetManager, MoonWidget0_3x1.class, titlePattern1));
            items.addAll(createWidgetListItems(context, widgetManager, SuntimesWidget2.class, titlePattern1));
            items.addAll(createWidgetListItems(context, widgetManager, SuntimesWidget2_3x1.class, titlePattern1));
            items.addAll(createWidgetListItems(context, widgetManager, SuntimesWidget2_3x2.class, titlePattern1));
            items.addAll(createWidgetListItems(context, widgetManager, ClockWidget0.class, titlePattern1));
            items.addAll(createWidgetListItems(context, widgetManager, ClockWidget0_3x1.class, titlePattern1));

            return new WidgetListAdapter(context, items);
        }

        public static String getWidgetName(Context context, Class widgetClass)
        {
            if (widgetClass == SolsticeWidget0.class)
                return context.getString(R.string.app_name_solsticewidget0);

            if (widgetClass == ClockWidget0.class)
                return context.getString(R.string.app_name_clockwidget0);

            if (widgetClass == ClockWidget0_3x1.class)
                return context.getString(R.string.app_name_clockwidget0) + " (3x1)";

            if (widgetClass == MoonWidget0.class)
                return context.getString(R.string.app_name_moonwidget0);

            if (widgetClass == MoonWidget0_2x1.class)
                return context.getString(R.string.app_name_moonwidget0) + " (2x1)";

            if (widgetClass == MoonWidget0_3x1.class)
                return context.getString(R.string.app_name_moonwidget0) + " (3x1)";

            if (widgetClass == SuntimesWidget1.class)
                return context.getString(R.string.app_name_widget1);

            if (widgetClass == SuntimesWidget2.class)
                return context.getString(R.string.app_name_widget2);

            if (widgetClass == SuntimesWidget2_3x1.class)
                return context.getString(R.string.app_name_widget2) + " (3x1)";

            if (widgetClass == SuntimesWidget2_3x2.class)
                return context.getString(R.string.app_name_widget2) + " (3x2)";

            if (widgetClass == SuntimesWidget0_2x1.class)
                return context.getString(R.string.app_name_widget0) + " (2x1)";

            return context.getString(R.string.app_name_widget0);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.widgetlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_themes:
                launchThemeEditor(SuntimesWidgetListActivity.this);
                return true;

            case R.id.action_help:
                showHelp();
                return true;

            case R.id.action_about:
                showAbout();
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu)
    {
        SuntimesUtils.forceActionBarIcons(menu);
        return super.onPrepareOptionsPanel(view, menu);
    }

}
