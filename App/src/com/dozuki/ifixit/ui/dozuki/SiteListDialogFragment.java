package com.dozuki.ifixit.ui.dozuki;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import com.actionbarsherlock.widget.SearchView;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.ui.topic_view.TopicActivity;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.squareup.otto.Subscribe;

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
         mSiteListView.getEmptyView().setVisibility(View.GONE);
         mSearchView.setVisibility(View.GONE);
         mLoadingIndicator.setVisibility(View.VISIBLE);

         APIService.call(getActivity(), APIService.getSitesAPICall());
      } else {
         initDialog(mSiteList);
      }

      return view;
   }

   private void initDialog(ArrayList<Site> sites) {
      setSiteList(sites);
      SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

      mSearchView.setSearchableInfo(searchManager.getSearchableInfo(
       getActivity().getComponentName()));
      mSearchView.setIconifiedByDefault(false);
      mSearchView.setOnQueryTextListener((SiteListActivity) getActivity());

      View searchPlate = mSearchView.findViewById(R.id.abs__search_plate);
      searchPlate.setBackgroundResource(R.drawable.textfield_search_view_holo_light);

      ImageView searchExit = (ImageView)mSearchView.findViewById(R.id.abs__search_close_btn);
      searchExit.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_search_exit));

      ImageView searchMag = (ImageView)mSearchView.findViewById(R.id.abs__search_mag_icon);
      searchMag.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_search_dark));
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

         mLoadingIndicator.setVisibility(View.GONE);
         mSearchView.setVisibility(View.VISIBLE);
         mSiteListView.setVisibility(View.VISIBLE);
      } else {
         APIService.getErrorDialog(getActivity(), event.getError(),
          APIService.getSitesAPICall()).show();
      }
   }

   protected void cancelSearch() {
      setSiteList(mSiteList);
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
