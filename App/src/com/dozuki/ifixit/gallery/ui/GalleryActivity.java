package com.dozuki.ifixit.gallery.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.login.model.LoginEvent;
import com.dozuki.ifixit.util.IfixitActivity;
import com.squareup.otto.Subscribe;

import org.holoeverywhere.app.AlertDialog;

public class GalleryActivity extends IfixitActivity {
   private static final String SHOWING_HELP = "SHOWING_HELP";
   private static final String MEDIA_FRAGMENT = "MEDIA_FRAGMENT";

   private ActionBar mActionBar;
   private boolean mShowingHelp = false;

   @Override
   public void onCreate(Bundle inState) {
      mActionBar = getSupportActionBar();
      mActionBar.setTitle("");
      mActionBar.setDisplayHomeAsUpEnabled(true);

      super.onCreate(inState);

      if (inState == null) {
         FragmentManager fragmentManager = getSupportFragmentManager();
         fragmentManager.beginTransaction().add(android.R.id.content, new MediaFragment(),
          MEDIA_FRAGMENT).commit();
      }

      if (inState != null) {
         mShowingHelp = inState.getBoolean(SHOWING_HELP);
      }

      if (mShowingHelp) {
         createHelpDialog().show();
      }
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putBoolean(SHOWING_HELP, mShowingHelp);
   }

   @Override
   public boolean showGalleryIcon() {
      return false;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home:
            finish();
            return true;
         case R.id.top_question_button:
            createHelpDialog().show();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Subscribe
   public void onLogin(LoginEvent.Login event) {
      if (MainApplication.get().isFirstTimeGalleryUser()) {
         createHelpDialog().show();
         MainApplication.get().setFirstTimeGalleryUser(false);
      }
   }

   @Override
   public boolean finishActivityIfLoggedOut() {
      return true;
   }

   private AlertDialog createHelpDialog() {
      mShowingHelp = true;
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder
            .setTitle(getString(R.string.media_help_title))
            .setMessage(getString(R.string.media_help_messege))
            .setPositiveButton(getString(R.string.media_help_confirm),
               new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                     mShowingHelp = false;
                     dialog.cancel();
                  }
               });

      AlertDialog dialog = builder.create();
      dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialog) {
            mShowingHelp = false;
         }
      });

      return dialog;
   }
}
