package com.test.ui.transition;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class AActivity extends AppCompatActivity
{
  private void setUpFragment()
  {
    FragmentManager localFragmentManager = getSupportFragmentManager();
    BFragment localBFragment = new BFragment();
    localFragmentManager.beginTransaction().replace(2131230796, localBFragment).commit();
    localFragmentManager.beginTransaction().show(localBFragment);
  }

  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(2131361820);
    setSupportActionBar((Toolbar)findViewById(2131230893));
    setUpFragment();
  }
}

/* Location:           D:\SoftwareData\AcademicTool\安卓APK分析\jd-gui-0.3.5.windows\jars\activity_fragment_2-dex2jar.jar
 * Qualified Name:     com.test.ui.transition.AActivity
 * JD-Core Version:    0.6.2
 */