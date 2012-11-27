package com.dozuki.ifixit.guide_view.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.actionbarsherlock.view.Window;
import com.dozuki.ifixit.R;

public class EmbedViewActivity extends Activity {
   protected static final String HTML = "HTML";
   private String mHTML;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      requestWindowFeature((int) Window.FEATURE_NO_TITLE);
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
         WindowManager.LayoutParams.FLAG_FULLSCREEN);

      setContentView(R.layout.embed_view);
      Bundle extras = getIntent().getExtras();
      mHTML = (String) extras.get(HTML);
      WebView webView = (WebView) findViewById(R.id.full_screen_web_view);
      webView.getSettings().setUseWideViewPort(true);
      webView.getSettings().setJavaScriptEnabled(true);
      webView.getSettings().setLoadWithOverviewMode(true);
      webView.getSettings().setBuiltInZoomControls(true);
      webView.setWebChromeClient(new WebChromeClient() {
      });
      webView.loadUrl(mHTML);

   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
      final WebView webview = (WebView) findViewById(R.id.full_screen_web_view);
      webview.loadData("", "text/html", "utf-8");
   }

}
