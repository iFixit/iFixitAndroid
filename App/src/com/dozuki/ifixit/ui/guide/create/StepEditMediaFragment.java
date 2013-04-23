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
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.gallery.MediaInfo;
import com.dozuki.ifixit.ui.gallery.GalleryActivity;
import com.dozuki.ifixit.ui.guide.view.ThumbnailView;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIImage;
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

public class StepEditMediaFragment extends Fragment {

   private static final int DEFAULT_IMAGE_ID = -1;
   private static final int GALLERY_REQUEST_CODE = 1;
   private static final int CAMERA_REQUEST_CODE = 1888;

   private Activity mContext;

   // images
   private ThumbnailView mThumbs;
   private ImageView mLargeImage;
   private ArrayList<APIImage> mImages;
   private String mTempFileName;

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
      View v = inflater.inflate(R.layout.guide_create_step_edit_media, container, false);

      mThumbs = (ThumbnailView) v.findViewById(R.id.edit_thumbnails);
      mLargeImage = (ImageView) v.findViewById(R.id.step_edit_large_image);
      DisplayMetrics metrics = new DisplayMetrics();

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

      mThumbs.setThumbsOnLongClickListener(new OnLongClickListener() {
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
                         //intent = new Intent(getActivity(), GalleryActivity.class);
                         //intent.putExtra(GalleryActivity.ACTIVITY_RETURN_MODE, 1);

                         break;
                      case 1:
                         //intent = new Intent(getActivity(), GalleryActivity.class);
                         //intent.putExtra(GalleryActivity.ACTIVITY_RETURN_MODE, 1);
                         break;
                      default:
                         return;

                   }
                   //getActivity().startActivityForResult(intent, imageKey);
                }
             });

            return true;
         }
      });

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

      StepsEditActivity activity = (StepsEditActivity) getActivity();

      mThumbs.setAddThumbButtonOnClick(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

            builder.setTitle("Attach media from")
             .setItems(R.array.step_image_actions, new DialogInterface.OnClickListener() {
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
                            Log.w("StepEditMediaFragment", "Filename = " + mTempFileName);


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
            builder.show();
         }
      });
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      APIImage newThumb;
      Log.w("onActivityResult", Integer.toString(requestCode));

      switch (requestCode) {
         case GALLERY_REQUEST_CODE:
            MediaInfo media = (MediaInfo) data.getSerializableExtra(GalleryActivity.MEDIA_RETURN_KEY);
            newThumb = new APIImage(Integer.parseInt(media.getItemId()), media.getGuid());
            mImages.add(newThumb);
            mThumbs.addThumb(newThumb, false);

            break;
         case CAMERA_REQUEST_CODE:

            Log.w("StepEditMediaFragment", "Camera returned");

            if (mTempFileName == null) {
               Log.w("iFixit", "Error cameraTempFile is null!");
               return;
            }

            newThumb = new APIImage(DEFAULT_IMAGE_ID, mTempFileName);

            Log.w("StepEditMediaFragment", "Image Path" + newThumb.mBaseUrl);
            mImages.add(newThumb);
            mThumbs.addThumb(newThumb, true);

            APIService.call((Activity) getActivity(), APIService.getUploadImageToStepAPICall(mTempFileName));

            break;
      }

      setGuideDirty();

      super.onActivityResult(requestCode, resultCode, data);

   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
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
                  Log.w("StepEditMediaFragment", "Step Image Uploaded: " + mImages.get(i).mBaseUrl);
               }
            }
         }
      } else {
         Log.w("Upload Image Error", event.getError().mMessage);
         // TODO
      }
   }


   /////////////////////////////////////////////////////
   // HELPERS
   /////////////////////////////////////////////////////

   protected void setImages(ArrayList<APIImage> images) {

      mImages = new ArrayList<APIImage>(images);
   }

   protected ArrayList<APIImage> getImages() {
      Log.w("StepEditMediaFragment", "Step images count: " + mImages.size());

      return mImages;
   }

   protected float navigationHeight() {
      int actionBarHeight = getResources().getDimensionPixelSize(
       com.actionbarsherlock.R.dimen.abs__action_bar_default_height);

      int bottomBarHeight = getResources().getDimensionPixelSize(
       R.dimen.guide_create_step_edit_bottom_bar_height);

      int stepPagerBar = getResources().getDimensionPixelSize(
       com.viewpagerindicator.R.dimen.default_title_indicator_footer_indicator_height);

      return actionBarHeight + bottomBarHeight + stepPagerBar;
   }

   protected void setGuideDirty() {
      if (((StepChangedListener) getActivity()) == null) {
         return;
      }

      ((StepChangedListener) getActivity()).onStepChanged();
   }

}