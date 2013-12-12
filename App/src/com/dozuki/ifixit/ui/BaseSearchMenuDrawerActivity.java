package com.dozuki.ifixit.ui;

import android.app.SearchManager;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.CheatSheet;
import com.google.analytics.tracking.android.MapBuilder;

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

            MainApplication.getGaTracker().send(MapBuilder.createEvent("ui_action", "search", "action_bar_search",
             null).build());

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

         applyThemeToSearchView(searchView);

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

      // Trigger onOptionsItemSelected for the custom menu item because it doesn't
      // happen automatically.
      final MenuItem barcodeItem = menu.findItem(R.id.action_scan_barcode);
      barcodeItem.getActionView().setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View view) {
            onOptionsItemSelected(barcodeItem);
         }
      });

      CheatSheet.setup(barcodeItem.getActionView(), R.string.slide_menu_barcode_scanner);

      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.action_scan_barcode:
            launchBarcodeScanner();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      // Only display barcode scanner menu item if it's enabled.
      menu.findItem(R.id.action_scan_barcode).setVisible(
       MainApplication.get().getSite().barcodeScanningEnabled());

      return super.onPrepareOptionsMenu(menu);
   }

   /**
    * SearchView's AutoCompleteTextView and other elements are not styleable via XML,
    * so we have to find the view by its ID and apply the style programmatically.
    *
    * Changes the color of the underline of the search edit text field.
    * @param searchView
    */
   private void applyThemeToSearchView(SearchView searchView) {
      View searchPlate = searchView.findViewById(R.id.abs__search_src_text);
      searchView.findViewById(R.id.abs__search_plate).setBackgroundColor(Color.TRANSPARENT);
      TypedValue typedValue = new TypedValue();
      getTheme().resolveAttribute(R.attr.doz__editTextBackground, typedValue, true);

      searchPlate.setBackgroundResource(typedValue.resourceId);
   }
}
