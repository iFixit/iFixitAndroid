package com.dozuki.ifixit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * This activity is only around to make it easy to test guide view
 */
public class MainActivity extends Activity {
   protected static final String GUIDEID = "guideid";
   protected static final String SPLASH_URL = "http://www.ifixit.com";

   protected WebView mWebView;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      this.viewGuide(3550);
   }

   public void viewGuide(int guideid) {
      Intent intent = new Intent(this, GuideView.class);

      intent.putExtra(GUIDEID, guideid);
      startActivity(intent);
   }
}
