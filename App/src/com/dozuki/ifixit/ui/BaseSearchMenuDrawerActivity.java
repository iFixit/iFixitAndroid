package com.dozuki.ifixit.ui;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;

public class BaseSearchMenuDrawerActivity extends BaseMenuDrawerActivity {

   private static final int MENU_BARCODE = Menu.FIRST + 1;

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.search_menu, menu);

      MenuItem searchItem =  menu.findItem(R.id.action_search);

      final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

      if (searchView != null) {
         SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
         String hint = getString(R.string.search_site_hint, App.get().getSite().mTitle);

         searchView.setQueryHint(hint);
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
    /*  final MenuItem barcodeItem = menu.findItem(R.id.action_scan_barcode);

      if (barcodeItem != null) {
         TextView barcodeView = (TextView)barcodeItem.getActionView();

         if (barcodeView != null) {
            barcodeView.setOnClickListener(new OnClickListener() {
               @Override
               public void onClick(View view) {
                  onOptionsItemSelected(barcodeItem);
               }
            });

            CheatSheet.setup(barcodeView, R.string.slide_menu_barcode_scanner);
         }

      }*/

      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case MENU_BARCODE:
            launchBarcodeScanner();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      // Only display barcode scanner menu item if it's enabled.
      if (App.get().getSite().barcodeScanningEnabled()) {
         menu
          .add(0, MENU_BARCODE, Menu.NONE, R.string.slide_menu_barcode_scanner_short)
          .setIcon(R.drawable.ic_action_qr_code);
      }

      return super.onPrepareOptionsMenu(menu);
   }
}
