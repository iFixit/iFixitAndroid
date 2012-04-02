package com.dozuki.ifixit;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ImageView;

import com.ifixit.android.imagemanager.ImageManager;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class MainApplication extends Application {
   public static final int SIZE_CUTOFF = 800;

   private ImageManager mImageManager;
   private ImageSizes mImageSizes;

   public ImageManager getImageManager() {
      if (mImageManager == null) {
         mImageManager = new ImageManager(this);

         mImageManager.setController(new ImageManager.Controller() {
            public boolean overrideDisplay(String url, ImageView imageView) {
               return false;
            }

            public void loading(ImageView imageView) {
            }

            public boolean displayImage(ImageView imageView, Bitmap bitmap,
             String url) {
               if (imageView instanceof ImageViewTouch) {
                  ((ImageViewTouch)imageView).setImageBitmapReset(bitmap, true);
                  return true;
               }

               return false;
            }

            public void fail(ImageView imageView) {
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
