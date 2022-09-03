package com.test.ui.transition;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class E extends Fragment {
  public void onAttach(Context paramContext) {
    super.onAttach(paramContext);
    Math.random();
    BlankFragment blankFragment = new BlankFragment();
    getActivity().getSupportFragmentManager().beginTransaction().replace(2131230796, blankFragment);
  }
  
  public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
    return paramLayoutInflater.inflate(2131361836, paramViewGroup, false);
  }
}


/* Location:              D:\SoftwareData\AcademicTool\安卓APK分析\jd-gui-0.3.5.windows\jars\fragment_1-dex2jar.jar!\com\tes\\ui\transition\E.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */