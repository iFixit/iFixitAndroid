package com.dozuki.ifixit;

import com.dozuki.ifixit.WebViewFragment;

import android.content.Intent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewFragment extends Fragment implements OnViewGuideListener {
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

      mWebView.setWebViewClient(new GuideWebView(this));

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

      if (mWebView != null) {
         mWebView.saveState(outState);
      }
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

   public void onViewGuide(int guideid) {
      Intent intent = new Intent(getActivity(), GuideView.class);

      intent.putExtra(TopicGuideListFragment.GUIDEID, guideid);
      getActivity().startActivity(intent);
   }

   private class GuideWebView extends WebViewClient {
      private static final int GUIDE_POSITION = 3;
      private static final int GUIDEID_POSITION = 5;
      private static final String GUIDE_URL = "Guide";
      private static final String TEARDOWN_URL = "Teardown";

      private OnViewGuideListener mGuideListener;

      public GuideWebView(OnViewGuideListener guideListener) {
         mGuideListener = guideListener;
      }

      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
         String[] pieces = url.split("/");
         int guideid;

         try {
            if (pieces[GUIDE_POSITION].equals(GUIDE_URL) ||
             pieces[GUIDE_POSITION].equals(TEARDOWN_URL)) {
               guideid = Integer.parseInt(pieces[GUIDEID_POSITION]);
               mGuideListener.onViewGuide(guideid);
               return true;
            }
         }
         catch (ArrayIndexOutOfBoundsException e) {}
         catch (NumberFormatException e) {}

         view.loadUrl(url);

         return true;
      }
   }
}
