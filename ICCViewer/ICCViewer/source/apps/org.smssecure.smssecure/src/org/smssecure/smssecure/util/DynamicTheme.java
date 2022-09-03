package org.smssecure.smssecure.util;

import android.app.Activity;
import android.content.Intent;

import org.smssecure.smssecure.R;

public class DynamicTheme {

  public static final String DARK  = "dark";
  public static final String LIGHT = "light";

  private int currentTheme;

  public void onCreate(Activity activity) {
    currentTheme = getSelectedTheme(activity);
    activity.setTheme(currentTheme);
  }

  public void onResume(Activity activity) {
    if (currentTheme != getSelectedTheme(activity)) {
      Intent intent = activity.getIntent();
      activity.finish();
      OverridePendingTransition.invoke(activity);
      activity.startActivity(intent);
      OverridePendingTransition.invoke(activity);
    }
  }

  protected int getSelectedTheme(Activity activity) {
    String theme = SilencePreferences.getTheme(activity);

    if (theme.equals(DARK)) return R.style.Silence_DarkTheme;

    return R.style.Silence_LightTheme;
  }

  private static final class OverridePendingTransition {
    static void invoke(Activity activity) {
      activity.overridePendingTransition(0, 0);
    }
  }
}
