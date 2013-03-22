package com.dozuki.ifixit.login.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.dozuki.model.Site;

import org.holoeverywhere.app.Activity;

public class OpenIDActivity extends Activity {
   public static String LOGIN_METHOD = "LOGIN_METHOD";
   public static String SINGLE_SIGN_ON = "SINGLE_SIGN_ON";

   public static String YAHOO_LOGIN = "yahoo";
   public static String GOOGLE_LOGIN = "google";

   private WebView mWebView;
   private String mBaseUrl;
   private Site mSite;
   private boolean mSingleSignOn;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.open_id_view);
      overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
      Bundle extras = this.getIntent().getExtras();

      mSingleSignOn = extras.getBoolean(SINGLE_SIGN_ON, false);
      mSite = ((MainApplication)getApplication()).getSite();

      String loginUrl;
      if (mSingleSignOn) {
         loginUrl = mSite.mSsoUrl;
         mBaseUrl = loginUrl;
      } else {
         mBaseUrl = mSite.getOpenIdLoginUrl();
         final String method = extras.getString(LOGIN_METHOD);
         loginUrl = mBaseUrl + method;
      }

      mWebView = (WebView)findViewById(R.id.open_id_web_view);

      CookieSyncManager.createInstance(this);
      CookieSyncManager.getInstance().sync();
      CookieManager.getInstance().removeAllCookie();

      mWebView.loadUrl(loginUrl);
      mWebView.getSettings().setJavaScriptEnabled(true);

      mWebView.setWebChromeClient(new WebChromeClient() {
         // Show loading progress in activity's title bar.
         @Override
         public void onProgressChanged(WebView view, int progress) {
            setProgress(progress * 100);
         }
      });
      mWebView.setWebViewClient(new WebViewClient() {
         // When start to load page, show url in activity's title bar
         @Override
         public void onPageStarted(WebView view, String url, Bitmap favicon) {
            setTitle(url);
         }
      });

      mWebView.setWebViewClient(new WebViewClient() {
         @Override
         public void onPageFinished(WebView view, String url) {
            CookieSyncManager.getInstance().sync();
            // Ignore page loads if it's on the openID site.
            if (!url.contains(mSite.mName) || url.contains(mBaseUrl)) {
               return;
            }

            /**
             * We've been bounced back to the original site - get the cookie from cookie jar.
             */
            String cookie = CookieManager.getInstance().getCookie(url);
            if (cookie == null) {
               return;
            }

            // Cookie is a string like NAME=VALUE [; NAME=VALUE]
            String[] pairs = cookie.split(";");
            for (int i = 0; i < pairs.length; i++) {
               String[] parts = pairs[i].split("=", 2);
               // If token is found, return it to the calling activity.
               if (parts.length == 2 && parts[0].equalsIgnoreCase("session")) {
                  Intent result = new Intent();
                  result.putExtra("session", parts[1]);
                  setResult(RESULT_OK, result);
                  finish();
                  return;
               }
            }

            Log.w("iFixit", "Couldn't find session in Cookie from OpenID login");
            Intent result = new Intent();
            setResult(RESULT_CANCELED, result);
            finish();
         }
      });
   }
}
