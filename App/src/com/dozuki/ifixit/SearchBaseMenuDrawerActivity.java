package com.dozuki.ifixit;

import android.app.SearchManager;
import android.widget.EditText;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.dozuki.ifixit.ui.BaseMenuDrawerActivity;

public class SearchBaseMenuDrawerActivity extends BaseMenuDrawerActivity {

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getSupportMenuInflater().inflate(R.menu.search_menu, menu);

      MenuItem searchItem = menu.findItem(R.id.action_search);
      searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
         @Override
         public boolean onMenuItemActionExpand(MenuItem item) {
            ((EditText) item.getActionView().findViewById(R.id.abs__search_src_text)).setHint(getString(R.string
             .search_site_hint, MainApplication.get().getSite().mTitle));
            return true;
         }

         @Override
         public boolean onMenuItemActionCollapse(MenuItem item) {
            return true;
         }
      });

      SearchView searchView = (SearchView) searchItem.getActionView();

      if (searchView != null) {
         SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
         searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
         searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
         searchView.setSubmitButtonEnabled(true);
      }

      return true;
   }

}
