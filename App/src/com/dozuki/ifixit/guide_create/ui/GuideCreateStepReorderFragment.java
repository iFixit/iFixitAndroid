package com.dozuki.ifixit.guide_create.ui;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.app.Fragment;

import android.R.color;
import android.os.Bundle;
import org.holoeverywhere.LayoutInflater;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import org.holoeverywhere.widget.TextView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.model.GuideCreateObject;
import com.dozuki.ifixit.guide_create.model.GuideCreateStepObject;
import com.dozuki.ifixit.guide_view.model.StepImage;
import com.ifixit.android.imagemanager.ImageManager;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

public class GuideCreateStepReorderFragment extends Fragment {

   private DragSortListView mDragListView;
   private DragSortController mController;
   private StepAdapter mAdapter;
   private ImageManager mImageManager;
   private GuideCreateObject mGuide;
   private boolean mDiscardChanges;
   private ArrayList<GuideCreateStepObject> mStepsCopy;

   public void setGuide(GuideCreateObject guide) {
      mGuide = guide;
      mStepsCopy = new ArrayList<GuideCreateStepObject>(mGuide.getSteps());
   }

   private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
      @Override
      public void drop(int from, int to) {
         GuideCreateStepObject item = mAdapter.getItem(from);
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
      controller.setDragHandleId(R.id.drag_handle_reorder);
      controller.setRemoveEnabled(false);
      controller.setSortEnabled(true);
      controller.setDragInitMode(DragSortController.ON_DOWN);
      controller.setRemoveMode(DragSortController.FLING_RIGHT_REMOVE);
      controller.setBackgroundColor(color.background_light);
      return controller;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      getSherlockActivity().startActionMode(new ContextualStepReorder());
      if (mImageManager == null) {
         mImageManager = ((MainApplication) getActivity().getApplication()).getImageManager();
      }
      if (savedInstanceState != null) {
         mGuide = (GuideCreateObject) savedInstanceState.get(GuideCreateStepsActivity.GUIDE_KEY);
      }
      mDiscardChanges = false;
      mAdapter = new StepAdapter(mStepsCopy);
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
      savedInstanceState.putSerializable(GuideCreateStepsActivity.GUIDE_KEY, mGuide);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.guide_create_step_portal_reorder, container, false);
      mDragListView = (DragSortListView) view.findViewById(R.id.steps_portal_list_reorder);
      mDragListView.setDropListener(onDrop);
      mDragListView.setRemoveListener(onRemove);
      mDragListView.setAdapter(mAdapter);
      mController = buildController(mDragListView);
      mDragListView.setFloatViewManager(mController);
      mDragListView.setOnTouchListener(mController);
      mDragListView.setDragEnabled(true);
      view.setFocusableInTouchMode(true);
      view.requestFocus();
      view.setOnKeyListener(new OnKeyListener() {
         @Override
         public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
               mDiscardChanges = true;
               return false;
            }
            return false;
         }
      });
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
         if (!mDiscardChanges)
         {
            for(int i = 0 ; i < mStepsCopy.size() ; i++)
            {
               mStepsCopy.get(i).setStepNum(i);
            }
            mGuide.setStepList(mStepsCopy);
         }
         getActivity().getSupportFragmentManager().popBackStack();
      }

      @Override
      public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

         return true;
      }
   };

   private class ViewHolder {
      public TextView stepsView;
      public TextView stepNumber;
      public ImageView mImageView;
   }

   private class StepAdapter extends ArrayAdapter<GuideCreateStepObject> {
      public StepAdapter(List<GuideCreateStepObject> list) {
         super(getActivity(), R.layout.guide_create_step_list_item_reorder, R.id.step_title_textview_reorder, list);
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
         View v = super.getView(position, convertView, parent);
         if (v != convertView && v != null) {
            final ViewHolder holder = new ViewHolder();

            TextView tv = (TextView) v.findViewById(R.id.step_title_textview_reorder);
            holder.stepsView = tv;
            holder.stepNumber = (TextView) v.findViewById(R.id.guide_create_step_item_number_reorder);
            holder.mImageView = (ImageView) v.findViewById(R.id.guide_step_item_thumbnail_reorder);
            v.setTag(holder);
         }
         final ViewHolder holder = (ViewHolder) v.getTag();
         String step = getItem(position).getTitle();
         holder.stepsView.setText(step);
         holder.stepNumber.setText("Step " + (mGuide.getSteps().indexOf(mStepsCopy.get(position)) + 1));
         setImageThumb( getItem(position).getImages(), holder.mImageView);
         return v;
      }
   }

   private void setImageThumb(ArrayList<StepImage> imageList, ImageView imagView) {
      for (StepImage imageinfo : imageList) {
         if (imageinfo.getImageid() > 0) {
            imagView.setScaleType(ScaleType.FIT_CENTER);
            mImageManager.displayImage(imageinfo.getText() + MainApplication.get().getImageSizes().getThumb(),
               getActivity(), imagView);
            imagView.setTag(imageinfo.getText() + MainApplication.get().getImageSizes().getThumb());
            imagView.invalidate();
            return;
         }
      }

   }
}
