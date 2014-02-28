package com.dozuki.ifixit.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.*;
import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.dozuki.Site;

public class OpenIDActivity extends Activity {
   public static String SESSION = "SESSION";
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
      Bundle extras = getIntent().getExtras();

      mSingleSignOn = extras.getBoolean(SINGLE_SIGN_ON, false);
      mSite = ((App)getApplication()).getSite();

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

            // Some subdomains of ifixit.com have their session name as 'edusession'
            // so it doesn't collide with ifixit.com's session. Use 'edusession' if
            // it exists, otherwise stick to 'session'.
            String sessionName = cookie.contains("edusession") ? "edusession" : "session";

            // Cookie is a string like NAME=VALUE [; NAME=VALUE]
            String[] pairs = cookie.split(";");
            for (int i = 0; i < pairs.length; i++) {
               String[] parts = pairs[i].split("=", 2);
               // If token is found, return it to the calling activity.
               if (parts.length == 2 && parts[0].trim().equalsIgnoreCase(sessionName)) {
                  Intent result = new Intent();
                  result.putExtra(SESSION, parts[1].trim());
                  setResult(RESULT_OK, result);
                  finish();
                  return;
               }
            }

            Log.w("iFixit", "Couldn't find session in Cookie from OpenID login: " +
             cookie);
            Intent result = new Intent();
            setResult(RESULT_CANCELED, result);
            finish();
         }
      });
   }
}
