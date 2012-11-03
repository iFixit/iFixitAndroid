package com.dozuki.ifixit.dozuki.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.WazaBe.HoloEverywhere.LayoutInflater;
import com.WazaBe.HoloEverywhere.app.Activity;
import com.WazaBe.HoloEverywhere.sherlock.SDialogFragment;
import com.actionbarsherlock.widget.SearchView;
import com.dozuki.ifixit.R;

public class SiteListDialogFragment extends SDialogFragment {
   private SiteListActivity mSiteListActivity;
   private ListView mSiteListView;
   private SearchView mSearchView;

   public static SiteListDialogFragment newInstance() {
      return new SiteListDialogFragment();
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }

   public void onAttach(Activity activity) {
      super.onAttach(activity);

      mSiteListActivity = (SiteListActivity)activity;
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      getDialog().setTitle(R.string.site_list_title);

      View view = inflater.inflate(R.layout.site_dialog_list, container, false);

      mSiteListView = (ListView)view.findViewById(R.id.siteListView);
      mSearchView = (SearchView)view.findViewById(R.id.dozuki_search_view);

      mSiteListActivity.setSiteListViews(mSiteListView, mSearchView);

      return view;
   }
}

