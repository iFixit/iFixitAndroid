package com.dozuki.ifixit.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.BuildConfig;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.guide.OnViewGuideListener;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;

import okhttp3.HttpUrl;

public class WebViewFragment extends BaseFragment implements OnViewGuideListener {
   public static final String URL_KEY = "URL_KEY";
   private WebView mWebView;
   private String mUrl;
   private Site mSite;
   private GuideWebViewClient mWebViewClient;
   protected RelativeLayout mProgressBar;

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {

      Bundle args = getArguments();

      if (args != null) {
         mUrl = args.getString(URL_KEY);
      }

      if (mWebView != null) {
         mWebView.destroy();
      }

      if (mSite == null) {
         mSite = ((App) getActivity().getApplication()).getSite();
      }

      View view = inflater.inflate(R.layout.topic_answers, container, false);
      RelativeLayout progressBar = (RelativeLayout) view.findViewById(R.id.webview_progress);
      mWebView = (WebView) view.findViewById(R.id.topic_answers_webview);

      CookieManager cookieManager = CookieManager.getInstance();

      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
         //noinspection deprecation
         CookieSyncManager.createInstance(getContext());
      }

      cookieManager.setAcceptCookie(true);

      WebSettings settings = mWebView.getSettings();
      settings.setJavaScriptEnabled(true);
      settings.setDomStorageEnabled(true);
      settings.setBuiltInZoomControls(true);
      settings.setSupportZoom(true);
      settings.setLoadWithOverviewMode(true);
      settings.setUseWideViewPort(true);
      settings.setAppCacheEnabled(true);
      settings.setCacheMode(WebSettings.LOAD_DEFAULT);
      mWebViewClient = new GuideWebViewClient(progressBar, this);
      mWebView.setWebViewClient(mWebViewClient);
      mWebView.setWebChromeClient(new WebChromeClient());
      mWebView.setVerticalScrollBarEnabled(true);
      mWebView.setHorizontalScrollBarEnabled(true);

      if (savedInstanceState != null) {
         mWebView.restoreState(savedInstanceState);
      } else if (mUrl != null) {
         loadUrl(mUrl);
      }

      return view;
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
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
         CookieSyncManager.getInstance().stopSync();
      }
   }

   @Override
   public void onResume() {
      mWebView.onResume();
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
         CookieSyncManager.getInstance().startSync();
      }

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
      HttpUrl base = HttpUrl.parse(url).newBuilder()
       .addQueryParameter("utm_source", App.get().getSite().mName + "-android-" + BuildConfig.VERSION_NAME.replace(".", "-"))
       .addQueryParameter("utm_medium", "android-app")
       .build();

      mUrl = base.toString();

      if (mWebView != null) {
         mWebViewClient.setSessionCookie(url);
         mWebView.loadUrl(mUrl);
      }
   }

   public boolean canGoBack() {
      return mWebView.canGoBack();
   }

   public void goBack() {
      mWebView.goBack();
   }

   public void onViewGuide(int guideid) {
      Intent intent = new Intent(getActivity(), GuideViewActivity.class);

      intent.putExtra(GuideViewActivity.GUIDEID, guideid);
      getActivity().startActivity(intent);
   }

   private class GuideWebViewClient extends BaseWebViewClient {
      private static final int GUIDE_POSITION = 3;
      private static final int GUIDEID_POSITION = 5;
      private static final String GUIDE_URL = "Guide";
      private static final String TEARDOWN_URL = "Teardown";

      private OnViewGuideListener mGuideListener;

      public GuideWebViewClient(RelativeLayout progressBar, OnViewGuideListener guideListener) {
         super(progressBar);

         mGuideListener = guideListener;
      }

      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
         String[] pieces = url.split("/");
         int guideid;

         if (url.startsWith("^(http|https)://" + mSite.mDomain + "/Guide/login")) {
            url = mUrl;
         } else if (!Uri.parse(url).getHost().equals(mSite.mDomain)) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
         } else {
            try {
               if (pieces[GUIDE_POSITION + 1].equals("login")) {
                  url = mUrl;
               } else if (pieces[GUIDE_POSITION].equals(GUIDE_URL)
                || pieces[GUIDE_POSITION].equals(TEARDOWN_URL)) {
                  guideid = Integer.parseInt(pieces[GUIDEID_POSITION]);
                  mGuideListener.onViewGuide(guideid);
                  return true;
               }
            } catch (ArrayIndexOutOfBoundsException e) {
               Log.e("GuideWebViewClient", "ArrayIndexOutOfBoundsException: " + e.toString());
            } catch (NumberFormatException e) {
               Log.e("GuideWebViewClient", "NumberFormatException: " + e.toString());
            }
         }

         return false;
      }
   }
}
