package com.dozuki.ifixit.ui;

import android.app.SearchManager;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.widget.EditText;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;

public class BaseSearchMenuDrawerActivity extends BaseMenuDrawerActivity {

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getSupportMenuInflater().inflate(R.menu.search_menu, menu);

      MenuItem searchItem = menu.findItem(R.id.action_search);
      searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
         @Override
         public boolean onMenuItemActionExpand(MenuItem item) {
            String hint = getString(R.string.search_site_hint, MainApplication.get().getSite().mTitle);
            ((EditText) item.getActionView().findViewById(R.id.abs__search_src_text)).setHint(hint);

            // Returns true to expand the menu item
            return true;
         }

         @Override
         public boolean onMenuItemActionCollapse(MenuItem item) {
            // Returns true to collapse the menu item
            return true;
         }
      });

      final SearchView searchView = (SearchView) searchItem.getActionView();

      if (searchView != null) {
         SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
         searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
         searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
               return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
               CursorAdapter cursorAdapter = searchView.getSuggestionsAdapter();
               Cursor cursor = cursorAdapter.getCursor();
               int suggestionIndex = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_QUERY);
               searchView.setQuery(cursor.getString(suggestionIndex), false);
               searchView.clearFocus();
               return false;
            }
         });
      }

      return super.onCreateOptionsMenu(menu);
   }
}
