package com.dozuki.ifixit.dozuki.ui;

import java.util.ArrayList;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.dozuki.model.Site;
import com.dozuki.ifixit.util.APIEndpoint;
import com.dozuki.ifixit.util.APIReceiver;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.view.ui.TopicsActivity;

public class SiteListActivity extends SherlockFragmentActivity {
   private static final String SITE_LIST = "SITE_LIST";

   private ListView mSiteListView;
   private SiteListAdapter mSiteListAdapter;
   private ArrayList<Site> mSiteList;

   private APIReceiver mApiReceiver = new APIReceiver() {
      @SuppressWarnings("unchecked")
      public void onSuccess(Object result, Intent intent) {
         mSiteList = (ArrayList<Site>)result;
         setSiteList(mSiteList);
      }

      public void onFailure(APIService.Error error, Intent intent) {
         APIService.getErrorDialog(SiteListActivity.this, error,
          APIService.getSitesIntent(SiteListActivity.this)).show();
      }
   };

   @SuppressWarnings("unchecked")
   @Override
   public void onCreate(Bundle savedInstanceState) {
      setTitle("");
      /**
       * TODO: Combine these into a single bitwise expression and compare > 0.
       */
      boolean isLarge = ((getResources().getConfiguration().screenLayout &
       Configuration.SCREENLAYOUT_SIZE_LARGE) ==
       Configuration.SCREENLAYOUT_SIZE_LARGE);
      boolean isXLarge = ((getResources().getConfiguration().screenLayout &
       Configuration.SCREENLAYOUT_SIZE_XLARGE) ==
       Configuration.SCREENLAYOUT_SIZE_XLARGE);

      if (isLarge || isXLarge) {
         getSupportActionBar().hide();
      }

      super.onCreate(savedInstanceState);

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
      filter.addAction(APIEndpoint.SITES.mAction);
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
      mSiteList = sites;
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

   private void getSiteList() {
      startService(APIService.getSitesIntent(this));
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
