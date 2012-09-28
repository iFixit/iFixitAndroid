package com.dozuki.ifixit.view.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.graphics.Bitmap;

import com.dozuki.ifixit.R;

public class OpenIDActivity extends Activity {

   public static String BASE_OPENID_URL =
      "https://www.ifixit.com/Guide/login/openid?host=";

   public static String LOGIN_METHOD = "LOGIN_METHOD";

   public static String YAHOO_LOGIN = "yahoo";
   public static String GOOGLE_LOGIN = "google";

   WebView _webView;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.open_id_view);
      overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
      Bundle extras = this.getIntent().getExtras();

      final String method = extras.getString(LOGIN_METHOD);

      _webView = (WebView) findViewById(R.id.open_id_web_view);

      CookieSyncManager.createInstance(this);
      CookieSyncManager.getInstance().sync();
      CookieManager.getInstance().removeAllCookie();

      _webView.loadUrl(BASE_OPENID_URL + method);
      _webView.getSettings().setJavaScriptEnabled(true);

      _webView.setWebChromeClient(new WebChromeClient() {
         // Show loading progress in activity's title bar.
         @Override
         public void onProgressChanged(WebView view, int progress) {
            setProgress(progress * 100);
         }
      });
      _webView.setWebViewClient(new WebViewClient() {
         // When start to load page, show url in activity's title bar
         @Override
         public void onPageStarted(WebView view, String url, Bitmap favicon) {
            setTitle(url);
         }
      });

      // CookieSyncManager.getInstance().sync();
      // CookieManager.getInstance().setCookie(BASE_OPENID_URL+method,
      // "sso-origin=SHOW_SUCCESS;");
      // CookieSyncManager.getInstance().sync();
      // CookieManager.getInstance().removeAllCookie();

      _webView.setWebViewClient(new WebViewClient() {
         @Override
         public void onPageFinished(WebView view, String url) {
            CookieSyncManager.getInstance().sync();
            // Get the cookie from cookie jar.
            Log.e("URL", url);
            if (!url.contains("ifixit")) {
               return;
            }
            if (url.contains(BASE_OPENID_URL)) {
               return;
            }

            String cookie = CookieManager.getInstance().getCookie(url);
            if (cookie == null) {
               return;
            }
            Log.e("cookie", cookie);
            // Cookie is a string like NAME=VALUE [; NAME=VALUE]
            String[] pairs = cookie.split(";");
            for (int i = 0; i < pairs.length; ++i) {
               String[] parts = pairs[i].split("=", 2);
               // If token is found, return it to the calling activity.
               if (parts.length == 2 && parts[0].equalsIgnoreCase("session")) {
                  Intent result = new Intent();
                  result.putExtra("session", parts[1]);
                  setResult(RESULT_OK, result);
                  finish();
               }
            }
         }
      });
   }

}
