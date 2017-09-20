package com.dozuki.ifixit.ui.search;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.search.SearchResult;
import com.dozuki.ifixit.model.search.SearchResults;
import com.dozuki.ifixit.ui.BaseActivity;
import com.dozuki.ifixit.ui.EndlessRecyclerViewScrollListener;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.squareup.otto.Subscribe;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity implements SearchView.OnQueryTextListener, SearchView.OnSuggestionListener {
   private static final int ALL_POSITION = 0;
   private static final int GUIDES_POSITION = 1;
   private static final int TOPIC_POSITION = 2;
   public static final String TOPIC_SEARCH_FRAGMENT = "TOPIC_SEARCH_FRAGMENT";
   public static final String GUIDE_SEARCH_FRAGMENT = "GUIDE_SEARCH_FRAGMENT";
   private static final String SEARCH_QUERY = "SEARCH_QUERY";

   private static final int LIMIT = 20;
   private static final String QUERY_KEY = "QUERY_KEY";
   private int mOffset = 0;

   private String mQuery = "";
   private Spinner mSpinner;
   private String mCurrentTag;
   private TextView mResultCount;
   private boolean mFocusSearch = false;
   private RecyclerView mListView;
   private LinearLayoutManager mLayoutManager;
   private SearchListRecyclerAdapter mAdapter;
   private SearchResults mResults;
   private ArrayList<SearchResult> mResultsList = new ArrayList<SearchResult>();
   private TextView mEmptyText;
   private EndlessRecyclerViewScrollListener mScrollListener;
   private AppBarLayout mAppBar;
   private Handler mHandler;
   private Runnable mRunnable;
   private SearchView mSearchView;

   public static Intent viewSearch(Context context, String query) {
      Intent intent = new Intent(context, SearchActivity.class);
      intent.putExtra(SEARCH_QUERY, query);
      return intent;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      mHandler = new Handler();
      super.onCreate(savedInstanceState);
      setContentView(R.layout.search);

      setTheme(R.style.Theme_Base_TransparentActionBar);

      mAppBar = (AppBarLayout) findViewById(R.id.appbar);
      mToolbar = (Toolbar) findViewById(R.id.toolbar);

      setSupportActionBar(mToolbar);

      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeButtonEnabled(true);

      if (savedInstanceState != null) {
         mQuery = savedInstanceState.getString(QUERY_KEY);
      }

      mResultCount = (TextView) findViewById(R.id.search_result_count);
      mSpinner = (Spinner) findViewById(R.id.search_type_spinner);

      mEmptyText = (TextView) findViewById(R.id.search_results_empty_text);

      mListView = (RecyclerView) findViewById(R.id.search_results_list);

      ArrayList<String> searchTypes = new ArrayList<String>();
      searchTypes.add(ALL_POSITION, getString(R.string.all));
      searchTypes.add(GUIDES_POSITION, getString(R.string.guides));
      searchTypes.add(TOPIC_POSITION, App.get().getSite().getObjectNamePlural());

      ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
       android.R.layout.simple_spinner_dropdown_item, searchTypes);

      SearchSpinnerInteractionListener listener = new SearchSpinnerInteractionListener();
      mSpinner.setAdapter(spinnerAdapter);
      mSpinner.setOnTouchListener(listener);
      mSpinner.setOnItemSelectedListener(listener);

      mLayoutManager = new LinearLayoutManager(this);
      mListView.setLayoutManager(mLayoutManager);

      mOffset = 0;
      mListView.addOnScrollListener(new EndlessRecyclerViewScrollListener(mLayoutManager) {
         @Override
         public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
            if (totalItemsCount >= LIMIT - 3) { // We have to offset by 3 because some responses should have 20 results, but they often have less
               mOffset += LIMIT;
               Log.d("SearchFragment", "Loading more...");

               String query = buildQuery(mQuery, mSpinner.getSelectedItemPosition());
               query += "&limit=" + LIMIT + "&offset=" + mOffset;

               Api.call(SearchActivity.this, ApiCall.search(query));
            }
         }
      });

      mAdapter = new SearchListRecyclerAdapter(this);
      mListView.setAdapter(mAdapter);

      handleIntent(getIntent(), false);
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      super.onSaveInstanceState(state);

      if (mQuery != null) {
         state.putString(QUERY_KEY, mQuery);
      }
   }

   @Override
   public void onNewIntent(Intent intent) {
      setIntent(intent);
      handleIntent(intent, true);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         // Respond to the action bar's Up/Home button
         case android.R.id.home:
            finish();
            return true;
      }

      return super.onOptionsItemSelected(item);
   }

   @SuppressWarnings("unused")
   @Subscribe
   public void onSearchResults(ApiEvent.Search event) {
      if (!event.hasError()) {
         SearchResults results = event.getResult();

         mResultsList.addAll(results.mResults);

         mResults = results;

         updateViewWithResults(mResultsList, mResults.mTotalResults);
      } else {
         Api.getErrorDialog(this, event).show();
      }
   }

   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      if (mFocusSearch) {
         MenuItem searchItem = menu.findItem(R.id.action_search);
         searchItem.expandActionView();
         mFocusSearch = false;
      }

      return super.onPrepareOptionsMenu(menu);
   }

   public static String buildQuery(String query, int position) {
      if (query.length() > 0) {
         try {
            query = URLEncoder.encode(query, "UTF-8");
         } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
         }

         switch (position) {
            case ALL_POSITION:
               query += "?filter=guide,teardown,device";
               break;
            case GUIDES_POSITION:
               query += "?filter=guide,teardown";
               break;
            case TOPIC_POSITION:
               query += "?filter=device";
               break;
         }
      }

      return query;
   }

   private void handleSearch(String query) {
      if (query.length() == 0) {
         focusSearch();
         return;
      }

      mResultsList.clear();
      mAdapter.clear();

      Api.call(this, ApiCall.search(query));
   }

   private void updateViewWithResults(List<SearchResult> results, int numResults) {
      mResultCount.setText(getString(R.string.result_count, numResults));

      if (numResults == 0) {
         mEmptyText.setVisibility(View.VISIBLE);
      } else {
         mEmptyText.setVisibility(View.GONE);
      }

      mAdapter.replaceAll(results);
   }

   private void focusSearch() {
      mFocusSearch = true;
      supportInvalidateOptionsMenu();
   }

   private void handleIntent(Intent intent, boolean sendQuery) {
      Bundle extras = intent.getExtras();
      if (extras != null && extras.getString(SEARCH_QUERY) != null) {
         search(extras.getString(SEARCH_QUERY), sendQuery);
      } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
         search(intent.getStringExtra(SearchManager.QUERY), sendQuery);
      }
   }

   private void search(String query, boolean sendQuery) {
      getSupportActionBar().setTitle(query);

      SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
       SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
      suggestions.saveRecentQuery(query, null);

      mQuery = query;

      if (sendQuery) {
         handleSearch(buildQuery(mQuery, ALL_POSITION));
      } else {
         focusSearch();
      }
   }

   @Override
   public boolean onQueryTextSubmit(String query) {
      return false;
   }

   @Override
   public boolean onQueryTextChange(final String query) {
      // Clear out any waiting Runnables, so we don't have old requests happening before the new ones
      mHandler.removeCallbacks(mRunnable);

      mRunnable = new Runnable() {
         @Override
         public void run() {
            mQuery = query;

            mResultsList.clear();
            mAdapter.clear();

            Api.call(SearchActivity.this, ApiCall.search(buildQuery(query, mSpinner.getSelectedItemPosition())));
         }
      };

      mHandler.postDelayed(mRunnable, 300);

      return false;
   }

   @Override
   public boolean onSuggestionSelect(int position) {
      return false;
   }

   @Override
   public boolean onSuggestionClick(int position) {
      mSearchView.getSuggestionsAdapter().getItem(position);
      CursorAdapter cursorAdapter = mSearchView.getSuggestionsAdapter();
      Cursor cursor = cursorAdapter.getCursor();
      int suggestionIndex = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_QUERY);
      mSearchView.setQuery(cursor.getString(suggestionIndex), false);
      mSearchView.clearFocus();
      return false;
   }

   public class SearchSpinnerInteractionListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
      private boolean userSelect = false;

      @Override
      public boolean onTouch(View v, MotionEvent event) {
         userSelect = true;
         return false;
      }

      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
         if (userSelect) {
            userSelect = false;
            mResultsList.clear();
            mAdapter.clear();
            Api.call(SearchActivity.this, ApiCall.search(buildQuery(mQuery, position)));
         }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
   }

   private String getTypeFromPosition(int position) {
      if (position == GUIDES_POSITION) {
         return "guide";
      } else if (position == TOPIC_POSITION) {
         return "device";
      }
      return "";
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.search_menu, menu);

      MenuItem searchItem = menu.findItem(R.id.action_search);

      mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);

      if (mSearchView != null) {
         SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
         String hint = getString(R.string.search_site_hint, App.get().getSite().mTitle);

         mSearchView.setQueryHint(hint);
         mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
         mSearchView.setOnQueryTextListener(this);
         mSearchView.setOnSuggestionListener(this);
      }

      return super.onCreateOptionsMenu(menu);
   }

   private static List<SearchResult> filterByType(SearchResults results, String type) {
      final List<SearchResult> filteredModelList = new ArrayList<>();
      if (results != null && results.mResults != null) {
         for (SearchResult result : results.mResults) {
            if (result.getType().contains(type)) {
               filteredModelList.add(result);
            }
         }
      }

      return filteredModelList;
   }
}
