package com.dozuki.ifixit.ui;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.dozuki.SiteChangedEvent;
import com.dozuki.ifixit.model.user.LoginEvent;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.ui.auth.LoginFragment;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.PicassoUtils;
import com.dozuki.ifixit.util.ViewServer;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiEvent;
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
   // If an Intent has a site argument it will change sites before displaying any content.
   private static final String SITE_ARGUMENT = "SITE_ARGUMENT";

   private static final int LOGGED_OUT_USERID = -1;

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
      public void onUnauthorized(ApiEvent.Unauthorized event) {
         openLoginDialogIfLoggedOut();
      }

      @SuppressWarnings("unused")
      @Subscribe
      public void onApiCall(ApiEvent.ActivityProxy activityProxy) {
         if (activityProxy.getActivityid() == mActivityid) {
            // Send the real event off to the real handler.
            App.getBus().post(activityProxy.getApiEvent());
         } else {
            // Send the event back to Api so it can retry it for the
            // intended Activity.
            App.getBus().post(new DeadEvent(App.getBus(),
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

   @Override
   public void onCreate(Bundle savedState) {
      App app = App.get();
      Site currentSite = app.getSite();

      if (savedState != null) {
         mActivityid = savedState.getInt(ACTIVITY_ID);
         mUserid = savedState.getInt(USERID);
         mSite = (Site)savedState.getSerializable(SITE);

         // If the site associated with this Activity is different than the current site,
         // set it to the one this Activity wants. Don't always do this because of the
         // overhead of reading the user from SharedPreferences.
         if (mSite.mSiteid != currentSite.mSiteid) {
            app.setSite(mSite);
         }
      } else {
         mActivityid = generateActivityid();
         setUserid();

         Site siteArgument = (Site)getIntent().getSerializableExtra(SITE_ARGUMENT);
         if (siteArgument != null && siteArgument.mSiteid != currentSite.mSiteid) {
            mSite = siteArgument;
            app.setSite(mSite);
         } else {
            mSite = app.getSite();
         }
      }

      Site site = app.getSite();
      ActionBar ab = getSupportActionBar();
      ab.setDisplayHomeAsUpEnabled(true);

      /**
       * Set the current site's theme. Must be before onCreate because of
       * inflating views.
       */
      setTheme(app.getSiteTheme());

      // This doesn't work on on versions below ICS.  Don't really care if the home button pressed state is the wrong
      // color on those devices so just ignore it.
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
         ((View)findViewById(android.R.id.home).getParent().getParent()).setBackgroundResource(R.drawable
          .item_background_holo_light);
      }

      if (site.actionBarUsesIcon()) {
         ab.setLogo(getResources().getIdentifier("icon", "drawable", getPackageName()));
         ab.setDisplayUseLogoEnabled(true);

         // Get the default action bar title resourceid
         int titleId = getResources().getIdentifier("action_bar_title", "id", "android");

         // If it doesn't exist, use actionbarsherlocks
         if (titleId == 0) {
            titleId = com.actionbarsherlock.R.id.abs__action_bar_title;
         }

         TextView title = (TextView) findViewById(titleId);

         // If we were able to get the title element, set it to multi-line and a bit smaller text size so that long
         // site titles (i.e. Hypertherm Waterjet Mobile Assistant) and long guide titles fit nicely.
         if (title != null) {
            title.setSingleLine(false);
            title.setMaxLines(2);
            title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
         }

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
             .load(site.mLogo.getPath(ImageSizes.logo))
             .error(R.drawable.logo_dozuki)
             .into(customLogo);
            customLogo.setVisibility(View.VISIBLE);
            siteTitle.setVisibility(View.GONE);
         } else {
            siteTitle.setText(site.mTitle);
            siteTitle.setVisibility(View.VISIBLE);
            customLogo.setVisibility(View.GONE);
         }

         v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
         });

         ab.setCustomView(v);
      }

      super.onCreate(savedState);

      /**
       * There is another register call in onResume but we also need it here for the onUnauthorized
       * call that is usually triggered in onCreate of derived Activities.
       */
      App.getBus().register(this);
      App.getBus().register(mBaseActivityListener);

      if (App.inDebug()) {
         ViewServer.get(this).addWindow(this);
      }

      Api.retryDeadEvents(this);
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
      if (App.get().getSite().actionBarUsesIcon()) {
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

      App.getBus().register(this);
      App.getBus().register(mBaseActivityListener);

      if (App.inDebug()) {
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
       */
      Api.retryDeadEvents(this);
   }

   @Override
   public void onDestroy() {
      super.onDestroy();

      if (App.inDebug()) {
         ViewServer.get(this).removeWindow(this);
      }
   }

   @Override
   public void onPause() {
      super.onPause();

      App.getBus().unregister(this);
      App.getBus().unregister(mBaseActivityListener);
   }

   public boolean openLoginDialogIfLoggedOut() {
      if (!App.get().isUserLoggedIn()) {
         LoginFragment.newInstance().show(getSupportFragmentManager(), "LoginFragment");
         return true;
      } else {
         return false;
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
      User user = App.get().getUser();
      mUserid = user == null ? LOGGED_OUT_USERID : user.getUserid();
   }

   /**
    * Finishes the Activity if the user should be logged in but isn't.
    */
   private void finishActivityIfPermissionDenied() {
      App app = App.get();
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
         // Because this is only hiding the loading fragment, it's fine to
         // commit with possible state loss.
         getSupportFragmentManager().beginTransaction()
          .remove(loadingFragment)
          .commitAllowingStateLoss();
      }
   }

   public static Intent addSite(Intent intent, Site site) {
      intent.putExtra(SITE_ARGUMENT, site);
      return intent;
   }
}
