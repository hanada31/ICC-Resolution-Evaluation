package com.test.ui.transition;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
  protected void onCreate(Bundle paramBundle) {
    super.onCreate(paramBundle);
    setContentView(2131361819);
    ((TextView)findViewById(2131230764)).setOnClickListener(new View.OnClickListener() {
          public void onClick(View param1View) {
            MainActivity.this.startActivity(new Intent(MainActivity.this.getApplicationContext(), NextActivity.class));
          }
        });
  }
}


/* Location:              D:\SoftwareData\AcademicTool\安卓APK分析\jd-gui-0.3.5.windows\jars\activity_innerclass_1-dex2jar.jar!\com\tes\\ui\transition\MainActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */