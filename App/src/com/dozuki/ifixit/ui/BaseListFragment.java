package com.dozuki.ifixit.ui;

import com.actionbarsherlock.app.SherlockListFragment;
import com.dozuki.ifixit.MainApplication;

/**
 * Base class for ListFragments. Handles bus registration and unregistration
 * in onResume/onPause.
 *
 * Note: This is basically a duplicate of other Base*Fragment classes.
 */
public class BaseListFragment extends SherlockListFragment {
   @Override
   public void onResume() {
      MainApplication.getBus().register(this);
      super.onResume();
   }

   @Override
   public void onPause() {
      MainApplication.getBus().unregister(this);
      super.onPause();
   }
}
