package com.test.ui.transition;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public class MainActivity extends FragmentActivity {
  private void showFragment() {
    BlankFragment blankFragment = new BlankFragment();
    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager.beginTransaction().replace(2131230796, blankFragment).commit();
    fragmentManager.beginTransaction().show(blankFragment);
  }
  
  protected void onCreate(Bundle paramBundle) {
    super.onCreate(paramBundle);
    setContentView(2131361819);
    showFragment();
  }
}


/* Location:              D:\SoftwareData\AcademicTool\安卓APK分析\jd-gui-0.3.5.windows\jars\fragment_2-dex2jar.jar!\com\tes\\ui\transition\MainActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */