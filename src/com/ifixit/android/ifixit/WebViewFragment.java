package com.ifixit.android.ifixit;

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

      mWebView.setWebViewClient(new WebViewClient() {
      
      });

      WebSettings settings = mWebView.getSettings();
      settings.setJavaScriptEnabled(true);

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

   /*
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      TextView text = new TextView(getActivity());

      text.setText("hi");

      return text;
   }
   */
}
