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

public class AnswersWebViewActivity extends BaseMenuDrawerActivity {
   private static final String FRAGMENT_TAG = "FRAGMENT_TAG";
   private WebViewFragment mWebView;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      super.setDrawerContent(R.layout.answers_web_view);

      getSupportActionBar().setTitle(getString(R.string.answers_forum));
      mWebView = (WebViewFragment)getSupportFragmentManager()
       .findFragmentByTag(FRAGMENT_TAG);

      if (mWebView == null) {
         mWebView = new WebViewFragment();
         Bundle args = new Bundle();
         args.putString(WebViewFragment.URL_KEY, "https://" + App.get().getSite().mDomain + "/Answers");
         mWebView.setArguments(args);

         getSupportFragmentManager().beginTransaction()
          .add(R.id.answers_webview_container, mWebView, FRAGMENT_TAG)
          .commit();
      }
   }

   @Override
   public boolean onKeyDown(final int keyCode, final KeyEvent event) {
      if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
         mWebView.goBack();
         return true;
      }
      return super.onKeyDown(keyCode, event);
   }
}
