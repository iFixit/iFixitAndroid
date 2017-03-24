package com.dozuki.ifixit.ui;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;

public class StoreWebViewActivity extends BaseMenuDrawerActivity {
   private static final String STORE_FRAGMENT_TAG = "STORE_FRAGMENT_TAG";
   private WebViewFragment mWebView;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      super.setDrawerContent(R.layout.store_web_view);
      
      mWebView = (WebViewFragment)getSupportFragmentManager()
       .findFragmentByTag(STORE_FRAGMENT_TAG);

      if (mWebView == null) {
         mWebView = new WebViewFragment();
         Bundle args = new Bundle();
         args.putString(WebViewFragment.URL_KEY, "https://www.ifixit.com/Store");
         mWebView.setArguments(args);

         getSupportFragmentManager().beginTransaction()
          .add(R.id.store_web_view_container, mWebView, STORE_FRAGMENT_TAG)
          .commit();
      }
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.store_web_view_menu, menu);

      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onKeyDown(final int keyCode, final KeyEvent event) {
      if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
         mWebView.goBack();
         return true;
      }
      return super.onKeyDown(keyCode, event);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.cart_menu_item:
            mWebView.loadUrl("https://www.ifixit.com/Cart");
            return true;
      }
      return super.onOptionsItemSelected(item);
   }
}
