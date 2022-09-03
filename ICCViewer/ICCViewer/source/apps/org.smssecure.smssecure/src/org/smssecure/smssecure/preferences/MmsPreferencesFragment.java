/**
 * Copyright (C) 2014 Open Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.smssecure.smssecure.preferences;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.preference.PreferenceFragment;
import android.util.Log;

import org.smssecure.smssecure.PassphraseRequiredActionBarActivity;
import org.smssecure.smssecure.R;
import org.smssecure.smssecure.components.CustomDefaultPreference;
import org.smssecure.smssecure.database.ApnDatabase;
import org.smssecure.smssecure.mms.LegacyMmsConnection;
import org.smssecure.smssecure.util.TelephonyUtil;
import org.smssecure.smssecure.util.SilencePreferences;

import java.io.IOException;


public class MmsPreferencesFragment extends PreferenceFragment {

  private static final String TAG = MmsPreferencesFragment.class.getSimpleName();

  @Override
  public void onCreate(Bundle paramBundle) {
    super.onCreate(paramBundle);
    addPreferencesFromResource(R.xml.preferences_manual_mms);

    ((PassphraseRequiredActionBarActivity) getActivity()).getSupportActionBar()
        .setTitle(R.string.preferences__advanced_mms_access_point_names);

    SilencePreferences.setManualMmsSettingsAsSeen(getActivity());
  }

  @Override
  public void onResume() {
    super.onResume();
    new LoadApnDefaultsTask().execute();
  }

  private class LoadApnDefaultsTask extends AsyncTask<Void, Void, LegacyMmsConnection.Apn> {

    @Override
    protected LegacyMmsConnection.Apn doInBackground(Void... params) {
      try {
        Context context = getActivity();

        if (context != null) {
          return ApnDatabase.getInstance(context)
                            .getDefaultApnParameters(TelephonyUtil.getMccMnc(context),
                                                     TelephonyUtil.getApn(context));
        }
      } catch (IOException e) {
        Log.w(TAG, e);
      }

      return null;
    }

    @Override
    protected void onPostExecute(LegacyMmsConnection.Apn apnDefaults) {
      ((CustomDefaultPreference)findPreference(SilencePreferences.MMSC_HOST_PREF))
          .setValidator(new CustomDefaultPreference.UriValidator())
          .setDefaultValue(apnDefaults.getMmsc());

      ((CustomDefaultPreference)findPreference(SilencePreferences.MMSC_PROXY_HOST_PREF))
          .setValidator(new CustomDefaultPreference.HostnameValidator())
          .setDefaultValue(apnDefaults.getProxy());

      ((CustomDefaultPreference)findPreference(SilencePreferences.MMSC_PROXY_PORT_PREF))
          .setValidator(new CustomDefaultPreference.PortValidator())
          .setDefaultValue(apnDefaults.getPort());

      ((CustomDefaultPreference)findPreference(SilencePreferences.MMSC_USERNAME_PREF))
          .setDefaultValue(apnDefaults.getPort());

      ((CustomDefaultPreference)findPreference(SilencePreferences.MMSC_PASSWORD_PREF))
          .setDefaultValue(apnDefaults.getPassword());

      ((CustomDefaultPreference)findPreference(SilencePreferences.MMS_USER_AGENT))
          .setDefaultValue(LegacyMmsConnection.USER_AGENT);
    }
  }

}
