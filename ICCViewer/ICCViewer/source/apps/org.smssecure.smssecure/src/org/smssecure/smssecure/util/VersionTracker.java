package org.smssecure.smssecure.util;

import android.content.Context;
import android.content.pm.PackageManager;

import java.io.IOException;

public class VersionTracker {


  public static int getLastSeenVersion(Context context) {
    return SilencePreferences.getLastVersionCode(context);
  }

  public static void updateLastSeenVersion(Context context) {
    try {
      int currentVersionCode = Util.getCurrentApkReleaseVersion(context);
      SilencePreferences.setLastVersionCode(context, currentVersionCode);
    } catch (IOException ioe) {
      throw new AssertionError(ioe);
    }
  }
}
