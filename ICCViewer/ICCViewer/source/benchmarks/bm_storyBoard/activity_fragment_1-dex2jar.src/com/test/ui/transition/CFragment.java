package com.test.ui.transition;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CFragment extends Fragment
{
  public void onAttach(Context paramContext)
  {
    super.onAttach(paramContext);
    startActivity(new Intent(paramContext, MainActivity.class));
  }

  public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle)
  {
    return paramLayoutInflater.inflate(2131361838, paramViewGroup, false);
  }
}

/* Location:           D:\SoftwareData\AcademicTool\安卓APK分析\jd-gui-0.3.5.windows\jars\activity_fragment_1-dex2jar.jar
 * Qualified Name:     com.test.ui.transition.CFragment
 * JD-Core Version:    0.6.2
 */