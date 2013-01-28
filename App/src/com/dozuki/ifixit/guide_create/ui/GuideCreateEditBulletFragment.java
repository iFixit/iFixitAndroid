package com.dozuki.ifixit.guide_create.ui;

import java.util.ArrayList;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.FrameLayout;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.Toast;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.ui.ChooseBulletDialog.BulletDialogListener;
import com.dozuki.ifixit.guide_create.ui.GuideCreateBulletReorderFragment.BulletRearrangeListener;
import com.dozuki.ifixit.guide_create.ui.GuideCreateStepEditFragment.GuideStepChangedListener;
import com.dozuki.ifixit.guide_view.model.StepLine;

public class GuideCreateEditBulletFragment extends Fragment implements BulletDialogListener, BulletRearrangeListener {
   private static final int BULLET_LIMIT = 8;
   private static final int BULLET_INDENT = 25;
   private static final String STEP_LIST_KEY = "STEP_LIST_KEY";
   private static final String SHOWING_BULLET_FRAG = "SHOWING_BULLET_FRAG";
   private static final String BULLET_FRAG_ID = "BULLET_FRAG_ID";
   private static final String SHOWING_REORDER_FRAG = "SHOWING_REORDER_FRAG";
   private static final int NONE = -1;
   private static final int INDENT_LIMIT = 3;
   private static final String REORDER_FRAG_ID = "REORDER_FRAG_ID";
   private LinearLayout mBulletContainer;
   private Button mNewBulletButton;
   private ArrayList<StepLine> mLines = new ArrayList<StepLine>();
   private ChooseBulletDialog mChooseBulletDialog;
   private boolean mShowingChooseBulletDialog;
   private boolean mReorderModeActive;
   private GuideCreateBulletReorderFragment mReorderFragment;
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }

   /** unchecked: we know what we are getting from the from the STEP_LIST_KEY **/
   @SuppressWarnings("unchecked")
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

      View v = inflater.inflate(R.layout.guide_create_edit_bullets, container, false);
      if (savedInstanceState != null) {
         mLines = (ArrayList<StepLine>) savedInstanceState.getSerializable(STEP_LIST_KEY);
         mChooseBulletDialog =
            (ChooseBulletDialog) getSupportFragmentManager().getFragment(savedInstanceState, BULLET_FRAG_ID);
         mReorderFragment =
            (GuideCreateBulletReorderFragment) getSupportFragmentManager().getFragment(savedInstanceState, REORDER_FRAG_ID);
         
         mShowingChooseBulletDialog = savedInstanceState.getBoolean(SHOWING_BULLET_FRAG, false);
         if (mChooseBulletDialog != null && mShowingChooseBulletDialog) {
            mChooseBulletDialog.setTargetFragment(this, 0);
         }
         mReorderModeActive = savedInstanceState.getBoolean(SHOWING_REORDER_FRAG, false);
         if (mReorderFragment != null && mReorderModeActive) {
            Log.e("WDEW", "EDE");
            mReorderFragment.setBulletRearrangeListener(this);
         }
      }  
      
      mNewBulletButton = (Button) v.findViewById(R.id.add_new_bullet_button);
      mNewBulletButton.setOnClickListener(new OnClickListener() {

         @Override
         public void onClick(View v) {
              mLines.add(new StepLine("black", 0, ""));
              mBulletContainer.addView(getView( mLines.get(mLines.size()-1), mLines.size()-1), mLines.size()-1);
              if(mLines.size() == BULLET_LIMIT) {
                 mNewBulletButton.setVisibility(View.GONE);
              }
         }
      });
      
      mBulletContainer = (LinearLayout) v.findViewById(R.id.edit_step_bullet_container);
      initilizeBulletContainer();

      return v;
   }
   
   @Override
   public void onResume() {
      super.onResume();
   }

   public void setSteps(ArrayList<StepLine> lines) {
      mLines.addAll(lines);
   }
   
   private void initilizeBulletContainer() {
      for(int i = 0; i < mLines.size(); i++) {
         mBulletContainer.addView(getView(mLines.get(i), i), i); 
      }
      if(mLines.size() == BULLET_LIMIT) {
         mNewBulletButton.setVisibility(View.GONE);
      }
   }
   

      public View getView(final StepLine line, int index) {
         LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View v = vi.inflate(R.layout.guide_create_step_edit_list_item, null);
         FrameLayout iconFrame = (FrameLayout) v.findViewById(R.id.guide_step_item_frame);
         
         iconFrame.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               FragmentManager fm = getActivity().getSupportFragmentManager();
               mChooseBulletDialog = new ChooseBulletDialog();
               mChooseBulletDialog.setTargetFragment(GuideCreateEditBulletFragment.this, 0);
               mChooseBulletDialog.setStepIndex(mLines.indexOf(line));
               mChooseBulletDialog.show(fm, "fragment_choose_bullet");
               mShowingChooseBulletDialog = true;
            }
         });
         LayoutParams params = (LayoutParams) iconFrame.getLayoutParams();
         params.setMargins(BULLET_INDENT * line.getLevel(), 0, 0, 0);
         iconFrame.setLayoutParams(params);
         EditText text = (EditText) v.findViewById(R.id.step_title_textview);
         text.setText(line.getText());
         text.setId(index);
         text.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
               if(s.toString().equals(line.getText())) {
                  return;
               }
               mLines.get(mLines.indexOf(line)).setText(s.toString());
               setGuideDirty();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

         });
        
         ImageView icon = (ImageView) v.findViewById(R.id.guide_step_item_thumbnail);
         icon.setImageResource(getBulletResource(line.getColor()));

         return v;
      }
      
   
   
   public int getBulletResource(String color) {
      int iconRes;

      if (color.equals("black")) {
         iconRes = R.drawable.ic_dialog_bullet_black;
      } else if (color.equals("orange")) {
         iconRes = R.drawable.ic_dialog_bullet_orange;
      } else if (color.equals("blue")) {
         iconRes = R.drawable.ic_dialog_bullet_blue;
      } else if (color.equals("purple")) {
         iconRes = R.drawable.ic_dialog_bullet_pink;
      } else if (color.equals("red")) {
         iconRes = R.drawable.ic_dialog_bullet_red;
      } else if (color.equals("teal")) {
         iconRes = R.drawable.ic_dialog_bullet_green;
      } else if (color.equals("white")) {
         iconRes = R.drawable.bullet_white;
      } else if (color.equals("yellow")) {
         iconRes = R.drawable.ic_dialog_bullet_yellow;
      } else if (color.equals("icon_reminder")) {
         iconRes = R.drawable.ic_dialog_bullet_reminder_dark;
      } else if (color.equals("icon_caution")) {
         iconRes = R.drawable.ic_dialog_bullet_caution;
      } else if (color.equals("icon_note")) {
         iconRes = R.drawable.ic_dialog_bullet_note_dark;
      } else {
         iconRes = R.drawable.ic_dialog_bullet_black;
      }
      return iconRes;
   }


   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
      savedInstanceState.putSerializable(STEP_LIST_KEY, mLines);
      if (mChooseBulletDialog != null && mShowingChooseBulletDialog) {
         getSupportFragmentManager().putFragment(savedInstanceState, BULLET_FRAG_ID, mChooseBulletDialog);
         savedInstanceState.putBoolean(SHOWING_BULLET_FRAG, mShowingChooseBulletDialog);
      }
      savedInstanceState.putBoolean(SHOWING_REORDER_FRAG, mReorderModeActive);
      
      if (mReorderFragment != null && mReorderModeActive) {
         getSupportFragmentManager().putFragment(savedInstanceState, REORDER_FRAG_ID, mReorderFragment);
         savedInstanceState.putBoolean(SHOWING_REORDER_FRAG, mReorderModeActive);
      }

   }
   
   @Override
   public void onFinishBulletDialog(int index, String color) {
      mShowingChooseBulletDialog = false;
      StepLine curStep = mLines.get(index);
      
      if (color.equals("action_indent")) {
         if (curStep.getLevel() == INDENT_LIMIT) {
            Toast.makeText(((Activity) getActivity()), R.string.indent_limit_above, Toast.LENGTH_SHORT).show();
            return;
         }
         curStep.setLevel(curStep.getLevel() + 1);
         mBulletContainer.removeViewAt(index);
         mBulletContainer.addView(getView(mLines.get(index), index), index);
      } else if (color.equals("action_unindent")) {
         if (curStep.getLevel() == 0) {
            Toast.makeText(((Activity) getActivity()), R.string.indent_limit_below, Toast.LENGTH_SHORT).show();
            return;
         }
         curStep.setLevel(curStep.getLevel() - 1);
         mBulletContainer.removeViewAt(index);
         mBulletContainer.addView(getView(mLines.get(index), index), index);
         
      } else if (color.equals("action_reorder")) {
         launchBulletReorder();
      } else if (color.equals("action_delete")) {
         mLines.remove(index);
         mBulletContainer.removeViewAt(index);
         mNewBulletButton.setVisibility(View.VISIBLE);
      } else if (color.equals("action_cancel")) {
         return;
      } else {
         curStep.setColor(color);
         mBulletContainer.removeViewAt(index);
         mBulletContainer.addView(getView(mLines.get(index), index), index);
      }
      setGuideDirty();
   }

   public ArrayList<StepLine> getLines() {
      return mLines;
   }
   
   private void launchBulletReorder() {
      FragmentManager fm = getActivity().getSupportFragmentManager();
      mReorderFragment = new GuideCreateBulletReorderFragment();
      mReorderFragment.setLines(mLines);
      mReorderFragment.setBulletRearrangeListener(this);
      mReorderFragment.show(fm, "fragment_reorder_bullet");
      mReorderModeActive = true;
   }

   @Override
   public void onReorderComplete(boolean cancled, ArrayList<StepLine> list) {
      mReorderModeActive = false;
      if (!cancled) {
         mLines.clear();
         mLines.addAll(list);
         removeBullets();
         initilizeBulletContainer();
         ((GuideStepChangedListener) getActivity()).enableSave();
         setGuideDirty();
      }
   }

   public void setGuideDirty() {
      ((GuideStepChangedListener) getActivity()).onGuideStepChanged();
   }

   public boolean isReorderModeActive() {
      return mReorderModeActive;
   }

   public void removeBullets() {
      mBulletContainer.removeViewsInLayout(0, mLines.size());
   }
}
