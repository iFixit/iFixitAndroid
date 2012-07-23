package com.dozuki.ifixit;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

public class SiteListActivity extends SherlockFragmentActivity
 implements SearchView.OnQueryTextListener {
   private static final String SITE_LIST = "SITE_LIST";

   private ListView mSiteListView;
   private SiteListAdapter mSiteListAdapter;
   private ArrayList<Site> mSiteList;

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

            application.setSite(mSiteList.get(position));
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
      SearchView searchView = (SearchView)menu.findItem(R.id.site_search)
       .getActionView();
      searchView.setSearchableInfo(searchManager.getSearchableInfo(
       getComponentName()));
      searchView.setIconifiedByDefault(false);

      searchView.setOnQueryTextListener(this);

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


   private void getSiteList() {
      new APIHelper.APIResponder<ArrayList<Site>>() {
         public void execute() {
            APIHelper.getSites(SiteListActivity.this, this);
         }

         public void setResult(ArrayList<Site> result) {
            mSiteList = result;

            setSiteList(result);
         }

         public void error(AlertDialog dialog) {
            dialog.show();
         }
      }.execute();
   }

   private class SiteListAdapter extends BaseAdapter {
      private ArrayList<Site> mSites;

      public SiteListAdapter(ArrayList<Site> sites) {
         mSites = sites;
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
