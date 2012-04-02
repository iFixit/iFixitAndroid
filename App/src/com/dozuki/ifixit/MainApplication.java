package com.dozuki.ifixit;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.ifixit.android.imagemanager.ImageManager;

public class MainApplication extends Application {
   public static final int SIZE_CUTOFF = 800;

   private ImageManager mImageManager;
   private ImageSizes mImageSizes;

   public ImageManager getImageManager() {
      if (mImageManager == null) {
         mImageManager = new ImageManager(this);
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
         if ((maxDimension / metrics.density) > 800) {
            mImageSizes = new ImageSizes(".large", ".large", ".large",
             ".standard");
         } else {
            mImageSizes = new ImageSizes(".medium", ".medium", ".medium",
             ".thumbnail");
         }
      }

      return mImageSizes;
   }
}
