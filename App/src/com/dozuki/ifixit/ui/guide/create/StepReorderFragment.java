package com.dozuki.ifixit.ui.guide.create;

import android.R.color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.model.APIImage;
import com.marczych.androidimagemanager.ImageManager;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class StepReorderFragment extends Fragment {

   public interface StepRearrangeListener {

      public void onReorderComplete(boolean reOdered);

   }

   private static final String STEP_LIST_ID = "STEP_LIST_ID";

   private DragSortListView mDragListView;
   private DragSortController mController;
   private StepAdapter mAdapter;
   private ImageManager mImageManager;
   private Guide mGuide;
   private ArrayList<GuideStep> mStepsCopy;
   private boolean mReturnVal;

   public void setGuide(Guide guide) {
      mGuide = guide;
      mStepsCopy = new ArrayList<GuideStep>(mGuide.getSteps());
   }

   private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
      @Override
      public void drop(int from, int to) {
         GuideStep item = mAdapter.getItem(from);
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
      controller.setRemoveMode(DragSortController.FLING_REMOVE);
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
         mGuide = (Guide) savedInstanceState.get(StepsActivity.GUIDE_KEY);
         mStepsCopy = (ArrayList<GuideStep>) savedInstanceState.get(STEP_LIST_ID);
      }
      mAdapter = new StepAdapter(mStepsCopy);
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
      savedInstanceState.putSerializable(StepsActivity.GUIDE_KEY, mGuide);
      savedInstanceState.putSerializable(STEP_LIST_ID, mStepsCopy);
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

      return view;
   }

   public final class ContextualStepReorder implements ActionMode.Callback {
      public ContextualStepReorder() {}

      @Override
      public boolean onCreateActionMode(ActionMode mode, Menu menu) {
         MenuInflater inflater = ((Activity) getActivity()).getSupportMenuInflater();
         inflater.inflate(R.menu.contextual_rearrange, menu);
         mode.setTitle(R.string.step_rearrange_title);
         return true;
      }

      @Override
      public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
         return false;
      }

      @Override
      public void onDestroyActionMode(ActionMode mode) {
         getActivity().getSupportFragmentManager().popBackStack();
         ((StepRearrangeListener) getActivity()).onReorderComplete(mReturnVal);
      }

      @Override
      public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
         switch (item.getItemId()) {
            case R.id.cab_action_save:
               for (int i = 0; i < mStepsCopy.size(); i++) {
                  mStepsCopy.get(i).setStepNum(i);
               }
               mGuide.setStepList(mStepsCopy);
               mReturnVal = true;
               mode.finish(); // Action picked, so close the CAB
               return true;
            case R.id.cab_action_cancel:
               mode.finish();
               return true;
            default:
               mReturnVal = false;
               mode.finish();
               return true;
         }
      }
   };

   private class ViewHolder {
      public TextView stepsView;
      public TextView stepNumber;
      public ImageView mImageView;
   }

   private class StepAdapter extends ArrayAdapter<GuideStep> {
      public StepAdapter(List<GuideStep> list) {
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
         if (step.equals("")) {
            holder.stepsView.setText("Step " + (mGuide.getSteps().indexOf(mStepsCopy.get(position)) + 1));
            holder.stepNumber.setVisibility(View.GONE);
         } else {
            holder.stepsView.setText(step);
            holder.stepNumber.setText("Step " + (mGuide.getSteps().indexOf(mStepsCopy.get(position)) + 1));
            holder.stepNumber.setVisibility(View.VISIBLE);
         }
         holder.mImageView.setTag("");
         setImageThumb(getItem(position).getImages(), holder.mImageView);
         holder.mImageView.invalidate();

         return v;
      }
   }

   private void setImageThumb(ArrayList<APIImage> imageList, ImageView image) {
      boolean img = false;
      for (APIImage imageInfo : imageList) {
         if (imageInfo.mId > 0) {
            mImageManager.displayImage(imageInfo.getSize(".thumbnail"),
             getActivity(), image);
            image.setTag(imageInfo.getSize(MainApplication.get().getImageSizes().getThumb()));
            image.invalidate();
            return;
         }
      }

      if (!img) {
         mImageManager.displayImage("", getActivity(), image);
         image.invalidate();
      }

   }

}
