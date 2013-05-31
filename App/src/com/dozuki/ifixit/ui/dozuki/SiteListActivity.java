package com.dozuki.ifixit.ui.dozuki;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import com.actionbarsherlock.widget.SearchView;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.ui.IfixitActivity;
import org.holoeverywhere.app.DialogFragment;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.ListView;

import java.util.ArrayList;

public class SiteListActivity extends IfixitActivity
 implements SearchView.OnQueryTextListener {

   private Button mSiteListButton;
   private SiteListDialogFragment mSiteListDialog;
   private ListView mSiteListView;
   private SearchView mSearchView;

   @SuppressWarnings("unchecked")
   @Override
   public void onCreate(Bundle savedInstanceState) {
      getSupportActionBar().hide();

      super.onCreate(savedInstanceState);

      setContentView(R.layout.site_list);

      mSiteListButton = (Button) findViewById(R.id.list_dialog_btn);
      Typeface btnType = Typeface.createFromAsset(getAssets(), "fonts/ProximaNovaRegular.ttf");
      mSiteListButton.setTypeface(btnType);

      mSiteListButton.setOnClickListener(new OnClickListener() {
         public void onClick(View view) {
            // Show site list dialog
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.addToBackStack(null);

            // Create and show the dialog.
            mSiteListDialog = SiteListDialogFragment.newInstance();
            mSiteListDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Holo_Theme_Dialog_Light);
            mSiteListDialog.show(ft);
         }
      });

      handleIntent(getIntent());
   }

   @Override
   protected void onNewIntent(Intent intent) {
      setIntent(intent);
      handleIntent(intent);
   }

   private void handleIntent(Intent intent) {
      if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
         String query = intent.getStringExtra(SearchManager.QUERY);
         search(query);
      }
   }

   @Override
   public boolean onQueryTextChange(String newText) {
      if (newText.length() == 0) {
         mSiteListDialog.cancelSearch();
      } else {
         // Perform search on every key press.
         search(newText);
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

      for (Site site : mSiteListDialog.getSiteList()) {
         if (site.search(lowerQuery)) {
            matchedSites.add(site);
         }
      }

      mSiteListDialog.setSiteList(matchedSites);
   }

   @Override
   public boolean onKeyUp(int keyCode, KeyEvent event) {
      if (keyCode == KeyEvent.KEYCODE_SEARCH) {
         /**
          * We want to ignore the hardware search button if the dialog doesn't handle it.
          */
         return true;
      } else {
         return super.onKeyUp(keyCode, event);
      }
   }

   @Override
   public boolean neverFinishActivityOnLogout() {
      return true;
   }

   @Override
   public void onResume() {
      MainApplication.get().setSite(Site.getSite("dozuki"));

      super.onResume();
   }
}
