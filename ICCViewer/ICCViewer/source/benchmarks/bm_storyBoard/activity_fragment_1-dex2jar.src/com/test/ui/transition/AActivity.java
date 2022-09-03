package com.test.ui.transition;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class AActivity extends AppCompatActivity
{
  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(2131361819);
    setSupportActionBar((Toolbar)findViewById(2131230893));
    paramBundle = getSupportFragmentManager();
    Bfragment localBfragment = new Bfragment();
    paramBundle.beginTransaction().replace(2131230796, localBfragment).commit();
    paramBundle.beginTransaction().show(localBfragment);
  }
}

/* Location:           D:\SoftwareData\AcademicTool\安卓APK分析\jd-gui-0.3.5.windows\jars\activity_fragment_1-dex2jar.jar
 * Qualified Name:     com.test.ui.transition.AActivity
 * JD-Core Version:    0.6.2
 */