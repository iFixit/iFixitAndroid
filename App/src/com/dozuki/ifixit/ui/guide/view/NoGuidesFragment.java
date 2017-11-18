package com.dozuki.ifixit.ui.guide.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.ui.BaseFragment;
import com.dozuki.ifixit.ui.guide.create.StepEditActivity;

public class NoGuidesFragment extends BaseFragment {
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      View v = inflater.inflate(R.layout.no_guides_fragment, container, false);

      View noGuidesButton = v.findViewById(R.id.new_guide_button);

      if (noGuidesButton != null) {
         noGuidesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(getActivity(), StepEditActivity.class);
               intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
               startActivity(intent);
            }
         });
      }
      return v;
   }
}
