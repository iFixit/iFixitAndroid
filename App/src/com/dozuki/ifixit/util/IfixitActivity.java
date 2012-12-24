package com.dozuki.ifixit.util;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.gallery.ui.GalleryActivity;
import com.dozuki.ifixit.login.ui.LoginFragment;

import org.holoeverywhere.app.Activity;

/**
 * Base Activity that handles registering for the event bus.
 */
public abstract class IfixitActivity extends Activity {
   @Override
   public void onCreate(Bundle savedState) {
      setTheme(MainApplication.get().getSiteTheme());
      super.onCreate(savedState);
   }

   @Override
   public void onResume() {
      super.onResume();

      MainApplication.getBus().register(this);
   }

   @Override
   public void onPause() {
      super.onPause();

      MainApplication.getBus().unregister(this);
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
            LoginFragment.getLogoutDialog(this).show();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      MenuItem logout = menu.findItem(R.id.logout_button);
      logout.setVisible(MainApplication.get().isUserLoggedIn());

      return super.onPrepareOptionsMenu(menu);
   }
}
