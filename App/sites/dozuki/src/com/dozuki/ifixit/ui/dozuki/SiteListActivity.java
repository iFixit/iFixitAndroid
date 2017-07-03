package com.dozuki.ifixit.ui.dozuki;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SearchView;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.user.LoginEvent;
import com.dozuki.ifixit.ui.BaseActivity;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.dozuki.ifixit.util.api.Api;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class SiteListActivity extends BaseActivity
 implements SearchView.OnQueryTextListener {
   private static final String SITE_LIST = "SITE_LIST";
   private static final String SITE_LIST_DIALOG = "SITE_LIST_DIALOG";

   private Button mSiteListButton;
   private SiteListDialogFragment mSiteListDialog;
   private ArrayList<Site> mSiteList;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (savedInstanceState != null) {
         mSiteList = (ArrayList<Site>)savedInstanceState.getSerializable(SITE_LIST);
      }

      if (mSiteList == null) {
         Api.call(this, ApiCall.sites());
      }

      setTheme(R.style.Theme_Dozuki);

      setContentView(R.layout.site_list);

      mSiteListButton = (Button)findViewById(R.id.list_dialog_btn);
      Typeface btnType = Typeface.createFromAsset(getAssets(), "fonts/ProximaNovaRegular.ttf");
      mSiteListButton.setTypeface(btnType);

      mSiteListButton.setOnClickListener(new OnClickListener() {
         public void onClick(View view) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            mSiteListDialog = SiteListDialogFragment.newInstance();
            mSiteListDialog.setSites(mSiteList, false);
            mSiteListDialog.setStyle(DialogFragment.STYLE_NO_TITLE,
             android.R.style.Theme_Holo_Light_DialogWhenLarge);
            mSiteListDialog.show(ft, SITE_LIST_DIALOG);
         }
      });

      mSiteListDialog = (SiteListDialogFragment)getSupportFragmentManager().
       findFragmentByTag(SITE_LIST_DIALOG);

      App.sendScreenView("/sitelist");
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putSerializable(SITE_LIST, mSiteList);
   }

   @Override
   public void onResume() {
      if (!App.get().isLoggingIn()) {
         /**
          * Reset the current site to Dozuki anytime this Activity resumes
          * unless the user is logging in because the current site needs to be
          * set for login so the API call goes to the right site.
          */
         App.get().setSite(Site.getSite("dozuki"));
      }

      super.onResume();
   }

   @Override
   public void onCancelLogin(LoginEvent.Cancel event) {
      // Reset to Dozuki when login is cancelled.
      App.get().setSite(Site.getSite("dozuki"));
   }

   @Override
   protected void onNewIntent(Intent intent) {
      setIntent(intent);
      handleIntent(intent);
   }

   @Subscribe
   public void onSites(ApiEvent.Sites event) {
      if (!event.hasError()) {
         mSiteList = event.getResult();
         if (mSiteListDialog != null) {
            mSiteListDialog.setSites(mSiteList, true);
         }
      } else {
         Api.getErrorDialog(this, event).show();
      }
   }

   private void handleIntent(Intent intent) {
      if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
         String query = intent.getStringExtra(SearchManager.QUERY);
         search(query);
      }
   }

   @Override
   public boolean onQueryTextChange(String newText) {
      if (mSiteListDialog != null) {
         if (newText.length() == 0) {
            mSiteListDialog.setSites(mSiteList, true);
         } else {
            // Perform search on every key press.
            search(newText);
         }
      }

      return false;
   }

   @Override
   public boolean onQueryTextSubmit(String query) {
      return false;
   }

   private void search(String query) {
      String lowerQuery = query.toLowerCase();
      ArrayList<Site> matchedSites = new ArrayList<Site>();

      for (Site site : mSiteList) {
         if (site.search(lowerQuery)) {
            matchedSites.add(site);
         }
      }

      mSiteListDialog.setSites(matchedSites, true);
   }

   @Override
   public boolean onKeyUp(int keyCode, KeyEvent event) {
      /**
       * We want to ignore the hardware search button if the dialog doesn't handle it.
       */
      return keyCode == KeyEvent.KEYCODE_SEARCH || super.onKeyUp(keyCode, event);
   }

   @Override
   public boolean neverFinishActivityOnLogout() {
      return true;
   }
}
