package com.dozuki.ifixit.ui.guide;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Embed;
import com.dozuki.ifixit.model.Video;
import com.dozuki.ifixit.model.VideoThumbnail;
import com.dozuki.ifixit.model.guide.OEmbed;
import com.dozuki.ifixit.ui.guide.view.EmbedViewActivity;
import com.dozuki.ifixit.util.JSONHelper;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class StepEmbedFragment extends SherlockFragment {

   public static final String GUIDE_EMBED_KEY = "GUIDE_EMBED_KEY";
   private Activity mContext;
   private VideoThumbnail mVideoPoster;
   private Video mVideo;
   private Resources mResources;
   private DisplayMetrics mMetrics;
   private WebView mMainWebView;
   private EmbedRetriever mEmbedRet;
   private Embed mEmbed;

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
      View v = LayoutInflater.from(mContext).inflate(R.layout.guide_step_embed, container, false);

      if (mMainWebView != null) {
         mMainWebView.destroy();
      }

      if (savedInstanceState != null) {
         mMainWebView.restoreState(savedInstanceState);
      }

      Bundle extras = getArguments();
      if (extras != null) {
         mEmbed = (Embed) extras.getSerializable(GUIDE_EMBED_KEY);
      }

      mMainWebView.setLayoutParams(
       fitToSpace(mMainWebView, (float) mEmbed.getWidth(), (float) mEmbed.getHeight())
      );

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
            // mMainProgress.setVisibility(View.GONE);

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

      mMainWebView.stopLoading();
      mMainWebView.setVisibility(View.GONE);

      if (mEmbed.hasOembed()) {
         String embedUrl = mEmbed.getOembed().getURL();
         mMainWebView.loadUrl(embedUrl);
         mMainWebView.setTag(embedUrl);
      } else {
         // TODO: find the best place and way to handle the returned
         // oembed
         mEmbedRet = new EmbedRetriever();
         mEmbedRet.execute(mEmbed);
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
      if (mEmbedRet != null) {
         mEmbedRet.cancel(true);
      }
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
      float newWidth = 0f;
      float newHeight = 0f;
      float padding = 0f;

      if (MainApplication.get().inPortraitMode()) {
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
      int actionBarHeight = 0, indicatorHeight = 50;

      actionBarHeight = mResources.getDimensionPixelSize(
       com.actionbarsherlock.R.dimen.abs__action_bar_default_height);

      float pagePadding = viewPadding(R.dimen.page_padding);

      return actionBarHeight + indicatorHeight + pagePadding;
   }

   private float viewPadding(int view) {
      return mResources.getDimensionPixelSize(view) * 2f;
   }

   public class EmbedRetriever extends AsyncTask<Embed, Void, OEmbed> {

      protected OEmbed doInBackground(Embed... embed) {
         OEmbed oe = null;
         try {
            URL url = new URL(embed[0].getURL());
            URLConnection urlConnection = null;
            InputStream in = null;
            StringBuilder x = null;
            urlConnection = url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream());
            byte[] bytes = new byte[1000];

            x = new StringBuilder();

            int numRead = 0;
            while ((numRead = in.read(bytes)) >= 0) {
               x.append(new String(bytes, 0, numRead));
            }
            in.close();
            oe = JSONHelper.parseOEmbed(x.toString());
            embed[0].addOembed(oe);
            return oe;

         } catch (Exception e) {

         }
         return oe;
      }

      protected void onPostExecute(OEmbed embed) {
         if (embed != null) {
            // TODO: decide if this is ok. Most likely because the setStep
            // function isnt intensive
            if (!isCancelled()) {
               String url = mEmbed.getOembed().getURL();
               mMainWebView.loadUrl(url);
               mMainWebView.setTag(url);
            }
         }
      }
   }

}