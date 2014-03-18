package com.dozuki.ifixit.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.guide.OnViewGuideListener;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;

public class WebViewFragment extends BaseFragment implements OnViewGuideListener {
   private WebView mWebView;
   private String mUrl;
   private Site mSite;
   private GuideWebView mWebViewClient;
   protected ProgressBar mProgressBar;

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      if (mWebView != null) {
         mWebView.destroy();
      }

      if (mSite == null) {
         mSite = ((App) getActivity().getApplication()).getSite();
      }

      View view = inflater.inflate(R.layout.web_view_fragment, container, false);
      mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
      mWebView = (WebView) view.findViewById(R.id.web_view);

      CookieSyncManager.createInstance(mWebView.getContext());
      CookieManager.getInstance().setAcceptCookie(true);

      WebSettings settings = mWebView.getSettings();
      settings.setJavaScriptEnabled(true);
      settings.setBuiltInZoomControls(true);
      settings.setSupportZoom(true);
      settings.setLoadWithOverviewMode(true);
      settings.setUseWideViewPort(true);
      settings.setAppCacheEnabled(true);
      settings.setCacheMode(WebSettings.LOAD_DEFAULT);

      mWebViewClient = new GuideWebView(this);
      mWebView.setWebViewClient(mWebViewClient);

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
      CookieSyncManager.getInstance().stopSync();
   }

   @Override
   public void onResume() {
      mWebView.onResume();
      CookieSyncManager.getInstance().startSync();

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
         mWebViewClient.setSessionCookie(url);
         mWebView.loadUrl(mUrl);
      }
   }

   public void onViewGuide(int guideid) {
      Intent intent = new Intent(getActivity(), GuideViewActivity.class);

      intent.putExtra(GuideViewActivity.GUIDEID, guideid);
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

      protected void setSessionCookie(String url) {
         User user = App.get().getUser();

         if (user != null) {
            String session = user.getAuthToken();

            CookieManager.getInstance().setCookie(url, "session=" + session);
            CookieSyncManager.getInstance().sync();
         }
      }

      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
         String[] pieces = url.split("/");
         int guideid;

         if (url.equals("http://" + mSite.mDomain + "/Guide/login")) {
            url = mUrl;
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
               Log.e("GuideWebView ArrayIndexOutOfBoundsException", e.toString());
            } catch (NumberFormatException e) {
               Log.e("GuideWebView NumberFormatException", e.toString());
            }
         }

         loadUrl(url);

         return true;
      }

      @Override
      public void onPageStarted(WebView view, String url, Bitmap favicon) {
         mProgressBar.setVisibility(View.VISIBLE);
      }

      @Override
      public void onPageFinished(WebView view, String url) {
         mProgressBar.setVisibility(View.GONE);

         if (App.get().getSite().isIfixit()) {
            // Amazon app store doesn't like our footer links to other app stores in the iFixit app,
            // so we are forced to hide them
            view.loadUrl("javascript:(function() { " +
             "document.getElementsByTagName('footer')[0].style.display = 'none'; " +
             "})()");
         }
      }
   }
}
