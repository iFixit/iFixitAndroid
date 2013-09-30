package com.dozuki.ifixit.ui.search;

import android.content.SearchRecentSuggestionsProvider;

public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider {
   public final static String AUTHORITY = "com.dozuki.ifixit.ui.search.SearchSuggestionProvider";
   public final static int MODE = DATABASE_MODE_QUERIES;

   public SearchSuggestionProvider() {
      setupSuggestions(AUTHORITY, MODE);
   }
}
