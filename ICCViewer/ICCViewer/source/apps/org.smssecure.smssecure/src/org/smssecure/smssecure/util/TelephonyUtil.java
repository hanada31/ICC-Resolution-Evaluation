package org.smssecure.smssecure.util;

import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import org.smssecure.smssecure.util.ServiceUtil;

public class TelephonyUtil {
  private static final String TAG = TelephonyUtil.class.getSimpleName();

  public static TelephonyManager getManager(final Context context) {
    return (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
  }

  public static String getMccMnc(final Context context) {
    final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    final int configMcc = context.getResources().getConfiguration().mcc;
    final int configMnc = context.getResources().getConfiguration().mnc;
    if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
      Log.w(TAG, "Choosing MCC+MNC info from TelephonyManager.getSimOperator()");
      return tm.getSimOperator();
    } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {
      Log.w(TAG, "Choosing MCC+MNC info from TelephonyManager.getNetworkOperator()");
      return tm.getNetworkOperator();
    } else if (configMcc != 0 && configMnc != 0) {
      Log.w(TAG, "Choosing MCC+MNC info from current context's Configuration");
      return String.format("%03d%d",
          configMcc,
          configMnc == Configuration.MNC_ZERO ? 0 : configMnc);
    } else {
      return null;
    }
  }

  public static String getApn(final Context context) {
    final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    return cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS).getExtraInfo();
  }

  public static boolean isMyPhoneNumber(final Context context, String number){
    return number != null && PhoneNumberUtils.compare(context, getPhoneNumber(context), number);
  }

  public static String getPhoneNumber(final Context context){
    final TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
    return tm.getLine1Number();
  }

  public static NetworkInfo getNetworkInfo(final Context context) {
    return ServiceUtil.getConnectivityManager(context).getActiveNetworkInfo();
  }

  public static boolean isConnectedRoaming(final Context context) {
    NetworkInfo info = getNetworkInfo(context);
    return info != null && info.isConnected() && info.isRoaming() && info.getType() == ConnectivityManager.TYPE_MOBILE;
  }
}
