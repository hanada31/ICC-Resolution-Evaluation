package org.smssecure.smssecure.components.reminder;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import org.smssecure.smssecure.R;
import org.smssecure.smssecure.util.SilencePreferences;

import java.util.concurrent.TimeUnit;

public class StoreRatingReminder extends Reminder {

  private static final String TAG = StoreRatingReminder.class.getSimpleName();

  private static final int DAYS_SINCE_INSTALL_THRESHOLD = 7;

  public StoreRatingReminder(final Context context) {
    super(context.getString(R.string.reminder_header_rate_title),
          context.getString(R.string.reminder_header_rate_text),
          context.getString(R.string.reminder_header_rate_button));

    final OnClickListener okListener = new OnClickListener() {
      @Override
      public void onClick(View v) {
        SilencePreferences.setRatingEnabled(context, false);
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
      }
    };
    final OnClickListener dismissListener = new OnClickListener() {
      @Override
      public void onClick(View v) {
        SilencePreferences.setRatingEnabled(context, false);
      }
    };
    setOkListener(okListener);
    setDismissListener(dismissListener);
  }

  public static boolean isEligible(Context context) {

    if (!SilencePreferences.isRatingEnabled(context))
      return false;

    // App needs to be installed via Play/Amazon store to show the rating dialog
    String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());
    if (installer == null || !(installer.equals("com.android.vending") || installer.equals("com.amazon.venezia"))){
      SilencePreferences.setRatingEnabled(context, false);
      return false;
    }

    long daysSinceInstall = getDaysSinceInstalled(context);
    long laterTimestamp   = SilencePreferences.getRatingLaterTimestamp(context);

    return daysSinceInstall >= DAYS_SINCE_INSTALL_THRESHOLD &&
            System.currentTimeMillis() >= laterTimestamp;
  }

  private static long getDaysSinceInstalled(Context context) {
    try {
      long installTimestamp = context.getPackageManager()
                                     .getPackageInfo(context.getPackageName(), 0)
                                     .firstInstallTime;

      return TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - installTimestamp);
    } catch (PackageManager.NameNotFoundException e) {
      Log.w(TAG, e);
      return 0;
    }
  }
}
