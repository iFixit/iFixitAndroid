package com.dozuki.ifixit.view.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.view.model.LoginListener;
import com.dozuki.ifixit.view.model.User;

public class GalleryActivity extends SherlockFragmentActivity
 implements LoginListener {

   private static final String LOGIN_VISIBLE = "LOGIN_VISIBLE";
   private static final String LOGIN_FRAGMENT = "LOGIN_FRAGMENT";
   private static final String FIRST_TIME_USER = "FIRST_TIME_USER";
   private MediaFragment mMediaView;

   private ActionBar mActionBar;
   private boolean mLoginVisible;
   private View mLoginView;

   /**
    * TODO Are these ever initialized?
    */
   private MenuItem galleryIcon;
   private MenuItem cameraIcon;
   private MenuItem helpIcon;
   private boolean mIconsHidden;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      setTheme(((MainApplication)getApplication()).getSiteTheme());
      getSupportActionBar().setTitle(((MainApplication)getApplication())
       .getSite().mTitle);

      ((MainApplication)this.getApplication()).getUserFromPreferenceFile();

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

      if (((MainApplication)this.getApplication()).getUserFromPreferenceFile() != null) {
      User user = ((MainApplication)this.getApplication()).getUserFromPreferenceFile();
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
      LoginFragment fg = new LoginFragment();;

      inAnim = R.anim.fade_in;
      outAnim = R.anim.fade_out;

      ft.setCustomAnimations(inAnim, outAnim);
      ft.add(R.id.login_fragment, fg, LOGIN_FRAGMENT);
      ft.addToBackStack(null);
      ft.commitAllowingStateLoss();
   }

   @Override
   public void onResume() {
      super.onResume();

      if (((MainApplication)getApplication()).getUser() == null) {
         hideMenuBarIcons();
      }
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

      SharedPreferences preferenceFile = getSharedPreferences(
            MainApplication.PREFERENCE_FILE, MODE_PRIVATE);
      boolean firstTimeUser = preferenceFile.getBoolean(FIRST_TIME_USER, true);

      if (firstTimeUser) {
         MediaFragment.createHelpDialog(this).show();
         Editor editor = preferenceFile.edit();
         editor.putBoolean(FIRST_TIME_USER, false);
         editor.commit();
      }
   }

   @Override
   public void onLogout() {
      ((MainApplication)getApplication()).clearUserFromPreferenceFile();
      finish();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      if (!mIconsHidden) {
         com.actionbarsherlock.view.MenuInflater inflater =
          getSupportMenuInflater();
         inflater.inflate(R.menu.gallery_menu, menu);
      }

      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event) {
      if (keyCode == KeyEvent.KEYCODE_BACK) {
         this.finish();
         return true;
      }
      return super.onKeyDown(keyCode, event);
   }

   public void hideMenuBarIcons() {
      if (galleryIcon != null)
         galleryIcon.setVisible(false);
      if (cameraIcon != null)
         cameraIcon.setVisible(false);
      if (helpIcon != null)
         helpIcon.setVisible(false);

   }

   public void showMenuBarIcons() {

      if (galleryIcon != null) {
         galleryIcon.setVisible(true);
         // galleryIcon.invalidate();
      }
      if (cameraIcon != null) {
         cameraIcon.setVisible(true);
      }
      if (helpIcon != null) {
         helpIcon.setVisible(true);
      }

   }

}
