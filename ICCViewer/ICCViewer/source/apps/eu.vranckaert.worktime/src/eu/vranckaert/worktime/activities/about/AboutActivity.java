/*
 * Copyright 2012 Dirk Vranckaert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.vranckaert.worktime.activities.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.activities.about.listadapter.AboutListAdapter;
import eu.vranckaert.worktime.constants.TrackerConstants;
import eu.vranckaert.worktime.dao.utils.DaoConstants;
import eu.vranckaert.worktime.utils.context.ContextUtils;
import eu.vranckaert.worktime.utils.context.IntentUtil;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.donations.DonationsActivity;
import eu.vranckaert.worktime.utils.tracker.AnalyticsTracker;
import eu.vranckaert.worktime.utils.view.actionbar.ActionBarGuiceListActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 05/02/11
 * Time: 19:06
 */
public class AboutActivity extends ActionBarGuiceListActivity {
    private static final String LOG_TAG = AboutActivity.class.getSimpleName();
    
    private AnalyticsTracker tracker;

    private List<AboutListElement> aboutListElements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setTitle(R.string.lbl_about_title);
        setDisplayHomeAsUpEnabled(true);
        
        tracker = AnalyticsTracker.getInstance(getApplicationContext());
        tracker.trackPageView(TrackerConstants.PageView.ABOUT_ACTIVITY);

        aboutListElements = createElementList();

        refill(aboutListElements);
        addClickEvent();
    }

    private void addClickEvent() {
        getListView().setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(getApplicationContext(), LOG_TAG, "Clicked on about item " + position);
                AboutListElement aboutListElement = aboutListElements.get(position);

                if (aboutListElement.getIntent() != null) {
                    Log.d(getApplicationContext(), LOG_TAG, "An intent is found for this about item (" + aboutListElement.getTitle() + "), firing it now!");
                    startActivity(aboutListElement.getIntent());
                } else {
                    Log.d(getApplicationContext(), LOG_TAG, "No intent is found to be fired for this about item (" + aboutListElement.getTitle() + ") !");
                }
            }
        });
    }

    private List<AboutListElement> createElementList() {
        List<AboutListElement> aboutListElements = new ArrayList<AboutListElement>();

        String project = getString(R.string.app_name);
        AboutListElement projectElement = new AboutListElement(R.string.lbl_about_project, project);
        aboutListElements.add(projectElement);

        String versionName = ContextUtils.getCurrentApplicationVersionName(AboutActivity.this);
        AboutListElement versionNameElement = new AboutListElement(R.string.lbl_about_version_name, versionName);
        aboutListElements.add(versionNameElement);

        int versionCode = ContextUtils.getCurrentApplicationVersionCode(AboutActivity.this);
        AboutListElement versionCodeElement = new AboutListElement(R.string.lbl_about_version_code, String.valueOf(versionCode));
        aboutListElements.add(versionCodeElement);

        Intent licenseIntent = new Intent(Intent.ACTION_VIEW);
        licenseIntent.setData(Uri.parse("http://www.apache.org/licenses/LICENSE-2.0"));
        AboutListElement licensingElement = new AboutListElement(R.string.lbl_about_license, getString(R.string.lbl_about_license_apache_2_0), licenseIntent);
        aboutListElements.add(licensingElement);

        Intent donationIntent = new Intent(this, DonationsActivity.class);
        AboutListElement donationElement = new AboutListElement(R.string.lbl_about_donation, getString(R.string.lbl_about_donation_summary), donationIntent);
        aboutListElements.add(donationElement);

        String databaseName  = DaoConstants.DATABASE;
        AboutListElement databaseNameElement = new AboutListElement(R.string.lbl_about_database_name, databaseName);
        aboutListElements.add(databaseNameElement);

        int databaseVersionCode = DaoConstants.VERSION;
        AboutListElement databaseVersionElement = new AboutListElement(R.string.lbl_about_database_version, String.valueOf(databaseVersionCode));
        aboutListElements.add(databaseVersionElement);

        final String website = "http://code.google.com/p/worktime/";
        Intent websiteIntent = new Intent(Intent.ACTION_VIEW);
        websiteIntent.setData(Uri.parse(website));
        AboutListElement websiteElement = new AboutListElement(R.string.lbl_about_website, website, websiteIntent);
        aboutListElements.add(websiteElement);

        final String bugTrackingWebsite = "http://code.google.com/p/worktime/issues/entry";
        Intent bugTrackingIntent = new Intent(Intent.ACTION_VIEW);
        bugTrackingIntent.setData(Uri.parse(bugTrackingWebsite));
        AboutListElement bugTrackingWebsiteElement = new AboutListElement(R.string.lbl_about_bug_tracking_website, bugTrackingWebsite, bugTrackingIntent);
        aboutListElements.add(bugTrackingWebsiteElement);

        return aboutListElements;
    }

    private void refill(List<AboutListElement> aboutListElements) {
        if (getListView().getAdapter() == null) {
            AboutListAdapter adapter = new AboutListAdapter(AboutActivity.this, aboutListElements);
            setListAdapter(adapter);
        } else {
            ((AboutListAdapter) getListView().getAdapter()).refill(aboutListElements);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                IntentUtil.goHome(AboutActivity.this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tracker.stopSession();
    }
}
