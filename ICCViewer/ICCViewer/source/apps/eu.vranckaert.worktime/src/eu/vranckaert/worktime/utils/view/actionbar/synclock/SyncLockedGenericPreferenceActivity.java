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

package eu.vranckaert.worktime.utils.view.actionbar.synclock;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.inject.Inject;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.service.AccountService;
import eu.vranckaert.worktime.utils.activity.GenericPreferencesActivity;
import eu.vranckaert.worktime.utils.context.AsyncHelper;

/**
 * User: Dirk Vranckaert
 * Date: 16/01/13
 * Time: 15:19
 */
public abstract class SyncLockedGenericPreferenceActivity extends GenericPreferencesActivity {
    private AsyncTask syncCheck;
    private boolean isLocking = false;

    @Inject
    private AccountService accountService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private class SyncCheck extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            while (!isCancelled()) {
                if (accountService.isSyncBusy()) {
                    publishProgress();
                }

                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {}
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if (!isLocking) {
                isLocking = true;
                startActivityForResult(new Intent(SyncLockedGenericPreferenceActivity.this, SyncLockingActivity.class), Constants.IntentRequestCodes.SYNC_BLOCKING_ACTIVITY);
                this.cancel(true);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (accountService.isUserLoggedIn()) {
            syncCheck = new SyncCheck();
            AsyncHelper.start(syncCheck);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (syncCheck != null) {
            syncCheck.cancel(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.IntentRequestCodes.SYNC_BLOCKING_ACTIVITY) {
            isLocking = false;
        }
    }
}
