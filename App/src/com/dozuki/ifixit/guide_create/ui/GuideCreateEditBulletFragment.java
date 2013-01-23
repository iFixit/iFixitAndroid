package com.dozuki.ifixit.guide_create.ui;

import java.util.ArrayList;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.FrameLayout;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.Toast;

import android.R.color;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.model.GuideCreateStepObject;
import com.dozuki.ifixit.guide_create.ui.ChooseBulletDialog.BulletDialogListener;
import com.dozuki.ifixit.guide_create.ui.GuideCreateBulletReorderFragment.BulletRearrangeListener;
import com.dozuki.ifixit.guide_create.ui.GuideCreateStepEditFragmentNew.GuideStepChangedListener;
import com.dozuki.ifixit.guide_view.model.StepLine;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

public class GuideCreateEditBulletFragment extends Fragment implements BulletDialogListener, BulletRearrangeListener {
   
   private static final String GUIDE_EDIT_KEY = "GuideEditKey";
   private static final String REORDER_STEPS_KEY = "ReorderStepsKey";
   private static final String STEP_LIST_KEY = "STEP_LIST_KEY";
   private static final String SHOWING_BULLET_FRAG = "SHOWING_BULLET_FRAG";
   private static final String BULLET_FRAG_ID = "BULLET_FRAG_ID";
   private static final String SHOWING_REORDER_FRAG = "SHOWING_REORDER_FRAG";
   private static final int NONE = -1;
   ImageView mMediaIcon;
   BulletListAdapter mBulletListAdapter;
   boolean mReorderStepsMode;
   ImageView mBottomBarSpinnerIcon;
   ListView mBulletList;
   ArrayList<StepLine> mLines = new ArrayList<StepLine>();
   private ChooseBulletDialog mChooseBulletDialog;
   private boolean mShowingChooseBulletDialog;
   private boolean mReorderModeActive;
   private int mCurrentFocusedRow;
   

  
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }
   
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, 
       Bundle savedInstanceState) {

      View v = inflater.inflate(R.layout.guide_create_edit_bullets, container, false);
      

      mMediaIcon = (ImageView) v.findViewById(R.id.step_edit_thumb_media);
      mBulletList = (ListView) v
            .findViewById(R.id.steps_portal_list);

  
      mReorderStepsMode = false;
      mCurrentFocusedRow = NONE;
      
      if (savedInstanceState != null) {
         mReorderStepsMode = savedInstanceState.getBoolean(REORDER_STEPS_KEY);
         mLines = (ArrayList<StepLine>) savedInstanceState.getSerializable(STEP_LIST_KEY);

         mChooseBulletDialog = (ChooseBulletDialog) getSupportFragmentManager().getFragment(savedInstanceState, BULLET_FRAG_ID );
         mShowingChooseBulletDialog = savedInstanceState.getBoolean(SHOWING_BULLET_FRAG, false);
         if(mChooseBulletDialog != null && mShowingChooseBulletDialog) {
            mChooseBulletDialog.setTargetFragment(this, 0);
         }
         mReorderModeActive = savedInstanceState.getBoolean(SHOWING_REORDER_FRAG, false);
        
      }
      mBulletListAdapter = new BulletListAdapter(this.getActivity(),
            R.layout.guide_create_step_edit_list_item,
            mLines);
      mBulletList.setAdapter(mBulletListAdapter);
      
     // mBulletList.setFocusableInTouchMode(true);
     // mBulletList.requestFocus();
      
       return v;
   }
   
   @Override
   public void onResume() {
      super.onResume();
      
      if(mBulletListAdapter != null)
         mBulletListAdapter.notifyDataSetChanged();
   }
   
   public void setSteps(ArrayList<StepLine> lines) {
      mLines.addAll(lines);
   }
   
   
   private class BulletListAdapter extends ArrayAdapter<StepLine> {

      private ArrayList<StepLine> items;
      private Context con;

      public BulletListAdapter(Context context, int textViewResourceId,
            ArrayList<StepLine> items) {
         super(context, textViewResourceId, items);
         this.items = items;
         con = context;
      }

      @Override
      public int getCount() {
         if(items.size() == 8)
         {
            return items.size();
         }
         return items.size() + 1;
      }

      @Override
      public View getView(final int position, View convertView, ViewGroup parent) {
         View v = convertView;
            LayoutInflater vi = (LayoutInflater) con
                  .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.guide_create_step_edit_list_item, null);

         if (position == items.size()) {
            
            ImageView newItem = (ImageView) v
                  .findViewById(R.id.add_new_bullet);
            v.findViewById(R.id.guide_step_item_thumbnail).setVisibility(
                  View.GONE);
            v.findViewById(R.id.step_title_textview).setVisibility(
                  View.GONE);
            newItem.setVisibility(View.VISIBLE);
            newItem.setOnClickListener(new OnClickListener() {

               @Override
               public void onClick(View v) {
                  mLines.add(new StepLine("black", 0,
                        ""));
                  notifyDataSetChanged();
               }
            });
            return v;
         }
         final int mPos = position;
         FrameLayout iconFrame = (FrameLayout) v
               .findViewById(R.id.guide_step_item_frame);
         iconFrame.setOnClickListener(new OnClickListener() {


            @Override
            public void onClick(View v) {
               FragmentManager fm = getActivity()
                     .getSupportFragmentManager();
               mChooseBulletDialog = new ChooseBulletDialog();
               mChooseBulletDialog.setTargetFragment(GuideCreateEditBulletFragment.this, 0);
               mChooseBulletDialog.setStepIndex(mPos);
               mChooseBulletDialog.show(fm, "fragment_choose_bullet");
               mShowingChooseBulletDialog = true;
            }

         });
         LayoutParams params = (LayoutParams) iconFrame.getLayoutParams();
         params.setMargins(25 * items.get(position).getLevel(), 0, 0, 0);
         iconFrame.setLayoutParams(params);
         final EditText text = (EditText) v.findViewById(R.id.step_title_textview);
         text.setText(items.get(position).getText());
        // text.requestFocusFromTouch();
         text.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
               items.get(position).setText(s.toString());
               setGuideDirty();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                  int after) {
               // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                  int count) {
            }

         });
         
         text.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
               if (hasFocus) {
                  mCurrentFocusedRow = position;
               }
            }
         });

         if (mCurrentFocusedRow == position) {
            text.requestFocus();
         } else {
            text.setFocusableInTouchMode(false);
         }

         text.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
               if (event.getAction() == MotionEvent.ACTION_UP) {
                  v.setFocusableInTouchMode(true);
               }
               return false;
            }

         });
         
         ImageView icon = (ImageView) v
               .findViewById(R.id.guide_step_item_thumbnail);
         icon.setImageResource(getBulletResource(items.get(position)
               .getColor()));
         

         
         return v;
      }

      public int getBulletResource(String color) {
         int iconRes;

         if (color.equals("black")) {
            iconRes = R.drawable.bullet_black;
         } else if (color.equals("orange")) {
            iconRes = R.drawable.bullet_orange;
         } else if (color.equals("blue")) {
            iconRes = R.drawable.bullet_blue;
         } else if (color.equals("purple")) {
            iconRes = R.drawable.bullet_purple;
         } else if (color.equals("red")) {
            iconRes = R.drawable.bullet_red;
         } else if (color.equals("teal")) {
            iconRes = R.drawable.bullet_teal;
         } else if (color.equals("white")) {
            iconRes = R.drawable.bullet_white;
         } else if (color.equals("yellow")) {
            iconRes = R.drawable.bullet_yellow;
         } else if (color.equals("icon_reminder")) {
            iconRes = R.drawable.ic_dialog_bullet_reminder_dark;
         } else if (color.equals("icon_caution")) {
            iconRes = R.drawable.ic_dialog_bullet_caution;
         } else if (color.equals("icon_note")) {
            iconRes = R.drawable.ic_dialog_bullet_note_dark;
         } else {
            iconRes = R.drawable.bullet_black;
         }

         return iconRes;
      }
   }

   public final class ContextualStepReorder implements ActionMode.Callback {
      public ContextualStepReorder() {
      }

      @Override
      public boolean onCreateActionMode(ActionMode mode, Menu menu) {
         return true;
      }

      @Override
      public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
         return false;
      }

      @Override
      public void onDestroyActionMode(ActionMode mode) {
         mReorderStepsMode = false;
         //((GuideCreateStepsEditActivity) getActivity()).invalidateStepAdapter();
      }

      @Override
      public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

         return true;
      }
   };
   
   public DragSortController buildController(DragSortListView dslv) {
      DragSortController controller = new DragSortController(dslv);
      controller.setDragHandleId(R.id.guide_step_drag_handle);
      controller.setRemoveEnabled(false);
      controller.setSortEnabled(true);
      controller.setDragInitMode(DragSortController.ON_DOWN);
      controller.setRemoveMode(DragSortController.FLING_RIGHT_REMOVE);
      controller.setBackgroundColor(color.background_light);
      return controller;
   }
   
   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
      savedInstanceState.putSerializable(STEP_LIST_KEY, mLines);
      if(mChooseBulletDialog != null && mShowingChooseBulletDialog) {
         getSupportFragmentManager().putFragment(savedInstanceState, BULLET_FRAG_ID, mChooseBulletDialog );
         savedInstanceState.putBoolean(SHOWING_BULLET_FRAG, mShowingChooseBulletDialog);
      }
      savedInstanceState.putBoolean(SHOWING_REORDER_FRAG, mReorderModeActive);
      
   
   }
   
   @Override
   public void onFinishBulletDialog(int index, String color) {
      mShowingChooseBulletDialog = false;
      StepLine curStep = mLines.get(index);
      
      if(color.equals("action_indent"))
      {
         if(curStep.getLevel() == 3) {
            Toast.makeText(((Activity)getActivity()), R.string.indent_limit_above, Toast.LENGTH_SHORT).show();
            return;
         }
         curStep.setLevel(curStep.getLevel() + 1);
      }
      else if(color.equals("action_unindent"))
      {
         if(curStep.getLevel() == 0) {
            Toast.makeText(((Activity)getActivity()), R.string.indent_limit_below, Toast.LENGTH_SHORT).show();
            return;
         }
         curStep.setLevel(curStep.getLevel() - 1);
      }
      else if(color.equals("action_reorder"))
      {
         launchBulletReorder();
      }
      else if(color.equals("action_reorder"))
      {
         launchBulletReorder();
      } else if(color.equals("action_delete"))
      {
         mLines.remove(index);
      }
      else if(color.equals("action_cancel"))
      {
        return;
      }
      else
      {
         curStep.setColor(color);
      }
      mBulletListAdapter.notifyDataSetChanged();
      setGuideDirty();
   }

   public ArrayList<StepLine> getLines() {
      return mLines;
   }
   
   private void launchBulletReorder()
   {
      
      mReorderModeActive = true;
      GuideCreateBulletReorderFragment mReorderFragment = new GuideCreateBulletReorderFragment();
      mReorderFragment.setLines(mLines);
      FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
      //mReorderFragment.setTargetFragment(this, 0);
      //mReorderFragment.setRetainInstance(true);
      transaction.add(R.id.guide_create_edit_bullet_reorder_fragment_container, mReorderFragment);
      transaction.addToBackStack(null);
      transaction.commit();
      ((GuideStepChangedListener) getActivity()).disableSave();
   }

   @Override
   public void onReorderComplete() {     
      ((GuideStepChangedListener) getActivity()).enableSave();;
      getChildFragmentManager().popBackStack();
      mBulletListAdapter.notifyDataSetChanged();
      setGuideDirty();
   }

   public void setGuideDirty() {
      ((GuideStepChangedListener) getActivity()).onGuideStepChanged();
   }

   public boolean isReorderModeActive() {
      return mReorderModeActive;
   }
}
