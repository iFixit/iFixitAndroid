package com.dozuki.ifixit.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.search.Search;
import com.dozuki.ifixit.ui.BaseFragment;

public class SearchFragment extends BaseFragment {
   private static final String SEACH_RESULTS_KEY = "SEARCH_RESULTS_KEY";
   private Search mSearch;
   private SearchAdapter mAdapter;
   private ListView mList;

   public static SearchFragment newInstance(Search search) {
      Bundle args = new Bundle();
      args.putSerializable(SEACH_RESULTS_KEY, search);

      SearchFragment frag = new SearchFragment();
      frag.setArguments(args);

      return frag;
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

      mList = (ListView)view.findViewById(R.id.search_list_view);
      mAdapter = new SearchAdapter(mSearch.mResults, getActivity());
      mList.setAdapter(mAdapter);

      return view;
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      super.onSaveInstanceState(state);

      state.putSerializable(SEACH_RESULTS_KEY, mSearch);
   }

   public void setSearchResults(Search search) {
      mSearch = search;
      mAdapter.setSearchResults(mSearch.mResults);
      mAdapter.notifyDataSetChanged();
      mList.invalidate();
   }
}
