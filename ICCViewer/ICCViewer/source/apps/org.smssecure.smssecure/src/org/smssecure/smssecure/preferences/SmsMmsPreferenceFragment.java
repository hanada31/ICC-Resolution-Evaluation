package org.smssecure.smssecure.preferences;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.preference.PreferenceFragment;
import android.text.TextUtils;

import org.smssecure.smssecure.ApplicationPreferencesActivity;
import org.smssecure.smssecure.R;
import org.smssecure.smssecure.components.OutgoingSmsPreference;
import org.smssecure.smssecure.util.SilencePreferences;
import org.smssecure.smssecure.util.Util;

import java.util.LinkedList;
import java.util.List;

public class SmsMmsPreferenceFragment extends PreferenceFragment {
  private static final String KITKAT_DEFAULT_PREF = "pref_set_default";
  private static final String MMS_PREF            = "pref_mms_preferences";

  @Override
  public void onCreate(Bundle paramBundle) {
    super.onCreate(paramBundle);
    addPreferencesFromResource(R.xml.preferences_sms_mms);

    this.findPreference(MMS_PREF)
      .setOnPreferenceClickListener(new ApnPreferencesClickListener());
  }

  @Override
  public void onResume() {
    super.onResume();
    ((ApplicationPreferencesActivity) getActivity()).getSupportActionBar().setTitle(R.string.preferences__sms_mms);

    initializePlatformSpecificOptions();
  }

  private void initializePlatformSpecificOptions() {
    PreferenceScreen preferenceScreen    = getPreferenceScreen();
    Preference       defaultPreference   = findPreference(KITKAT_DEFAULT_PREF);
    Preference       allSmsPreference    = findPreference(SilencePreferences.ALL_SMS_PREF);
    Preference       allMmsPreference    = findPreference(SilencePreferences.ALL_MMS_PREF);
    Preference       manualMmsPreference = findPreference(MMS_PREF);

    if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
      if (allSmsPreference != null) preferenceScreen.removePreference(allSmsPreference);
      if (allMmsPreference != null) preferenceScreen.removePreference(allMmsPreference);

      if (Util.isDefaultSmsProvider(getActivity())) {
        defaultPreference.setIntent(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        defaultPreference.setTitle(getString(R.string.ApplicationPreferencesActivity_sms_enabled));
        defaultPreference.setSummary(getString(R.string.ApplicationPreferencesActivity_tap_to_change_your_default_sms_app));
      } else {
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getActivity().getPackageName());
        defaultPreference.setIntent(intent);
        defaultPreference.setTitle(getString(R.string.ApplicationPreferencesActivity_sms_disabled));
        defaultPreference.setSummary(getString(R.string.ApplicationPreferencesActivity_tap_to_make_silence_your_default_sms_app));
      }
    } else if (defaultPreference != null) {
      preferenceScreen.removePreference(defaultPreference);
    }

    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && manualMmsPreference != null) {
      preferenceScreen.removePreference(manualMmsPreference);
    }
  }

  private class ApnPreferencesClickListener implements Preference.OnPreferenceClickListener {

    @Override
    public boolean onPreferenceClick(Preference preference) {
      Fragment            fragment            = new MmsPreferencesFragment();
      FragmentManager     fragmentManager     = getActivity().getSupportFragmentManager();
      FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
      fragmentTransaction.replace(android.R.id.content, fragment);
      fragmentTransaction.addToBackStack(null);
      fragmentTransaction.commit();

      return true;
    }
  }

  public static CharSequence getSummary(Context context) {
    return getIncomingSmsSummary(context);
  }

  private static CharSequence getIncomingSmsSummary(Context context) {
    final int onResId          = R.string.ApplicationPreferencesActivity_on;
    final int offResId         = R.string.ApplicationPreferencesActivity_off;
    final int smsResId         = R.string.ApplicationPreferencesActivity_sms;
    final int mmsResId         = R.string.ApplicationPreferencesActivity_mms;
    final int incomingSmsResId = R.string.ApplicationPreferencesActivity_incoming_summary;

    final int incomingSmsSummary;
    boolean postKitkatSMS = Util.isDefaultSmsProvider(context);
    boolean preKitkatSMS  = SilencePreferences.isInterceptAllSmsEnabled(context);
    boolean preKitkatMMS  = SilencePreferences.isInterceptAllMmsEnabled(context);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      if (postKitkatSMS)                      incomingSmsSummary = onResId;
      else                                    incomingSmsSummary = offResId;
    } else {
      if      (preKitkatSMS && preKitkatMMS)  incomingSmsSummary = onResId;
      else if (preKitkatSMS && !preKitkatMMS) incomingSmsSummary = smsResId;
      else if (!preKitkatSMS && preKitkatMMS) incomingSmsSummary = mmsResId;
      else                                    incomingSmsSummary = offResId;
    }
    return context.getString(incomingSmsResId, context.getString(incomingSmsSummary));
  }
}
