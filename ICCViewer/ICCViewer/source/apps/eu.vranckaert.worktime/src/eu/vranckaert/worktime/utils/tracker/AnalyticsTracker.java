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

package eu.vranckaert.worktime.utils.tracker;

import android.content.Context;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.apps.analytics.Item;
import com.google.android.apps.analytics.Transaction;
import eu.vranckaert.worktime.utils.context.ContextUtils;

/**
 * User: DIRK VRANCKAERT
 * Date: 17/08/11
 * Time: 17:06
 */
public class AnalyticsTracker {
    private static final String ACCOUNT_UA = "UA-3183255-5";
    private static final int DISPATCH_INTERVAL_SEC = 60;

    private Context ctx;

    private GoogleAnalyticsTracker gat;

    private AnalyticsTracker() {}

    public static AnalyticsTracker getInstance(Context ctx) {
        AnalyticsTracker tracker = new AnalyticsTracker();
        tracker.ctx = ctx;

        if (!ContextUtils.isStableBuild(ctx)) {
            return tracker;
        }

        tracker.gat = GoogleAnalyticsTracker.getInstance();
        tracker.gat.startNewSession(ACCOUNT_UA, DISPATCH_INTERVAL_SEC, ctx);

        return tracker;
    }

    public void addTransaction(Transaction transaction) {
        if (!ContextUtils.isStableBuild(ctx)) {
            return;
        }
        gat.addTransaction(transaction);
    }

    public void addItem(Item item) {
        if (!ContextUtils.isStableBuild(ctx)) {
            return;
        }
        gat.addItem(item);
    }

    public void trackTransactions() {
        if (!ContextUtils.isStableBuild(ctx)) {
            return;
        }
        gat.trackTransactions();
    }

    public void clearTransactions() {
        if (!ContextUtils.isStableBuild(ctx)) {
            return;
        }
        gat.clearTransactions();
    }

    public void trackEvent(String source, String action) {
        if (!ContextUtils.isStableBuild(ctx)) {
            return;
        }
        gat.trackEvent(source, action, "", -1);
    }

    public void trackPageView(String pageView) {
        if (!ContextUtils.isStableBuild(ctx)) {
            return;
        }
        gat.trackPageView(pageView);
    }

    public void stopSession() {
        if (!ContextUtils.isStableBuild(ctx)) {
            return;
        }
        gat.stopSession();
    }
}
