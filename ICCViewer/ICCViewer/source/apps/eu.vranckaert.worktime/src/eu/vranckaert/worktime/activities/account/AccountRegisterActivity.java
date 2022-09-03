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
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.activities.preferences.AccountSyncPreferencesActivity;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.TrackerConstants;
import eu.vranckaert.worktime.exceptions.network.NoNetworkConnectionException;
import eu.vranckaert.worktime.exceptions.worktime.account.PasswordLengthValidationException;
import eu.vranckaert.worktime.exceptions.worktime.account.RegisterEmailAlreadyInUseException;
import eu.vranckaert.worktime.exceptions.worktime.account.RegisterFieldRequiredException;
import eu.vranckaert.worktime.service.AccountService;
import eu.vranckaert.worktime.utils.context.AsyncHelper;
import eu.vranckaert.worktime.utils.context.ContextUtils;
import eu.vranckaert.worktime.utils.context.IntentUtil;
import eu.vranckaert.worktime.utils.string.StringUtils;
import eu.vranckaert.worktime.utils.tracker.AnalyticsTracker;
import eu.vranckaert.worktime.utils.view.actionbar.ActionBarGuiceActivity;
import eu.vranckaert.worktime.web.json.exception.GeneralWebException;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.RegexValidator;
import roboguice.inject.InjectView;

/**
 * User: Dirk Vranckaert
 * Date: 12/12/12
 * Time: 10:04
 */
public class AccountRegisterActivity extends ActionBarGuiceActivity {
    private AnalyticsTracker tracker;

    @Inject private AccountService accountService;

    @InjectView(R.id.account_register_email) private EditText email;
    @InjectView(R.id.account_register_firstname) private EditText firstName;
    @InjectView(R.id.account_register_lastname) private EditText lastName;
    @InjectView(R.id.account_register_password) private EditText password;
    @InjectView(R.id.account_register_password_confirmation) private EditText passwordConfirmation;
    @InjectView(R.id.account_register_password_show) private CheckBox showPassword;
    @InjectView(R.id.account_register_button) private Button registerButton;
    @InjectView(R.id.account_register_error) private TextView registrationError;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_account_register);

        setTitle(R.string.lbl_account_register_title);
        setDisplayHomeAsUpEnabled(true);

        tracker = AnalyticsTracker.getInstance(getApplicationContext());
        tracker.trackPageView(TrackerConstants.PageView.ACCOUNT_REGISTER_ACTIVITY);

        showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    TransformationMethod tm = new TransformationMethod() {
                        @Override
                        public CharSequence getTransformation(CharSequence source, View view) {
                            return source;
                        }

                        @Override
                        public void onFocusChanged(View view, CharSequence sourceText, boolean focused, int direction, Rect previouslyFocusedRect) {}
                    };

                    password.setTransformationMethod(tm);
                    passwordConfirmation.setTransformationMethod(tm);
                } else {
                    password.setTransformationMethod(new PasswordTransformationMethod());
                    passwordConfirmation.setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    AsyncHelper.startWithParams(
                            new RegisterTask(),
                            new String[]{
                                    email.getText().toString(),
                                    firstName.getText().toString(),
                                    lastName.getText().toString(),
                                    password.getText().toString()
                            }
                    );
                }
            }
        });
    }

    /**
     *
     * @return
     */
    private boolean validateInput() {
        registrationError.setVisibility(View.GONE);

        ContextUtils.hideKeyboard(AccountRegisterActivity.this, email);
        ContextUtils.hideKeyboard(AccountRegisterActivity.this, firstName);
        ContextUtils.hideKeyboard(AccountRegisterActivity.this, lastName);
        ContextUtils.hideKeyboard(AccountRegisterActivity.this, password);
        ContextUtils.hideKeyboard(AccountRegisterActivity.this, passwordConfirmation);

        String firstName = this.firstName.getText().toString().trim();
        String lastName = this.lastName.getText().toString().trim();
        String email = this.email.getText().toString();
        String password = this.password.getText().toString();
        String passwordConfirmation = this.passwordConfirmation.getText().toString();

        if (StringUtils.isBlank(firstName) || StringUtils.isBlank(lastName) || StringUtils.isBlank(email) ||
                StringUtils.isBlank(password) || StringUtils.isBlank(passwordConfirmation)) {
            registrationError.setText(R.string.lbl_account_register_error_all_fields_required);
            registrationError.setVisibility(View.VISIBLE);
            return false;
        }
        if (!EmailValidator.getInstance().isValid(email)) {
            registrationError.setText(R.string.lbl_account_register_error_invalid_email);
            registrationError.setVisibility(View.VISIBLE);
            return false;
        }
        RegexValidator rv = new RegexValidator("[A-Za-z0-9_\\-]*");
        if (!rv.isValid(password)) {
            registrationError.setText(R.string.lbl_account_register_error_password_characters);
            registrationError.setVisibility(View.VISIBLE);
            return false;
        }
        if (password.length() < 6 || password.length() > 30) {
            registrationError.setText(R.string.lbl_account_register_error_password_invalid_length);
            registrationError.setVisibility(View.VISIBLE);
            return false;
        }
        if (!password.equals(passwordConfirmation)) {
            registrationError.setText(R.string.lbl_account_register_error_password_confirmation);
            registrationError.setVisibility(View.VISIBLE);
            return false;
        }

        return true;
    }

    private class RegisterTask extends AsyncTask<String, Void, Void> {
        String error = "";
        View focusableView = null;

        @Override
        protected void onPreExecute() {
            registrationError.setVisibility(View.GONE);

            registerButton.setEnabled(false);
            email.setEnabled(false);
            firstName.setEnabled(false);
            lastName.setEnabled(false);
            password.setEnabled(false);
            passwordConfirmation.setEnabled(false);

            getActionBarHelper().setLoadingIndicator(true);
        }

        @Override
        protected Void doInBackground(String... params) {
            String email = params[0];
            String firstName = params[1];
            String lastName = params[2];
            String password = params[3];

            error = null;

            try {
                accountService.register(email, firstName, lastName, password);
            } catch (GeneralWebException e) {
                error = AccountRegisterActivity.this.getString(R.string.error_general_web_exception);
            } catch (NoNetworkConnectionException e) {
                error = AccountRegisterActivity.this.getString(R.string.error_no_network_connection);
            } catch (RegisterEmailAlreadyInUseException e) {
                error = AccountRegisterActivity.this.getString(R.string.lbl_account_register_error_email_account_exists);
                focusableView = AccountRegisterActivity.this.email;
            } catch (PasswordLengthValidationException e) {
                error = AccountRegisterActivity.this.getString(R.string.lbl_account_register_error_password_invalid_length);
                focusableView = AccountRegisterActivity.this.password;
            } catch (RegisterFieldRequiredException e) {
                error = AccountRegisterActivity.this.getString(R.string.lbl_account_register_error_all_fields_required);
                focusableView = AccountRegisterActivity.this.email;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void o) {
            getActionBarHelper().setLoadingIndicator(false);

            registerButton.setEnabled(true);
            email.setEnabled(true);
            firstName.setEnabled(true);
            lastName.setEnabled(true);
            password.setEnabled(true);
            passwordConfirmation.setEnabled(true);

            if (focusableView != null) {
                focusableView.requestFocus();
            }

            if (error != null) {
                registrationError.setText(error);
                registrationError.setVisibility(View.VISIBLE);
            } else {
                AccountSyncPreferencesActivity.scheduleAlarm(AccountRegisterActivity.this, accountService);
                //AlarmUtil.setAlarmSyncCycle(AccountRegisterActivity.this, null, Preferences.Account.syncInterval(AccountRegisterActivity.this));
                Intent intent = new Intent(AccountRegisterActivity.this, AccountProfileActivity.class);
                startActivityForResult(intent, Constants.IntentRequestCodes.ACCOUNT_DETAILS);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Constants.IntentResultCodes.RESULT_LOGOUT) {
            setResult(resultCode);
        } else {
            setResult(RESULT_OK);
        }
        finish();
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
}