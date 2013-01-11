package com.dozuki.ifixit.guide_create.ui;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.model.GuideCreateStepBullet.BulletTypes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class ChooseBulletDialog extends SherlockDialogFragment implements OnClickListener {

   public interface BulletDialogListener {
      void onFinishBulletDialog(String bulletID, BulletTypes type);
   }

   public ChooseBulletDialog() {
      // Empty constructor required for DialogFragment
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      setStyle(STYLE_NO_TITLE, 0);
      super.onCreate(savedInstanceState);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.guide_create_steps_bullet_popup, container);
      view.findViewById(R.id.bullet_dialog_color_black).setOnClickListener(this);
      view.findViewById(R.id.bullet_dialog_color_red).setOnClickListener(this);
      view.findViewById(R.id.bullet_dialog_color_orange).setOnClickListener(this);
      view.findViewById(R.id.bullet_dialog_color_yellow).setOnClickListener(this);

      view.findViewById(R.id.bullet_dialog_color_white).setOnClickListener(this);

      view.findViewById(R.id.bullet_dialog_color_blue).setOnClickListener(this);

      view.findViewById(R.id.bullet_dialog_color_purple).setOnClickListener(this);
      view.findViewById(R.id.bullet_dialog_color_orange).setOnClickListener(this);
      view.findViewById(R.id.bullet_dialog_color_teal).setOnClickListener(this);
      view.findViewById(R.id.bullet_dialog_indent).setOnClickListener(this);
      view.findViewById(R.id.bullet_dialog_unindent).setOnClickListener(this);
      view.findViewById(R.id.bullet_dialog_rearrange).setOnClickListener(this);
      view.findViewById(R.id.bullet_dialog_cancel).setOnClickListener(this);

      return view;
   }

   @Override
   public void onClick(View v) {
      BulletDialogListener activity = (BulletDialogListener) getActivity();
      switch (v.getId()) {
         case R.id.bullet_dialog_color_black:

            break;

         case R.id.bullet_dialog_color_red:

            break;

         case R.id.bullet_dialog_color_orange:

            break;
         case R.id.bullet_dialog_color_yellow:

            break;
         case R.id.bullet_dialog_color_white:

          
            break;
         case R.id.bullet_dialog_color_blue:
           
            this.dismiss();
            break;
         case R.id.bullet_dialog_color_purple:
            this.dismiss();
            break;

         case R.id.bullet_dialog_color_teal:
            this.dismiss();
            break;
         case R.id.bullet_dialog_indent:
            this.dismiss();
            break;
         case R.id.bullet_dialog_unindent:
            break;
         case R.id.bullet_dialog_cancel:
            break;

      }
      this.dismiss();

   }
}