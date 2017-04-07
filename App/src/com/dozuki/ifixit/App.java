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
import android.os.StatFs;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dozuki.ifixit.model.auth.Authenticator;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.dozuki.SiteChangedEvent;
import com.dozuki.ifixit.model.user.LoginEvent;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiContentProvider;
import com.dozuki.ifixit.util.api.ApiSyncAdapter;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Logger;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.google.analytics.tracking.android.Tracker;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

public class App extends Application {
   private static final long MIN_DISK_CACHE_SIZE = 10 * 1024 * 1024; // 10 MB

   /*
    * Google Analytics configuration values.
    */

   // Dispatch period in seconds.
   private static final int GA_DISPATCH_PERIOD = 30;

   // Key used to store a user's tracking preferences in SharedPreferences.
   private static final String TRACKING_PREF_KEY = "trackingPreference";

   private static Tracker mGaTracker;

   private static final String PREFERENCE_FILE = "PREFERENCE_FILE";
   private static final String FIRST_TIME_GALLERY_USER =
    "FIRST_TIME_GALLERY_USER";
   private static final String LAST_SYNC_TIME = "LAST_SYNC_TIME";
   public static final long NEVER_SYNCED_VALUE = -1;
   private static final String TAG = "App";


   /**
    * Singleton reference.
    */
   private static App sApp;

   /**
    * Singleton for Bus (Otto).
    */
   private static Bus sBus;

   /**
    * Singleton for OkHttpClient (OkHttp3)
    */
   private static OkHttpClient sClient;

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
   private boolean mConnectionFactorySet = false;

   @Override
   public void onCreate() {

      // Install memory leak analyzer
      if (LeakCanary.isInAnalyzerProcess(this)) {
         // This process is dedicated to LeakCanary for heap analysis.
         // You should not init your app in this process.
         return;
      }
      LeakCanary.install(this);

      if (inDebug()) {
         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
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
      ImageSizes.init(this);

      sApp = this;
      setSite(getDefaultSite());

      // Build our custom Picasso instance with the OkHttp3 Downloader,
      // and set a singleton of it with Picasso so it's used everywhere
      Picasso picasso = new Picasso.Builder(getApplicationContext())
       .downloader(new OkHttp3Downloader(getClient()))
       .build();

      if (BuildConfig.DEBUG) {
         picasso.setIndicatorsEnabled(true);
      }
      
      try {
         Picasso.setSingletonInstance(picasso);
      } catch (IllegalStateException ignored) {
         // Picasso instance was already set
         // cannot set it after Picasso.with(Context) was already in use
      }
   }

   public static void sendEvent(String category, String action, String label, Long value) {
      mGaTracker.send(MapBuilder.createEvent(category, action, label, value).build());
   }

   public static void sendScreenView(String screenName) {
      mGaTracker.send(MapBuilder.createAppView().set(Fields.SCREEN_NAME, screenName).build());
   }

   public static void sendException(String tag, String message, Exception exception) {
      Log.e(tag, message, exception);

      mGaTracker.send(MapBuilder.createException(
       new StandardExceptionParser(get(), null).getDescription(
        Thread.currentThread().getName(), exception), false).build());
   }

   /*
    * Method to handle basic Google Analytics initialization. This call will not
    * block as all Google Analytics work occurs off the main thread.
    */
   private void initializeGa() {
      GoogleAnalytics ga = GoogleAnalytics.getInstance(this);
      mGaTracker = ga.getTracker(BuildConfig.GA_PROPERTY_ID);

      GAServiceManager.getInstance().setLocalDispatchPeriod(GA_DISPATCH_PERIOD);

      // Set dryRun to disable event dispatching.
      ga.setDryRun(BuildConfig.DEBUG);
      ga.getLogger().setLogLevel(BuildConfig.DEBUG ? Logger.LogLevel.INFO :
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

   /**
    * Singleton getter.
    */
   public static App get() {
      return sApp;
   }

   public static OkHttpClient getClient() {
      if (sClient == null) {
         File cache = App.get().getCacheDir();
         if (!cache.exists()) {
            //noinspection ResultOfMethodCallIgnored
            cache.mkdirs();
         }

         OkHttpClient.Builder builder = new OkHttpClient.Builder()
          .cache(new Cache(cache, getCacheSize(cache)));

         // Trust all certs in Debug because some sites have untrusted certs in dev.  This should NOT be enabled live,
         // that would be a very bad thing.
         if (App.inDebug()) {
            SSLContext sslContext = null;
            TrustManager[] trustManagers = new TrustManager[0];
            try {
               KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
               keyStore.load(null, null);
               InputStream certInputStream = get().getApplicationContext().getAssets().open("certs/server.crt");
               BufferedInputStream bis = new BufferedInputStream(certInputStream);
               CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
               while (bis.available() > 0) {
                  Certificate cert = certificateFactory.generateCertificate(bis);
                  keyStore.setCertificateEntry(BuildConfig.DEV_SERVER, cert);
               }
               TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
               trustManagerFactory.init(keyStore);
               trustManagers = trustManagerFactory.getTrustManagers();
               sslContext = SSLContext.getInstance("TLS");
               sslContext.init(null, trustManagers, null);
            } catch (Exception e) {
               e.printStackTrace();
               // unhandled
            }
            builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagers[0]);
         }

         sClient = builder.build();
      }

      return sClient;
   }

   private static long getCacheSize(File cache) {
      long size = MIN_DISK_CACHE_SIZE;
      try {
         StatFs statFs = new StatFs(cache.getAbsolutePath());
         long available;

         if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            available = statFs.getBlockCountLong() * statFs.getBlockSizeLong();
         } else {
            available = ((long) statFs.getBlockCount()) * statFs.getBlockSize();
         }

         // Target around 10% of the total space available in the current external cache.
         size = available / 10;
         Log.i("iFixit Cache", "Cache Size: " + String.valueOf(size));
      } catch (IllegalArgumentException ignored) {
      }

      return size;
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

   public int getTransparentSiteTheme() {
      return mSite.transparentTheme();
   }

   public String getUserAgent() {
      if (mUserAgent == null) {
         int versionCode = BuildConfig.VERSION_CODE;
         String versionName = BuildConfig.VERSION_NAME;

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

   /**
    * Requests a sync for the current user. This operation does nothing if
    * force is false and a sync is already in progress.
    */
   public void requestSync(boolean force) {
      if (!isUserLoggedIn()) {
         return;
      }

      String authority = ApiContentProvider.getAuthority();
      boolean syncActive = ContentResolver.isSyncActive(mAccount, authority);

      if (syncActive && !force) {
         // Do nothing if the sync is active and we don't want to force it.
         return;
      }

      if (syncActive) {
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

   public void setSyncAutomatically(boolean syncAutomatically) {
      if (!isUserLoggedIn()) {
         return;
      }

      ContentResolver.setSyncAutomatically(mAccount,
       ApiContentProvider.getAuthority(), syncAutomatically);
   }

   public boolean getSyncAutomatically() {
      if (!isUserLoggedIn()) {
         return false;
      }

      return ContentResolver.getSyncAutomatically(mAccount,
       ApiContentProvider.getAuthority());
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

   public File getCacheDirPath() {
      return new File(getApplicationContext().getExternalCacheDir(), App.get().getCacheDirName());
   }

   public String getCacheDirName() {
      return getSite().mName.toLowerCase().replace(" ", "-") + "-cache";
   }
}
