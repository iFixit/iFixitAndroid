package com.dozuki.ifixit.util;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.gallery.ui.GalleryActivity;
import com.dozuki.ifixit.login.model.LoginEvent;
import com.dozuki.ifixit.login.ui.LogoutDialog;
import com.squareup.otto.Subscribe;

import org.holoeverywhere.app.Activity;

/**
 * Base Activity that handles registering for the event bus.
 */
public abstract class IfixitActivity extends Activity {
   /**
    * This is incredibly hacky. The issue is that Otto does not search for @Subscribed
    * methods in parent classes because the performance hit is far too big for Android
    * because of the deep inheritance with the framework and views. Because of this
    * @Subscribed methods on IfixitActivity itself don't get registered. The workaround
    * is to make an anonymous object that is registered on behalf of the parent class.
    *
    * Workaround courtesy of: https://github.com/square/otto/issues/26
    *
    * Note: The '@SuppressWarnings("unused")' is to prevent warnings that are incorrect
    * (the methods *are* actually used.
    */
   private Object loginEventListener = new Object() {
      @SuppressWarnings("unused")
      @Subscribe
      public void onLogin(LoginEvent.Login event) {
         // Update menu icons.
         supportInvalidateOptionsMenu();
      }

      @SuppressWarnings("unused")
      @Subscribe
      public void onLogout(LoginEvent.Logout event) {
         finishActivityIfPermissionDenied();
         // Update menu icons.
         supportInvalidateOptionsMenu();
      }

      @SuppressWarnings("unused")
      @Subscribe
      public void onCancel(LoginEvent.Cancel event) {
         finishActivityIfPermissionDenied();
      }
   };

   @Override
   public void onCreate(Bundle savedState) {
      setTheme(MainApplication.get().getSiteTheme());
      super.onCreate(savedState);
   }

   /**
    * If the user is coming back to this Activity make sure they still have permission
    * to view it. onRestoreInstanceState is for Activities that are being recreated
    * and onRestart is for Activities who are merely being restarted. Unfortunately
    * both are needed.
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
      supportInvalidateOptionsMenu();

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
   public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getSupportMenuInflater();
      inflater.inflate(R.menu.menu_bar, menu);

      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.gallery_button:
            Intent intent = new Intent(this, GalleryActivity.class);
            startActivity(intent);
            return true;
         case R.id.logout_button:
            LogoutDialog.create(this).show();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      MenuItem logout = menu.findItem(R.id.logout_button);
      logout.setVisible(MainApplication.get().isUserLoggedIn());

      menu.findItem(R.id.gallery_button).setVisible(showGalleryIcon());

      return super.onPrepareOptionsMenu(menu);
   }

   public boolean showGalleryIcon() {
      return true;
   }

   public boolean finishActivityIfLoggedOut() {
      return false;
   }

   public boolean neverFinishActivityOnLogout() {
      return false;
   }

   private void finishActivityIfPermissionDenied() {
      MainApplication app = MainApplication.get();

      if (app.isUserLoggedIn() || app.isLoggingIn()) {
         return;
      }

      // Finish if the site is private or activity requires authentication.
      if (!neverFinishActivityOnLogout() &&
       (finishActivityIfLoggedOut() || !app.getSite().mPublic)) {
         finish();
      }
   }
}
