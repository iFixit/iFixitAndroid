package com.dozuki.ifixit.gallery.ui;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.dozuki.ui.SiteListActivity;
import com.dozuki.ifixit.login.model.LoginListener;
import com.dozuki.ifixit.login.model.User;
import com.dozuki.ifixit.login.ui.LoginFragment;
import com.dozuki.ifixit.util.APIEndpoint;
import com.dozuki.ifixit.util.APIError;
import com.dozuki.ifixit.util.APIReceiver;

import org.holoeverywhere.app.Activity;

public class GalleryActivity extends Activity
 implements LoginListener {

   private static final String LOGIN_VISIBLE = "LOGIN_VISIBLE";

   private MediaFragment mMediaView;

   private ActionBar mActionBar;
   private boolean mLoginVisible;

   private boolean mIconsHidden;

   // TODO: Make this work.
   // private APIReceiver mApiReceiver = new APIReceiver() {
   //    public void onSuccess(Object result, Intent intent) {
   //       /**
   //        * The success are handled by the media fragment. This is here to catch
   //        * if the user has an invalid session.
   //        */
   //    }

   //    public void onFailure(APIError error, Intent intent) {
   //       if (error.mType == APIError.ErrorType.INVALID_USER) {
   //          LoginFragment editNameDialog = new LoginFragment();
   //          editNameDialog.show(getSupportFragmentManager(), "");
   //       }
   //    }
   // };

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
       findFragmentByTag(MainApplication.LOGIN_FRAGMENT);

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

      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
   }

   private void displayLogin() {
      mIconsHidden = true;
      supportInvalidateOptionsMenu();
      
      Fragment editNameDialog = new LoginFragment();
      FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
      
      transaction.add(editNameDialog, MainApplication.LOGIN_FRAGMENT);
      transaction.setTransition(android.R.anim.fade_in);
      // Commit the transaction
      transaction.commit();
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
      ((LoginListener)mMediaView).onLogin(user);
      if (((MainApplication)getApplication()).isFirstTimeGalleryUser()) {
         MediaFragment.createHelpDialog(this).show();
         ((MainApplication)getApplication()).setFirstTimeGalleryUser(false);
      }
   }

   @Override
   public void onLogout() {
      MainApplication app = ((MainApplication)getApplication());
      app.logout();
      if (!app.getSite().mPublic) {
         Intent intent = new Intent(this, SiteListActivity.class);
         startActivity(intent);
      }
      finish();
   }

   @Override
   public void onCancel() {
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
}
