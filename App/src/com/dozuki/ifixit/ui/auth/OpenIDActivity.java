package com.dozuki.ifixit.ui.auth;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.ui.BaseActivity;

public class OpenIDActivity extends BaseActivity {
   public static String SESSION = "SESSION";
   public static String LOGIN_METHOD = "LOGIN_METHOD";
   public static String SINGLE_SIGN_ON = "SINGLE_SIGN_ON";
   public static String YAHOO_LOGIN = "yahoo";
   public static String GOOGLE_LOGIN = "google";

   private String mBaseUrl;
   private String mDomain;
   private String mCustomDomain;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.open_id_view);
      setTheme(R.style.Theme_AppCompat_NoActionBar);
      overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
      Bundle extras = getIntent().getExtras();

      boolean singleSignOn = extras.getBoolean(SINGLE_SIGN_ON, false);

      Site site = ((App) getApplication()).getSite();

      mDomain = site.mDomain;
      mCustomDomain = site.mCustomDomain;
      if (mCustomDomain.length() == 0) {
         mCustomDomain = mDomain;
      }

      String loginUrl;
      if (singleSignOn) {
         loginUrl = site.mSsoUrl;
         mBaseUrl = loginUrl;
      } else {
         mBaseUrl = site.getOpenIdLoginUrl();
         final String method = extras.getString(LOGIN_METHOD);
         loginUrl = mBaseUrl + method;
      }


      WebView webView = (WebView) findViewById(R.id.open_id_web_view);

      CookieSyncManager.createInstance(this);
      CookieSyncManager.getInstance().sync();
      CookieManager.getInstance().removeAllCookie();

      WebSettings settings = webView.getSettings();
      settings.setJavaScriptEnabled(true);
      settings.setBuiltInZoomControls(true);
      settings.setSupportZoom(true);
      settings.setLoadWithOverviewMode(true);
      settings.setUseWideViewPort(true);
      settings.setAppCacheEnabled(true);
      settings.setCacheMode(WebSettings.LOAD_DEFAULT);

      webView.setWebViewClient(new WebViewClient() {
         @Override
         public void onPageFinished(WebView view, String url) {
            CookieSyncManager.getInstance().sync();

            String nakedUrl = url.replaceFirst("^(http://|https://)", "");
            String nakedBaseUrl = mBaseUrl.replaceFirst("^(http://|https://)", "");

            // Ignore page loads if it's on the openID / SAML site.
            if (nakedUrl.startsWith(nakedBaseUrl) ||
             // OR if it's NOT on one of the sites domains
             !(nakedUrl.startsWith(mDomain) || nakedUrl.startsWith(mCustomDomain)) ||
             // OR if its NOT a google or yahoo domain
             ((url.contains(YAHOO_LOGIN) || url.contains(GOOGLE_LOGIN) && !nakedUrl.startsWith(mDomain)))) {
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

            for (String pair : pairs) {
               String[] parts = pair.split("=", 2);

               // If token is found, return it to the calling activity.
               if (parts.length == 2 && parts[0].trim().equalsIgnoreCase(sessionName)) {
                  Intent result = new Intent();
                  result.putExtra(SESSION, parts[1].trim());
                  setResult(RESULT_OK, result);
                  finish();
                  return;
               }
            }

            Log.w("iFixit", "Couldn't find session in Cookie from OpenID login: " + cookie);
            Intent result = new Intent();
            setResult(RESULT_CANCELED, result);
            finish();
         }

         @Override
         public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.e("iFixit", "Error: " + description);
         }

         @Override
         public void  onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if (App.inDebug()) {
               handler.proceed(); // Ignore SSL certificate errors
            }
         }

      });

      webView.loadUrl(loginUrl);
   }
}
