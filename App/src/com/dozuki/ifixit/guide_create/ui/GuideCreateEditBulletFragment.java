package com.dozuki.ifixit.guide_create.ui;

import java.util.ArrayList;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.FrameLayout;

import android.R.color;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
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
import com.dozuki.ifixit.guide_view.model.StepLine;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

public class GuideCreateEditBulletFragment extends Fragment implements BulletDialogListener {
   
   private static final String GUIDE_EDIT_KEY = "GuideEditKey";
   private static final String REORDER_STEPS_KEY = "ReorderStepsKey";
   private static final String STEP_LIST_KEY = "STEP_LIST_KEY";
   GuideCreateStepObject mStepObject;
   ImageView mMediaIcon;
   DragSortController mController;
   BulletListAdapter mBulletListAdapter;
   boolean mReorderStepsMode;
   ImageView mBottomBarSpinnerIcon;
   DragSortListView mBulletList;
   ArrayList<StepLine> mLines = new ArrayList<StepLine>();
   
   private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
      @Override
      public void drop(int from, int to) {
         StepLine item = mBulletListAdapter.getItem(from);
         mBulletListAdapter.remove(item);
         mBulletListAdapter.insert(item, to);
         mBulletList.invalidateViews();
      }
   };

   private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {
      @Override
      public void remove(int which) {
         mBulletListAdapter.remove(mBulletListAdapter.getItem(which));
      }
   };
   
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }
   
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, 
       Bundle savedInstanceState) {

      View v = inflater.inflate(R.layout.guide_create_edit_bullets, container, false);
      

      mMediaIcon = (ImageView) v.findViewById(R.id.step_edit_thumb_media);
      mBulletList = (DragSortListView) v
            .findViewById(R.id.steps_portal_list);
      mBulletList.setDropListener(onDrop);
      mBulletList.setRemoveListener(onRemove);
   
      mController = buildController(mBulletList);
      mBulletList.setFloatViewManager(mController);
      mBulletList.setOnTouchListener(mController);
      mBulletList.setDragEnabled(true);
      mReorderStepsMode = false;
      
      
      if (savedInstanceState != null) {
         mReorderStepsMode = savedInstanceState.getBoolean(REORDER_STEPS_KEY);
         mLines = (ArrayList<StepLine>) savedInstanceState.getSerializable(STEP_LIST_KEY);
      }
      mBulletListAdapter = new BulletListAdapter(this.getActivity(),
            R.layout.guide_create_step_edit_list_item,
            mLines);
      mBulletList.setAdapter(mBulletListAdapter);
      
       return v;
   }
   
   public void setSteps(GuideCreateStepObject so) {
      mStepObject = so;
      mLines.addAll(mStepObject.getLines());
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
         return items.size() + 1;
      }

      @Override
      public View getView(final int position, View convertView, ViewGroup parent) {
         View v = convertView;
         //if (v == null) {
            LayoutInflater vi = (LayoutInflater) con
                  .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.guide_create_step_edit_list_item, null);
         //}

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
               final Dialog dialog = new Dialog(con);
               dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
               FragmentManager fm = getActivity()
                     .getSupportFragmentManager();
               ChooseBulletDialog chooseBulletDialog = new ChooseBulletDialog();
               chooseBulletDialog.setTargetFragment(GuideCreateEditBulletFragment.this, 0);
               chooseBulletDialog.setStepIndex(mPos);
               chooseBulletDialog.show(fm, "fragment_choose_bullet");
            }

         });
         LayoutParams params = (LayoutParams) iconFrame.getLayoutParams();
         params.setMargins(25 * items.get(position).getLevel(), 0, 0, 0);
         iconFrame.setLayoutParams(params);
         final EditText text = (EditText) v.findViewById(R.id.step_title_textview);
         text.setText(items.get(position).getText());
         text.requestFocusFromTouch();
         text.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                  int after) {
               // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                  int count) {
               Log.i("GuideCreateStepEditFragment", "GuideTitle changed to: "
                     + s.toString());
               items.get(position).setText(s.toString());
            }

         });
         ImageView icon = (ImageView) v
               .findViewById(R.id.guide_step_item_thumbnail);
         icon.setImageResource(getBulletResource(items.get(position)
               .getColor()));
         
         RelativeLayout reorderDragHandle = (RelativeLayout) v.findViewById(R.id.guide_step_drag_handle);
         if(mReorderStepsMode)
         {
             reorderDragHandle.setVisibility(View.VISIBLE);
         }
         else
         {
             reorderDragHandle.setVisibility(View.GONE);
         }
         
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
   }
   
   @Override
   public void onFinishBulletDialog(int index, String color) {
      StepLine curStep = mLines.get(index);
      
      if(color.equals("action_indent"))
      {
         curStep.setLevel(curStep.getLevel() + 1);
      }
      else if(color.equals("action_unindent"))
      {
         curStep.setLevel(curStep.getLevel() - 1);
      }
      else if(color.equals("action_reorder"))
      {
      // mCurStepFragment.setReorderStepsMode();
         //mPager.setPagingEnabled(false);
      }
      else
      {
         curStep.setColor(color);
      }
      mBulletListAdapter.notifyDataSetChanged();
   }

   public ArrayList<StepLine> getLines() {
      return mLines;
   }
}
