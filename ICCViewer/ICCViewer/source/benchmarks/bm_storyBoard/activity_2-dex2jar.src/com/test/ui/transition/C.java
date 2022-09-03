package com.test.ui.transition;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class C extends AppCompatActivity
{
  private void goToActivity(Context paramContext, Class paramClass)
  {
    startActivity(new Intent(paramContext, paramClass));
  }

  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(2131361821);
    setSupportActionBar((Toolbar)findViewById(2131230892));
    getApplicationContext();
    double d = Math.random();
    if (d > 0.5D)
    {
      goToActivity(this, A.class);
      return;
    }
    if (d > 0.3D)
    {
      goToActivity(this, MainActivity.class);
      return;
    }
    if (d > 0.1D)
    {
      goToActivity(this, NextActivity.class);
      return;
    }
    goToActivity(this, D.class);
  }
}

/* Location:           D:\SoftwareData\AcademicTool\安卓APK分析\jd-gui-0.3.5.windows\jars\activity_2-dex2jar.jar
 * Qualified Name:     com.test.ui.transition.C
 * JD-Core Version:    0.6.2
 */