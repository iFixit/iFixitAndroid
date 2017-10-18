package com.dozuki.ifixit.ui;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.model.user.User;


public class BaseWebViewClient extends WebViewClient {
   private View mProgressBar;

   public BaseWebViewClient(View progressBar) {
      super();

      mProgressBar = progressBar;
   }

   public void setSessionCookie(String url) {
      User user = App.get().getUser();

      if (user != null) {
         String session = user.getAuthToken();

         CookieManager.getInstance().setCookie(url, "session=" + session);
         CookieSyncManager.getInstance().sync();
      }
   }

   @Override
   public void onPageStarted(WebView view, String url, Bitmap favicon) {
      super.onPageStarted(view, url, favicon);
      mProgressBar.setVisibility(View.VISIBLE);
   }

   @Override
   public void onPageFinished(WebView view, String url) {
      super.onPageFinished(view, url);
      mProgressBar.setVisibility(View.GONE);

      view.loadUrl("javascript:(function() { " +
       "if (document.getElementById('mainHeader')) document.getElementById('mainHeader').style.display = 'none'; " +
       "if (document.getElementById('page')) document.getElementById('page').style.paddingTop = '20px'; " +
       "})()");
   }

   @Override
   public void  onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
      // Ignore SSL certificate errors in debug
      if (App.inDebug()) {
         handler.proceed();
      }
   }

}
