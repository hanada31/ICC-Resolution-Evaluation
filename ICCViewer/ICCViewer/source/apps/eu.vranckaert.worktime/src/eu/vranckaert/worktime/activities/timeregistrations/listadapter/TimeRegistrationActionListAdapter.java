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

package eu.vranckaert.worktime.activities.timeregistrations.listadapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import eu.vranckaert.worktime.utils.context.Log;

import java.util.List;

public class TimeRegistrationActionListAdapter extends ArrayAdapter<Object> {
    private final String LOG_TAG = TimeRegistrationActionListAdapter.class.getSimpleName();

    private Activity ctx;
    private List<Object> objects;

    /**
     * {@inheritDoc}
     */
    public TimeRegistrationActionListAdapter(Activity ctx, List<Object> objects) {
        super(ctx, android.R.layout.simple_spinner_item, objects);
        Log.d(ctx, LOG_TAG, "Creating the time registrations list adapter");

        this.ctx = ctx;
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView row = null;
        if (convertView == null) {
            Log.d(ctx, LOG_TAG, "Render a new line in the list");
            row = (TextView) ctx.getLayoutInflater().inflate(android.R.layout.simple_spinner_item, parent, false);
        } else {
            Log.d(ctx, LOG_TAG, "Recycling an existing line in the list");
            row = (TextView) convertView;
        }

        row.setText(objects.get(position).toString());

        return row;
    }
}
