package com.test.ui.transition;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class B extends AppCompatActivity
{
  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(2131361820);
    setSupportActionBar((Toolbar)findViewById(2131230892));
    double d = Math.random();
    if (d > 0.8D)
    {
      startActivity(new Intent(this, MainActivity.class));
      return;
    }
    if (d > 0.5D)
    {
      startActivity(new Intent(this, NextActivity.class));
      return;
    }
    startActivity(new Intent(this, C.class));
  }
}

/* Location:           D:\SoftwareData\AcademicTool\安卓APK分析\jd-gui-0.3.5.windows\jars\activity_1-dex2jar.jar
 * Qualified Name:     com.test.ui.transition.B
 * JD-Core Version:    0.6.2
 */