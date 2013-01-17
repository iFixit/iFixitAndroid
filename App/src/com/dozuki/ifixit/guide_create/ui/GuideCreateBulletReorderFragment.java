package com.dozuki.ifixit.guide_create.ui;

import java.util.ArrayList;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.FrameLayout;
import org.holoeverywhere.widget.TextView;

import android.R.color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_view.model.StepLine;
import com.ifixit.android.imagemanager.ImageManager;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

public class GuideCreateBulletReorderFragment extends Fragment {

   public interface BulletRearrangeListener {

      public void onReorderComplete();

   }

   private static String LINES_KEY = "LINES_KEY";
   private DragSortListView mDragListView;
   private DragSortController mController;
   private StepAdapter mAdapter;
   private ImageManager mImageManager;

   // private GuideCreateObject mGuide;
   ArrayList<StepLine> mLines = new ArrayList<StepLine>();

   public void setLines(ArrayList<StepLine> lines) {
      // mGuide = guide;
      mLines = lines;
   }

   private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
      @Override
      public void drop(int from, int to) {
         StepLine item = mAdapter.getItem(from);
         mAdapter.remove(item);
         mAdapter.insert(item, to);
         mDragListView.invalidateViews();
      }
   };

   private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {
      @Override
      public void remove(int which) {
         mAdapter.remove(mAdapter.getItem(which));
      }
   };

   /**
    * Called in onCreateView. Override this to provide a custom
    * DragSortController.
    */
   public DragSortController buildController(DragSortListView dslv) {
      DragSortController controller = new DragSortController(dslv);
      controller.setDragHandleId(R.id.guide_step_drag_handle);
      // controller.setClickRemoveId(R.id.click_remove);
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
      savedInstanceState.putSerializable(LINES_KEY, mLines);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      
      getSherlockActivity().startActionMode(new ContextualStepReorder());
      if (mImageManager == null) {
         mImageManager = ((MainApplication) getActivity().getApplication()).getImageManager();
      }

      if (savedInstanceState != null) {
         mLines = (ArrayList<StepLine>) savedInstanceState.get(LINES_KEY);
         // mGuide = (GuideCreateObject)savedInstanceState.get(GuideCreateStepsActivity.GuideKey);
      }

      mAdapter = new StepAdapter(mLines);
      View view = inflater.inflate(R.layout.guide_create_step_portal_reorder, container, false);
      mDragListView = (DragSortListView) view.findViewById(R.id.steps_portal_list_reorder);
      mDragListView.setDropListener(onDrop);
      mDragListView.setRemoveListener(onRemove);
      mDragListView.setAdapter(mAdapter);
      mController = buildController(mDragListView);
      mDragListView.setFloatViewManager(mController);
      mDragListView.setOnTouchListener(mController);
      mDragListView.setDragEnabled(true);

      return view;
   }

   public final class ContextualStepReorder implements ActionMode.Callback {
      public ContextualStepReorder() {}

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
         getActivity().getSupportFragmentManager().popBackStack();
         ((BulletRearrangeListener) getTargetFragment()).onReorderComplete();
      }

      @Override
      public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

         return true;
      }
   };

   private class ViewHolder {
      public TextView stepsView;
      public ImageView mImageView;
      public FrameLayout mItemHolder;
   }

   private class StepAdapter extends ArrayAdapter<StepLine> {
      public StepAdapter(ArrayList<StepLine> lines) {
         super(getActivity(), R.layout.guide_create_step_edit_list_item_reorder, R.id.bullet_text_textview, lines);
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
         View v = super.getView(position, convertView, parent);
         if (v != convertView && v != null) {
            final ViewHolder holder = new ViewHolder();

            TextView tv = (TextView) v.findViewById(R.id.bullet_text_textview);
            holder.stepsView = tv;

            holder.mImageView = (ImageView) v.findViewById(R.id.guide_step_item_thumbnail);
            v.setTag(holder);
            
            holder.mItemHolder = (FrameLayout) v
               .findViewById(R.id.guide_step_item_frame);
         }
         final ViewHolder holder = (ViewHolder) v.getTag();
         String step = getItem(position).getText();
         holder.stepsView.setText(step);
         holder.mImageView.setImageResource(getBulletResource(getItem(position).getColor()));
         LayoutParams params = (LayoutParams) holder.mItemHolder.getLayoutParams();
         params.setMargins(25 * getItem(position).getLevel(), 0, 0, 0);
         holder.mItemHolder.setLayoutParams(params);

         return v;
      }
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
