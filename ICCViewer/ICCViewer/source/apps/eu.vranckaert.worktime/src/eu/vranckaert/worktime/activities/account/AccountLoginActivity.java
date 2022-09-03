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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.activities.preferences.AccountSyncPreferencesActivity;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.TrackerConstants;
import eu.vranckaert.worktime.exceptions.network.NoNetworkConnectionException;
import eu.vranckaert.worktime.exceptions.worktime.account.LoginCredentialsMismatchException;
import eu.vranckaert.worktime.service.AccountService;
import eu.vranckaert.worktime.utils.context.AsyncHelper;
import eu.vranckaert.worktime.utils.context.ContextUtils;
import eu.vranckaert.worktime.utils.context.IntentUtil;
import eu.vranckaert.worktime.utils.string.StringUtils;
import eu.vranckaert.worktime.utils.tracker.AnalyticsTracker;
import eu.vranckaert.worktime.utils.view.actionbar.ActionBarGuiceActivity;
import eu.vranckaert.worktime.web.json.exception.GeneralWebException;
import org.apache.commons.validator.routines.EmailValidator;
import roboguice.inject.InjectView;

/**
 * User: Dirk Vranckaert
 * Date: 12/12/12
 * Time: 10:04
 */
public class AccountLoginActivity extends ActionBarGuiceActivity {
    private AnalyticsTracker tracker;

    @Inject private AccountService accountService;

    @InjectView(R.id.account_login_button) private Button loginButton;
    @InjectView(R.id.account_login_register_button) private Button registerButton;
    @InjectView(R.id.account_login_email) private EditText emailInput;
    @InjectView(R.id.account_login_password) private EditText passwordInput;
    @InjectView(R.id.account_login_error) private TextView errorTextView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (accountService.isUserLoggedIn()){
            Intent intent = new Intent(AccountLoginActivity.this, AccountProfileActivity.class);
            startActivityForResult(intent, Constants.IntentRequestCodes.ACCOUNT_DETAILS);
        }

        setContentView(R.layout.activity_account_login);

        setTitle(R.string.lbl_account_login_title);
        setDisplayHomeAsUpEnabled(true);

        tracker = AnalyticsTracker.getInstance(getApplicationContext());
        tracker.trackPageView(TrackerConstants.PageView.ACCOUNT_LOGIN_ACTIVITY);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();

                errorTextView.setVisibility(View.GONE);
                if (StringUtils.isBlank(email) || StringUtils.isBlank(password)) {
                    errorTextView.setText(R.string.lbl_account_login_error_all_required);
                    errorTextView.setVisibility(View.VISIBLE);
                    return;
                } else if (!EmailValidator.getInstance().isValid(email)) {
                    errorTextView.setText(R.string.lbl_account_login_error_invalid_email);
                    errorTextView.setVisibility(View.VISIBLE);
                    return;
                }

                AsyncHelper.startWithParams(new LoginTask(), new String[]{email, password});
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountLoginActivity.this, AccountRegisterActivity.class);
                startActivityForResult(intent, Constants.IntentRequestCodes.ACCOUNT_REGISTER);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.IntentRequestCodes.ACCOUNT_REGISTER && resultCode == RESULT_CANCELED) {
            // DO NOTHING
        } else if (requestCode == Constants.IntentRequestCodes.ACCOUNT_REGISTER && resultCode == Constants.IntentResultCodes.RESULT_LOGOUT) {
            // DO NOTHING
        } else if (requestCode == Constants.IntentRequestCodes.ACCOUNT_REGISTER) {
            finish();
        } else if (requestCode == Constants.IntentRequestCodes.ACCOUNT_DETAILS && resultCode == Constants.IntentResultCodes.RESULT_LOGOUT) {
            // DO NOTHING
        } else if (requestCode == Constants.IntentRequestCodes.ACCOUNT_DETAILS) {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.ab_activity_acount_login, menu);

        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                IntentUtil.goBack(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tracker.stopSession();
    }

    private class LoginTask extends AsyncTask<String, Void, Void> {
        String error = "";

        @Override
        protected void onPreExecute() {
            errorTextView.setVisibility(View.GONE);

            loginButton.setEnabled(false);
            registerButton.setEnabled(false);
            emailInput.setEnabled(false);
            passwordInput.setEnabled(false);

            ContextUtils.hideKeyboard(AccountLoginActivity.this, emailInput);
            ContextUtils.hideKeyboard(AccountLoginActivity.this, passwordInput);

            getActionBarHelper().setLoadingIndicator(true);
        }

        @Override
        protected Void doInBackground(String... params) {
            String email = params[0];
            String password = params[1];

            error = null;

            try {
                accountService.login(email, password);
            } catch (GeneralWebException e) {
                error = AccountLoginActivity.this.getString(R.string.error_general_web_exception);
            } catch (NoNetworkConnectionException e) {
                error = AccountLoginActivity.this.getString(R.string.error_no_network_connection);
            } catch (LoginCredentialsMismatchException e) {
                error = AccountLoginActivity.this.getString(R.string.lbl_account_login_error_credential_mismatch);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void o) {
            getActionBarHelper().setLoadingIndicator(false);

            if (error != null) {
                errorTextView.setText(error);
                errorTextView.setVisibility(View.VISIBLE);
            } else {
                AccountSyncPreferencesActivity.scheduleAlarm(AccountLoginActivity.this, accountService);
                //AlarmUtil.setAlarmSyncCycle(AccountLoginActivity.this, null, Preferences.Account.syncInterval(AccountLoginActivity.this));
                Intent intent = new Intent(AccountLoginActivity.this, AccountProfileActivity.class);
                startActivityForResult(intent, Constants.IntentRequestCodes.ACCOUNT_DETAILS);
            }

            loginButton.setEnabled(true);
            registerButton.setEnabled(true);
            emailInput.setEnabled(true);
            passwordInput.setEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        errorTextView.setVisibility(View.GONE);
        emailInput.requestFocus();
        emailInput.setText("");
        passwordInput.setText("");
        super.onResume();
    }
}