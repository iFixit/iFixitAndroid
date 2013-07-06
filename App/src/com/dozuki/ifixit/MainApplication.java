package com.dozuki.ifixit;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.user.LoginEvent;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.util.ImageSizes;
import com.squareup.otto.Bus;

public class MainApplication extends Application {
   public static final int LARGE_SIZE_CUTOFF = 1000;
   public static final int MEDIUM_SIZE_CUTOFF = 800;
   // The current version of the app (this is replaced by dozukify.sh).
   public static final String CURRENT_SITE = "SITE_ifixit";

   public static final String PREFERENCE_FILE = "PREFERENCE_FILE";
   private static final String FIRST_TIME_GALLERY_USER =
    "FIRST_TIME_GALLERY_USER";
   private static final String AUTH_TOKEN_KEY = "AUTH_TOKEN_KEY";
   private static final String USERNAME_KEY = "USERNAME_KEY";
   private static final String USERID_KEY = "USERID_KEY";

   /**
    * Singleton reference.
    */
   private static MainApplication sMainApplication;

   /**
    * Singleton for Bus (Otto).
    */
   private static Bus sBus;

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

   /**
    * User agent singleton.
    */
   private String mUserAgent = null;

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

      // Update logged in user based on current site.
      mUser = getUserFromPreferenceFile(site);
   }

   public String getTopicName() {
      String topicName = getString(R.string.topic);

      if (mSite.mName.equals("ifixit")) {
         topicName = getString(R.string.device);
      }

      return topicName;
   }

   public boolean inPortraitMode() {
      return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
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
      }
      return mSite.theme();
   }

   public String getUserAgent() {
      if (mUserAgent == null) {
         int versionCode = -1;

         try {
            PackageInfo packageInfo = null;
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionCode = packageInfo.versionCode;
         } catch (PackageManager.NameNotFoundException e) {
            Log.e("iFixit", "Can't get application version", e);
         }

         /**
          * Returns the Site that this app is "built" for. e.g. Dozuki even if the user
          * is currently viewing a different nanosite.
          */
         Site currentApp = getDefaultSite();
         mUserAgent = currentApp.mTitle + "Android/" + versionCode;
      }

      return mUserAgent;
   }

   public void setIsLoggingIn(boolean isLoggingIn) {
      mIsLoggingIn = isLoggingIn;
   }

   public boolean isLoggingIn() {
      return mIsLoggingIn;
   }

   // Returns true if the app is in debug mode (not in production)
   public static boolean inDebug() {
      return (0 != (get().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
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

         float screenSize = (maxDimension / metrics.density);
         
         // Larger screen = larger images
         if (screenSize > LARGE_SIZE_CUTOFF) {
            mImageSizes = new ImageSizes(".medium", ".medium", ".huge",
             ".standard");
         } else if (screenSize > MEDIUM_SIZE_CUTOFF) {
            mImageSizes = new ImageSizes(".standard", ".standard", ".large",
             ".standard");
         } else {
            mImageSizes = new ImageSizes(".thumbnail", ".standard", ".large",
             ".thumbnail");
         }
      }

      return mImageSizes;
   }

   public User getUser() {
      return mUser;
   }

   private User getUserFromPreferenceFile(Site site) {
      SharedPreferences preferenceFile = getSharedPreferences(PREFERENCE_FILE,
       MODE_PRIVATE);
      String authToken = preferenceFile.getString(site.mName + AUTH_TOKEN_KEY, null);
      String username = preferenceFile.getString(site.mName + USERNAME_KEY, null);
      int userid = preferenceFile.getInt(site.mName + USERID_KEY, 0);
      User user = null;

      if (username != null && authToken != null) {
         user = new User();
         user.setAuthToken(authToken);
         user.setUsername(username);
         user.setUserid(userid);
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
      editor.putString(mSite.mName + AUTH_TOKEN_KEY, user.getAuthToken());
      editor.putString(mSite.mName + USERNAME_KEY, user.getUsername());
      editor.putInt(mSite.mName + USERID_KEY, user.getUserid());
      editor.commit();
      mUser = user;

      getBus().post(new LoginEvent.Login(mUser));

      setIsLoggingIn(false);

      /**
       * Execute pending API call if one exists.
       */
      Intent pendingApiCall = APIService.getAndRemovePendingApiCall(this);
      if (pendingApiCall != null) {
         startService(pendingApiCall);
      }
   }

   /**
    * Light version of logout that doesn't fire any events or perform any API calls.
    * logout, bleow, should almost always be the one to use.
    */
   public void shallowLogout() {
      final SharedPreferences prefs = getSharedPreferences(PREFERENCE_FILE,
       Context.MODE_PRIVATE);
      Editor editor = prefs.edit();
      editor.remove(mSite.mName + AUTH_TOKEN_KEY);
      editor.remove(mSite.mName + USERNAME_KEY);
      editor.remove(mSite.mName + USERID_KEY);
      editor.commit();

      mUser = null;
   }

   /**
    * Logs the currently logged in user out by deleting it from SharedPreferences, making
    * the logout API call to delete the auth token, and * resetting mUser.
    */
   public void logout(Activity activity) {
      // Check if the user is null because we're paranoid.
      if (mUser != null && activity != null) {
         // Perform the API call to delete the user's authToken.
         APIService.call((SherlockFragmentActivity) activity, APIService.getLogoutAPICall(mUser));
      }

      shallowLogout();

      getBus().post(new LoginEvent.Logout());
   }

   /**
    * Call when the user has cancelled login.
    */
   public void cancelLogin() {
      // Clear the pending api call if one exists.
      APIService.getAndRemovePendingApiCall(this);
      setIsLoggingIn(false);

      getBus().post(new LoginEvent.Cancel());
   }

   public boolean isScreenLarge() {
      final int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
      return screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE
       || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
   }
}
