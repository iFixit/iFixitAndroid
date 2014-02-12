package com.dozuki.ifixit;

import android.accounts.Account;
import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dozuki.ifixit.model.auth.Authenticator;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.dozuki.SiteChangedEvent;
import com.dozuki.ifixit.model.user.LoginEvent;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.OkConnectionFactory;
import com.dozuki.ifixit.util.Utils;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiContentProvider;
import com.dozuki.ifixit.util.api.ApiSyncAdapter;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Logger;
import com.google.analytics.tracking.android.Tracker;
import com.squareup.otto.Bus;

import java.net.URL;

public class MainApplication extends Application {
   /*
    * Google Analytics configuration values.
    */

   // Dispatch period in seconds.
   private static final int GA_DISPATCH_PERIOD = 30;

   // Key used to store a user's tracking preferences in SharedPreferences.
   private static final String TRACKING_PREF_KEY = "trackingPreference";

   private static GoogleAnalytics mGa;
   private static Tracker mTracker;

   private static final String PREFERENCE_FILE = "PREFERENCE_FILE";
   private static final String FIRST_TIME_GALLERY_USER =
    "FIRST_TIME_GALLERY_USER";
   private static final String LAST_SYNC_TIME = "LAST_SYNC_TIME";
   public static final long NEVER_SYNCED_VALUE = -1;
   private static final String TAG = "MainApplication";


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
    * Current logged in Account. Must be kept in sync with mUser.
    */
   private Account mAccount;

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
      Api.init();

      sMainApplication = this;
      setSite(getDefaultSite());
   }

   /*
    * Method to handle basic Google Analytics initialization. This call will not
    * block as all Google Analytics work occurs off the main thread.
    */
   private void initializeGa() {
      mGa = GoogleAnalytics.getInstance(this);
      mTracker = mGa.getTracker(BuildConfig.GA_PROPERTY_ID);

      GAServiceManager.getInstance().setLocalDispatchPeriod(GA_DISPATCH_PERIOD);

      // Set dryRun to disable event dispatching.
      mGa.setDryRun(BuildConfig.DEBUG);
      mGa.getLogger().setLogLevel(BuildConfig.DEBUG ? Logger.LogLevel.INFO :
       Logger.LogLevel.WARNING);

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
      setupLoggedInUser(site);

      getBus().post(new SiteChangedEvent(mSite, mUser));
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
         mImageSizes = new ImageSizes(
            getString(R.string.image_sizes_actionbar_logo),
            getString(R.string.image_sizes_guide_step_thumbnail),
            getString(R.string.image_sizes_guide_step_main),
            getString(R.string.image_sizes_guide_step_fullsize),
            getString(R.string.image_sizes_grid)
         );
      }

      return mImageSizes;
   }

   public User getUser() {
      return mUser;
   }

   public Account getUserAccount() {
      return mAccount;
   }

   private void setupLoggedInUser(Site site) {
      Authenticator authenticator = new Authenticator(this);
      mAccount = authenticator.getAccountForSite(site);
      mUser = null;

      if (mAccount != null) {
         mUser = authenticator.createUser(mAccount);
      }
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
    * Returns true iff this is the dozuki app (com.dozuki.dozuki).
    */
   public static boolean isDozukiApp() {
      return BuildConfig.SITE_NAME.equals("dozuki");
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
   public void login(User user, String email, String password, boolean notify) {
      mUser = user;

      // Set the email because it isn't included in the API response.
      mUser.mEmail = email;

      mAccount = new Authenticator(this).onAccountAuthenticated(mSite, email,
       user.getUsername(), user.getUserid(), password, user.getAuthToken());

      if (notify) {
         getBus().post(new LoginEvent.Login(mUser));
      }

      setIsLoggingIn(false);

      /**
       * Execute pending API call if one exists.
       */
      ApiCall pendingApiCall = Api.getAndRemovePendingApiCall(this);
      if (pendingApiCall != null) {
         pendingApiCall.updateUser(mUser);
         Api.call(null, pendingApiCall);
      }
   }

   /**
    * Light version of logout that doesn't fire any events or perform any API calls.
    * logout, below, should almost always be the one to use.
    *
    * Warning: This removes the account from AccountManager which could have very bad
    * consequences for account preferences including sync.
    */
   public void shallowLogout(boolean removeAccount) {
      if (removeAccount && mAccount != null) {
         new Authenticator(this).removeAccount(mAccount);
      }

      mUser = null;
      mAccount = null;
   }

   /**
    * Logs the currently logged in user out by deleting it from SharedPreferences, making
    * the logout API call to delete the auth token, and resetting mUser.
    */
   public void logout(Activity activity) {
      // Check if the user is null because we're paranoid.
      if (mUser != null && activity != null) {
         // Perform the API call to delete the user's authToken.
         Api.call(activity, ApiCall.logout(mUser));
      }

      shallowLogout(true);

      getBus().post(new LoginEvent.Logout());
   }

   /**
    * Call when the user has cancelled login.
    */
   public void cancelLogin() {
      // Clear the pending api call if one exists.
      Api.getAndRemovePendingApiCall(this);
      setIsLoggingIn(false);

      getBus().post(new LoginEvent.Cancel());
   }

   public void requestSync() {
      if (!isUserLoggedIn()) {
         return;
      }

      String authority = ApiContentProvider.getAuthority();

      if (ContentResolver.isSyncActive(mAccount, authority)) {
         // Sync is already started so lets restart it.
         ApiSyncAdapter.restartSync(this);
      } else {
         Bundle bundle = new Bundle();
         bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
         bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
         ContentResolver.requestSync(mAccount, authority, bundle);
      }
   }

   public void cancelSync() {
      if (!isUserLoggedIn()) {
         return;
      }

      String authority = ApiContentProvider.getAuthority();
      ContentResolver.cancelSync(mAccount, authority);
   }

   /**
    * Sets the last sync time for the given user to the current time.
    */
   public void setLastSyncTime(Site site, User user) {
      String lastSyncTimeKey = getLastSyncTimeKey(site, user);
      SharedPreferences preferenceFile = getSharedPreferences(PREFERENCE_FILE,
       MODE_PRIVATE | MODE_MULTI_PROCESS);
      Editor editor = preferenceFile.edit();
      editor.putLong(lastSyncTimeKey, System.currentTimeMillis());

      editor.commit();
   }

   public long getLastSyncTime() {
      if (!isUserLoggedIn()) {
         return NEVER_SYNCED_VALUE;
      }

      String lastSyncTimeKey = getLastSyncTimeKey(mSite, mUser);
      SharedPreferences preferenceFile = getSharedPreferences(PREFERENCE_FILE,
       MODE_PRIVATE | MODE_MULTI_PROCESS);

      return preferenceFile.getLong(lastSyncTimeKey, NEVER_SYNCED_VALUE);
   }

   private String getLastSyncTimeKey(Site site, User user) {
      return LAST_SYNC_TIME + "_" + site.mSiteid + "_" + user.getUserid();
   }

   public boolean isScreenLarge() {
      final int screenSize = getResources().getConfiguration().screenLayout &
       Configuration.SCREENLAYOUT_SIZE_MASK;
      return screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
       screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
   }

   public boolean isConnected() {
      ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo netInfo = cm.getActiveNetworkInfo();

      return netInfo != null && netInfo.isConnected();
   }
}
