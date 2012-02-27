package com.ifixit.android.ifixit;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

public class MainApplication extends Application {
   public static final int SIZE_CUTOFF = 800;

   private ImageManager mImageManager;
   private ImageSizes mImageSizes;
	
	public ImageManager getImageManager() {
		if (mImageManager == null)
		   mImageManager = new ImageManager(this);
		
		return mImageManager;
	}

   public ImageSizes getImageSizes() {
      if (mImageSizes == null) {
         WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
         DisplayMetrics metrics = new DisplayMetrics();
         wm.getDefaultDisplay().getMetrics(metrics);
         int maxDimension = Math.max(metrics.heightPixels, metrics.widthPixels);

         // Left in for testing purposes
         Log.w("iFixit", "density: " + metrics.density);
         Log.w("iFixit", "densityDpi: " + metrics.densityDpi);
         Log.w("iFixit", "heightPixels: " + metrics.heightPixels);
         Log.w("iFixit", "scaledDensity: " + metrics.scaledDensity);
         Log.w("iFixit", "widthPixels: " + metrics.widthPixels);
         Log.w("iFixit", "xdpi: " + metrics.xdpi);
         Log.w("iFixit", "ydpi: " + metrics.ydpi);

         Log.w("iFixit", "dim/res: " + (maxDimension / metrics.density));

         // Larger screen = larger images
         if ((maxDimension / metrics.density) > 800) {
            Log.w("iFixit", "Large images");
            mImageSizes = new ImageSizes(".large", ".large", ".huge");
         } else {
            Log.w("iFixit", "Small images");
            mImageSizes = new ImageSizes(".medium", ".medium", ".large");
         }
      }

      return mImageSizes;
   }
}
