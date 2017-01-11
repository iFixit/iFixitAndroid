package com.dozuki.ifixit.ui;

import android.support.v4.app.DialogFragment;

import com.dozuki.ifixit.App;

/**
 * Base class for DialogFragments. Handles bus registration and unregistration
 * in onResume/onPause.
 *
 * Note: This is basically a duplicate of other Base*Fragment classes.
 */
public class BaseDialogFragment extends DialogFragment {

   @Override
   public void onResume() {
      App.getBus().register(this);
      super.onResume();
   }

   @Override
   public void onPause() {
      App.getBus().unregister(this);
      super.onPause();
   }

}
