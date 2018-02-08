package com.dozuki.ifixit.ui.search;

import android.content.SearchRecentSuggestionsProvider;

import com.dozuki.ifixit.BuildConfig;

public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider {
   public final static String AUTHORITY = BuildConfig.SEARCH_PROVIDER_AUTHORITY;
   public final static int MODE = DATABASE_MODE_QUERIES;

   public SearchSuggestionProvider() {
      setupSuggestions(AUTHORITY, MODE);
   }
}
