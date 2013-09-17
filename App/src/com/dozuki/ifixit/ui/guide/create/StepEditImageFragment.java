package com.dozuki.ifixit.ui.guide.create;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.ui.BaseFragment;
import com.dozuki.ifixit.ui.gallery.GalleryActivity;
import com.dozuki.ifixit.ui.guide.ThumbnailView;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.util.CaptureHelper;
import com.google.analytics.tracking.android.MapBuilder;
import com.squareup.otto.Bus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class StepEditImageFragment extends BaseFragment {

   private static final int COPY_TO_MEDIA_MANAGER = 0;
   private static final int DETACH_TO_MEDIA_MANAGER = 1;
   private static final int DELETE_FROM_STEP = 2;
   private static final String IMAGES_KEY = "IMAGES_KEY";
   private static final int CAPTURE_IMAGE = 0;
   private static final int MEDIA_MANAGER = 1;

   private Activity mContext;

   // images
   private ThumbnailView mThumbs;
   private ArrayList<Image> mImages;

   /////////////////////////////////////////////////////
   // LIFECYCLE
   /////////////////////////////////////////////////////

   @Override
   public void onCreate(Bundle savedInstanceState) {
      mContext = getActivity();
      super.onCreate(savedInstanceState);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      // Inflate the layout for this fragment
      View v = inflater.inflate(R.layout.guide_create_step_edit_image, container, false);

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
         mThumbs.setAddImageMain();

         mThumbs.fitToSpace();
      }

      return v;
   }

   @Override
   public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);

      mThumbs.setAddThumbButtonOnClick(new View.OnClickListener() {
         @Override
         public void onClick(View v) {

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

            builder.setTitle(MainApplication.get().getString(R.string.step_edit_new_thumb_actions_title))
             .setItems(R.array.new_image_actions, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                   Intent intent;
                   switch (which) {
                      case CAPTURE_IMAGE:
                         MainApplication.getGaTracker().send(MapBuilder.createEvent("ui_action", "add_image",
                          "add_from_camera", null).build());
                         try {
                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                            String imageFileName = CaptureHelper.getFileName();

                            File file = File.createTempFile(imageFileName, ".jpg", CaptureHelper.getAlbumDir());
                            String tempFileName = file.getAbsolutePath();

                            if (isAdded()) {
                               SharedPreferences prefs = getActivity().getSharedPreferences(
                                "com.dozuki.ifixit", Context.MODE_PRIVATE);
                               prefs.edit().putString(StepEditActivity.TEMP_FILE_NAME_KEY, tempFileName).commit();
                            }

                            cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                            mContext.startActivityForResult(cameraIntent, StepEditActivity.CAMERA_REQUEST_CODE);
                         } catch (IOException e) {
                            Log.e("StepEditImageFragment", "Capture image", e);
                         }

                         break;
                      case MEDIA_MANAGER:
                         MainApplication.getGaTracker().send(MapBuilder.createEvent("ui_action", "add_image",
                          "add_from_gallery", null).build());
                         intent = new Intent(mContext, GalleryActivity.class);
                         intent.putExtra(GalleryActivity.ACTIVITY_RETURN_MODE, 1);
                         intent.putExtra(GalleryActivity.ATTACHED_MEDIA_IDS, mImages);
                         mContext.startActivityForResult(intent, StepEditActivity.GALLERY_REQUEST_CODE);
                         break;
                   }
                }
             });
            builder.create().show();
         }
      });

      mThumbs.setThumbsOnLongClickListener(new OnLongClickListener() {
         @Override
         public boolean onLongClick(final View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder
             .setTitle(mContext.getString(R.string.step_edit_existing_image_actions_title))
             .setItems(R.array.existing_image_actions, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                   Image thumbImage = (Image) v.getTag();

                   switch (which) {
                      case COPY_TO_MEDIA_MANAGER:
                         MainApplication.getGaTracker().send(MapBuilder.createEvent("ui_action", "edit_image",
                          "copy_to_media_manager",
                          null).build());
                         APIService.call(getActivity(),
                          APIService.getCopyImageAPICall(thumbImage.getId() + ""));
                         break;
                      case DETACH_TO_MEDIA_MANAGER:
                         MainApplication.getGaTracker().send(MapBuilder.createEvent("ui_action", "edit_image",
                          "detach_to_media_manager",
                          null).build());

                         APIService.call(getActivity(),
                          APIService.getCopyImageAPICall(thumbImage.getId() + ""));
                      case DELETE_FROM_STEP:
                         MainApplication.getGaTracker().send(MapBuilder.createEvent("ui_action", "edit_image",
                          "delete_from_step", null).build());
                         mThumbs.removeThumb(v);
                         mImages.remove(thumbImage);

                         Bus bus = MainApplication.getBus();
                         bus.post(new StepImageDeleteEvent(thumbImage));
                         bus.post(new StepChangedEvent());

                         break;
                   }
                }
             });
            builder.create().show();

            return true;
         }
      });
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

   protected void setImages(ArrayList<Image> images) {
      mImages = new ArrayList<Image>(images);

      if (mThumbs != null) {
         mThumbs.setThumbs(mImages);
      }
   }

   protected float navigationHeight() {
      int actionBarHeight = getResources().getDimensionPixelSize(
       com.actionbarsherlock.R.dimen.abs__action_bar_default_height);

      int bottomBarHeight = getResources().getDimensionPixelSize(
       R.dimen.guide_create_step_edit_bottom_bar_height);

      int stepPagerBar = getActivity().getResources().getDimensionPixelSize(R.dimen.step_pager_bar_height);

      return actionBarHeight + bottomBarHeight + stepPagerBar;
   }
}
