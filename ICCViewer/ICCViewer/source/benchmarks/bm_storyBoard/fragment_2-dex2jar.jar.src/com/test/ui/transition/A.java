package com.test.ui.transition;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class A extends Fragment {
  private void showFragment(Fragment paramFragment) {
    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
    fragmentManager.beginTransaction().replace(2131230796, paramFragment).commit();
    fragmentManager.beginTransaction().show(paramFragment);
  }
  
  public void onAttach(Context paramContext) {
    BlankFragment blankFragment;
    super.onAttach(paramContext);
    if (Math.random() > 0.6D) {
      B b = new B();
    } else {
      blankFragment = new BlankFragment();
    } 
    showFragment(blankFragment);
  }
  
  public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
    return paramLayoutInflater.inflate(2131361837, paramViewGroup, false);
  }
}


/* Location:              D:\SoftwareData\AcademicTool\安卓APK分析\jd-gui-0.3.5.windows\jars\fragment_2-dex2jar.jar!\com\tes\\ui\transition\A.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */