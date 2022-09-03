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

package eu.vranckaert.worktime.activities.account;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.activities.preferences.AccountSyncPreferencesActivity;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.TrackerConstants;
import eu.vranckaert.worktime.exceptions.network.NoNetworkConnectionException;
import eu.vranckaert.worktime.exceptions.worktime.account.UserNotLoggedInException;
import eu.vranckaert.worktime.model.SyncHistory;
import eu.vranckaert.worktime.model.User;
import eu.vranckaert.worktime.service.AccountService;
import eu.vranckaert.worktime.utils.alarm.AlarmUtil;
import eu.vranckaert.worktime.utils.context.AsyncHelper;
import eu.vranckaert.worktime.utils.context.IntentUtil;
import eu.vranckaert.worktime.utils.date.DateFormat;
import eu.vranckaert.worktime.utils.date.DateUtils;
import eu.vranckaert.worktime.utils.date.TimeFormat;
import eu.vranckaert.worktime.utils.string.StringUtils;
import eu.vranckaert.worktime.utils.tracker.AnalyticsTracker;
import eu.vranckaert.worktime.utils.view.actionbar.synclock.SyncLockedActivity;
import eu.vranckaert.worktime.web.json.exception.GeneralWebException;
import org.joda.time.PeriodType;
import roboguice.inject.InjectView;

import java.util.Date;

/**
 * User: Dirk Vranckaert
 * Date: 12/12/12
 * Time: 10:04
 */
public class AccountProfileActivity extends SyncLockedActivity {
    private static final String LOG_TAG = AccountProfileActivity.class.getSimpleName();

    private AnalyticsTracker tracker;

    @Inject private AccountService accountService;

    @InjectView(R.id.account_profile_profile_container) private View profileContainer;
    @InjectView(R.id.account_profile_email) private TextView email;
    @InjectView(R.id.account_profile_name) private TextView name;
    @InjectView(R.id.account_profile_registered_since) private TextView registeredSince;
    @InjectView(R.id.account_profile_logged_in_since) private TextView loggedInSince;

    @InjectView(R.id.account_profile_sync_history_container) private View syncHistoryContainer;
    @InjectView(R.id.account_profile_last_start_time) private TextView syncHistoryStartTime;
    @InjectView(R.id.account_profile_last_end_time) private TextView syncHistoryEndTime;
    @InjectView(R.id.account_profile_last_resolution) private TextView syncHistoryResolution;
    @InjectView(R.id.account_profile_last_reason_label) private TextView syncHistoryReasonLabel;
    @InjectView(R.id.account_profile_last_reason) private TextView syncHistoryReason;
    @InjectView(R.id.account_profile_view_full_history_button) private Button fullHistoryButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_account_profile);

        setTitle(R.string.lbl_account_profile_title);
        setDisplayHomeAsUpEnabled(true);

        tracker = AnalyticsTracker.getInstance(getApplicationContext());
        tracker.trackPageView(TrackerConstants.PageView.ACCOUNT_DETAILS_ACTIVITY);

        User user = accountService.getOfflineUserDate();
        if (user != null)
            updateUI(user);

        fullHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountProfileActivity.this, AccountSyncHistoryActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.ab_activity_account_profile, menu);

        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        boolean r = super.onCreateOptionsMenu(menu);

        // Disable click on home-button
        getActionBarHelper().setHomeButtonEnabled(false);
        return r;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                IntentUtil.goBack(this);
                break;
            case R.id.menu_account_profile_activity_sync: {
                getActionBarHelper().setRefreshActionItemState(true, R.id.menu_account_profile_activity_sync);

                Intent intent = new Intent(AccountProfileActivity.this, AccountSyncService.class);
                startService(intent);
                break;
            }
            case R.id.menu_account_profile_activity_settings: {
                Intent intent = new Intent(AccountProfileActivity.this, AccountSyncPreferencesActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.menu_account_profile_activity_logout:
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(R.string.lbl_account_profile_logout_confirmation_title)
                             .setMessage(R.string.lbl_account_profile_logout_confirmation_message)
                             .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialogInterface, int i) {
                                     dialogInterface.dismiss();
                                     logout();
                                 }
                             })
                             .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialogInterface, int i) {
                                     dialogInterface.dismiss();
                                 }
                             })
                             .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                 @Override
                                 public void onCancel(DialogInterface dialogInterface) {
                                     dialogInterface.dismiss();
                                 }
                             })
                             .setCancelable(true);
                dialogBuilder.create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tracker.stopSession();
    }

    private void logout() {
        AsyncHelper.start(new LogoutTask());
        AlarmUtil.removeAllSyncAlarms(AccountProfileActivity.this);
        setResult(Constants.IntentResultCodes.RESULT_LOGOUT);
        finish();
    }

    private class LogoutTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            accountService.logout();
            return null;
        }
    }

    private class LoadProfileTask extends AsyncTask<Void, Void, User> {
        private String errorMsg = null;
        private boolean logout = false;

        @Override
        protected User doInBackground(Void... params) {
            try {
                return accountService.loadUserData();
            } catch (UserNotLoggedInException e) {
                errorMsg = AccountProfileActivity.this.getString(R.string.lbl_account_profile_error_user_not_logged_in);
                logout = true;
                return null;
            } catch (GeneralWebException e) {
                errorMsg = AccountProfileActivity.this.getString(R.string.error_general_web_exception);
                return null;
            } catch (NoNetworkConnectionException e) {
                errorMsg = AccountProfileActivity.this.getString(R.string.error_no_network_connection);
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            if (user == null) {
                Toast.makeText(AccountProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                if (logout) {
                    setResult(Constants.IntentResultCodes.RESULT_LOGOUT);
                    finish();
                } else {
                    updateUI(accountService.getLastSyncHistory());
                }
            } else {
                updateUI(user);
            }
        }
    }

    private void updateUI(User user) {
        profileContainer.setVisibility(View.VISIBLE);

        email.setText(user.getEmail());
        name.setText(user.getFirstName() + " " + user.getLastName());
        registeredSince.setText(DateUtils.DateTimeConverter.convertDateTimeToString(user.getRegisteredSince(), DateFormat.MEDIUM, TimeFormat.MEDIUM, AccountProfileActivity.this));

        String loggedInSinceText = DateUtils.DateTimeConverter.convertDateTimeToString(user.getLoggedInSince(), DateFormat.MEDIUM, TimeFormat.MEDIUM, AccountProfileActivity.this);
        String loggedInDurationText = DateUtils.TimeCalculator.calculateDuration(AccountProfileActivity.this, user.getLoggedInSince(), new Date(), PeriodType.dayTime());

        loggedInSince.setText(loggedInSinceText + " (" + loggedInDurationText + ")");
    }

    private void updateUI(SyncHistory syncHistory) {
        if (syncHistory == null) {
            syncHistoryContainer.setVisibility(View.GONE);
            return;
        }

        syncHistoryContainer.setVisibility(View.VISIBLE);

        String startTimeText = DateUtils.DateTimeConverter.convertDateTimeToString(syncHistory.getStarted(), DateFormat.MEDIUM, TimeFormat.MEDIUM, AccountProfileActivity.this);
        String durationText = DateUtils.TimeCalculator.calculateDuration(AccountProfileActivity.this, syncHistory.getStarted(), new Date(), PeriodType.dayTime());
        syncHistoryStartTime.setText(startTimeText + " (" + durationText + ")");

        if (syncHistory.getEnded() != null)
            syncHistoryEndTime.setText(DateUtils.DateTimeConverter.convertDateTimeToString(syncHistory.getEnded(), DateFormat.MEDIUM, TimeFormat.MEDIUM, AccountProfileActivity.this));

        syncHistoryReasonLabel.setVisibility(View.GONE);
        syncHistoryReason.setVisibility(View.GONE);

        switch (syncHistory.getStatus()) {
            case SUCCESSFUL:
                syncHistoryResolution.setText(R.string.lbl_account_sync_resolution_successful);
                break;
            case INTERRUPTED:
                syncHistoryResolution.setText(R.string.lbl_account_sync_resolution_interrupted);
                syncHistoryReasonLabel.setVisibility(View.VISIBLE);
                syncHistoryReason.setText(R.string.lbl_account_sync_history_reason_interruption);
                syncHistoryReason.setVisibility(View.VISIBLE);
                break;
            case FAILED:
                syncHistoryResolution.setText(R.string.lbl_account_sync_resolution_failed);
                if (StringUtils.isNotBlank(syncHistory.getFailureReason())) {
                    syncHistoryReasonLabel.setVisibility(View.VISIBLE);
                    syncHistoryReason.setText(syncHistory.getFailureReason());
                    syncHistoryReason.setVisibility(View.VISIBLE);
                }
                break;
            case TIMED_OUT:
                syncHistoryResolution.setText(R.string.lbl_account_sync_resolution_timed_out);
                break;
            case BUSY:
                syncHistoryResolution.setText(R.string.lbl_account_sync_resolution_busy);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getActionBarHelper().setRefreshActionItemState(false, R.id.menu_account_profile_activity_sync);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!accountService.isSyncBusy()) {
            AsyncHelper.start(new LoadProfileTask());
        }

        SyncHistory syncHistory = accountService.getLastSyncHistory();
        updateUI(syncHistory);
    }
}