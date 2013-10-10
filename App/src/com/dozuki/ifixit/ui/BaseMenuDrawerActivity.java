package com.dozuki.ifixit.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.user.LoginEvent;
import com.dozuki.ifixit.ui.gallery.GalleryActivity;
import com.dozuki.ifixit.ui.guide.create.GuideCreateActivity;
import com.dozuki.ifixit.ui.guide.create.StepEditActivity;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.ui.guide.view.FeaturedGuidesActivity;
import com.dozuki.ifixit.ui.guide.view.TeardownsActivity;
import com.dozuki.ifixit.ui.search.SearchActivity;
import com.dozuki.ifixit.ui.topic_view.TopicActivity;
import com.google.analytics.tracking.android.MapBuilder;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Base activity that displays the menu drawer.
 */
public abstract class BaseMenuDrawerActivity extends BaseActivity {
   private static final String STATE_ACTIVE_POSITION =
    "com.dozuki.ifixit.ui.BaseMenuDrawerActivity.activePosition";
   private static final String PEEK_MENU = "PEEK_MENU_KEY";
   private static final String INTERFACE_STATE = "IFIXIT_INTERFACE_STATE";

   /**
    * Slide Out Menu Drawer
    */
   private MenuDrawer mMenuDrawer;

   private int mActivePosition = -1;

   @Override
   public void onCreate(Bundle savedState) {
      super.onCreate(savedState);

      if (savedState != null) {
         mActivePosition = savedState.getInt(STATE_ACTIVE_POSITION);
      }

      mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.Type.OVERLAY,
       Position.LEFT, MenuDrawer.MENU_DRAG_CONTENT);
      mMenuDrawer.setMenuSize(getResources().getDimensionPixelSize(R.dimen.menu_size));
      mMenuDrawer.setTouchMode(MenuDrawer.TOUCH_MODE_BEZEL);
      mMenuDrawer.setTouchBezelSize(getResources().getDimensionPixelSize(R.dimen.menu_bezel_size));

      SharedPreferences prefs = getSharedPreferences(INTERFACE_STATE, MODE_PRIVATE);

      if (!prefs.contains(PEEK_MENU)) {
         prefs.edit().putBoolean(PEEK_MENU, false).commit();
         mMenuDrawer.openMenu();
      }

      buildSliderMenu();
   }

   @Override
   public void setContentView(int layoutResId) {
      mMenuDrawer.setContentView(layoutResId);
   }

   @Override
   public void onRestart() {
      super.onRestart();
      // Invalidate the options menu in case the user logged in/out in a child Activity.
      buildSliderMenu();
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home:
            mMenuDrawer.toggleMenu();
            return true;
      }

      return super.onOptionsItemSelected(item);
   }

   @Override
   public void onLogin(LoginEvent.Login event) {
      super.onLogin(event);

      // Reload app to update the menu to include the user name and logout button.
      buildSliderMenu();
   }

   @Override
   public void onLogout(LoginEvent.Logout event) {
      super.onLogout(event);

      // Reload app to remove username and logout button from menu.
      buildSliderMenu();
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
      String barcodeScannerResult = getBarcodeScannerResult(requestCode, resultCode, intent);

      if (barcodeScannerResult != null) {
         startActivity(GuideViewActivity.viewUrl(this, barcodeScannerResult));
      } else {
         super.onActivityResult(requestCode, resultCode, intent);
      }
   }

   private String getBarcodeScannerResult(int requestCode, int resultCode, Intent intent) {
      // The classes below might not exist if barcode scanning isn't enabled.
      if (!MainApplication.get().getSite().barcodeScanningEnabled()) {
         return null;
      }

      try {
         // Call IntentIntegrator.parseResult(requestCode, resultCode, intent);
         Class<?> c = Class.forName("com.google.zxing.integration.android.IntentIntegrator");
         Class[] argTypes = new Class[] { Integer.TYPE, Integer.TYPE, Intent.class };
         Method parseResult = c.getDeclaredMethod("parseActivityResult", argTypes);
         Object intentResult = parseResult.invoke(null, requestCode, resultCode, intent);

         // Call intentResult.getContents().
         c = Class.forName("com.google.zxing.integration.android.IntentResult");
         argTypes = new Class[] { };
         Method getContents = c.getDeclaredMethod("getContents", argTypes);
         Object contents = getContents.invoke(intentResult);

         return (String)contents;
      } catch (Exception e) {
         Toast.makeText(this, "Failed to parse result.", Toast.LENGTH_SHORT).show();
         Log.e("BaseMenuDrawerActivity", "Failure parsing activity result", e);
         return null;
      }
   }

   private void buildSliderMenu() {
      Site site = MainApplication.get().getSite();
      boolean onIfixit = site.isIfixit();

      // Add items to the menu.  The order Items are added is the order they appear in the menu.
      List<Object> items = new ArrayList<Object>();

      if (MainApplication.isDozukiApp()) {
         items.add(new Item(getString(R.string.back_to_site_list),
          R.drawable.ic_action_list, NavigationItem.SITE_LIST));
      }

      items.add(new Item(getString(R.string.search), R.drawable.ic_action_search, NavigationItem.SEARCH));

      if (site.barcodeScanningEnabled()) {
         items.add(new Item(getString(R.string.slide_menu_barcode_scanner),
          R.drawable.ic_action_qr_code, NavigationItem.SCAN_BARCODE));
      }

      items.add(new Category(getString(R.string.slide_menu_browse_content)));
      items.add(new Item(getString(R.string.slide_menu_browse_devices, MainApplication.get().getSite()
       .getObjectNamePlural()), R.drawable.ic_action_list_2, NavigationItem.BROWSE_TOPICS));

      if (onIfixit) {
         items.add(new Item(getString(R.string.featured_guides), R.drawable.ic_action_star_10, NavigationItem.FEATURED_GUIDES));
         items.add(new Item(getString(R.string.teardowns), R.drawable.ic_menu_stack, NavigationItem.TEARDOWNS));
      }

      items.add(new Category(buildAccountMenuCategoryTitle()));
      items.add(new Item(getString(R.string.slide_menu_favorite_guides), R.drawable.ic_menu_favorite_light, NavigationItem.USER_FAVORITES));
      items.add(new Item(getString(R.string.slide_menu_my_guides), R.drawable.ic_menu_spinner_guides, NavigationItem.USER_GUIDES));
      items.add(new Item(getString(R.string.slide_menu_create_new_guide), R.drawable.ic_menu_add_guide, NavigationItem.NEW_GUIDE));
      items.add(new Item(getString(R.string.slide_menu_media_gallery), R.drawable.ic_menu_spinner_gallery, NavigationItem.MEDIA_GALLERY));

      if (MainApplication.get().isUserLoggedIn()) {
         items.add(new Item(getString(R.string.slide_menu_logout), R.drawable.ic_action_exit, NavigationItem.LOGOUT));
      }

      if (onIfixit) {
         items.add(new Category(getString(R.string.slide_menu_ifixit_everywhere)));
         items.add(new Item(getString(R.string.slide_menu_youtube), R.drawable.ic_action_youtube, NavigationItem.YOUTUBE));
         items.add(new Item(getString(R.string.slide_menu_facebook), R.drawable.ic_action_facebook, NavigationItem.FACEBOOK));
         items.add(new Item(getString(R.string.slide_menu_twitter), R.drawable.ic_action_twitter, NavigationItem.TWITTER));
      }

      /*items.add(new Category(getString(R.string.slide_menu_more_info)));
      items.add(new Item(getString(R.string.slide_menu_help), R.drawable.ic_action_help, NavigationItem.HELP));
      items.add(new Item(getString(R.string.slide_menu_about), R.drawable.ic_action_info, NavigationItem.ABOUT)); */

      // A custom ListView is needed so the drawer can be notified when it's scrolled. This is to update the position
      // of the arrow indicator.
      ListView menuList = new ListView(this);
      MenuAdapter adapter = new MenuAdapter(items);
      menuList.setAdapter(adapter);
      menuList.setOnItemClickListener(mItemClickListener);
      menuList.setCacheColorHint(Color.TRANSPARENT);

      mMenuDrawer.setMenuView(menuList);
      mMenuDrawer.setSlideDrawable(R.drawable.ic_drawer);
      mMenuDrawer.setDrawerIndicatorEnabled(true);

      mMenuDrawer.invalidate();
   }

   public void setMenuDrawerSlideDrawable(int drawable) {
      mMenuDrawer.setSlideDrawable(drawable);
   }

   /**
    * Close the menu drawer if back is pressed and the menu is open.
    */
   @Override
   public void onBackPressed() {
      final int drawerState = mMenuDrawer.getDrawerState();
      if (drawerState == MenuDrawer.STATE_OPEN
       || drawerState == MenuDrawer.STATE_OPENING) {
         mMenuDrawer.closeMenu();
         return;
      }

      super.onBackPressed();
   }

   @Override
   protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      outState.putInt(STATE_ACTIVE_POSITION, mActivePosition);
   }

   protected enum NavigationItem {
      SITE_LIST(new Action() {
         public void performAction(BaseMenuDrawerActivity activity) {
            activity.returnToSiteList();
         }
      }),
      SEARCH(SearchActivity.class),
      SCAN_BARCODE(new Action() {
         public void performAction(BaseMenuDrawerActivity activity) {
            activity.launchBarcodeScanner();
         }
      }),
      FEATURED_GUIDES(FeaturedGuidesActivity.class),
      BROWSE_TOPICS(TopicActivity.class),
      USER_GUIDES(GuideCreateActivity.class),
      NEW_GUIDE(
         StepEditActivity.class,
         Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
      ),
      MEDIA_GALLERY(GalleryActivity.class),
      LOGOUT(new Action() {
         public void performAction(BaseMenuDrawerActivity activity) {
            MainApplication.get().logout(activity);
         }
      }),
      USER_FAVORITES(FavoritesActivity.class),
      YOUTUBE("https://www.youtube.com/user/iFixitYourself"),
      FACEBOOK("https://www.facebook.com/iFixit"),
      TWITTER("https://twitter.com/iFixit"),
      TEARDOWNS(TeardownsActivity.class);

      private static final int DEFAULT_FLAGS = Intent.FLAG_ACTIVITY_NO_ANIMATION;

      /**
       * Allows items to run arbitrary code when it is selected.
       */
      private interface Action {
         public void performAction(BaseMenuDrawerActivity activity);
      }

      public final Class<? extends BaseActivity> mActivityClass;
      public final int mIntentFlags;
      public final String mUrl;
      public final Action mAction;

      private NavigationItem(Class activityClass) {
         this(activityClass, DEFAULT_FLAGS);
      }

      private NavigationItem(Class<? extends BaseActivity> activityClass, int intentFlags) {
         this(activityClass, intentFlags, null, null);
      }

      private NavigationItem(String url) {
         this(null, DEFAULT_FLAGS, url, null);
      }

      private NavigationItem(Action action) {
         this(null, DEFAULT_FLAGS, null, action);
      }

      private NavigationItem(Class<? extends BaseActivity>  activityClass, int intentFlags,
       String url, Action action) {
         mActivityClass = activityClass;
         mIntentFlags = intentFlags;
         mUrl = url;
         mAction = action;
      }
   }

   private AdapterView.OnItemClickListener mItemClickListener =
    new AdapterView.OnItemClickListener() {
       @Override
       public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          NavigationItem item = (NavigationItem)view.getTag();
          AlertDialog navigationDialog = getNavigationAlertDialog(item);

          if (navigationDialog != null) {
             navigationDialog.show();
          } else {
             mMenuDrawer.closeMenu();

             MainApplication.getGaTracker().send(MapBuilder
              .createEvent("menu_action", "drawer_item_click", item.toString().toLowerCase(), null)
              .build());

             mActivePosition = position;
             mMenuDrawer.setActiveView(view, position);

             navigateMenuDrawer(item);
          }
       }
    };

   protected void navigateMenuDrawer(NavigationItem item) {
      if (item.mActivityClass != null) {
         Intent intent = new Intent(this, item.mActivityClass);
         intent.setFlags(item.mIntentFlags);
         startActivity(intent);
      } else if (item.mUrl != null) {
         Intent intent = new Intent(Intent.ACTION_VIEW);
         intent.setData(Uri.parse(item.mUrl));
         startActivity(intent);
      } else if (item.mAction != null) {
         item.mAction.performAction(this);
      } else {
         Log.e("BaseMenuDrawerActivity",
          "Could not take action on NavigationItem: " + item.toString());
      }
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
         Class<?> c = Class.forName("com.google.zxing.integration.android.IntentIntegrator");
         Class[] argTypes = new Class[] { android.app.Activity.class };
         Method initiateScan = c.getDeclaredMethod("initiateScan", argTypes);
         initiateScan.invoke(null, this);
      } catch (Exception e) {
         Toast.makeText(this, "Failed to launch QR code scanner.", Toast.LENGTH_SHORT).show();
         Log.e("BaseMenuDrawerActivity", "Cannot launch barcode scanner", e);
      }
   }

   private static class Item {
      protected String mTitle;
      protected int mIconRes;
      protected NavigationItem mItem;

      public Item(String title, int iconRes, NavigationItem item) {
         mTitle = title;
         mIconRes = iconRes;
         mItem = item;
      }
   }

   private static class Category {
      protected String mTitle;

      public Category(String title) {
         mTitle = title;
      }
   }

   private class MenuAdapter extends BaseAdapter {
      private List<Object> mItems;
      private static final int VIEW_TYPE_COUNT = 2;

      public MenuAdapter(List<Object> items) {
         mItems = items;
      }

      @Override
      public int getCount() {
         return mItems.size();
      }

      @Override
      public Object getItem(int position) {
         return mItems.get(position);
      }

      @Override
      public long getItemId(int position) {
         return position;
      }

      @Override
      public int getItemViewType(int position) {
         return getItem(position) instanceof Item ? 0 : 1;
      }

      @Override
      public int getViewTypeCount() {
         return VIEW_TYPE_COUNT;
      }

      @Override
      public boolean isEnabled(int position) {
         return getItem(position) instanceof Item;
      }

      @Override
      public boolean areAllItemsEnabled() {
         return false;
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
         View v = convertView;
         Object item = getItem(position);

         if (item instanceof Category) {
            if (v == null) {
               v = getLayoutInflater().inflate(R.layout.menu_row_category, parent, false);
            }

            ((TextView) v).setText(((Category) item).mTitle);

         } else {
            if (v == null) {
               v = getLayoutInflater().inflate(R.layout.menu_row_item, parent, false);
            }

            TextView tv = (TextView) v;
            tv.setText(((Item) item).mTitle);
            tv.setCompoundDrawablesWithIntrinsicBounds(((Item) item).mIconRes, 0, 0, 0);
            tv.setTag(((Item) item).mItem);
         }

         v.setTag(R.id.mdActiveViewPosition, position);

         if (position == mActivePosition) {
            mMenuDrawer.setActiveView(v, position);
         }

         return v;
      }
   }

   /**
    * Returns an AlertDialog to warn the user before navigating away from the Activity.
    * null is returned if the user shouldn't be warned.
    */
   public AlertDialog getNavigationAlertDialog(NavigationItem item) {
      return null;
   }

   private String buildAccountMenuCategoryTitle() {
      MainApplication app = MainApplication.get();
      boolean loggedIn = app.isUserLoggedIn();
      String title;

      if (loggedIn) {
         String username = app.getUser().getUsername();
         title = getString(R.string.account_username_title, username);
      } else {
         title = getString(R.string.account_menu_title);
      }

      return title;
   }
}
