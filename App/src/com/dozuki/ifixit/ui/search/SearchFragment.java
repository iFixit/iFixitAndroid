package com.dozuki.ifixit.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.search.SearchResult;
import com.dozuki.ifixit.model.search.SearchResults;
import com.dozuki.ifixit.ui.BaseListFragment;
import com.dozuki.ifixit.ui.EndlessScrollListener;
import com.dozuki.ifixit.util.APIEndpoint;
import com.dozuki.ifixit.util.APIService;

import java.util.ArrayList;

public class SearchFragment extends BaseListFragment {

   private static final int LIMIT = 20;
   private int mOffset = 0;

   private static final String SEARCH_RESULTS_KEY = "SEARCH_RESULTS_KEY";
   private SearchResults mSearch;
   private ArrayList<SearchResult> mSearchResults;
   private SearchAdapter mAdapter;
   private EndlessScrollListener mScrollListener;

   public static SearchFragment newInstance(SearchResults search) {
      Bundle args = new Bundle();
      args.putSerializable(SEARCH_RESULTS_KEY, search);

      SearchFragment frag = new SearchFragment();
      frag.setArguments(args);

      return frag;
   }

   public SearchFragment() {
      mSearchResults = new ArrayList<SearchResult>();
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.search_list, container, false);

      Bundle args = getArguments();

      if (args != null) {
         mSearch = (SearchResults) args.getSerializable(SEARCH_RESULTS_KEY);
      } else if (savedInstanceState != null) {
         mSearch = (SearchResults) savedInstanceState.getSerializable(SEARCH_RESULTS_KEY);
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

      initializeScrollListener();
   }


   @Override
   public void onSaveInstanceState(Bundle state) {
      super.onSaveInstanceState(state);

      state.putSerializable(SEARCH_RESULTS_KEY, mSearch);
   }

   private void initializeScrollListener() {
      mOffset = 0;

      mScrollListener = new EndlessScrollListener(getListView(), new EndlessScrollListener.RefreshList() {
         @Override
         public void onRefresh(int pageNumber) {
            mOffset += LIMIT;

            String query = ((SearchActivity) getActivity()).buildQuery(mSearch.mQuery);
            query += "&limit=" + LIMIT + "&offset=" + mOffset;

            APIService.call(getActivity(), APIService.getSearchAPICall(query));
         }
      });

      getListView().setOnScrollListener(mScrollListener);
   }

   public void setSearchResults(SearchResults search) {
      // If the new search query is different than the existing one, clear out the old search results.
      if (!search.mQuery.equals(mSearch.mQuery)) {
         mSearchResults.clear();

         mSearch = search;

         initializeScrollListener();
      } else {
         mSearch = search;
      }

      mSearchResults.addAll(search.mResults);

      mAdapter.setSearchResults(mSearchResults);
      mAdapter.notifyDataSetChanged();

      if (!mSearch.mHasMoreResults) {
         mScrollListener.noMorePages();
      } else {
         mScrollListener.notifyMorePages();
      }

      getListView().invalidate();
   }
}
