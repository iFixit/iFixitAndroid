package com.dozuki.ifixit.ui.dozuki;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.actionbarsherlock.widget.SearchView;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.ui.topic_view.TopicActivity;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.squareup.otto.Subscribe;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.DialogFragment;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.ProgressBar;

import java.util.ArrayList;

public class SiteListDialogFragment extends DialogFragment {
   private static final String SITE_LIST = "SITE_LIST";

   private ListView mSiteListView;
   private SearchView mSearchView;
   private ArrayList<Site> mSiteList;
   private ProgressBar mLoadingIndicator;

   public static SiteListDialogFragment newInstance() {
      return new SiteListDialogFragment();
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {

      if (savedInstanceState != null) {
         mSiteList = (ArrayList<Site>) savedInstanceState.getSerializable(SITE_LIST);
      }

      View view = inflater.inflate(R.layout.site_dialog_list, container, false);

      mLoadingIndicator = (ProgressBar)view.findViewById(R.id.loading_progress);
      mSiteListView = (ListView) view.findViewById(R.id.siteListView);
      mSiteListView.setEmptyView(view.findViewById(R.id.emptyView));

      mSearchView = (SearchView) view.findViewById(R.id.dozuki_search_view);

      if (mSiteList == null) {
         mSiteListView.setVisibility(View.GONE);
         mLoadingIndicator.setVisibility(View.VISIBLE);
         mSiteListView.getEmptyView().setVisibility(View.GONE);

         APIService.call((Activity)getActivity(), APIService.getSitesAPICall());
      } else {
         initDialog(mSiteList);
      }

      return view;
   }

   private void initDialog(ArrayList<Site> sites) {
      setSiteList(sites);
      SearchManager searchManager = (SearchManager) getSystemService(
       Context.SEARCH_SERVICE);

      mSearchView.setSearchableInfo(searchManager.getSearchableInfo(
       getActivity().getComponentName()));
      mSearchView.setIconifiedByDefault(false);
      mSearchView.setOnQueryTextListener((SiteListActivity)getActivity());
   }

   @Override
   public void onResume() {
      super.onResume();

      MainApplication.getBus().register(this);

      getDialog().setOnKeyListener(new OnKeyListener() {
         public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_SEARCH) {
               /**
                * Phones with a hardware search button open up the SearchDialog by
                * default. This overrides that by setting focus on the SearchView.
                * Unfortunately it does not open the soft keyboard as of now.
                */
               mSearchView.requestFocus();
               return true;
            } else {
               return false;
            }
         }
      });
   }

   @Override
   public void onPause() {
      super.onPause();

      MainApplication.getBus().unregister(this);
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putSerializable(SITE_LIST, mSiteList);
   }

   @Subscribe
   public void onSites(APIEvent.Sites event) {
      if (!event.hasError()) {
         mSiteList = event.getResult();
         initDialog(mSiteList);

         mSiteListView.setVisibility(View.VISIBLE);
         mLoadingIndicator.setVisibility(View.GONE);
      } else {
         APIService.getErrorDialog(getActivity(), event.getError(),
          APIService.getSitesAPICall()).show();
      }
   }

   public void setSiteList(ArrayList<Site> sites) {
      if (mSiteListView == null || mSiteList == null) {
         return;
      }

      final SiteListAdapter siteListAdapter = new SiteListAdapter(sites);
      mSiteListView.setAdapter(siteListAdapter);

      mSiteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> arg0, View view, int position,
          long id) {
            Intent intent = new Intent(getActivity(), TopicActivity.class);

            MainApplication.get().setSite(siteListAdapter.getSiteList().get(position));
            startActivity(intent);
         }
      });
   }

   public ArrayList<Site> getSiteList() {
      return mSiteList;
   }
}
