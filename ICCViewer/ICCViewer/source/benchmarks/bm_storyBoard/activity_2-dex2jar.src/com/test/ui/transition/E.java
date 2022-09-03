package com.test.ui.transition;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class E extends AppCompatActivity
{
  private void goToActivity(Context paramContext, Class paramClass)
  {
    startActivity(new Intent(paramContext, paramClass));
  }

  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(2131361823);
    setSupportActionBar((Toolbar)findViewById(2131230892));
    goToActivity(getApplicationContext(), MainActivity.class);
  }
}

/* Location:           D:\SoftwareData\AcademicTool\安卓APK分析\jd-gui-0.3.5.windows\jars\activity_2-dex2jar.jar
 * Qualified Name:     com.test.ui.transition.E
 * JD-Core Version:    0.6.2
 */