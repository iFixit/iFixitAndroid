package com.dozuki.ifixit.guide_create.ui;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.dozuki.ifixit.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ChooseBulletDialog extends SherlockDialogFragment {

  // private EditText mEditText;

   public ChooseBulletDialog() {
       // Empty constructor required for DialogFragment
   }
   
   @Override
   public void onCreate(Bundle savedInstanceState) {
      setStyle(STYLE_NO_TITLE, 0);
      super.onCreate(savedInstanceState);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
           Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.guide_create_steps_bullet_popup, container);
     //  mEditText = (EditText) view.findViewById(R.id.txt_your_name);
      // getDialog().setTitle("Hello");

       return view;
   }
}