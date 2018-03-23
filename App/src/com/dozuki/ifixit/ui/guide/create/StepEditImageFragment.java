package com.dozuki.ifixit.ui.guide.create;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.ui.BaseFragment;
import com.dozuki.ifixit.ui.gallery.GalleryActivity;
import com.dozuki.ifixit.ui.guide.ThumbnailView;
import com.dozuki.ifixit.util.CaptureHelper;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiCall;
import com.squareup.otto.Bus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StepEditImageFragment extends BaseFragment {

   private static final int COPY_TO_MEDIA_MANAGER = 0;
   private static final int DETACH_TO_MEDIA_MANAGER = 1;
   private static final int DELETE_FROM_STEP = 2;
   private static final String IMAGES_KEY = "IMAGES_KEY";
   private static final int CAPTURE_IMAGE = 0;
   private static final int MEDIA_MANAGER = 1;
   private static final int REQUEST_TAKE_PHOTO = 2;
   private static final String CAMERA_PATH = "STEP_EDIT_CAMERA_PATH";

   private Activity mContext;

   // images
   private ThumbnailView mThumbs;
   private ArrayList<Image> mImages;
   private String mCurrentPhotoPath;

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

      String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

      if (ContextCompat.checkSelfPermission(getActivity(),
       Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
       ContextCompat.checkSelfPermission(getActivity(),
        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
       ContextCompat.checkSelfPermission(getActivity(),
        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
         mThumbs.setCanEdit(false);
         ActivityCompat.requestPermissions(getActivity(),
          permissions, CaptureHelper.PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
      } else {
         mThumbs.setCanEdit(true);
      }

      DisplayMetrics metrics = new DisplayMetrics();

      if (App.get().inPortraitMode()) {
         ((LinearLayout) v).setOrientation(LinearLayout.HORIZONTAL);
      }

      if (savedInstanceState != null) {
         mImages = (ArrayList<Image>) savedInstanceState.getSerializable(IMAGES_KEY);

         if (savedInstanceState.getString(CAMERA_PATH) != null) {
            mCurrentPhotoPath = savedInstanceState.getString(CAMERA_PATH);
         }
      }

      mContext.getWindowManager().getDefaultDisplay().getMetrics(metrics);
      mThumbs.setDisplayMetrics(metrics);
      mThumbs.setNavigationHeight(navigationHeight());

      // Initialize the step thumbnails and set the main image to the first thumbnail if it exists
      if (mImages != null && mImages.size() > 0) {
         mThumbs.setThumbs(mImages, false);
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

            builder.setTitle(App.get().getString(R.string.step_edit_new_thumb_actions_title))
             .setItems(R.array.new_image_actions, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                   switch (which) {
                      case CAPTURE_IMAGE:
                         try {
                            // Create the File where the photo should go
                            File photoFile = CaptureHelper.createImageFile(getActivity());
                            mCurrentPhotoPath = photoFile.getAbsolutePath();

                            Intent i = CaptureHelper.getCaptureIntent(mContext, photoFile);

                            // Ensure that there's a camera activity to handle the intent
                            if (i.resolveActivity(mContext.getPackageManager()) == null) {
                               Toast.makeText(getActivity(), "We had a problem launching your camera.", Toast.LENGTH_SHORT).show();
                               break;
                            }

                            startActivityForResult(i, CaptureHelper.CAMERA_REQUEST_CODE);

                         } catch (IOException e) {
                            Log.e("StepEditImageFragment", "Launch camera", e);
                            Toast.makeText(getActivity(), "We had a problem launching your camera.", Toast.LENGTH_SHORT).show();
                         }

                         break;
                      case MEDIA_MANAGER:
                         dispatchAttachFromMediaManager();
                         break;
                   }
                }
             });
            builder.create().show();
         }
      });

      mThumbs.setThumbsOnLongClickListener(new View.OnLongClickListener() {
         @Override
         public boolean onLongClick(final View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder
             .setTitle(mContext.getString(R.string.step_edit_existing_image_actions_title))
             .setItems(R.array.existing_image_actions, new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                   Image thumbImage = (Image) v.getTag();

                   switch (which) {
                      case COPY_TO_MEDIA_MANAGER:
                         App.sendEvent("ui_action", "edit_image", "copy_to_media_manager", null);
                         Api.call(getActivity(),
                          ApiCall.copyImage(thumbImage.getId() + ""));
                         break;
                      case DETACH_TO_MEDIA_MANAGER:
                         App.sendEvent("ui_action", "edit_image", "detach_to_media_manager", null);

                         Api.call(getActivity(),
                          ApiCall.copyImage(thumbImage.getId() + ""));
                      case DELETE_FROM_STEP:
                         App.sendEvent("ui_action", "edit_image", "delete_from_step", null);
                         mThumbs.removeThumb(v);
                         mImages.remove(thumbImage);

                         Bus bus = App.getBus();
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
   public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
      switch (requestCode) {
         case CaptureHelper.PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
            //premission to read storage
            if (grantResults.length > 0
             && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               App.sendEvent("ui_action", "add_image", "add_from_camera", null);
               Log.i("ImageFragment", "Permission given");
               mThumbs.setCanEdit(true);
            } else {
               Toast.makeText(getActivity(), "We need permission to capture an image from your camera.", Toast.LENGTH_SHORT).show();
            }
            return;
         }
      }
   }

   private void dispatchAttachFromMediaManager() {
      App.sendEvent("ui_action", "add_image", "add_from_gallery", null);
      Intent intent = new Intent(mContext, GalleryActivity.class);
      intent.putExtra(GalleryActivity.ACTIVITY_RETURN_MODE, 1);
      intent.putExtra(GalleryActivity.ATTACHED_MEDIA_IDS, mImages);
      startActivityForResult(intent, StepEditActivity.GALLERY_REQUEST_CODE);
   }


   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      App.getBus().register(this);

      StepEditActivity activity = (StepEditActivity) getActivity();
      Guide guide = activity.getGuide();
      int pagePosition = activity.getCurrentPosition();
      Image newThumb;

      switch (requestCode) {
         case StepEditActivity.GALLERY_REQUEST_CODE:
            if (data != null) {
               newThumb = (Image) data.getSerializableExtra(GalleryActivity.MEDIA_RETURN_KEY);
               guide.getStep(pagePosition).addImage(newThumb);
               activity.refreshView(pagePosition);
               activity.onGuideChanged(null);
            } else {
               Log.e("StepEditActivity", "Error data is null!");
               return;
            }
            break;
         case CaptureHelper.CAMERA_REQUEST_CODE:
            if (resultCode == Activity.RESULT_OK) {
               if (mCurrentPhotoPath == null) {
                  Log.e("CaptureHelper", "Error mCurrentPhotoPath is null!");
                  return;
               }
               // Prevent a save from being called until the image uploads and returns with the imageid
               activity.lockSave();

               newThumb = new Image();
               newThumb.setLocalPath(mCurrentPhotoPath);

               guide.getStep(pagePosition).addImage(newThumb);
               activity.refreshView(pagePosition);

               Api.call(activity, ApiCall.uploadImageToStep(mCurrentPhotoPath));
            }
            break;
      }
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);

      if (mCurrentPhotoPath != null) {
         savedInstanceState.putString(CAMERA_PATH, mCurrentPhotoPath);
      }
      savedInstanceState.putSerializable(IMAGES_KEY, mImages);
   }

   @Override
   public void onDestroy() {
      super.onDestroy();

      mThumbs.destroy();
   }

   // Returns true if external storage for photos is available
   private boolean isExternalStorageAvailable() {
      String state = Environment.getExternalStorageState();
      return state.equals(Environment.MEDIA_MOUNTED);
   }

   /////////////////////////////////////////////////////
   // HELPERS
   /////////////////////////////////////////////////////

   protected void setImages(ArrayList<Image> images) {
      mImages = images;

      if (mThumbs != null) {
         mThumbs.setThumbs(mImages, false);
      }
   }

   protected float navigationHeight() {
      int actionBarHeight = 48;

      int bottomBarHeight = getResources().getDimensionPixelSize(
       R.dimen.guide_create_step_edit_bottom_bar_height);

      int stepPagerBar = getActivity().getResources().getDimensionPixelSize(R.dimen.step_pager_bar_height);

      return actionBarHeight + bottomBarHeight + stepPagerBar;
   }
}
