package com.dozuki.ifixit;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.dozuki.ifixit.dozuki.model.Site;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.util.ImageSizes;
import com.ifixit.android.imagemanager.ImageManager;

public class MainApplication extends Application {
   public static final int SIZE_CUTOFF = 800;
   // The current version of the app (this is replaced by dozukify.sh).
   public static final String CURRENT_SITE = "SITE_ifixit";

   private ImageManager mImageManager;
   private ImageSizes mImageSizes;
   private Site mSite;

   public MainApplication() {
      setSite(getDefaultSite());
   }

   public Site getSite() {
      return mSite;
   }

   public void setSite(Site site) {
      mSite = site;
      APIService.setSite(site);
   }

   /**
    * Returns the resource id for the current site's theme.
    */
   public int getSiteTheme() {
      if (mSite == null) {
         return R.style.Theme_Dozuki;

      // Put custom site themes here.
      } else if (mSite.mName.equals("ifixit")) {
         return R.style.Theme_iFixit;
      } else {
         // We don't have a custom theme for the site - check for generic theme.
         String theme = mSite.mTheme;

         if (theme.equals("custom")) {
            // Site has a custom theme but we don't have one implemented yet.
            return R.style.Theme_Dozuki;
         } else if (theme.equals("green")) {
            return R.style.Theme_Dozuki_Green;
         } else if (theme.equals("blue")) {
            return R.style.Theme_Dozuki_Blue;
         } else if (theme.equals("white")) {
            return R.style.Theme_Dozuki_White;
         } else if (theme.equals("orange")) {
            return R.style.Theme_Dozuki_Orange;
         } else if (theme.equals("black")) {
            return R.style.Theme_Dozuki_Grey;
         }
      }

      return R.style.Theme_Dozuki;
   }

   /**
    * Returns the current site's object name.
    */
   public String getSiteObjectName() {
      if (mSite.mName.equals("ifixit")) {
         return getString(R.string.devices);
      } else {
         return getString(R.string.topics);
      }
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

            public void fail(final ImageView imageView) {
               if (imageView instanceof ImageViewTouch) {
                  Bitmap noImage = BitmapFactory.decodeResource(getResources(),
                   R.drawable.no_image);

                  ((ImageViewTouch)imageView).setImageBitmapReset(noImage, true);
               } else {
                  imageView.setImageResource(R.drawable.no_image);
               }

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

   /**
    * Should only be used to get the current site for a "custom" app
    * (iFixit/Crucial etc.).
    */
   private Site getDefaultSite() {
      String siteName = CURRENT_SITE.replace("SITE_", "");

      return Site.getSite(siteName);
   }
}
