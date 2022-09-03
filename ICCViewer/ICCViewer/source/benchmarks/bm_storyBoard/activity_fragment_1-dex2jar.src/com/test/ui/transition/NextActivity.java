package com.test.ui.transition;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;

public class NextActivity extends AppCompatActivity
{
  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(2131361821);
    setSupportActionBar((Toolbar)findViewById(2131230893));
    ((FloatingActionButton)findViewById(2131230790)).setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        NextActivity.this.startActivity(new Intent(NextActivity.this.getApplicationContext(), AActivity.class));
      }
    });
  }
}

/* Location:           D:\SoftwareData\AcademicTool\安卓APK分析\jd-gui-0.3.5.windows\jars\activity_fragment_1-dex2jar.jar
 * Qualified Name:     com.test.ui.transition.NextActivity
 * JD-Core Version:    0.6.2
 */