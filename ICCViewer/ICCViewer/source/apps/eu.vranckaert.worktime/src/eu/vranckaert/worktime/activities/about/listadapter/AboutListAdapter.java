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
package eu.vranckaert.worktime.activities.about.listadapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.activities.about.AboutListElement;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.string.StringUtils;

import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 23/12/11
 * Time: 14:19
 */
public class AboutListAdapter extends ArrayAdapter<AboutListElement> {
    private final String LOG_TAG = AboutListAdapter.class.getSimpleName();

    private Activity ctx;
    private List<AboutListElement> aboutListElements;
    
    private static final int layoutResId = R.layout.list_item_about;
    /**
     * {@inheritDoc}
     */
    public AboutListAdapter(Activity ctx, List<AboutListElement> aboutListElements) {
        super(ctx, layoutResId, aboutListElements);
        Log.d(ctx, LOG_TAG, "Creating the about list adapter");

        this.ctx = ctx;
        this.aboutListElements = aboutListElements;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(ctx, LOG_TAG, "Start rendering/recycling row " + position);
        View row = null;
        final AboutListElement element = aboutListElements.get(position);
        Log.d(ctx, LOG_TAG, "Title of row is: " + element.getTitle());

        if (convertView == null) {
            Log.d(ctx, LOG_TAG, "Render a new line in the list");
            row = ctx.getLayoutInflater().inflate(layoutResId, parent, false);
        } else {
            Log.d(ctx, LOG_TAG, "Recycling an existing line in the list");
            row = convertView;
        }

        Log.d(ctx, LOG_TAG, "Ready to update the title of an element...");
        TextView title = (TextView) row.findViewById(R.id.lbl_about_title);
        title.setText(element.getTitle());

        TextView value = (TextView) row.findViewById(R.id.lbl_about_value);
        if (StringUtils.isNotBlank(element.getValue())) {
            Log.d(ctx, LOG_TAG, "Ready to update the summary of an element...");
            value.setText(element.getValue());
        } else {
            value.setVisibility(View.GONE);
        }

        Log.d(ctx, LOG_TAG, "Done rendering row " + position);
        return row;
    }

    public void refill(List<AboutListElement> aboutListElements) {
        this.aboutListElements.clear();
        this.aboutListElements.addAll(aboutListElements);
        notifyDataSetChanged();
    }
}
