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
import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.user.LoginEvent;
import com.dozuki.ifixit.ui.gallery.GalleryActivity;
import com.dozuki.ifixit.ui.guide.create.GuideCreateActivity;
import com.dozuki.ifixit.ui.guide.create.StepEditActivity;
import com.dozuki.ifixit.ui.guide.view.FeaturedGuidesActivity;
import com.dozuki.ifixit.ui.guide.view.OfflineGuidesActivity;
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
public abstract class BaseMenuDrawerActivity extends BaseActivity
 implements AdapterView.OnItemClickListener {
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

      if (!App.get().getSite().actionBarUsesIcon()) {
         findViewById(R.id.menu_title_wrapper).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               mMenuDrawer.toggleMenu();
            }
         });
      }
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
         startActivity(IntentFilterActivity.viewUrl(this, barcodeScannerResult));
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
         // Call IntentIntegrator.parseResult(requestCode, resultCode, intent);
         Class<?> c = Class.forName("com.google.zxing.integration.android.IntentIntegrator");
         Class[] argTypes = new Class[]{Integer.TYPE, Integer.TYPE, Intent.class};
         Method parseResult = c.getDeclaredMethod("parseActivityResult", argTypes);
         Object intentResult = parseResult.invoke(null, requestCode, resultCode, intent);

         // The request code didn't match.
         if (intentResult == null) {
            return null;
         }

         // Call intentResult.getContents().
         c = Class.forName("com.google.zxing.integration.android.IntentResult");
         argTypes = new Class[]{};
         Method getContents = c.getDeclaredMethod("getContents", argTypes);
         Object contents = getContents.invoke(intentResult);

         return (String) contents;
      } catch (Exception e) {
         Toast.makeText(this, "Failed to parse result.", Toast.LENGTH_SHORT).show();
         Log.e("BaseMenuDrawerActivity", "Failure parsing activity result", e);
         return null;
      }
   }

   private void buildSliderMenu() {
      // Add items to the menu.  The order Items are added is the order they appear in the menu.
      List<NavigationItem> items = new ArrayList<NavigationItem>();

      for (NavigationItem item : NavigationItem.values()) {
         if (item.shouldDisplay()) {
            items.add(item);
         }
      }

      // A custom ListView is needed so the drawer can be notified when it's scrolled. This is to update the position
      // of the arrow indicator.
      ListView menuList = new ListView(this);
      MenuAdapter adapter = new MenuAdapter(items);
      menuList.setAdapter(adapter);
      menuList.setOnItemClickListener(this);
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

   /**
    * Represents an item or separator in the navigation drawer. Items are displayed
    * in the order that they are defined.
    */
   protected enum NavigationItem {
      SITE_LIST(
         R.string.back_to_site_list,
         R.drawable.ic_action_list
      ) {
         @Override
         public boolean shouldDisplay() {
            return App.isDozukiApp();
         }

         @Override
         public void performNavigation(BaseMenuDrawerActivity activity) {
            activity.returnToSiteList();
         }
      },

      SEARCH(
         R.string.search,
         R.drawable.ic_action_search,
         SearchActivity.class
      ),

      SCAN_BARCODE(
         R.string.slide_menu_barcode_scanner,
         R.drawable.ic_action_qr_code
      ) {
         @Override
         public boolean shouldDisplay() {
            return App.get().getSite().barcodeScanningEnabled();
         }

         @Override
         public void performNavigation(BaseMenuDrawerActivity activity) {
            activity.launchBarcodeScanner();
         }
      },

      BROWSE_CONTENT_SEPARATOR(R.string.slide_menu_browse_content),

      BROWSE_TOPICS(
         R.string.slide_menu_browse_devices,
         R.drawable.ic_action_list_2,
         TopicActivity.class
      ) {
         @Override
         public String getTitle(Context context) {
            return context.getString(mTitle,
             App.get().getSite().getObjectNamePlural());
         }
      },

      STORE(
       R.string.parts_and_tools,
       R.drawable.ic_action_basket,
       "http://www.ifixit.com/Store"
      ) {
         @Override
         public boolean shouldDisplay() {
            return App.get().getSite().isIfixit();
         }
      },

      FEATURED_GUIDES(
         R.string.featured_guides,
         R.drawable.ic_action_star_10,
         FeaturedGuidesActivity.class
      ) {
         @Override
         public boolean shouldDisplay() {
            return App.get().getSite().isIfixit();
         }
      },

      TEARDOWNS(
         R.string.teardowns,
         R.drawable.ic_menu_stack,
         TeardownsActivity.class
      ) {
         @Override
         public boolean shouldDisplay() {
            return App.get().getSite().isIfixit();
         }
      },

      ACCOUNT_MENU_SEPARATOR() {
         @Override
         public String getTitle(Context context) {
            App app = App.get();
            boolean loggedIn = app.isUserLoggedIn();

            if (loggedIn) {
               String username = app.getUser().getUsername();
               return context.getString(R.string.account_username_title, username);
            } else {
               return context.getString(R.string.account_menu_title);
            }
         }
      },

      // Note: This doesn't use live data but rather displays guides stored
      // offline.
      USER_FAVORITES(
         R.string.slide_menu_favorite_guides,
         R.drawable.ic_menu_favorite_light,
         OfflineGuidesActivity.class
      ),

      USER_GUIDES(
         R.string.slide_menu_my_guides,
         R.drawable.ic_menu_spinner_guides,
         GuideCreateActivity.class
      ),

      NEW_GUIDE(
         R.string.slide_menu_create_new_guide,
         R.drawable.ic_menu_add_guide,
         StepEditActivity.class,
         Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
      ),

      MEDIA_GALLERY(
         R.string.slide_menu_media_gallery,
         R.drawable.ic_menu_spinner_gallery,
         GalleryActivity.class
      ),

      LOGOUT(
         R.string.slide_menu_logout,
         R.drawable.ic_action_exit
      ) {
         @Override
         public boolean shouldDisplay() {
            return App.get().isUserLoggedIn();
         }

         @Override
         public void performNavigation(BaseMenuDrawerActivity activity) {
            App.get().logout(activity);
         }
      },

      IFIXIT_EVERYWHERE_SEPARATOR(
         R.string.slide_menu_ifixit_everywhere
      ) {
         @Override
         public boolean shouldDisplay() {
            return App.get().getSite().isIfixit();
         }
      },

      YOUTUBE(
         R.string.slide_menu_youtube,
         R.drawable.ic_action_youtube,
         "https://www.youtube.com/user/iFixitYourself"
      ) {
         @Override
         public boolean shouldDisplay() {
            return App.get().getSite().isIfixit();
         }
      },

      FACEBOOK(
         R.string.slide_menu_facebook,
         R.drawable.ic_action_facebook,
         "https://www.facebook.com/iFixit"
      ) {
         @Override
         public boolean shouldDisplay() {
            return App.get().getSite().isIfixit();
         }
      },

      TWITTER(
         R.string.slide_menu_twitter,
         R.drawable.ic_action_twitter,
         "https://twitter.com/iFixit"
      ) {
         @Override
         public boolean shouldDisplay() {
            return App.get().getSite().isIfixit();
         }
      };

      private static final int NO_RES = -1;
      private static final int DEFAULT_FLAGS = Intent.FLAG_ACTIVITY_NO_ANIMATION;

      /**
       * Used for display.
       */
      public final boolean mSeparator;
      public final int mTitle;
      public final int mIcon;

      /**
       * Used for default navigation actions.
       */
      public final Class<? extends BaseActivity> mActivityClass;
      public final int mIntentFlags;
      public final String mUrl;

      private NavigationItem() {
         this(true, NO_RES, NO_RES, null, DEFAULT_FLAGS, null);
      }

      private NavigationItem(int title) {
         this(true, title, NO_RES, null, DEFAULT_FLAGS, null);
      }

      private NavigationItem(int title, int icon) {
         this(false, title, icon, null, DEFAULT_FLAGS, null);
      }

      private NavigationItem(int title, int icon, Class activityClass) {
         this(title, icon, activityClass, DEFAULT_FLAGS);
      }

      private NavigationItem(int title, int icon,
       Class<? extends BaseActivity> activityClass, int intentFlags) {
         this(false, title, icon, activityClass, intentFlags, null);
      }

      private NavigationItem(int title, int icon, String url) {
         this(false, title, icon, null, DEFAULT_FLAGS, url);
      }

      private NavigationItem(boolean separator, int title, int icon,
       Class<? extends BaseActivity>  activityClass, int intentFlags, String url) {
         mSeparator = separator;
         mTitle = title;
         mIcon = icon;
         mActivityClass = activityClass;
         mIntentFlags = intentFlags;
         mUrl = url;
      }

      /**
       * Display all by default.
       */
      public boolean shouldDisplay() {
         return true;
      }

      public String getTitle(Context context) {
         return context.getString(mTitle);
      }

      /**
       * Enums can override this to run arbitrary code when the item is selected.
       */
      public void performNavigation(BaseMenuDrawerActivity activity) {
         if (mActivityClass != null) {
            Intent intent = new Intent(activity, mActivityClass);
            intent.setFlags(mIntentFlags);
            activity.startActivity(intent);
         } else if (mUrl != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(mUrl));
            activity.startActivity(intent);
         } else {
            Log.e("BaseMenuDrawerActivity",
             "Could not take action on NavigationItem: " + toString());
         }
      }
   }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       NavigationItem item = (NavigationItem)view.getTag();
       AlertDialog navigationDialog = getNavigationAlertDialog(item);

       if (navigationDialog != null) {
          navigationDialog.show();
       } else {
          mMenuDrawer.closeMenu();

          App.getGaTracker().send(MapBuilder
           .createEvent("menu_action", "drawer_item_click", item.toString().toLowerCase(), null)
           .build());

          mActivePosition = position;
          mMenuDrawer.setActiveView(view, position);

          navigateMenuDrawer(item);
       }
    }

   protected void navigateMenuDrawer(NavigationItem item) {
      item.performNavigation(this);
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
         Class[] argTypes = new Class[]{android.app.Activity.class};
         Method initiateScan = c.getDeclaredMethod("initiateScan", argTypes);
         initiateScan.invoke(null, this);
      } catch (Exception e) {
         Toast.makeText(this, "Failed to launch QR code scanner.", Toast.LENGTH_SHORT).show();
         Log.e("BaseMenuDrawerActivity", "Cannot launch barcode scanner", e);
      }
   }

   private class MenuAdapter extends BaseAdapter {
      private List<NavigationItem> mItems;
      private static final int VIEW_TYPE_COUNT = 2;

      public MenuAdapter(List<NavigationItem> items) {
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
         return mItems.get(position).mSeparator ? 0 : 1;
      }

      @Override
      public int getViewTypeCount() {
         return VIEW_TYPE_COUNT;
      }

      @Override
      public boolean isEnabled(int position) {
         return !mItems.get(position).mSeparator;
      }

      @Override
      public boolean areAllItemsEnabled() {
         return false;
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
         View view = convertView;
         NavigationItem item = mItems.get(position);

         if (item.mSeparator) {
            if (view == null) {
               view = getLayoutInflater().inflate(R.layout.menu_row_category, parent, false);
            }

            ((TextView)view).setText(item.getTitle(BaseMenuDrawerActivity.this));
         } else {
            if (view == null) {
               view = getLayoutInflater().inflate(R.layout.menu_row_item, parent, false);
            }

            TextView textView = (TextView)view;
            textView.setText(item.getTitle(BaseMenuDrawerActivity.this));
            textView.setCompoundDrawablesWithIntrinsicBounds(item.mIcon, 0, 0, 0);
            textView.setTag(item);
         }

         view.setTag(R.id.mdActiveViewPosition, position);

         if (position == mActivePosition) {
            mMenuDrawer.setActiveView(view, position);
         }

         return view;
      }
   }

   /**
    * Returns an AlertDialog to warn the user before navigating away from the Activity.
    * null is returned if the user shouldn't be warned.
    */
   public AlertDialog getNavigationAlertDialog(NavigationItem item) {
      return null;
   }
}
