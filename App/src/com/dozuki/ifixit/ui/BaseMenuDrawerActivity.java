package com.dozuki.ifixit.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.webkit.URLUtil;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.BuildConfig;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.user.LoginEvent;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.ui.gallery.GalleryActivity;
import com.dozuki.ifixit.ui.guide.create.GuideCreateActivity;
import com.dozuki.ifixit.ui.guide.create.StepEditActivity;
import com.dozuki.ifixit.ui.guide.view.FeaturedGuidesActivity;
import com.dozuki.ifixit.ui.guide.view.OfflineGuidesActivity;
import com.dozuki.ifixit.ui.guide.view.TeardownsActivity;
import com.dozuki.ifixit.ui.search.SearchActivity;
import com.dozuki.ifixit.ui.topic.TopicActivity;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.Utils;
import com.dozuki.ifixit.util.transformations.CircleTransformation;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

/**
 * Base activity that displays the menu drawer.
 */
public abstract class BaseMenuDrawerActivity extends BaseActivity
 implements NavigationView.OnNavigationItemSelectedListener {
   private static final String STATE_ACTIVE_POSITION =
    "com.dozuki.ifixit.ui.BaseMenuDrawerActivity.activePosition";
   private static final String PEEK_MENU = "PEEK_MENU_KEY";
   private static final String INTERFACE_STATE = "IFIXIT_INTERFACE_STATE";

   private int mActivePosition = -1;
   protected DrawerLayout mDrawer;
   private NavigationView mDrawerList;
   private String mTitle;
   private ActionBarDrawerToggle mDrawerToggle;
   private Menu mMenu;

   @Override
   public void onCreate(Bundle savedState) {
      super.onCreate(savedState);

      setContentView(R.layout.base_layout);

      mContentFrame = (FrameLayout) findViewById(R.id.content_frame);

      mToolbar = (Toolbar) findViewById(R.id.toolbar);

      setSupportActionBar(mToolbar);

      if (savedState != null) {
         mActivePosition = savedState.getInt(STATE_ACTIVE_POSITION);
      }

      mTitle = App.get().getSite().mTitle;
      mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
      mDrawerList = (NavigationView) findViewById(R.id.left_drawer);

      buildMenu();

      mDrawerList.setNavigationItemSelectedListener(this);

      mDrawerToggle = new ActionBarDrawerToggle(
       this, mDrawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close
      ) {
         /** Called when a drawer has settled in a completely closed state. */
         public void onDrawerClosed(View view) {
            super.onDrawerClosed(view);
            getSupportActionBar().setTitle(mTitle);
            syncActionBarArrowState();
            invalidateOptionsMenu();
         }

         /** Called when a drawer has settled in a completely open state. */
         public void onDrawerOpened(View drawerView) {
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            invalidateOptionsMenu();
         }
      };

      // Set the drawer toggle as the DrawerListener
      mDrawer.addDrawerListener(mDrawerToggle);
      mDrawerToggle.syncState();

      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeButtonEnabled(true);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
         mDrawerToggle.setHomeAsUpIndicator(
          getResources().getDrawable(R.drawable.ic_arrow_back_24dp, getTheme()));
      } else {
         mDrawerToggle.setHomeAsUpIndicator(
          getResources().getDrawable(R.drawable.ic_arrow_back_24dp));
      }

      SharedPreferences prefs = getSharedPreferences(INTERFACE_STATE, MODE_PRIVATE);

      if (!prefs.contains(PEEK_MENU)) {
         prefs.edit().putBoolean(PEEK_MENU, false).apply();
         mDrawer.openDrawer(mDrawerList);
      }
   }

   public void syncActionBarArrowState() {
      int backStackEntryCount =
       getSupportFragmentManager().getBackStackEntryCount();
      mDrawerToggle.setDrawerIndicatorEnabled(backStackEntryCount == 0);
      if (backStackEntryCount > 0) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mDrawerToggle.setHomeAsUpIndicator(
             getResources().getDrawable(R.drawable.ic_arrow_back_24dp, getTheme()));
         } else {
            mDrawerToggle.setHomeAsUpIndicator(
             getResources().getDrawable(R.drawable.ic_arrow_back_24dp));
         }
      }
   }

   public ActionBarDrawerToggle getDrawerToggle() {
      return mDrawerToggle;
   }

   @Override
   protected void onPostCreate(Bundle savedInstanceState) {
      super.onPostCreate(savedInstanceState);
      mDrawerToggle.syncState();
   }

   @Override
   public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      mDrawerToggle.onConfigurationChanged(newConfig);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      // Pass the event to ActionBarDrawerToggle, if it returns
      // true, then it has handled the app icon touch event
      if (mDrawerToggle.onOptionsItemSelected(item)) {
         return true;
      }

      return super.onOptionsItemSelected(item);
   }

   @Override
   public void onRestart() {
      super.onRestart();
      // Invalidate the options menu in case the user logged in/out in a child Activity.
      rebuildSliderMenu();
   }

   @Override
   protected void onCustomMenuTitleClick(View v) {
      // Rather than finishing the Activity and going "up", toggle the menu drawer.
      mDrawer.openDrawer(mDrawerList);
   }

   @Override
   public void onLogin(LoginEvent.Login event) {
      super.onLogin(event);

      // Reload app to update the menu to include the user name and logout button.
      rebuildSliderMenu();
   }

   @Override
   public void onLogout(LoginEvent.Logout event) {
      super.onLogout(event);

      // Reload app to remove username and logout button from menu.
      rebuildSliderMenu();
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent intent) {
      String barcodeScannerResult = getBarcodeScannerResult(requestCode, resultCode, intent);

      if (barcodeScannerResult != null) {
         if (URLUtil.isValidUrl(barcodeScannerResult)) {
            startActivity(IntentFilterActivity.viewUrl(this, barcodeScannerResult));
         } else {
            Toast.makeText(this, "The contents of that barcode / QR code were not a valid URL; This is what was read: " + barcodeScannerResult, Toast.LENGTH_LONG).show();
            Log.e("BaseMenuDrawerActivity", "Cannot launch barcode scanner: " + barcodeScannerResult);
         }
      } else {
         super.onActivityResult(requestCode, resultCode, intent);
      }
   }

   private String getBarcodeScannerResult(int requestCode, int resultCode, Intent intent) {
      // The classes below might not exist if barcode scanning isn't enabled.
      if (!App.get().getSite().barcodeScanningEnabled()) {
         return null;
      }

      try {
         IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

         if (scanResult == null) {
            return null;
         }

         return scanResult.getContents();
      } catch (Exception e) {
         Toast.makeText(this, "Failed to parse result.", Toast.LENGTH_SHORT).show();
         Log.e("BaseMenuDrawerActivity", "Failure parsing activity result", e);
         return null;
      }
   }

   private void rebuildSliderMenu() {
      mDrawerList.getMenu().clear();
      buildMenu();
      mDrawerToggle.syncState();
   }

   private void buildMenu() {
      App app = App.get();
      Site site = app.getSite();
      mDrawerList.inflateMenu(R.menu.drawer_view);
      mMenu = mDrawerList.getMenu();
      mMenu.findItem(R.id.nav_browse_content).setTitle(
       this.getString(R.string.slide_menu_browse_devices, site.getObjectNamePlural()));

      int[] ids;
      if (site.isIfixit()) {
         ids = new int[]{R.id.nav_parts_and_tools, R.id.nav_teardowns, R.id.nav_social_section};
      } else if (site.isDozuki()) {
         ids = new int[]{R.id.nav_back_to_site_list};
      } else {
         ids = new int[]{};
      }

      for (int id : ids) {
         mMenu.findItem(id).setVisible(true);
      }

      if (site.barcodeScanningEnabled()) {
         mMenu.findItem(R.id.nav_scan_barcode).setVisible(true);
      }

      if (site.mAnswers) {
         mMenu.findItem(R.id.nav_answers).setVisible(true);
      }

      if (BuildConfig.DEBUG) {
         mMenu.findItem(R.id.nav_debug).setVisible(true);
      }

      View header;
      if (app.isUserLoggedIn()) {
         header = getLayoutInflater().inflate(R.layout.navigation_header_logged_in, null);
         User user = app.getUser();
         AppCompatTextView displayName = (AppCompatTextView) header.findViewById(R.id.navigation_display_name);
         displayName.setText(user.getUsername());
         AppCompatTextView username = (AppCompatTextView) header.findViewById(R.id.navigation_username);

         String uniqueUsername = user.getUniqueUsername();
         if (uniqueUsername != null && uniqueUsername.length() > 0) {
            username.setText(user.getUniqueUsername());
            username.setVisibility(View.VISIBLE);
         } else {
            MarginLayoutParams marginParams = (MarginLayoutParams) displayName.getLayoutParams();

            marginParams.setMargins(0, (int) Utils.pxFromDp(this, 20), 0 ,0);
            displayName.setLayoutParams(marginParams);
         }

         AppCompatImageView avatar = (AppCompatImageView) header.findViewById(R.id.navigation_avatar);

         Image avatarImage = user.getAvatar();

         Picasso avatarPicasso = Picasso.with(this);
         RequestCreator request;
         if (avatarImage == null) {
            request = avatarPicasso.load(R.drawable.default_user);
         } else {
            request = avatarPicasso.load(avatarImage.getPath(ImageSizes.headerAvatar));
         }

         request
          .fit()
          .centerInside()
          .transform(new CircleTransformation())
          .into(avatar);

         mMenu.findItem(R.id.nav_logout).setVisible(true);
      } else if (App.get().getSite().isIfixit()) {
         header = getLayoutInflater().inflate(R.layout.navigation_header, null);

         AppCompatImageView navLogoView = (AppCompatImageView) header.findViewById(R.id.navigation_site_logo);
         Picasso.with(this)
          .load(R.drawable.ic_logo_header)
          .into(navLogoView);
      } else {
         Image logo = site.getLogo();
         header = getLayoutInflater().inflate(R.layout.navigation_header, null);

         if (logo != null) {
            AppCompatImageView navLogoView = (AppCompatImageView) header.findViewById(R.id.navigation_site_logo);
            Picasso.with(this)
             .load(logo.getPath(ImageSizes.logo))
             .into(navLogoView);
         }
      }

      if (mDrawerList.getHeaderCount() > 0) {
         mDrawerList.removeHeaderView(mDrawerList.getHeaderView(0));
      }

      mDrawerList.addHeaderView(header);
   }


   @Override
   protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      outState.putInt(STATE_ACTIVE_POSITION, mActivePosition);
   }

   @Override
   public boolean onNavigationItemSelected(final MenuItem menuItem) {
      switch (menuItem.getItemId()) {
         case R.id.nav_back_to_site_list:
            returnToSiteList();
            break;
         case R.id.nav_favorites:
            performActivityNavigation(OfflineGuidesActivity.class);
            break;
         case R.id.nav_new_guide:
            performActivityNavigation(StepEditActivity.class,
             Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            break;
         case R.id.nav_my_guides:
            performActivityNavigation(GuideCreateActivity.class);
            break;
         case R.id.nav_media_manager:
            performActivityNavigation(GalleryActivity.class);
            break;
         case R.id.nav_logout:
            App.get().logout(this);
            break;
         case R.id.nav_search:
            performActivityNavigation(SearchActivity.class);
            break;
         case R.id.nav_scan_barcode:
            this.launchBarcodeScanner();
            break;
         case R.id.nav_teardowns:
            performActivityNavigation(TeardownsActivity.class);
            break;
         case R.id.nav_browse_content:
            performActivityNavigation(TopicActivity.class);
            break;
         case R.id.nav_answers:
            performActivityNavigation(AnswersWebViewActivity.class);
            break;
         case R.id.nav_parts_and_tools:
            performActivityNavigation(StoreWebViewActivity.class);
            break;
         case R.id.nav_featured_guides:
            performActivityNavigation(FeaturedGuidesActivity.class);
            break;
         case R.id.nav_youtube:
            performUrlNavigation("https://www.youtube.com/user/iFixitYourself");
            break;
         case R.id.nav_facebook:
            performUrlNavigation("https://www.facebook.com/iFixit");
            break;
         case R.id.nav_twitter:
            performUrlNavigation("https://twitter.com/iFixit");
            break;

      }
      mDrawer.closeDrawers();
      return true;
   }

   public void performActivityNavigation(Class<? extends BaseActivity> activityClass) {
      Intent intent = new Intent(this, activityClass);
      this.startActivity(intent);
   }

   public void performActivityNavigation(Class<? extends BaseActivity> activityClass, int intentFlags) {
      Intent intent = new Intent(this, activityClass);
      intent.setFlags(intentFlags);
      this.startActivity(intent);
   }

   public void performUrlNavigation(String url) {
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setData(Uri.parse(url));
      this.startActivity(intent);
   }

   private void returnToSiteList() {
      try {
         // We need to use reflection because SiteListActivity only exists for
         // the dozuki build but this code is around for all builds.
         Intent intent = new Intent(this,
          Class.forName("com.dozuki.ifixit.ui.dozuki.SiteListActivity"));
         intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION |
          Intent.FLAG_ACTIVITY_CLEAR_TOP);
         startActivity(intent);
         finish();
      } catch (ClassNotFoundException e) {
         Toast.makeText(this, "Failed to return to site list.", Toast.LENGTH_SHORT).show();
         Log.e("BaseMenuDrawerActivity", "Cannot start SiteListActivity", e);
      }
   }

   protected void launchBarcodeScanner() {
      // We want to just call `IntentIntegrator.initiateScan(this);` but it doesn't
      // compile unless the dependency exists.
      try {
         IntentIntegrator integrator = new IntentIntegrator(this);
         integrator.initiateScan();
      } catch (Exception e) {
         Toast.makeText(this, "Failed to launch QR code scanner.", Toast.LENGTH_SHORT).show();
         Log.e("BaseMenuDrawerActivity", "Cannot launch barcode scanner", e);
      }
   }
}
