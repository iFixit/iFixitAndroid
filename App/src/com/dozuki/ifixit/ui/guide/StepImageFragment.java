package com.dozuki.ifixit.ui.guide;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Image;

import java.util.ArrayList;

public class StepImageFragment extends SherlockFragment {
   private static final String IMAGES_KEY = "IMAGES_KEY";

   private Activity mContext;

   // images
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
   public void onCreate(Bundle savedInstanceState) {
      mContext = (Activity) getActivity();
      super.onCreate(savedInstanceState);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      // Inflate the layout for this fragment
      View v = inflater.inflate(R.layout.guide_step_image, container, false);

      mThumbs = (ThumbnailView) v.findViewById(R.id.thumbnail_viewer);
      DisplayMetrics metrics = new DisplayMetrics();

      if (MainApplication.get().inPortraitMode()) {
         ((LinearLayout) v).setOrientation(LinearLayout.HORIZONTAL);
      }

      if (savedInstanceState != null) {
         mImages = (ArrayList<Image>) savedInstanceState.getSerializable(IMAGES_KEY);
      }

      mContext.getWindowManager().getDefaultDisplay().getMetrics(metrics);
      mThumbs.setDisplayMetrics(metrics);
      mThumbs.setNavigationHeight(navigationHeight());

      // Initialize the step thumbnails and set the main image to the first thumbnail if it exists
      if (mImages != null && mImages.size() > 0) {
         mThumbs.setThumbs(mImages);
      } else {
         mThumbs.fitToSpace();
      }

      return v;
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      savedInstanceState.putSerializable(IMAGES_KEY, mImages);

      super.onSaveInstanceState(savedInstanceState);
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