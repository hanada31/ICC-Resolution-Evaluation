package com.test.ui.transition;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class MainActivity extends FragmentActivity
{
  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(2131361820);
    paramBundle = getSupportFragmentManager();
    BlankFragment localBlankFragment = new BlankFragment();
    paramBundle.beginTransaction().replace(2131230796, localBlankFragment).commit();
    paramBundle.beginTransaction().show(localBlankFragment);
  }
}

/* Location:           D:\SoftwareData\AcademicTool\安卓APK分析\jd-gui-0.3.5.windows\jars\activity_fragment_1-dex2jar.jar
 * Qualified Name:     com.test.ui.transition.MainActivity
 * JD-Core Version:    0.6.2
 */