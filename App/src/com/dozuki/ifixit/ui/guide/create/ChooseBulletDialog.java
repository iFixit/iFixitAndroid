package com.dozuki.ifixit.ui.guide.create;

import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.ui.BaseDialogFragment;

public class ChooseBulletDialog extends BaseDialogFragment implements OnClickListener {

   private static final String INDEX_KEY = "INDEX_KEY";
   private static final String DELETE_VIS_KEY = "DELETE_VIS_KEY";
   private static final String INDENT_VIS_KEY = "INDENT_VIS_KEY";
   private static final String UNINDENT_VIS_KEY = "UNINDENT_VIS_KEY";
   private static final String REARRANGE_VIS_KEY = "REARRANGE_VIS_KEY";
   private int mStepIndex;
   private TextView mRearrangeText;
   private TextView mIndentText;
   private TextView mUnIndentText;
   private TextView mDeleteText;

   private boolean mRearrangeTextInVis;
   private boolean mIndentTextInVis;
   private boolean mUnIndentTextInVis;
   private boolean mDeleteTextInVis;

   public interface BulletDialogListener {
      void onFinishBulletDialog(int index, String color);
   }

   public ChooseBulletDialog() {
      // Empty constructor required for DialogFragment
   }

   public void setStepIndex(int indx) {
      mStepIndex = indx;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setStyle(STYLE_NO_TITLE, 0);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

      if (savedInstanceState != null) {
         mStepIndex = savedInstanceState.getInt(INDEX_KEY);
         mDeleteTextInVis =  savedInstanceState.getBoolean(DELETE_VIS_KEY);
         mIndentTextInVis = savedInstanceState.getBoolean(INDENT_VIS_KEY);
         mUnIndentTextInVis = savedInstanceState.getBoolean(UNINDENT_VIS_KEY);
         mRearrangeTextInVis = savedInstanceState.getBoolean(REARRANGE_VIS_KEY);
      }

      View view = inflater.inflate(R.layout.guide_create_steps_bullet_popup, container);
      view.findViewById(R.id.bullet_dialog_color_black).setOnClickListener(this);
      view.findViewById(R.id.bullet_dialog_color_red).setOnClickListener(this);
      view.findViewById(R.id.bullet_dialog_color_orange).setOnClickListener(this);
      view.findViewById(R.id.bullet_dialog_color_yellow).setOnClickListener(this);
      view.findViewById(R.id.bullet_dialog_color_green).setOnClickListener(this);
      view.findViewById(R.id.bullet_dialog_color_light_blue).setOnClickListener(this);
      view.findViewById(R.id.bullet_dialog_color_blue).setOnClickListener(this);
      view.findViewById(R.id.bullet_dialog_color_violet).setOnClickListener(this);
      view.findViewById(R.id.bullet_dialog_caution).setOnClickListener(this);
      view.findViewById(R.id.bullet_dialog_note).setOnClickListener(this);
      view.findViewById(R.id.bullet_dialog_reminder).setOnClickListener(this);
      mIndentText = (TextView) view.findViewById(R.id.bullet_dialog_indent);
      mIndentText.setOnClickListener(this);
      if(mIndentTextInVis) {
         mIndentText.setVisibility(View.GONE);
      }
      
      mUnIndentText = (TextView) view.findViewById(R.id.bullet_dialog_unindent);
      mUnIndentText.setOnClickListener(this);
      if(mUnIndentTextInVis) {
         mUnIndentText.setVisibility(View.GONE);
      }
      mRearrangeText = (TextView) view.findViewById(R.id.bullet_dialog_rearrange);
      mRearrangeText.setOnClickListener(this);
      if(mRearrangeTextInVis) {
         mRearrangeText.setVisibility(View.GONE);
      }
      
      mDeleteText = (TextView) view.findViewById(R.id.bullet_dialog_delete);
      mDeleteText.setOnClickListener(this);
      if(mDeleteTextInVis) {
         mDeleteText.setVisibility(View.GONE);
      }
      
      view.findViewById(R.id.bullet_dialog_cancel).setOnClickListener(this);

      LayoutParams params = getDialog().getWindow().getAttributes();
      params.width = LayoutParams.WRAP_CONTENT;
      getDialog().getWindow().setAttributes(params);
      getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));

      return view;
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
      savedInstanceState.putInt(INDEX_KEY, mStepIndex);

      savedInstanceState.putBoolean(DELETE_VIS_KEY, mDeleteTextInVis);
      savedInstanceState.putBoolean(INDENT_VIS_KEY, mIndentTextInVis);
      savedInstanceState.putBoolean(UNINDENT_VIS_KEY, mUnIndentTextInVis);
      savedInstanceState.putBoolean(REARRANGE_VIS_KEY, mRearrangeTextInVis);
   }

   @Override
   public void onClick(View v) {
      BulletDialogListener frag = (BulletDialogListener) getTargetFragment();
      String label = null;

      switch (v.getId()) {
         case R.id.bullet_dialog_color_black:
            frag.onFinishBulletDialog(mStepIndex, "black");
            label = "bullet_dialog_color_black";
            break;
         case R.id.bullet_dialog_color_red:
            frag.onFinishBulletDialog(mStepIndex, "red");
            label = "bullet_dialog_color_red";
            break;
         case R.id.bullet_dialog_color_orange:
            frag.onFinishBulletDialog(mStepIndex, "orange");
            label = "bullet_dialog_color_orange";
            break;
         case R.id.bullet_dialog_color_yellow:
            frag.onFinishBulletDialog(mStepIndex, "yellow");
            label = "bullet_dialog_color_yellow";
            break;
         case R.id.bullet_dialog_color_blue:
            frag.onFinishBulletDialog(mStepIndex, "blue");
            label = "bullet_dialog_color_blue";
            break;
         case R.id.bullet_dialog_color_light_blue:
            frag.onFinishBulletDialog(mStepIndex, "light_blue");
            label = "bullet_dialog_color_light_blue";
            break;
         case R.id.bullet_dialog_color_violet:
            frag.onFinishBulletDialog(mStepIndex, "violet");
            label = "bullet_dialog_color_violet";
            break;
         case R.id.bullet_dialog_color_green:
            frag.onFinishBulletDialog(mStepIndex, "green");
            label = "bullet_dialog_color_green";
            break;
         case R.id.bullet_dialog_caution:
            frag.onFinishBulletDialog(mStepIndex, "icon_caution");
            label = "bullet_dialog_caution";
            break;
         case R.id.bullet_dialog_note:
            frag.onFinishBulletDialog(mStepIndex, "icon_note");
            label = "bullet_dialog_note";
            break;
         case R.id.bullet_dialog_reminder:
            frag.onFinishBulletDialog(mStepIndex, "icon_reminder");
            label = "bullet_dialog_reminder";
            break;
         case R.id.bullet_dialog_indent:
            frag.onFinishBulletDialog(mStepIndex, "action_indent");
            label = "bullet_dialog_indent";
            break;
         case R.id.bullet_dialog_unindent:
            frag.onFinishBulletDialog(mStepIndex, "action_unindent");
            label = "bullet_dialog_unindent";
            break;
         case R.id.bullet_dialog_rearrange:
            frag.onFinishBulletDialog(mStepIndex, "action_reorder");
            label = "bullet_dialog_rearrange";
            break;
         case R.id.bullet_dialog_delete:
            frag.onFinishBulletDialog(mStepIndex, "action_delete");
            label = "bullet_dialog_delete";
            break;
         case R.id.bullet_dialog_cancel:
            frag.onFinishBulletDialog(mStepIndex, "action_cancel");
            label = "bullet_dialog_cancel";
            break;
      }

      if (label != null) {
         App.sendEvent("ui_action", "button_click", label, null);
      }

      dismiss();
   }

   @Override
   public void onCancel(DialogInterface d) {
      BulletDialogListener frag = (BulletDialogListener) getTargetFragment();
      frag.onFinishBulletDialog(mStepIndex, "action_cancel");
      super.onCancel(d);
   }

   @Override
   public void onStart() {
      super.onStart();

      App.sendScreenView("/guide/edit/" + ((StepEditActivity)getActivity())
       .getGuideId() + "/" + mStepIndex + "/bullet_dialog");
   }

   public void disableUnIndent() {

      if (mUnIndentText != null) {
         mUnIndentText.setVisibility(View.GONE);
      }

      mUnIndentTextInVis = true;
   }

   public void disableIndent() {
      if (mIndentText != null) {
         mIndentText.setVisibility(View.GONE);
      }
      mIndentTextInVis = true;
   }

   public void disableDelete() {
      if (mDeleteText != null) {
         mDeleteText.setVisibility(View.GONE);
      }
      mDeleteTextInVis = true;
   }

   public void disableRearrange() {
      if (mRearrangeText != null) {
         mRearrangeText.setVisibility(View.GONE);
      }
      mRearrangeTextInVis = true;
   }
}
