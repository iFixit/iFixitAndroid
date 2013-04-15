package com.dozuki.ifixit.ui.guide.create;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.ui.guide.view.ThumbnailView;
import com.dozuki.ifixit.util.APIImage;
import com.marczych.androidimagemanager.ImageManager;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;

import java.util.ArrayList;

public class StepEditMediaFragment extends Fragment {

   private Activity mContext;
   private MainApplication mApp;
   private Resources mResources;

   // images
   private ThumbnailView mThumbs;
   private ImageManager mImageManager;
   private ImageView mLargeImage;
   private ArrayList<APIImage> mImages;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      mContext = (Activity) getActivity();

      super.onCreate(savedInstanceState);

      mApp = (MainApplication) mContext.getApplication();
      mImageManager = mApp.getImageManager();
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      // Inflate the layout for this fragment
      View v = inflater.inflate(R.layout.guide_create_step_edit_media, container, false);

      mThumbs = (ThumbnailView) v.findViewById(R.id.edit_thumbnails);
      mLargeImage = (ImageView) v.findViewById(R.id.step_edit_large_image);

      mThumbs.setImageSizes(mApp.getImageSizes());
      mThumbs.setMainImage(mLargeImage);

      // Initialize the step thumbnails and set the main image to the first thumbnail if it exists
      if (mImages != null && mImages.size() > 0) {
         mThumbs.setThumbs(mImages);

         mThumbs.setCurrentThumb(mImages.get(0).mBaseUrl);
      }

      mThumbs.setThumbsOnLongClickListener(
       new OnLongClickListener() {
          @Override
          public boolean onLongClick(View v) {
             AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

             builder.setMessage(R.string.guide_create_step_media_add_dialog_title)
              .setTitle(R.string.guide_create_step_media_add_dialog_title)
              .setItems(R.array.step_image_actions, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                    Intent intent = null;

                    switch (which) {
                       case 0:
                          intent = new Intent(getActivity(), GalleryActivity.class);
                          intent.putExtra(GalleryActivity.ACTIVITY_RETURN_MODE, 1);

                          break;
                       case 1:
                          intent = new Intent(getActivity(), GalleryActivity.class);
                          intent.putExtra(GalleryActivity.ACTIVITY_RETURN_MODE, 1);

                          break;
                       default:
                          return;

                    }
                    //getActivity().startActivityForResult(intent, imageKey);
                 }
              });

             return true;
          }
       }
      );

      DisplayMetrics metrics = new DisplayMetrics();
      mContext.getWindowManager().getDefaultDisplay().getMetrics(metrics);

      mThumbs.fitToSpace(metrics, navigationHeight());

      return v;
   }

   private float navigationHeight() {
      int actionBarHeight = getResources().getDimensionPixelSize(
       com.actionbarsherlock.R.dimen.abs__action_bar_default_height);

      int bottomBarHeight = getResources().getDimensionPixelSize(
       R.dimen.guide_create_step_edit_bottom_bar_height);

      int stepPagerBar = getResources().getDimensionPixelSize(
       com.viewpagerindicator.R.dimen.default_title_indicator_footer_indicator_height);

      return actionBarHeight + bottomBarHeight + stepPagerBar + 75f;
   }

   public void setImages(ArrayList<APIImage> images) {
      mImages = images;
   }
   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
   }

   public void removeImage() {
      setGuideDirty();
   }

   public void setGuideDirty() {
      if (((StepChangedListener) getActivity()) == null) {
         return;
      }

      ((StepChangedListener) getActivity()).onStepChanged();
   }

}