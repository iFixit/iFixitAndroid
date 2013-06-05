package com.dozuki.ifixit.ui.guide.create;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.gallery.MediaInfo;
import com.dozuki.ifixit.ui.gallery.GalleryActivity;
import com.dozuki.ifixit.ui.guide.ThumbnailView;
import com.dozuki.ifixit.util.APIError;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.model.APIImage;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.util.CaptureHelper;
import com.squareup.otto.Subscribe;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class StepEditImageFragment extends Fragment {

   public static final int DEFAULT_IMAGE_ID = -1;
   private static final int GALLERY_REQUEST_CODE = 1;
   private static final int CAMERA_REQUEST_CODE = 1888;
   private static final int COPY_TO_MEDIA_MANAGER = 0;
   private static final int DETACH_TO_MEDIA_MANAGER = 1;
   private static final int DELETE_FROM_STEP = 2;
   private static final String IMAGES_KEY = "IMAGES_KEY";

   private Activity mContext;

   // images
   private ThumbnailView mThumbs;
   private ImageView mLargeImage;
   private ArrayList<APIImage> mImages;
   private String mTempFileName;

   // Position of the temporary image captured on the phone
   private int mTempThumbPosition;
   private String images_key;

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
      View v = inflater.inflate(R.layout.guide_create_step_edit_image, container, false);

      mThumbs = (ThumbnailView) v.findViewById(R.id.edit_thumbnails);
      mLargeImage = (ImageView) v.findViewById(R.id.step_edit_large_image);
      DisplayMetrics metrics = new DisplayMetrics();

      if (MainApplication.get().inPortraitMode()) {
         ((LinearLayout) v).setOrientation(LinearLayout.HORIZONTAL);
      }

      if (savedInstanceState != null) {
         mImages = (ArrayList<APIImage>)savedInstanceState.getSerializable(IMAGES_KEY);
      }

      mContext.getWindowManager().getDefaultDisplay().getMetrics(metrics);
      mThumbs.setDisplayMetrics(metrics);
      mThumbs.setNavigationHeight(navigationHeight());

      mThumbs.setImageSizes(MainApplication.get().getImageSizes());
      mThumbs.setMainImage(mLargeImage);

      // Initialize the step thumbnails and set the main image to the first thumbnail if it exists
      if (mImages != null && mImages.size() > 0) {
         mThumbs.setThumbs(mImages);
         mThumbs.setCurrentThumb(mImages.get(0).mBaseUrl);
      } else {
         mThumbs.fitToSpace();
      }

      return v;
   }

   @Override
   public void onResume() {
      super.onResume();
      MainApplication.getBus().register(this);
   }

   @Override
   public void onPause() {
      super.onPause();
      MainApplication.getBus().unregister(this);
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
                      case 0:
                         try {
                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                            // Create an image file name
                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            String imageFileName = CaptureHelper.IMAGE_PREFIX + timeStamp + "_";

                            File file = File.createTempFile(imageFileName, ".jpg", CaptureHelper.getAlbumDir());
                            mTempFileName = file.getAbsolutePath();

                            cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                            mContext.startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                         } catch (IOException e) {
                            e.printStackTrace();
                         }

                         break;
                      case 1:
                         intent = new Intent(mContext, GalleryActivity.class);
                         intent.putExtra(GalleryActivity.ACTIVITY_RETURN_MODE, 1);
                         mContext.startActivityForResult(intent, GALLERY_REQUEST_CODE);
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
                   Intent intent = null;
                   APIImage thumbImage = (APIImage) v.getTag();

                   switch (which) {
                      case COPY_TO_MEDIA_MANAGER:
                         APIService.call((Activity) getActivity(),
                          APIService.getCopyImageAPICall(Integer.toString(thumbImage.mId)));
                         break;
                      case DETACH_TO_MEDIA_MANAGER:
                         APIService.call((Activity) getActivity(),
                          APIService.getCopyImageAPICall(Integer.toString(thumbImage.mId)));
                      case DELETE_FROM_STEP:
                         mThumbs.removeThumb((ImageView) v);
                         mImages.remove(thumbImage);
                         setGuideDirty();
                         break;

                      default:
                         return;
                   }
                }
             });
            builder.create().show();

            return true;
         }
      });
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      APIImage newThumb;
      Log.w("onActivityResult", Integer.toString(requestCode));

      switch (requestCode) {
         case GALLERY_REQUEST_CODE:
            if (data != null) {
               MediaInfo media = (MediaInfo) data.getSerializableExtra(GalleryActivity.MEDIA_RETURN_KEY);
               newThumb = new APIImage(Integer.parseInt(media.getItemId()), media.getGuid());
               mImages.add(newThumb);
               mThumbs.addThumb(newThumb, false);
               setGuideDirty();
            } else {
               Log.e("StepEditImageFragment", "Error cameraTempFile is null!");
               return;
            }

            break;
         case CAMERA_REQUEST_CODE:
            if (resultCode == Activity.RESULT_OK) {

               if (mTempFileName == null) {
                  Log.e("StepEditImageFragment", "Error cameraTempFile is null!");
                  return;
               }
               // Prevent a save from being called until the image uploads and returns with the imageid
               ((StepEditActivity) getActivity()).lockSave();

               newThumb = new APIImage(DEFAULT_IMAGE_ID, mTempFileName);

               mImages.add(newThumb);
               mTempThumbPosition = mThumbs.addThumb(newThumb, true);

               APIService.call((Activity) getActivity(), APIService.getUploadImageToStepAPICall(mTempFileName));

            }
            break;
      }


      super.onActivityResult(requestCode, resultCode, data);

   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      savedInstanceState.putSerializable(IMAGES_KEY, mImages);

      super.onSaveInstanceState(savedInstanceState);
   }

   /////////////////////////////////////////////////////
   // NOTIFICATION LISTENERS
   /////////////////////////////////////////////////////

   @Subscribe
   public void onUploadStepImage(APIEvent.UploadStepImage event) {
      if (!event.hasError()) {
         APIImage newThumb = event.getResult();

         // Find the temporarily stored image object to update the filename to the image path and
         // imageid
         if (newThumb != null) {
            for (int i = 0; i < mImages.size(); i++) {
               if (mImages.get(i).mId == DEFAULT_IMAGE_ID) {
                  mImages.set(i, newThumb);
                  //mThumbs.updateThumb(newThumb, mTempThumbPosition);
               }
            }
         }
         ((StepEditActivity) getActivity()).unlockSave();

         // Set guide dirty after the image is uploaded so the user can't save the guide before we have the imageid
         setGuideDirty();
      } else {
         Log.e("Upload Image Error", event.getError().mMessage);
         event.setError(APIError.getFatalError(getActivity()));
         APIService.getErrorDialog(getActivity(), event.getError(), null).show();
      }
   }


   /////////////////////////////////////////////////////
   // HELPERS
   /////////////////////////////////////////////////////

   protected void setImages(ArrayList<APIImage> images) {

      mImages = new ArrayList<APIImage>(images);
   }

   protected ArrayList<APIImage> getImages() {
      return mImages;
   }

   protected float navigationHeight() {
      int actionBarHeight = getResources().getDimensionPixelSize(
       com.actionbarsherlock.R.dimen.abs__action_bar_default_height);

      int bottomBarHeight = getResources().getDimensionPixelSize(
       R.dimen.guide_create_step_edit_bottom_bar_height);

      int stepPagerBar = getActivity().getResources().getDimensionPixelSize(R.dimen.step_pager_bar_height);

      return actionBarHeight + bottomBarHeight + stepPagerBar;
   }

   private void setGuideDirty() {
      MainApplication.getBus().post(new StepChangedEvent());
   }
}