package com.dozuki.ifixit;

import com.dozuki.ifixit.R;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.user.LoginEvent;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.OkConnectionFactory;
import com.dozuki.ifixit.util.Utils;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Logger;
import com.google.analytics.tracking.android.Tracker;
import com.squareup.otto.Bus;

import java.net.URL;

public class MainApplication extends Application {
   private static final int LARGE_SIZE_CUTOFF = 800;

   /*
    * Google Analytics configuration values.
    */
   // Placeholder property ID.
   private static final String GA_PROPERTY_ID = "UA-30506-9";

   // Dispatch period in seconds.
   private static final int GA_DISPATCH_PERIOD = 30;

   // Prevent hits from being sent to reports, i.e. during testing.
   private static final boolean GA_IS_DRY_RUN = false;

   // GA Logger verbosity.
   private static final Logger.LogLevel GA_LOG_VERBOSITY = Logger.LogLevel.VERBOSE;

   // Key used to store a user's tracking preferences in SharedPreferences.
   private static final String TRACKING_PREF_KEY = "trackingPreference";

   private static GoogleAnalytics mGa;
   private static Tracker mTracker;

   private static final String PREFERENCE_FILE = "PREFERENCE_FILE";
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
   private boolean mUrlStreamFactorySet = false;
   private boolean mConnectionFactorySet = false;

   @Override
   public void onCreate() {
      // OkHttp changes the global SSL context, breaks other HTTP clients.  Google Analytics uses a different http
      // client, which OkHttp doesn't handle well.
      // https://github.com/square/okhttp/issues/184
      if (!mUrlStreamFactorySet) {
         URL.setURLStreamHandlerFactory(Utils.createOkHttpClient());
         mUrlStreamFactorySet = true;
      }

      // Use OkHttp instead of HttpUrlConnection to handle HTTP requests, OkHttp supports 2.2 while HttpURLConnection
      // is a bit buggy on froyo.
      if (!mConnectionFactorySet) {
         HttpRequest.setConnectionFactory(new OkConnectionFactory());
         mConnectionFactorySet = true;
      }

      if (false && inDebug()) {
         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
          .detectDiskReads()
          .detectDiskWrites()
          .detectNetwork()   // or .detectAll() for all detectable problems
          .penaltyLog()
          .build());
         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
          .detectLeakedSqlLiteObjects()
          .detectLeakedClosableObjects()
          .penaltyLog()
          .penaltyDeath()
          .build());
      }

      super.onCreate();
      initializeGa();

      sMainApplication = this;
      setSite(getDefaultSite());
   }

   /*
    * Method to handle basic Google Analytics initialization. This call will not
    * block as all Google Analytics work occurs off the main thread.
    */
   private void initializeGa() {
      mGa = GoogleAnalytics.getInstance(this);
      mTracker = mGa.getTracker(GA_PROPERTY_ID);

      // Set dispatch period.
      GAServiceManager.getInstance().setLocalDispatchPeriod(GA_DISPATCH_PERIOD);

      // Set dryRun flag.
      mGa.setDryRun(GA_IS_DRY_RUN);

      // Set Logger verbosity.
      mGa.getLogger().setLogLevel(GA_LOG_VERBOSITY);

      // Set the opt out flag when user updates a tracking preference.
      SharedPreferences userPrefs = PreferenceManager.getDefaultSharedPreferences(this);
      userPrefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
         @Override
         public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
          String key) {
            if (key.equals(TRACKING_PREF_KEY)) {
               GoogleAnalytics.getInstance(getApplicationContext())
                .setAppOptOut(sharedPreferences.getBoolean(key, false));
            }
         }
      });
   }

   /*
    * Returns the Google Analytics tracker.
    */
   public static Tracker getGaTracker() {
      return mTracker;
   }

   /*
    * Returns the Google Analytics instance.
    */
   public static GoogleAnalytics getGaInstance() {
      return mGa;
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
      String topicName = getString(R.string.category);

      if (mSite.mName.equals("ifixit")) {
         topicName = getString(R.string.device);
      }

      return topicName;
   }

   public boolean inPortraitMode() {
      return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
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
         String versionName = "";

         try {
            PackageInfo packageInfo;
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            versionCode = packageInfo.versionCode;
            versionName = packageInfo.versionName;
         } catch (PackageManager.NameNotFoundException e) {
            Log.e("iFixit", "Can't get application version", e);
         }

         /**
          * Returns the Site that this app is "built" for. e.g. Dozuki even if the user
          * is currently viewing a different nanosite.
          */
         Site currentApp = getDefaultSite();
         mUserAgent = currentApp.mTitle + "Android/" + versionName +
          " (" + versionCode + ") | " + System.getProperty("http.agent");
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
      return BuildConfig.DEBUG;
   }

   public static Bus getBus() {
      if (sBus == null) {
         sBus = new Bus();
      }

      return sBus;
   }

   public ImageSizes getImageSizes() {
      if (mImageSizes == null) {
         TypedArray imageSizes = getResources().obtainTypedArray(R.array.image_sizes);
         mImageSizes = new ImageSizes(
          imageSizes.getString(4),
          imageSizes.getString(0),
          imageSizes.getString(1),
          imageSizes.getString(2),
          imageSizes.getString(3));
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
      return Site.getSite(BuildConfig.SITE_NAME);
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
         APIService.call(activity, APIService.getLogoutAPICall(mUser));
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
      final int screenSize = getResources().getConfiguration().screenLayout &
       Configuration.SCREENLAYOUT_SIZE_MASK;
      return screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
       screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
   }
}
