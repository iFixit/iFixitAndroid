package com.dozuki.ifixit.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.search.Search;
import com.dozuki.ifixit.model.search.Searchable;
import com.dozuki.ifixit.ui.BaseListFragment;
import com.dozuki.ifixit.ui.EndlessScrollListener;
import com.dozuki.ifixit.util.APIEndpoint;
import com.dozuki.ifixit.util.APIService;

import java.util.ArrayList;

public class SearchFragment extends BaseListFragment {

   private static final int LIMIT = 20;
   private int mOffset = 0;

   private static final String SEACH_RESULTS_KEY = "SEARCH_RESULTS_KEY";
   private Search mSearch;
   private ArrayList<Searchable> mSearchResults;
   private SearchAdapter mAdapter;
   private EndlessScrollListener mScrollListener;

   public static SearchFragment newInstance(Search search) {
      Bundle args = new Bundle();
      args.putSerializable(SEACH_RESULTS_KEY, search);

      SearchFragment frag = new SearchFragment();
      frag.setArguments(args);

      return frag;
   }

   public SearchFragment() {
      mSearchResults = new ArrayList<Searchable>();
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.search_list, container, false);

      Bundle args = getArguments();

      if (args != null) {
         mSearch = (Search)args.getSerializable(SEACH_RESULTS_KEY);
      } else if (savedInstanceState != null) {
         mSearch = (Search)savedInstanceState.getSerializable(SEACH_RESULTS_KEY);
      }

      if (mSearch != null) {
         mSearchResults = mSearch.mResults;
      }

      mAdapter = new SearchAdapter(mSearchResults, getActivity());
      setListAdapter(mAdapter);

      return view;
   }

   @Override
   public void onStart() {
      super.onStart();

      mScrollListener = new EndlessScrollListener(getListView(), new EndlessScrollListener.RefreshList() {
         @Override
         public void onRefresh(int pageNumber) {
            mOffset += LIMIT;

            String query = ((SearchActivity)getActivity()).buildQuery(mSearch.mQuery);
            query += "&limit=" + LIMIT + "&offset=" + mOffset;

            APIService.call(getActivity(), APIService.getSearchAPICall(APIEndpoint.SEARCH, query));
         }
      });

      getListView().setOnScrollListener(mScrollListener);
   }


   @Override
   public void onSaveInstanceState(Bundle state) {
      super.onSaveInstanceState(state);

      state.putSerializable(SEACH_RESULTS_KEY, mSearch);
   }

   public void setSearchResults(Search search) {
      // If the new search query is different than the existing one, clear out the old search results.
      if (!search.mQuery.equals(mSearch.mQuery)) {
         mSearchResults.clear();
      }

      mSearch = search;

      mSearchResults.addAll(search.mResults);

      if (!mSearch.mHasMoreResults) {
         mScrollListener.noMorePages();
      } else {
         mScrollListener.notifyMorePages();
      }

      mAdapter.setSearchResults(mSearchResults);
      mAdapter.notifyDataSetChanged();
      getListView().invalidate();
   }
}
