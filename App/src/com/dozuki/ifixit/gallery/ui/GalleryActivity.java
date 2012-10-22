package com.dozuki.ifixit.gallery.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.login.model.LoginListener;
import com.dozuki.ifixit.login.model.User;
import com.dozuki.ifixit.login.ui.LoginFragment;

public class GalleryActivity extends SherlockFragmentActivity
 implements LoginListener {

   private static final String LOGIN_VISIBLE = "LOGIN_VISIBLE";
   private static final String LOGIN_FRAGMENT = "LOGIN_FRAGMENT";
   private MediaFragment mMediaView;

   private ActionBar mActionBar;
   private boolean mLoginVisible;
   private View mLoginView;

   private boolean mIconsHidden;

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
      mMediaView.noImagesText.setVisibility(View.GONE);
      mLoginView = findViewById(R.id.login_fragment);
      LoginFragment mLogin = (LoginFragment)getSupportFragmentManager().
       findFragmentByTag(LOGIN_FRAGMENT);

      User user = ((MainApplication)getApplication()).getUserFromPreferenceFile();

      if (user != null) {
         mLoginView.setVisibility(View.INVISIBLE);
         ((MainApplication)getApplication()).setUser(user);
         mMediaView.onLogin(user);
         mIconsHidden = false;
         supportInvalidateOptionsMenu();
      } else {
         mIconsHidden = true;
         if (mLogin == null) {
            displayLogin();
         }
      }

      LoginFragment.clearLoginListeners();
      LoginFragment.registerOnLoginListener(this);
      LoginFragment.registerOnLoginListener(mMediaView);

      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
   }

   private void displayLogin() {
      mIconsHidden = true;
      supportInvalidateOptionsMenu();

      FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
      int inAnim, outAnim;
      LoginFragment fg = new LoginFragment();

      inAnim = R.anim.fade_in;
      outAnim = R.anim.fade_out;

      ft.setCustomAnimations(inAnim, outAnim);
      ft.add(R.id.login_fragment, fg, LOGIN_FRAGMENT);
      ft.addToBackStack(null);
      ft.commitAllowingStateLoss();
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      outState.putBoolean(LOGIN_VISIBLE, mLoginVisible);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home:
            finish();
            return true;
         case R.id.top_camera_button:
            if (((MainApplication)getApplication()).getUser() == null) {
               return false;
            }

            mMediaView.launchCamera();
            return true;
         case R.id.top_gallery_button:
            if (((MainApplication)getApplication()).getUser() == null) {
               return false;
            }

            mMediaView.launchGallery();
            return true;
         case R.id.top_question_button:
            if (((MainApplication)getApplication()).getUser() == null) {
               return false;
            }

            MediaFragment.createHelpDialog(this).show();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Override
   public void onLogin(User user) {
      mIconsHidden = false;
      supportInvalidateOptionsMenu();

      getSupportFragmentManager().popBackStack();
      mLoginView.setVisibility(View.INVISIBLE);

      if (((MainApplication)getApplication()).isFirstTimeGalleryUser()) {
         MediaFragment.createHelpDialog(this).show();
         ((MainApplication)getApplication()).setFirstTimeGalleryUser(false);
      }
   }

   @Override
   public void onLogout() {
      ((MainApplication)getApplication()).logout();
      finish();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      if (!mIconsHidden) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.gallery_menu, menu);
      }

      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event) {
      if (keyCode == KeyEvent.KEYCODE_BACK) {
         finish();
         return true;
      }

      return super.onKeyDown(keyCode, event);
   }
}
