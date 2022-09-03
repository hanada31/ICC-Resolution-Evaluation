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

package eu.vranckaert.worktime.utils.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * User: Dirk Vranckaert
 * Date: 12/12/12
 * Time: 13:25
 */
public class NetworkUtil {
    private static final String LOG_TAG = NetworkUtil.class.getSimpleName();

    /**
     * Checks if the device is connected with internet or not.
     * @param ctx The app-context.
     * @return {@link Boolean#TRUE} if the device is connected or connecting, {@link Boolean#FALSE} if no connection is
     * available.
     */
    public static boolean isOnline(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            Log.d(LOG_TAG, "Device is online");
            return true;
        }
        Log.d(LOG_TAG, "Device is not online");
        return false;
    }

    /**
     * Checks if the device can access a website (makes sure that proxy settings are ok).
     * @param endpoint The endpoint to try to reach.
     * @return {@link Boolean#TRUE} if the device can reach the endpoint, {@link Boolean#FALSE} if not.
     */
    public static boolean canReachEndpoint(String endpoint) {
        HttpGet requestForTest = new HttpGet(endpoint);
        try {
            HttpResponse response = new DefaultHttpClient().execute(requestForTest);
            int statusCode = response.getStatusLine().getStatusCode();
            Log.d(LOG_TAG, "Trying to surf with status code " + statusCode);
            if (statusCode == 200) {
                Log.d(LOG_TAG, "Device can surf");
                return true;
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Exception, cannot surf");
        }
        Log.d(LOG_TAG, "Device cannot surf");
        return false;
    }

    /**
     * Tests if the device is connected to the internet and if the device can reach a website.
     * @param ctx The app-context.
     * @param endpoint The endpoint to try to reach.
     * @return {@link Boolean#TRUE} if the device is connected and can reach the endpoint website, {@link Boolean#FALSE}
     * if not.
     */
    public static boolean canSurf(Context ctx, String endpoint) {
        return (isOnline(ctx) && canReachEndpoint(endpoint));
    }

    /**
     * Checks if the device is connected to a WiFi network or not.
     * @param ctx The app-context.
     * @return {@link Boolean#TRUE} if the device is connected to a WiFi network, {@link Boolean#FALSE} otherwise.
     */
    public static boolean isConnectedToWifi(Context ctx) {
        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            SupplicantState state = wifiManager.getConnectionInfo().getSupplicantState();
            if (state != null) {
                NetworkInfo.DetailedState detailedState = WifiInfo.getDetailedStateOf(state);
                if (detailedState != null) {
                    if (detailedState == NetworkInfo.DetailedState.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
