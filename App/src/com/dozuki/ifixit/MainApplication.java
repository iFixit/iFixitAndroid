package com.dozuki.ifixit;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.ifixit.android.imagemanager.ImageManager;

@ReportsCrashes(formKey = "dFRlbjlVamRObWhBLW5Ib3c0QlozdWc6MQ")
public class MainApplication extends Application {
   public static final int SIZE_CUTOFF = 800;

   private ImageManager mImageManager;
   private ImageSizes mImageSizes;

   @Override
   public void onCreate() {
      ACRA.init(this);
      super.onCreate();
   }
   
   private GoogleAnalyticsTracker mTracker;

   public GoogleAnalyticsTracker getAnalyticsTracker() {
      if (mTracker == null) {
         mTracker = GoogleAnalyticsTracker.getInstance();
         mTracker.startNewSession("UA-30506-14", this);
      } 
      
      return mTracker;
   }
   
   public ImageManager getImageManager() {
      if (mImageManager == null) {
         mImageManager = new ImageManager(this);

         mImageManager.setController(new ImageManager.Controller() {
            public boolean overrideDisplay(String url, ImageView imageView) {
               if (url.equals("") || url.indexOf(".") == 0) {
                  fail(imageView);

                  return true;
               }

               return false;
            }

            public void loading(ImageView imageView) {
               imageView.setImageBitmap(null);
            }

            public boolean displayImage(ImageView imageView, Bitmap bitmap,
             String url) {
               if (imageView instanceof ImageViewTouch) {
                  ((ImageViewTouch)imageView).setImageBitmapReset(bitmap, true);
                  ((ImageViewTouch)imageView).setVisibility(View.VISIBLE);
                  return true;
               }

               return false;
            }

            public void fail(ImageView imageView) {
               if (imageView instanceof ImageViewTouch) {
                  Bitmap noImage = BitmapFactory.decodeResource(getResources(),
                   R.drawable.no_image);

                  ((ImageViewTouch)imageView).setImageBitmapReset(noImage, true);
               } else {
                  imageView.setImageResource(R.drawable.no_image);
               }
               
               imageView.getLayoutParams().height = (int)(imageView.getWidth() * (3f/4f) + 0.5f);

               imageView.setTag("");
            }
         });
      }

      return mImageManager;
   }

   public ImageSizes getImageSizes() {
      if (mImageSizes == null) {
         WindowManager wm = (WindowManager)getSystemService(
          Context.WINDOW_SERVICE);
         DisplayMetrics metrics = new DisplayMetrics();
         wm.getDefaultDisplay().getMetrics(metrics);
         int maxDimension = Math.max(metrics.heightPixels,
          metrics.widthPixels);

         // Larger screen = larger images
         if ((maxDimension / metrics.density) > SIZE_CUTOFF) {
            mImageSizes = new ImageSizes(".medium", ".medium", ".large",
             ".standard");
         } else {
            mImageSizes = new ImageSizes(".standard", ".standard", ".large",
             ".thumbnail");
         }
      }

      return mImageSizes;
   }
}
