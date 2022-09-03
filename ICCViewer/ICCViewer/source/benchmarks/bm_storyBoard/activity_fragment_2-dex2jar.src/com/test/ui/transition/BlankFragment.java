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

public class BlankFragment extends Fragment
{
  private void goToFragment(FragmentActivity paramFragmentActivity, Fragment paramFragment)
  {
    paramFragmentActivity.getSupportFragmentManager().beginTransaction().replace(2131230796, paramFragment).commit();
  }

  private void jump2Activity(Context paramContext, Class<NextActivity> paramClass)
  {
    startActivity(new Intent(paramContext, paramClass));
  }

  public void onAttach(Context paramContext)
  {
    super.onAttach(paramContext);
    if (Math.random() > 0.5D)
    {
      goToFragment(getActivity(), new AFragment());
      return;
    }
    jump2Activity(paramContext, NextActivity.class);
  }

  public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle)
  {
    return paramLayoutInflater.inflate(2131361836, paramViewGroup, false);
  }
}

/* Location:           D:\SoftwareData\AcademicTool\安卓APK分析\jd-gui-0.3.5.windows\jars\activity_fragment_2-dex2jar.jar
 * Qualified Name:     com.test.ui.transition.BlankFragment
 * JD-Core Version:    0.6.2
 */