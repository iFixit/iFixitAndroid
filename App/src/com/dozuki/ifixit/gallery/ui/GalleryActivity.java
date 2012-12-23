package com.dozuki.ifixit.gallery.ui;

import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.login.model.LoginEvent;
import com.dozuki.ifixit.util.IfixitActivity;
import com.squareup.otto.Subscribe;

public class GalleryActivity extends IfixitActivity {
   private MediaFragment mMediaView;

   private ActionBar mActionBar;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      setTheme(((MainApplication)getApplication()).getSiteTheme());
      getSupportActionBar().setTitle(((MainApplication)getApplication())
       .getSite().mTitle);

      mActionBar = getSupportActionBar();
      mActionBar.setTitle("");

      super.onCreate(savedInstanceState);

      setContentView(R.layout.gallery);

      mMediaView = (MediaFragment)getSupportFragmentManager().findFragmentById(
       R.id.gallery_view_fragment);

      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home:
            finish();
            return true;
         case R.id.top_camera_button:
            mMediaView.launchCamera();
            return true;
         case R.id.top_gallery_button:
            mMediaView.launchGallery();
            return true;
         case R.id.top_question_button:
            MediaFragment.createHelpDialog(this).show();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Subscribe
   public void onLogin(LoginEvent.Login event) {
      if (((MainApplication)getApplication()).isFirstTimeGalleryUser()) {
         MediaFragment.createHelpDialog(this).show();
         ((MainApplication)getApplication()).setFirstTimeGalleryUser(false);
      }
   }

   @Subscribe
   public void onLogout(LoginEvent.Logout event) {
      finish();
   }

   @Subscribe
   public void onCancel(LoginEvent.Cancel event) {
      finish();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getSupportMenuInflater();
      inflater.inflate(R.menu.gallery_menu, menu);

      return super.onCreateOptionsMenu(menu);
   }
}
