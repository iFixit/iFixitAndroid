package com.dozuki.ifixit.dozuki.ui;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.view.ui.TopicsActivity;

public class SiteListActivity extends SherlockFragmentActivity {
   private static final String SITE_LIST = "SITE_LIST";

   private ListView mSiteListView;
   private SiteListAdapter mSiteListAdapter;
   private ArrayList<Site> mSiteList;

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
      setTheme(((MainApplication)getApplication()).getSiteTheme());
      setTitle("");
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
