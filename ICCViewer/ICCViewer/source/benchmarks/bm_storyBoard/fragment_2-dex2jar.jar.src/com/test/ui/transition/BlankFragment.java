package com.test.ui.transition;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BlankFragment extends Fragment {
  private void showFragment() {
    NotBlankFragment notBlankFragment = new NotBlankFragment();
    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
    fragmentManager.beginTransaction().replace(2131230796, notBlankFragment).commit();
    fragmentManager.beginTransaction().show(notBlankFragment);
  }
  
  public void onAttach(Context paramContext) {
    super.onAttach(paramContext);
    showFragment();
  }
  
  public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
    return paramLayoutInflater.inflate(2131361836, paramViewGroup, false);
  }
}


/* Location:              D:\SoftwareData\AcademicTool\安卓APK分析\jd-gui-0.3.5.windows\jars\fragment_2-dex2jar.jar!\com\tes\\ui\transition\BlankFragment.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */