package com.dozuki.ifixit.ui.guide.create;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.gallery.MediaInfo;
import com.dozuki.ifixit.model.gallery.UploadedImageInfo;
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

   private static final int GALLERY_REQUEST_CODE = 1;
   private static final int CAMERA_REQUEST_CODE = 2;

   private Activity mContext;
   private MainApplication mApp;

   // images
   private ThumbnailView mThumbs;
   private ImageView mLargeImage;
   private ArrayList<APIImage> mImages;
   private String mTempFileName;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      mContext = (Activity) getActivity();

      super.onCreate(savedInstanceState);

      mApp = (MainApplication) mContext.getApplication();
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

      mThumbs.setImageSizes(mApp.getImageSizes());
      mThumbs.setMainImage(mLargeImage);

      // Initialize the step thumbnails and set the main image to the first thumbnail if it exists
      if (mImages != null && mImages.size() > 0) {
         mThumbs.setThumbs(mImages);

         mThumbs.setCurrentThumb(mImages.get(0).mBaseUrl);
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
                         Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                         try {
                            // Create an image file name
                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            String imageFileName = CaptureHelper.IMAGE_PREFIX + timeStamp + "_";

                            File file = File.createTempFile(imageFileName, ".jpg", CaptureHelper.getAlbumDir());
                            mTempFileName = file.getAbsolutePath();

                            cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
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

   @Subscribe
   public void onUploadImage(APIEvent.UploadImage event) {
      if (!event.hasError()) {
         UploadedImageInfo imageInfo = event.getResult();

        /* mTempFileName;
         LocalImage cur = mLocalURL.get(url);
         if (cur == null)
            return;
         cur.mImgid = imageInfo.getImageid();
         mLocalURL.put(url, cur);
         mItemsDownloaded++;
         mGalleryAdapter.invalidatedView();
      */
      } else {
         // TODO
      }
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {

      switch (requestCode) {
         case GALLERY_REQUEST_CODE:
            MediaInfo media = (MediaInfo) data.getSerializableExtra(GalleryActivity.MEDIA_RETURN_KEY);
            APIImage newThumb = new APIImage(Integer.parseInt(media.getItemId()), media.getGuid());
            mImages.add(newThumb);
            mThumbs.addThumb(newThumb);

            setGuideDirty();

            break;
         case CAMERA_REQUEST_CODE:
            if (mTempFileName == null) {
               Log.w("iFixit", "Error cameraTempFile is null!");
               return;
            }
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = 2;
            opt.inDither = true;
            opt.inPreferredConfig = Bitmap.Config.ARGB_8888;

            APIService.call((Activity) getActivity(), APIService.getUploadImageToStepAPICall(mTempFileName));

            break;
         default:
            return;
      }
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

   public ArrayList<APIImage> getImages() {
      return mImages;
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