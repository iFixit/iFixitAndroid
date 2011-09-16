package com.ifixit.guidebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;

public class GuidebookActivity extends Activity {
   protected static final String GUIDEID = "guideid";

   protected WebView mWebView;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.webview);
      mWebView = (WebView)findViewById(R.id.webView);
      mWebView.getSettings().setJavaScriptEnabled(true);
      mWebView.loadUrl("http://www.ifixit.com");
      mWebView.setWebViewClient(new GuideWebView(this));
   }

   public void viewGuide(int guideid) {
      Intent intent = new Intent(this, GuideView.class);

      intent.putExtra(GUIDEID, guideid);
      startActivity(intent);
   }

   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event) {
       if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
           mWebView.goBack();
           return true;
       }
       return super.onKeyDown(keyCode, event);
   }

}
