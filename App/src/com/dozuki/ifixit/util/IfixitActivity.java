package com.dozuki.ifixit.util;

import com.dozuki.ifixit.MainApplication;

import org.holoeverywhere.app.Activity;

/**
 * Base Activity that handles registering for the event bus.
 */
public abstract class IfixitActivity extends Activity {
   @Override
   public void onResume() {
      super.onResume();

      MainApplication.getBus().register(this);
   }

   @Override
   public void onPause() {
      super.onPause();

      MainApplication.getBus().unregister(this);
   }
}
