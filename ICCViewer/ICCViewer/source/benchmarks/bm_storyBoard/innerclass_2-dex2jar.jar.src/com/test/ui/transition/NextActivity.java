package com.test.ui.transition;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class NextActivity extends AppCompatActivity {
  protected void onCreate(Bundle paramBundle) {
    super.onCreate(paramBundle);
    setContentView(2131361820);
    setSupportActionBar((Toolbar)findViewById(2131230893));
    ((FloatingActionButton)findViewById(2131230791)).setOnClickListener(new View.OnClickListener() {
          public void onClick(View param1View) {
            Snackbar.make(param1View, "Replace with your own action", 0).setAction("Action", null).show();
          }
        });
  }
}


/* Location:              D:\SoftwareData\AcademicTool\安卓APK分析\jd-gui-0.3.5.windows\jars\innerclass_2-dex2jar.jar!\com\tes\\ui\transition\NextActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */