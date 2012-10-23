package com.dozuki.ifixit.gallery.ui;

import android.content.Intent;
import android.content.IntentFilter;
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
import com.dozuki.ifixit.gallery.model.UploadedImageInfo;
import com.dozuki.ifixit.gallery.model.UserImageList;
import com.dozuki.ifixit.login.model.LoginListener;
import com.dozuki.ifixit.login.model.User;
import com.dozuki.ifixit.login.ui.LocalImage;
import com.dozuki.ifixit.login.ui.LoginFragment;
import com.dozuki.ifixit.util.APIEndpoint;
import com.dozuki.ifixit.util.APIReceiver;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.util.Error;

public class GalleryActivity extends SherlockFragmentActivity
 implements LoginListener {

   private static final String LOGIN_VISIBLE = "LOGIN_VISIBLE";
   private static final String LOGIN_FRAGMENT = "LOGIN_FRAGMENT";
   private MediaFragment mMediaView;

   private ActionBar mActionBar;
   private boolean mLoginVisible;

   private boolean mIconsHidden;
   
   private APIReceiver mApiReceiver = new APIReceiver() {
      public void onSuccess(Object result, Intent intent) {
     
      }

      public void onFailure(Error error, Intent intent) {
         if (error.mType == Error.ErrorType.INVALID_USER) {
            LoginFragment editNameDialog = new LoginFragment();
            editNameDialog.show(getSupportFragmentManager(), "");
         }	
		}
	};


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
      LoginFragment mLogin = (LoginFragment)getSupportFragmentManager().
       findFragmentByTag(LOGIN_FRAGMENT);

      User user = ((MainApplication)getApplication()).getUserFromPreferenceFile();

      if (user != null) {
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

   /*   FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
      int inAnim, outAnim;
      LoginFragment fg = new LoginFragment();

      inAnim = R.anim.fade_in;
      outAnim = R.anim.fade_out;

      ft.setCustomAnimations(inAnim, outAnim);
      ft.add(R.id.login_fragment, fg, LOGIN_FRAGMENT);
      ft.addToBackStack(null);
      ft.commitAllowingStateLoss();*/
      LoginFragment editNameDialog = new LoginFragment();
      //editNameDialog.getDialog();
      editNameDialog.show(getSupportFragmentManager(), LOGIN_FRAGMENT);
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      outState.putBoolean(LOGIN_VISIBLE, mLoginVisible);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      boolean isLoggedIn = ((MainApplication)getApplication()).isUserLoggedIn();
      switch (item.getItemId()) {
         case android.R.id.home:
            finish();
            return true;
         case R.id.top_camera_button:
            if (!isLoggedIn) {
               return false;
            }

            mMediaView.launchCamera();
            return true;
         case R.id.top_gallery_button:
            if (!isLoggedIn) {
               return false;
            }

            mMediaView.launchGallery();
            return true;
         case R.id.top_question_button:
            if (!isLoggedIn) {
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
   public void onCancel() {
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
   public void onResume()
   {
      IntentFilter filter = new IntentFilter();
      filter.addAction(APIEndpoint.USER_IMAGES.mAction);
      filter.addAction(APIEndpoint.UPLOAD_IMAGE.mAction);
      filter.addAction(APIEndpoint.DELETE_IMAGE.mAction);
      registerReceiver(mApiReceiver, filter);
      super.onResume();
   }
   
   @Override
   public void onPause()
   {
      IntentFilter filter = new IntentFilter();
      filter.addAction(APIEndpoint.USER_IMAGES.mAction);
      filter.addAction(APIEndpoint.UPLOAD_IMAGE.mAction);
      filter.addAction(APIEndpoint.DELETE_IMAGE.mAction);
      registerReceiver(mApiReceiver, filter);
      super.onPause();
   }
}
