package com.dozuki.ifixit;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewFragment extends Fragment {
   private WebView mWebView;
   private String mUrl;

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      if (mWebView != null) {
         mWebView.destroy();
      }

      mWebView = new WebView(getActivity());
      WebSettings settings = mWebView.getSettings();
      settings.setJavaScriptEnabled(true);
      settings.setBuiltInZoomControls(true);
      settings.setSupportZoom(true);
      settings.setLoadWithOverviewMode(true);
      settings.setUseWideViewPort(true);

      mWebView.setWebViewClient(new WebViewClient() {

      });

      if (savedInstanceState != null) {
         mWebView.restoreState(savedInstanceState);
      } else if (mUrl != null) {
         mWebView.loadUrl(mUrl);
      }

      return mWebView;
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      mWebView.saveState(outState);
   }

   @Override
   public void onPause() {
      super.onPause();
      mWebView.onPause();
   }

   @Override
   public void onResume() {
      mWebView.onResume();
      super.onResume();
   }

   @Override
   public void onDestroy() {
      if (mWebView != null) {
         mWebView.destroy();
         mWebView = null;
      }

      super.onDestroy();
   }

   public void loadUrl(String url) {
      mUrl = url;

      if (mWebView != null) {
         mWebView.loadUrl(mUrl);
      }
   }
}
