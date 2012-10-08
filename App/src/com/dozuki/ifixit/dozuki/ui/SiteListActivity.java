package com.dozuki.ifixit.dozuki.ui;

import java.util.ArrayList;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
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

public class SiteListActivity extends SherlockFragmentActivity
 implements SearchView.OnQueryTextListener {
   private static final String SITE_LIST = "SITE_LIST";

   private ListView mSiteListView;
   private SiteListAdapter mSiteListAdapter;
   private ArrayList<Site> mSiteList;
   private SearchView mSearchView;

   private BroadcastReceiver mApiReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
         APIService.Result result = (APIService.Result)
          intent.getExtras().getSerializable(APIService.RESULT);

         if (!result.hasError()) {
            mSiteList = (ArrayList<Site>)result.getResult();
            setSiteList(mSiteList);
         } else {
            APIService.getErrorDialog(SiteListActivity.this, result.getError(),
             APIService.getSitesIntent(SiteListActivity.this)).show();
         }
      }
   };

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      getSupportActionBar().setTitle("");
      setContentView(R.layout.site_list);

      mSiteListView = (ListView)findViewById(R.id.siteListView);

      if (savedInstanceState != null) {
         mSiteList = (ArrayList<Site>)savedInstanceState.getSerializable(
          SITE_LIST);
      }

      if (mSiteList != null) {
         setSiteList(mSiteList);
      } else {
         getSiteList();
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
      ArrayList<Site> matchedSites = new ArrayList<Site>();

      for (Site site : mSiteList) {
         if (site.search(query)) {
            matchedSites.add(site);
         }
      }

      setSiteList(matchedSites);
   }

   private void cancelSearch() {
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

   public void setSiteList(ArrayList<Site> sites) {
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
          * Phones with a hardware search button open up the SearchDialog.
          * This sets focus on the SearchView but unfortunately does not
          * open the soft keyboard.
          */
         mSearchView.requestFocus();
         return true;
      } else {
         return super.onKeyUp(keyCode, event);
      }
   }

   private void getSiteList() {
      startService(APIService.getSitesIntent(this));
   }

   private class SiteListAdapter extends BaseAdapter {
      private ArrayList<Site> mSites;

      public SiteListAdapter(ArrayList<Site> sites) {
         mSites = sites;
      }

      public ArrayList<Site> getSiteList() {
         return mSites;
      }

      public int getCount() {
         return mSites.size();
      }

      public Object getItem(int position) {
         return mSites.get(position);
      }

      public long getItemId(int position) {
         return position;
      }

      public View getView(int position, View convertView, ViewGroup parent) {
         SiteRowView siteView = (SiteRowView)convertView;

         if (convertView == null) {
            siteView = new SiteRowView(SiteListActivity.this);
         }

         siteView.setSite(mSites.get(position));

         return siteView;
      }
   }
}
