package com.dozuki.ifixit.ui.guide.view;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.ui.BaseFragment;
import com.dozuki.ifixit.ui.guide.ThumbnailView;

import java.util.ArrayList;

public class StepImageFragment extends BaseFragment {
   private static final String IMAGES_KEY = "IMAGES_KEY";

   // Step Images
   private ThumbnailView mThumbs;
   private ArrayList<Image> mImages;

   public StepImageFragment() {}

   public StepImageFragment(ArrayList<Image> images) {
      mImages = new ArrayList<Image>(images);
   }

   /////////////////////////////////////////////////////
   // LIFECYCLE
   /////////////////////////////////////////////////////

   @Override
   @SuppressWarnings("unchecked")
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      // Inflate the layout for this fragment
      View v = inflater.inflate(R.layout.guide_step_image, container, false);

      mThumbs = (ThumbnailView) v.findViewById(R.id.thumbnail_viewer);
      DisplayMetrics metrics = new DisplayMetrics();

      if (App.get().inPortraitMode()) {
         ((LinearLayout) v).setOrientation(LinearLayout.HORIZONTAL);
      }

      if (savedInstanceState != null) {
         mImages = (ArrayList<Image>) savedInstanceState.getSerializable(IMAGES_KEY);
      }

      getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
      mThumbs.setDisplayMetrics(metrics);
      mThumbs.setNavigationHeight(navigationHeight());

      // Initialize the step thumbnails and set the main image to the first thumbnail if it exists
      if (mImages != null && mImages.size() > 0) {
         mThumbs.setThumbs(mImages);
      } else {
         mThumbs.setDefaultMainImage();
         mThumbs.fitToSpace();
      }

      return v;
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);

      savedInstanceState.putSerializable(IMAGES_KEY, mImages);
   }

   @Override
   public void onDestroy() {
      super.onDestroy();

      mThumbs.destroy();
   }

   /////////////////////////////////////////////////////
   // HELPERS
   /////////////////////////////////////////////////////

   protected float navigationHeight() {
      int actionBarHeight = getResources().getDimensionPixelSize(
       com.actionbarsherlock.R.dimen.abs__action_bar_default_height);

      int stepPagerBar = getActivity().getResources().getDimensionPixelSize(R.dimen.step_pager_bar_height);

      return actionBarHeight + stepPagerBar;
   }
}
