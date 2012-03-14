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
      mWebView = (WebView)inflater.inflate(R.layout.web_view, container, false);

      WebSettings settings = mWebView.getSettings();
      settings.setJavaScriptEnabled(true);
      settings.setBuiltInZoomControls(true);
      settings.setSupportZoom(true);
      settings.setLoadWithOverviewMode(true);
      settings.setUseWideViewPort(true);

      mWebView.setWebViewClient(new WebViewClient() {

      });

      if (mUrl != null) {
         mWebView.loadUrl(mUrl);
      }

      return mWebView;
   }

   public void loadUrl(String url) {
      mUrl = url;

      if (mWebView != null) {
         mWebView.loadUrl(mUrl);
      }
   }
}
