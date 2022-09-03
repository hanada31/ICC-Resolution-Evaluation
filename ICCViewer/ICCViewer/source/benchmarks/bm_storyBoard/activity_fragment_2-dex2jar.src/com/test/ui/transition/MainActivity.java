package com.test.ui.transition;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class MainActivity extends FragmentActivity
{
  private void setUpFragment()
  {
    FragmentManager localFragmentManager = getSupportFragmentManager();
    BlankFragment localBlankFragment = new BlankFragment();
    localFragmentManager.beginTransaction().replace(2131230796, localBlankFragment).commit();
    localFragmentManager.beginTransaction().show(localBlankFragment);
  }

  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(2131361819);
    setUpFragment();
  }
}

/* Location:           D:\SoftwareData\AcademicTool\安卓APK分析\jd-gui-0.3.5.windows\jars\activity_fragment_2-dex2jar.jar
 * Qualified Name:     com.test.ui.transition.MainActivity
 * JD-Core Version:    0.6.2
 */