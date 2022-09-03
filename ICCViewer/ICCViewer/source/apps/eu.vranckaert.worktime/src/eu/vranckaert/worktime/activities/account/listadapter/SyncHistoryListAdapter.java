/*
 * Copyright 2013 Dirk Vranckaert
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

package eu.vranckaert.worktime.activities.account.listadapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.activities.account.AccountSyncHistoryActivity;
import eu.vranckaert.worktime.model.SyncHistory;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.date.DateFormat;
import eu.vranckaert.worktime.utils.date.DateUtils;
import eu.vranckaert.worktime.utils.date.TimeFormat;
import org.joda.time.PeriodType;

import java.util.Date;
import java.util.List;

/**
 * User: Dirk Vranckaert
 * Date: 11/02/13
 * Time: 13:23
 */
public class SyncHistoryListAdapter extends ArrayAdapter<SyncHistory> {
    private final String LOG_TAG = SyncHistoryListAdapter.class.getSimpleName();

    private AccountSyncHistoryActivity ctx;
    private List<SyncHistory> syncHistories;
    /**
     * {@inheritDoc}
     */
    public SyncHistoryListAdapter(AccountSyncHistoryActivity ctx, List<SyncHistory> syncHistories) {
        super(ctx, R.layout.list_item_sync_histories, syncHistories);
        Log.d(ctx, LOG_TAG, "Creating the sync history list adapter");

        this.ctx = ctx;
        this.syncHistories = syncHistories;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(ctx, LOG_TAG, "Start rendering/recycling row " + position);
        View row = null;
        final SyncHistory sh = syncHistories.get(position);

        if (convertView == null) {
            Log.d(ctx, LOG_TAG, "Render a new line in the list");
            row = ctx.getLayoutInflater().inflate(R.layout.list_item_sync_histories, parent, false);
        } else {
            Log.d(ctx, LOG_TAG, "Recycling an existing line in the list");
            row = convertView;
        }

        Log.d(ctx, LOG_TAG, "Ready to update the sync history data...");
        TextView started = (TextView) row.findViewById(R.id.account_sync_history_start_time);
        started.setText(DateUtils.DateTimeConverter.convertDateTimeToString(sh.getStarted(), DateFormat.MEDIUM, TimeFormat.MEDIUM, ctx));

        String syncDuration = "";
        if (sh.getEnded() != null) {
            syncDuration = DateUtils.TimeCalculator.calculateDuration(ctx, sh.getStarted(), sh.getEnded(), PeriodType.time());
        } else {
            syncDuration = DateUtils.TimeCalculator.calculateDuration(ctx, sh.getStarted(), new Date(), PeriodType.time());
        }

        TextView duration = (TextView) row.findViewById(R.id.account_sync_history_duration);
        duration.setText(syncDuration);

        TextView result = (TextView) row.findViewById(R.id.account_sync_history_result);
        ImageView resultIcon = (ImageView) row.findViewById(R.id.account_sync_history_result_icon);
        View reasonContainer = row.findViewById(R.id.account_sync_history_reason_container);
        reasonContainer.setVisibility(View.GONE);

        switch (sh.getStatus()) {
            case BUSY:
                resultIcon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_av_download));
                result.setText(ctx.getString(R.string.lbl_account_sync_resolution_busy));
                break;
            case SUCCESSFUL:
                resultIcon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_navigation_accept));
                result.setText(ctx.getString(R.string.lbl_account_sync_resolution_successful));
                break;
            case INTERRUPTED:
                resultIcon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_alerts_and_states_error));
                result.setText(ctx.getString(R.string.lbl_account_sync_resolution_interrupted));

                TextView interruptedReason = (TextView) row.findViewById(R.id.account_sync_history_reason);
                interruptedReason.setText(ctx.getString(R.string.lbl_account_sync_history_reason_interruption));
                reasonContainer.setVisibility(View.VISIBLE);
                break;
            case FAILED:
                resultIcon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_alerts_and_states_error));
                result.setText(ctx.getString(R.string.lbl_account_sync_resolution_failed));

                TextView failureReason = (TextView) row.findViewById(R.id.account_sync_history_reason);
                failureReason.setText(sh.getFailureReason());
                reasonContainer.setVisibility(View.VISIBLE);
                break;
            case TIMED_OUT:
                resultIcon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_device_access_time));
                result.setText(ctx.getString(R.string.lbl_account_sync_resolution_timed_out));
                break;
        }

        Log.d(ctx, LOG_TAG, "Done rendering row " + position);
        return row;
    }

    public void refill(List<SyncHistory> syncHistories) {
        this.syncHistories.clear();
        this.syncHistories.addAll(syncHistories);
        notifyDataSetChanged();
    }
}
