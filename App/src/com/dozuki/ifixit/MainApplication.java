package com.dozuki.ifixit;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.dozuki.ifixit.dozuki.model.Site;
import com.dozuki.ifixit.login.model.LoginEvent;
import com.dozuki.ifixit.login.model.User;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.util.ImageSizes;
import com.marczych.androidimagemanager.ImageManager;
import com.squareup.otto.Bus;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class MainApplication extends Application {
   public static final int SIZE_CUTOFF = 800;
   // The current version of the app (this is replaced by dozukify.sh).
   public static final String CURRENT_SITE = "SITE_ifixit";

   public static final String PREFERENCE_FILE = "PREFERENCE_FILE";
   private static final String FIRST_TIME_GALLERY_USER =
    "FIRST_TIME_GALLERY_USER";
   private static final String SESSION_KEY = "SESSION_KEY";
   private static final String USERNAME_KEY = "USERNAME_KEY";

   /**
    * Singleton reference.
    */
   private static MainApplication sMainApplication;

   /**
    * Singleton for Bus (Otto).
    */
   private static Bus sBus;

   /**
    * Singleton for ImageManager.
    */
   private ImageManager mImageManager;

   /**
    * Singleton for ImageSizes.
    */
   private ImageSizes mImageSizes;

   /**
    * Currently logged in user or null if user is not logged in.
    */
   private User mUser;

   /**
    * Current site. Shouldn't ever be null. Set to "dozuki" for dozuki splash screen.
    */
   private Site mSite;

   /**
    * True if the user is in the middle of authenticating. Used to determine whether or
    * not to open a new login dialog and for finishing Activities that require the user
    * to be logged in.
    */
   private boolean mIsLoggingIn = false;

   @Override
   public void onCreate() {
      super.onCreate();

      sMainApplication = this;
      setSite(getDefaultSite());
   }

   /**
    * Singleton getter.
    */
   public static MainApplication get() {
      return sMainApplication;
   }

   public Site getSite() {
      return mSite;
   }

   public void setSite(Site site) {
      mSite = site;
      APIService.setSite(site);

      // Update logged in user based on current site.
      mUser = getUserFromPreferenceFile(site);
   }

   /**
    * Returns the site title that should be displayed in the ActionBar.
    * Returns an empty string if this isn't the Dozuki app. This is
    * because the custom app will have a nice logo and shouldn't have a text
    * title displayed.
    */
   public String getSiteDisplayTitle() {
      if (CURRENT_SITE.equals("SITE_dozuki")) {
         return getSite().mTitle;
      } else {
         return "";
      }
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

   public void setIsLoggingIn(boolean isLoggingIn) {
      mIsLoggingIn = isLoggingIn;
   }

   public boolean isLoggingIn() {
      return mIsLoggingIn;
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

   public static Bus getBus() {
      if (sBus == null) {
         sBus = new Bus();
      }

      return sBus;
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

   public User getUser() {
      return mUser;
   }

   private User getUserFromPreferenceFile(Site site) {
      SharedPreferences preferenceFile = getSharedPreferences(
       PREFERENCE_FILE, MODE_PRIVATE);
      String session = preferenceFile.getString(site.mName + SESSION_KEY,
       null);
      String username = preferenceFile.getString(site.mName + USERNAME_KEY,
       null);
      User user = null;
      if (username != null && session != null) {
         user = new User();
         user.setSession(session);
         user.setUsername(username);
      }

      return user;
   }

   public boolean isFirstTimeGalleryUser() {
      SharedPreferences preferenceFile = getSharedPreferences(PREFERENCE_FILE,
       MODE_PRIVATE);

      return preferenceFile.getBoolean(FIRST_TIME_GALLERY_USER, true);
   }

   public void setFirstTimeGalleryUser(boolean firstTimeGalleryUser) {
      SharedPreferences preferenceFile = getSharedPreferences(PREFERENCE_FILE,
       MODE_PRIVATE);
      Editor editor = preferenceFile.edit();
      editor.putBoolean(FIRST_TIME_GALLERY_USER, firstTimeGalleryUser);
      editor.commit();
   }

   public boolean isUserLoggedIn() {
      return mUser != null;
   }

   /**
    * Should only be used to get the current site for a "custom" app
    * (iFixit/Crucial etc.).
    */
   private Site getDefaultSite() {
      String siteName = CURRENT_SITE.replace("SITE_", "");

      return Site.getSite(siteName);
   }

   /**
    * Logs the given user in by writing it to SharedPreferences and setting mUser.
    */
   public void login(User user) {
      final SharedPreferences prefs = getSharedPreferences(PREFERENCE_FILE,
       Context.MODE_PRIVATE);
      Editor editor = prefs.edit();
      editor.putString(mSite.mName + SESSION_KEY, user.getSession());
      editor.putString(mSite.mName + USERNAME_KEY, user.getUsername());
      editor.commit();
      mUser = user;

      getBus().post(new LoginEvent.Login(mUser));

      setIsLoggingIn(false);

      /**
       * Execute pending API call if one exists.
       */
      Intent pendingApiCall = APIService.getAndRemovePendingApiCall();
      if (pendingApiCall != null) {
         startService(pendingApiCall);
      }
   }

   /**
    * Logs the currently logged in user out by deleting it from SharedPreferences and
    * resetting mUser.
    */
   public void logout() {
      final SharedPreferences prefs = getSharedPreferences(PREFERENCE_FILE,
       Context.MODE_PRIVATE);
      Editor editor = prefs.edit();
      editor.remove(mSite.mName + SESSION_KEY);
      editor.remove(mSite.mName + USERNAME_KEY);
      editor.commit();

      mUser = null;

      getBus().post(new LoginEvent.Logout());
   }

   /**
    * Call when the user has cancelled login.
    */
   public void cancelLogin() {
      // Clear the pending api call if one exists.
      APIService.getAndRemovePendingApiCall();
      setIsLoggingIn(false);

      getBus().post(new LoginEvent.Cancel());
   }
}
