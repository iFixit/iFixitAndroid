package com.dozuki.ifixit.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.dozuki.SiteChangedEvent;
import com.dozuki.ifixit.model.user.LoginEvent;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.ui.login.LoginFragment;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.util.PicassoUtils;
import com.dozuki.ifixit.util.ViewServer;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.squareup.otto.DeadEvent;
import com.squareup.otto.Subscribe;

/**
 * Base Activity that performs various functions that all Activities in this app
 * should do. Such as:
 *
 * Registering for the event bus. Setting the current site's theme. Finishing
 * the Activity if the user logs out but the Activity requires authentication.
 */
public abstract class BaseActivity extends SherlockFragmentActivity {
   protected static final String LOADING = "LOADING_FRAGMENT";
   private static final String ACTIVITY_ID = "ACTIVITY_ID";
   private static final String USERID = "USERID";
   private static final String SITE = "SITE";

   private static final int LOGGED_OUT_USERID = -1;

   private APIService mAPIService;
   private int mActivityid;
   private int mUserid;
   private Site mSite;

   /**
    * This is incredibly hacky. The issue is that Otto does not search for @Subscribed
    * methods in parent classes because the performance hit is far too big for
    * Android because of the deep inheritance with the framework and views.
    * Because of this, @Subscribed methods on BaseActivity itself don't get
    * registered. The workaround is to make an anonymous object that is registered
    * on behalf of the parent class. Workaround courtesy of:
    * https://github.com/square/otto/issues/26
    *
    * Note: The '@SuppressWarnings("unused")' is to prevent
    * warnings that are incorrect (the methods *are* actually used.
    */
   private Object mBaseActivityListener = new Object() {
      @SuppressWarnings("unused")
      @Subscribe
      public void onLoginEvent(LoginEvent.Login event) {
         onLogin(event);
      }

      @SuppressWarnings("unused")
      @Subscribe
      public void onLogoutEvent(LoginEvent.Logout event) {
         onLogout(event);
      }

      @SuppressWarnings("unused")
      @Subscribe
      public void onCancelEvent(LoginEvent.Cancel event) {
         onCancelLogin(event);
      }

      @SuppressWarnings("unused")
      @Subscribe
      public void onUnauthorized(APIEvent.Unauthorized event) {
         openLoginDialogIfLoggedOut();
      }

      @SuppressWarnings("unused")
      @Subscribe
      public void onApiCall(APIEvent.ActivityProxy activityProxy) {
         if (activityProxy.getActivityid() == mActivityid) {
            // Send the real event off to the real handler.
            MainApplication.getBus().post(activityProxy.getApiEvent());
         } else {
            // Send the event back to APIService so it can retry it for the
            // intended Activity.
            MainApplication.getBus().post(new DeadEvent(MainApplication.getBus(),
             activityProxy.getApiEvent()));
         }
      }

      @SuppressWarnings("unused")
      @Subscribe
      public void onSiteChanged(SiteChangedEvent event) {
         mSite = event.mSite;
         // Reset the userid so we don't erroneously finish the Activity.
         setUserid();
      }
   };

   private ServiceConnection mConnection = new ServiceConnection() {
      public void onServiceDisconnected(ComponentName name) {
         mAPIService = null;
      }

      public void onServiceConnected(ComponentName name, IBinder service) {
         APIService.LocalBinder mLocalBinder = (APIService.LocalBinder)service;
         mAPIService = mLocalBinder.getAPIServiceInstance();

         mAPIService.retryDeadEvents(BaseActivity.this);
      }
   };

   @Override
   public void onCreate(Bundle savedState) {
      if (savedState != null) {
         mActivityid = savedState.getInt(ACTIVITY_ID);
         mUserid = savedState.getInt(USERID);
         mSite = (Site)savedState.getSerializable(SITE);

         Site currentSite = MainApplication.get().getSite();

         // If the site associated with this Activity is different than the current site,
         // set it to the one this Activity wants. Don't always do this because of the
         // overhead of reading the user from SharedPreferences.
         if (mSite.mSiteid != currentSite.mSiteid) {
            MainApplication.get().setSite(mSite);
         }
      } else {
         mActivityid = generateActivityid();
         setUserid();
         mSite = MainApplication.get().getSite();
      }

      MainApplication app = MainApplication.get();
      Site site = app.getSite();
      ActionBar ab = getSupportActionBar();
      ab.setDisplayHomeAsUpEnabled(true);

      /**
       * Set the current site's theme. Must be before onCreate because of
       * inflating views.
       */
      setTheme(app.getSiteTheme());

      if (site.isIfixit()) {
         ab.setLogo(R.drawable.logo_ifixit);
         ab.setDisplayUseLogoEnabled(true);
      } else {
         ab.setDisplayUseLogoEnabled(false);
         ab.setDisplayShowTitleEnabled(false);
         ab.setDisplayShowCustomEnabled(true);
         ab.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

         View v = getLayoutInflater().inflate(R.layout.menu_title, null);

         ImageView customLogo = (ImageView) v.findViewById(R.id.custom_logo);
         TextView siteTitle = (TextView) v.findViewById(R.id.custom_site_title);

         if (site.mLogo != null) {
            PicassoUtils.with(this)
             .load(site.mLogo.getPath(app.getImageSizes().getLogo()))
             .error(R.drawable.logo_dozuki)
             .into(customLogo);
            customLogo.setVisibility(View.VISIBLE);
            siteTitle.setVisibility(View.GONE);
         } else {
            siteTitle.setText(site.mTitle);
            siteTitle.setVisibility(View.VISIBLE);
            customLogo.setVisibility(View.GONE);
         }

         ab.setCustomView(v);
      }

      super.onCreate(savedState);

      /**
       * There is another register call in onResume but we also need it here for the onUnauthorized
       * call that is usually triggered in onCreate of derived Activities.
       */
      MainApplication.getBus().register(this);
      MainApplication.getBus().register(mBaseActivityListener);

      if (MainApplication.inDebug()) {
         ViewServer.get(this).addWindow(this);
      }

      Intent mIntent = new Intent(this, APIService.class);
      bindService(mIntent, mConnection, BIND_AUTO_CREATE);
   }

   /**
    * Returns a unique integer for use as an activity id.
    */
   private static int sActivityIdCounter = 0;
   private int generateActivityid() {
      return sActivityIdCounter++;
   }

   public int getActivityid() {
      return mActivityid;
   }

   public void setTitle(String title) {
      if (MainApplication.get().getSite().isIfixit()) {
         getSupportActionBar().setTitle(title);
      } else {
         TextView titleView = ((TextView)getSupportActionBar().getCustomView().
          findViewById(R.id.custom_page_title));
         titleView.setText(title);
      }
   }

   @Override
   protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putInt(ACTIVITY_ID, mActivityid);
      outState.putInt(USERID, mUserid);
      outState.putSerializable(SITE, mSite);
   }

   /**
    * If the user is coming back to this Activity make sure they still have
    * permission to view it. onRestoreInstanceState is for Activities that are
    * being recreated and onRestart is for Activities who are merely being
    * restarted. Unfortunately both are needed.
    */
   @Override
   public void onRestoreInstanceState(Bundle savedState) {
      super.onRestoreInstanceState(savedState);
      finishActivityIfPermissionDenied();
   }

   @Override
   public void onStart() {
      super.onStart();

      overridePendingTransition(0, 0);
   }

   @Override
   public void onRestart() {
      super.onRestart();
      finishActivityIfPermissionDenied();
   }

   @Override
   public void onResume() {
      super.onResume();

      MainApplication.getBus().register(this);
      MainApplication.getBus().register(mBaseActivityListener);

      if (MainApplication.inDebug()) {
         ViewServer.get(this).setFocusedWindow(this);
      }
   }

   @Override
   protected void onPostResume() {
      super.onPostResume();

      /**
       * This covers missed events caused by dialogs or other views causing the
       * Activity's onPause method to be called which unregisters the Activity
       * as well as returning to an already running Activity via the back button.
       * If the service isn't connected yet then dead events will be retried in
       * onServiceConnected.
       */
      if (mAPIService != null) {
         mAPIService.retryDeadEvents(this);
      }
   }

   @Override
   public void onDestroy() {
      super.onDestroy();

      if (mAPIService != null) {
         unbindService(mConnection);
      }

      if (MainApplication.inDebug()) {
         ViewServer.get(this).removeWindow(this);
      }
   }

   @Override
   public void onPause() {
      super.onPause();

      MainApplication.getBus().unregister(this);
      MainApplication.getBus().unregister(mBaseActivityListener);
   }

   public void openLoginDialogIfLoggedOut() {
      if (!MainApplication.get().isUserLoggedIn()) {
         LoginFragment.newInstance().show(getSupportFragmentManager(), "LoginFragment");
      }
   }

   public void onLogin(LoginEvent.Login event) {
      setUserid();
   }

   public void onLogout(LoginEvent.Logout event) {
      /**
       * Check permissions before setting mUserid. Otherwise the Activity
       * will never be finished because mUserid matches the currently logged
       * in user.
       */
      finishActivityIfPermissionDenied();
      setUserid();
   }

   public void onCancelLogin(LoginEvent.Cancel event) {
      finishActivityIfPermissionDenied();
   }

   /**
    * Sets the userid to the currently logged in user's userid.
    */
   private void setUserid() {
      User user = MainApplication.get().getUser();
      mUserid = user == null ? LOGGED_OUT_USERID : user.getUserid();
   }

   /**
    * Finishes the Activity if the user should be logged in but isn't.
    */
   private void finishActivityIfPermissionDenied() {
      MainApplication app = MainApplication.get();
      User user = app.getUser();
      int currentUserid = user == null ? LOGGED_OUT_USERID : user.getUserid();

      // Never finish the activity if the user is logging in.
      if (neverFinishActivityOnLogout() || app.isLoggingIn()) {
         return;
      }

      // Finish if the site is private or activity requires authentication.
      if ((currentUserid == LOGGED_OUT_USERID || currentUserid != mUserid) &&
       (finishActivityIfLoggedOut() || !app.getSite().mPublic)) {
         finish();
      }
   }

   /**
    * Returns true if the Activity should be finished if the user logs out or
    * cancels authentication.
    */
   public boolean finishActivityIfLoggedOut() {
      return false;
   }

   /**
    * Returns true if the Activity should never be finished despite meeting
    * other conditions.
    *
    * This exists because of a race condition of sorts involving logging out of
    * private Dozuki sites. SiteListActivity can't reset the current site to
    * one that is public so it is erroneously finished unless flagged
    * otherwise.
    */
   public boolean neverFinishActivityOnLogout() {
      return false;
   }

   public void showLoading(int container) {
      showLoading(container, getString(R.string.loading));
   }

   public void showLoading(int container, String message) {
      getSupportFragmentManager().beginTransaction()
       .add(container, new LoadingFragment(message), LOADING)
       .commit();
   }

   public void hideLoading() {
      Fragment loadingFragment = getSupportFragmentManager().findFragmentByTag(LOADING);
      if (loadingFragment != null) {
         getSupportFragmentManager().beginTransaction()
          .remove(loadingFragment)
          .commit();
      }
   }
}
