package com.dozuki.ifixit.ui.guide.create;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.ui.BaseFragment;

public class StepEditEmbedFragment extends BaseFragment {
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View v = inflater.inflate(R.layout.guide_step_video, container, false);

      return v;
   }
}
