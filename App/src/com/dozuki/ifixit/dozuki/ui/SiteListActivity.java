package com.dozuki.ifixit.dozuki.ui;

import java.util.ArrayList;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.widget.SearchView;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.dozuki.model.Site;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.view.ui.TopicsActivity;
import com.dozuki.ifixit.dozuki.ui.SiteListAdapter;
import com.dozuki.ifixit.dozuki.ui.SiteListDialogFragment;

public class SiteListActivity extends SherlockFragmentActivity
 implements SearchView.OnQueryTextListener {
   private static final String SITE_LIST = "SITE_LIST";

   private ArrayList<Site> mSiteList;
   private SearchView mSearchView;

   private SiteListAdapter mSiteListAdapter;
   private ListView mSiteListView;
   private Boolean onTablet;

   private BroadcastReceiver mApiReceiver = new BroadcastReceiver() {
      @SuppressWarnings("unchecked")
      @Override
      public void onReceive(Context context, Intent intent) {
         APIService.Result result = (APIService.Result)
          intent.getExtras().getSerializable(APIService.RESULT);

         if (!result.hasError()) {
            mSiteList = (ArrayList<Site>)result.getResult();
            if (!onTablet)
               setSiteList(mSiteList);
         } else {
            APIService.getErrorDialog(SiteListActivity.this, result.getError(),
             APIService.getSitesIntent(SiteListActivity.this)).show();
         }
      }
   };

   @SuppressWarnings("unchecked")
   @Override
   public void onCreate(Bundle savedInstanceState) {
      setTitle("");
      Boolean isLarge = ((getResources().getConfiguration().screenLayout & 
            Configuration.SCREENLAYOUT_SIZE_LARGE) == 
             Configuration.SCREENLAYOUT_SIZE_LARGE);
      Boolean isXLarge = ((getResources().getConfiguration().screenLayout & 
            Configuration.SCREENLAYOUT_SIZE_XLARGE) == 
            Configuration.SCREENLAYOUT_SIZE_XLARGE);
     
      onTablet = (isLarge || isXLarge);
      
      if (onTablet) {
         getSupportActionBar().hide();
      }      
      
      super.onCreate(savedInstanceState);

      setContentView(R.layout.site_list);

      if (savedInstanceState != null) {
         mSiteList = (ArrayList<Site>)savedInstanceState.getSerializable(
          SITE_LIST);
      }
      
      if (mSiteList == null) {
         getSiteList();
      } 
      
      // Non-tablets just show the list view
      if (!onTablet && mSiteList != null) {
         setSiteList(mSiteList);
      } else {
         // Otherwise we set up listeners for the FragmentDialog list view
         Button siteListButton = (Button)findViewById(R.id.list_dialog_btn);
         siteListButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
               if (mSiteList != null) {
                  showSiteListDialog(mSiteList);
               }
            }
         });
      }

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

   private void search(String query) {
      String lowerQuery = query.toLowerCase();
      ArrayList<Site> matchedSites = new ArrayList<Site>();

      for (Site site : mSiteList) {
         if (site.search(lowerQuery)) {
            matchedSites.add(site);
         }
      }

      if (!onTablet)
         setSiteList(matchedSites);
   }

   private void cancelSearch() {
      if (!onTablet)
         setSiteList(mSiteList);
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putSerializable(SITE_LIST, mSiteList);
   }

   @Override
   public void onResume() {
      super.onResume();

      IntentFilter filter = new IntentFilter();
      filter.addAction(APIService.ACTION_SITES);
      registerReceiver(mApiReceiver, filter);
   }

   @Override
   public void onPause() {
      super.onPause();

      try {
         unregisterReceiver(mApiReceiver);
      } catch (IllegalArgumentException e) {
         // Do nothing. This happens in the unlikely event that
         // unregisterReceiver has been called already.
      }
   }


   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      super.onCreateOptionsMenu(menu);

      MenuInflater inflater = getSupportMenuInflater();
      inflater.inflate(R.menu.site_search_menu, menu);

      SearchManager searchManager = (SearchManager)getSystemService(
       Context.SEARCH_SERVICE);
      mSearchView = (SearchView)menu.findItem(R.id.site_search)
       .getActionView();
      mSearchView.setSearchableInfo(searchManager.getSearchableInfo(
       getComponentName()));
      mSearchView.setIconifiedByDefault(false);

      mSearchView.setOnQueryTextListener(this);

      return true;
   }

   public boolean onQueryTextChange(String newText) {
      if (newText.length() == 0) {
         cancelSearch();
      } else {
         // Perform search on every key press.
         search(newText);
      }

      return false;
   }

   public boolean onQueryTextSubmit(String query) {
      return false;
   }

   public boolean onClose() {
      return false;
   }

   protected boolean isAlwaysExpanded() {
      return false;
   }

   @Override
   public boolean onKeyUp(int keyCode, KeyEvent event) {
      if (keyCode == KeyEvent.KEYCODE_SEARCH) {
         /**
          * Phones with a hardware search button open up the SearchDialog by
          * default. This overrides that by setting focus on the SearchView.
          * Unfortunately it does not open the soft keyboard as of now.
          */
         mSearchView.requestFocus();
         return true;
      } else {
         return super.onKeyUp(keyCode, event);
      }
   }

   private void setSiteList(ArrayList<Site> sites) {
      mSiteListAdapter = new SiteListAdapter(sites);
      mSiteListView.setAdapter(mSiteListAdapter);
     
      mSiteListView.setOnItemClickListener(new OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> arg0, View view, int position,
          long id) {
            MainApplication application = ((MainApplication)getApplication());
            Intent intent = new Intent(SiteListActivity.this,
             TopicsActivity.class);
     
            application.setSite(mSiteListAdapter.getSiteList().get(position));
            startActivity(intent);
         }
      });
   }
   
   private void getSiteList() {
      startService(APIService.getSitesIntent(this));
   }
   
   private void showSiteListDialog(ArrayList<Site> sites) {
       FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
       Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
       if (prev != null) {
          ft.remove(prev);
       }
       ft.addToBackStack(null);

       // Create and show the dialog.
       DialogFragment newFragment = SiteListDialogFragment.newInstance(sites);
       newFragment.show(ft, "dialog"); 
   }
}
