package com.dozuki.ifixit.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.login.LoginEvent;
import com.dozuki.ifixit.ui.gallery.GalleryActivity;
import com.dozuki.ifixit.ui.guide.create.GuideCreateActivity;
import com.dozuki.ifixit.ui.guide.create.GuideIntroActivity;
import com.dozuki.ifixit.ui.topic_view.TopicActivity;
import com.squareup.otto.Subscribe;
import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Base Activity that performs various functions that all Activities in this app
 * should do. Such as:
 * <p/>
 * Registering for the event bus. Setting the current site's theme. Finishing
 * the Activity if the user logs out but the Activity requires authentication.
 * Displaying various menu icons.
 */
public abstract class IfixitActivity extends Activity {

   private static final String STATE_ACTIVE_POSITION = "com.dozuki.ifixit.ui.ifixitActivity.activePosition";
   private static final String STATE_CONTENT_TEXT = "com.dozuki.ifixit.ui.ifixitActivity.contentText";

   private static final int MENU_OVERFLOW = 1;


   /**
    * Slide Out Menu Drawer
    */
   private MenuDrawer mMenuDrawer;

   private MenuAdapter mAdapter;
   private ListView mList;

   private int mActivePosition = -1;

   /**
    * This is incredibly hacky. The issue is that Otto does not search for @Subscribed
    * methods in parent classes because the performance hit is far too big for
    * Android because of the deep inheritance with the framework and views.
    * Because of this
    *
    * @Subscribed methods on IfixitActivity itself don't get registered. The
    * workaround is to make an anonymous object that is registered
    * on behalf of the parent class.
    * <p/>
    * Workaround courtesy of:
    * https://github.com/square/otto/issues/26
    * <p/>
    * Note: The '@SuppressWarnings("unused")' is to prevent
    * warnings that are incorrect (the methods *are* actually used.
    */
   private Object loginEventListener = new Object() {
      @SuppressWarnings("unused")
      @Subscribe
      public void onLogin(LoginEvent.Login event) {
         // Reload app to update the menu to include the User name and logout button
         buildSliderMenu();
         mMenuDrawer.invalidate();
      }

      @SuppressWarnings("unused")
      @Subscribe
      public void onLogout(LoginEvent.Logout event) {
         finishActivityIfPermissionDenied();

         // Reload app to remove username and logout button from menu
         buildSliderMenu();

         mMenuDrawer.closeMenu(true);
         mMenuDrawer.invalidate();
      }

      @SuppressWarnings("unused")
      @Subscribe
      public void onCancel(LoginEvent.Cancel event) {
         finishActivityIfPermissionDenied();
      }
   };

   public enum Navigation {
      SEARCH, FEATURED_GUIDES, BROWSE_TOPICS, USER_GUIDES, NEW_GUIDE, MEDIA_GALLERY, LOGOUT,
      YOUTUBE, FACEBOOK, TWITTER, HELP, ABOUT, NOVALUE;

      public static Navigation navigate(String str) {
         try {
            return valueOf(str.toUpperCase());
         } catch (Exception ex) {
            return NOVALUE;
         }
      }
   }

   private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         Intent intent;
         Context context = parent.getContext();
         String url = "";
         mActivePosition = position;
         mMenuDrawer.setActiveView(view, position);

         switch (Navigation.navigate((String) view.getTag())) {
            case SEARCH:
            case FEATURED_GUIDES:
            case BROWSE_TOPICS:
               intent = new Intent(context, TopicActivity.class);
               intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
               startActivity(intent);
               break;

            case USER_GUIDES:
               intent = new Intent(context, GuideCreateActivity.class);
               intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
               startActivity(intent);
               break;

            case NEW_GUIDE:
               intent = new Intent(context, GuideIntroActivity.class);
               intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
               startActivity(intent);
               break;

            case MEDIA_GALLERY:
               intent = new Intent(context, GalleryActivity.class);
               intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
               startActivity(intent);
               break;
            case LOGOUT:
               MainApplication.get().logout();
               break;

            case YOUTUBE:
               intent = new Intent(Intent.ACTION_VIEW);
               url = "https://www.youtube.com/user/iFixitYourself";

               intent.setData(Uri.parse(url));
               startActivity(intent);
               break;

            case FACEBOOK:
               intent = new Intent(Intent.ACTION_VIEW);
               url = "https://www.facebook.com/iFixit";

               intent.setData(Uri.parse(url));
               startActivity(intent);
               break;

            case TWITTER:
               intent = new Intent(Intent.ACTION_VIEW);
               url = "https://twitter.com/iFixit";

               intent.setData(Uri.parse(url));
               startActivity(intent);
               break;

            case HELP:
            case ABOUT:
         }
         mMenuDrawer.closeMenu();
      }
   };

   @Override
   public void onCreate(Bundle savedState) {
      /**
       * Set the current site's theme. Must be before onCreate because of
       * inflating views.
       */

      setTheme(MainApplication.get().getSiteTheme());
      setTitle("");

      super.onCreate(savedState);

      if (savedState != null) {
         mActivePosition = savedState.getInt(STATE_ACTIVE_POSITION);
      }
      mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW, Position.RIGHT);

      buildSliderMenu();
   }

   private void buildSliderMenu() {
      // Add items to the menu.  The order Items are added is the order they appear in the menu.
      List<Object> items = new ArrayList<Object>();

      //items.add(new Item(getString(R.string.slide_menu_search), R.drawable.ic_action_search, "search"));

      items.add(new Category(getString(R.string.slide_menu_browse_content)));
      //items.add(new Item(getString(R.string.slide_menu_featured_guides), R.drawable.ic_action_star_10,
      // "featured_guides"));
      items.add(new Item(getString(R.string.slide_menu_browse_devices), R.drawable.ic_action_list_2, "browse_topics"));

      items.add(new Category(buildAccountMenuCategoryTitle()));
      items.add(new Item(getString(R.string.slide_menu_my_guides), R.drawable.ic_menu_spinner_guides, "user_guides"));
      //items.add(new Item(getString(R.string.slide_menu_favorite_guides), R.drawable.ic_menu_spinner_guides, "favorite_guides"));
      items.add(new Item(getString(R.string.slide_menu_create_new_guide), R.drawable.ic_menu_add_guide, "new_guide"));
      items.add(new Item(getString(R.string.slide_menu_media_gallery), R.drawable.ic_menu_spinner_gallery, "media_gallery"));

      if (MainApplication.get().isUserLoggedIn()) {
         items.add(new Item(getString(R.string.slide_menu_logout), R.drawable.ic_action_exit, "logout"));
      }

      if (MainApplication.get().getSite().mName.compareTo("ifixit") == 0) {
         items.add(new Category(getString(R.string.slide_menu_ifixit_everywhere)));
         items.add(new Item(getString(R.string.slide_menu_youtube), R.drawable.ic_action_youtube, "youtube"));
         items.add(new Item(getString(R.string.slide_menu_facebook), R.drawable.ic_action_facebook, "facebook"));
         items.add(new Item(getString(R.string.slide_menu_twitter), R.drawable.ic_action_twitter, "twitter"));
      }

      /*items.add(new Category(getString(R.string.slide_menu_more_info)));
      items.add(new Item(getString(R.string.slide_menu_help), R.drawable.ic_action_help, "help"));
      items.add(new Item(getString(R.string.slide_menu_about), R.drawable.ic_action_info, "about")); */

      // A custom ListView is needed so the drawer can be notified when it's scrolled. This is to update the position
      // of the arrow indicator.
      mList = new ListView(this);
      mAdapter = new MenuAdapter(items);
      mList.setAdapter(mAdapter);
      mList.setOnItemClickListener(mItemClickListener);

      mMenuDrawer.setMenuView(mList);

      mMenuDrawer.setMenuSize(getResources().getDimensionPixelSize(R.dimen.menu_size));

      mMenuDrawer.setTouchMode(MenuDrawer.TOUCH_MODE_BEZEL);
      mMenuDrawer.setTouchBezelSize(10);
   }

   @Override
   protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      outState.putInt(STATE_ACTIVE_POSITION, mActivePosition);
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
   public void onRestart() {
      super.onRestart();
      finishActivityIfPermissionDenied();
   }

   @Override
   public void onResume() {
      super.onResume();

      // Invalidate the options menu in case the user has logged in or out.
      //supportInvalidateOptionsMenu();

      MainApplication.getBus().register(this);
      MainApplication.getBus().register(loginEventListener);
   }

   @Override
   public void onPause() {
      super.onPause();

      MainApplication.getBus().unregister(this);
      MainApplication.getBus().unregister(loginEventListener);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home:
            finish();
            break;
         case MENU_OVERFLOW:
            mMenuDrawer.toggleMenu();
            break;
      }

      return super.onOptionsItemSelected(item);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      MenuItem overflowItem = menu.add(0, MENU_OVERFLOW, 0, null);
      overflowItem.setIcon(R.drawable.ic_action_list);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
         overflowItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
      }

      return true;
   }

   @Override
   public void onStart() {
      this.overridePendingTransition(0, 0);
      super.onStart();
   }

   private static class Item {

      String mTitle;
      int mIconRes;
      String mTag;

      Item(String title, int iconRes, String tag) {
         mTitle = title;
         mIconRes = iconRes;
         mTag = tag;
      }
   }

   private static class Category {

      String mTitle;

      Category(String title) {
         mTitle = title;
      }
   }

   private class MenuAdapter extends BaseAdapter {

      private List<Object> mItems;

      MenuAdapter(List<Object> items) {
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
         return 2;
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
            tv.setTag(((Item) item).mTag);
         }

         v.setTag(R.id.mdActiveViewPosition, position);

         if (position == mActivePosition) {
            mMenuDrawer.setActiveView(v, position);
         }

         return v;
      }
   }

   /**
    * Finishes the Activity if the user should be logged in but isn't.
    */
   private void finishActivityIfPermissionDenied() {
      MainApplication app = MainApplication.get();

      /**
       * Never finish if user is logged in or is logging in.
       */
      if (app.isUserLoggedIn() || app.isLoggingIn()) {
         return;
      }

      /**
       * Finish if the site is private or activity requires authentication.
       */
      if (!neverFinishActivityOnLogout()
       && (finishActivityIfLoggedOut() || !app.getSite().mPublic)) {
         finish();
      }
   }

   /**
    * "Settings" methods for derived classes are found below. Decides when to
    * finish the Activity, what icons to display etc.
    */

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
    * <p/>
    * This exists because of a race condition of sorts involving logging out of
    * private Dozuki sites. SiteListActivity can't reset the current site to
    * one that is public so it is erroneously finished unless flagged
    * otherwise.
    */
   public boolean neverFinishActivityOnLogout() {
      return false;
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
