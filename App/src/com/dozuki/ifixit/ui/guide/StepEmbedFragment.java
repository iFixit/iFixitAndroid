package com.dozuki.ifixit.ui.guide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Embed;
import com.dozuki.ifixit.ui.BaseFragment;
import com.dozuki.ifixit.ui.guide.view.EmbedViewActivity;

public class StepEmbedFragment extends BaseFragment {

   private static final String GUIDE_EMBED_KEY = "GUIDE_EMBED_KEY";
   private Activity mContext;
   private Resources mResources;
   private DisplayMetrics mMetrics;
   private WebView mMainWebView;
   private Embed mEmbed;

   public static StepEmbedFragment newInstance(Embed embed) {
      Bundle args = new Bundle();
      args.putSerializable(GUIDE_EMBED_KEY, embed);
      StepEmbedFragment frag = new StepEmbedFragment();
      frag.setArguments(args);

      return frag;
   }

   /////////////////////////////////////////////////////
   // LIFECYCLE
   /////////////////////////////////////////////////////

   @Override
   public void onCreate(Bundle savedInstanceState) {
      mContext = getActivity();

      super.onCreate(savedInstanceState);

      mResources = mContext.getResources();

      mMetrics = new DisplayMetrics();
      mContext.getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

      // Inflate the layout for this fragment
      View v = ((LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
       .inflate(R.layout.guide_step_embed, container, false);

      mMainWebView = (WebView)v.findViewById(R.id.embed_web_view);

      if (savedInstanceState != null) {
         mMainWebView.restoreState(savedInstanceState);
      }

      Bundle extras = getArguments();
      if (extras != null) {
         mEmbed = (Embed) extras.getSerializable(GUIDE_EMBED_KEY);
      }

      WebSettings settings = mMainWebView.getSettings();
      settings.setUseWideViewPort(true);
      settings.setJavaScriptEnabled(true);
      settings.setSupportZoom(false);
      settings.setLoadWithOverviewMode(true);
      settings.setAppCacheEnabled(true);
      settings.setCacheMode(WebSettings.LOAD_DEFAULT);

      mMainWebView.setWebViewClient(new WebViewClient() {

         public void onPageFinished(WebView view, String url) {
            mMainWebView.setVisibility(View.VISIBLE);

            super.onPageFinished(view, url);
         }
      });

      mMainWebView.setOnTouchListener(new View.OnTouchListener() {

         @Override
         public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_UP) {
               String url = (String) v.getTag();
               if (url.equals("") || url.indexOf(".") == 0) {
                  return true;
               }
               Intent i = new Intent(mContext, EmbedViewActivity.class);
               i.putExtra(EmbedViewActivity.HTML, url);
               startActivity(i);
            }
            return true;
         }
      });

      if (mEmbed != null) {
         mMainWebView.loadData("<html><body>" + mEmbed.mHtml + "</body></html>", "text/html; charset=UTF-8", null);
         mMainWebView.setTag(mEmbed.mSourceUrl);
         mMainWebView.setLayoutParams(
          fitToSpace(mMainWebView, mEmbed.mWidth, mEmbed.mHeight));
      }

      return v;
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      if (mMainWebView != null) {
         mMainWebView.saveState(outState);
      }
   }

   @Override
   public void onDestroyView() {
      mMainWebView.stopLoading();

      super.onDestroyView();
   }

   @Override
   public void onDestroy() {
      if (mMainWebView != null) {
         mMainWebView.destroy();
         mMainWebView = null;
      }

      super.onDestroy();
   }

   /////////////////////////////////////////////////////
   // NOTIFICATION LISTENERS
   /////////////////////////////////////////////////////

   private ViewGroup.LayoutParams fitToSpace(View view, float width, float height) {
      float newWidth;
      float newHeight;
      float padding = 0f;

      if (App.get().inPortraitMode()) {
         padding = viewPadding(R.dimen.page_padding);

         newWidth = mMetrics.widthPixels - padding;
         newHeight = newWidth * (height / width);
      } else {
         padding += navigationHeight();

         newHeight = ((mMetrics.heightPixels - padding) * 3f) / 5f;
         newWidth = (newHeight * (width / height));

         // Correct height to match ratio of image
         newHeight = newWidth * (height / width);
      }

      //fitProgressIndicator(newWidth, newHeight);

      ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
      layoutParams.width = (int) (newWidth - .5f);
      layoutParams.height = (int) (newHeight - .5f);

      return layoutParams;
   }

   private float navigationHeight() {
      int indicatorHeight = 50;
      int actionBarHeight = mResources.getDimensionPixelSize(
       com.actionbarsherlock.R.dimen.abs__action_bar_default_height);

      float pagePadding = viewPadding(R.dimen.page_padding);

      return actionBarHeight + indicatorHeight + pagePadding;
   }

   private float viewPadding(int view) {
      return mResources.getDimensionPixelSize(view) * 2f;
   }
}
