package com.dozuki.ifixit.ui;

import android.app.Activity;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;

public class StoreWebViewActivity extends BaseMenuDrawerActivity {
   private WebView mWebView;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.store_web_view);

      mWebView = (WebView) findViewById(R.id.store_web_view);

      WebSettings settings = mWebView.getSettings();
      settings.setJavaScriptEnabled(true);
      settings.setUseWideViewPort(true);
      settings.setAppCacheEnabled(true);
      settings.setDomStorageEnabled(true);
      settings.setCacheMode(WebSettings.LOAD_DEFAULT);

      mWebView = new WebView(this);

      mWebView.setWebViewClient(new WebViewClient());

      if (savedInstanceState != null) {
         mWebView.restoreState(savedInstanceState);
      } else {
         //mWebView.loadUrl("https://ifixit.com/Store?inApp=true");
         mWebView.loadUrl(App.get().getSite().getAPIDomain() + "/Store?inApp=true");
      }

      this.setContentView(mWebView);
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
