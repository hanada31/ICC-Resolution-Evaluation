package com.test.ui.transition;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Bfragment extends Fragment
{
  public void onAttach(Context paramContext)
  {
    super.onAttach(paramContext);
    double d = Math.random();
    if (d > 0.5D)
    {
      startActivity(new Intent(paramContext, NextActivity.class));
      return;
    }
    if (d > 0.3D)
    {
      startActivity(new Intent(paramContext, MainActivity.class));
      return;
    }
    getActivity().getSupportFragmentManager().beginTransaction().replace(2131230796, new CFragment()).commit();
  }

  public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle)
  {
    return paramLayoutInflater.inflate(2131361838, paramViewGroup, false);
  }
}

/* Location:           D:\SoftwareData\AcademicTool\安卓APK分析\jd-gui-0.3.5.windows\jars\activity_fragment_1-dex2jar.jar
 * Qualified Name:     com.test.ui.transition.Bfragment
 * JD-Core Version:    0.6.2
 */