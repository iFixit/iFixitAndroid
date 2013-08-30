package com.dozuki.ifixit.ui.guide.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.ui.BaseFragment;

public class NoGuidesFragment extends BaseFragment {
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      return inflater.inflate(R.layout.no_guides_fragment, container, false);
   }
}
