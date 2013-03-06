package com.dozuki.ifixit.guide_view.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_view.model.Embed;
import com.dozuki.ifixit.guide_view.model.GuideStep;
import com.dozuki.ifixit.guide_view.model.OEmbed;
import com.dozuki.ifixit.guide_view.model.StepImage;
import com.dozuki.ifixit.guide_view.model.StepLine;
import com.dozuki.ifixit.guide_view.model.StepVideo;
import com.dozuki.ifixit.guide_view.model.StepVideoThumbnail;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.JSONHelper;
import com.ifixit.android.imagemanager.ImageManager;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class GuideStepViewFragment extends Fragment {
   
   private TextView mTitle;
   private ThumbnailView mThumbs;
   private ImageView mMainImage;
   private WebView mMainWebView;
   private RelativeLayout mMainProgress;
   private RelativeLayout mVideoPlayButtonContainer;
   private GuideStep mStep;
   private ImageManager mImageManager;
   private StepTextArrayAdapter mTextAdapter;
   private ListView mLineList;
   private Typeface mFont;
   private ImageSizes mImageSizes;
   private EmbedRetriever mEmbedRet;
   private Activity mContext;
   private static Resources mResources;
   private DisplayMetrics mMetrics;
   private ImageButton mVideoPlayButton;

   public GuideStepViewFragment() { }

   public GuideStepViewFragment(ImageManager im, GuideStep step) {
      mStep = step;
      mImageManager = im;
   }

   @Override
   public void onCreate(Bundle savedState) {      
      mContext = getActivity();

      MainApplication app = (MainApplication)mContext.getApplication();

      mContext.setTheme(app.getSiteTheme());

      super.onCreate(savedState);

      if (mImageManager == null) {
         mImageManager = app.getImageManager();
      }

      mImageSizes = app.getImageSizes();
      mFont = Typeface.createFromAsset(mContext.getAssets(), "fonts/Ubuntu-B.ttf");
      mResources = mContext.getResources();
      
      mMetrics = new DisplayMetrics();
      mContext.getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {

      if (mMainWebView != null) {
         mMainWebView.destroy();
      }

      View view = inflater.inflate(R.layout.guide_step, container, false);

      mLineList = (ListView)view.findViewById(R.id.step_text_list);
      mTitle = (TextView)view.findViewById(R.id.step_title);

      mMainProgress = (RelativeLayout) view.findViewById(R.id.progress_bar_guide_step);
      mVideoPlayButtonContainer = (RelativeLayout) view.findViewById(R.id.video_play_button_container);
      mVideoPlayButton = (ImageButton) view.findViewById(R.id.video_play_button);
      mMainImage = (ImageView) view.findViewById(R.id.main_image);
      mMainWebView = (WebView) view.findViewById(R.id.main_web_view);
      mThumbs = (ThumbnailView)view.findViewById(R.id.thumbnails);

      // Initialize the step content
      if (mStep != null) {         
         setStep();
      }

      if (savedInstanceState != null) {
         mMainWebView.restoreState(savedInstanceState);
      }

      return view;
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

   private void setStep() {
      DisplayMetrics metrics = new DisplayMetrics();
      mContext.getWindowManager().getDefaultDisplay().getMetrics(metrics);

      // Set the guide title text, defaults to Step #
      mTitle.setText(mStep.getTitle());
      mTitle.setTypeface(mFont);
      
      // Initialize the step media
      if (mStep.hasVideo()) {
         setVideo();
      } else if (mStep.hasEmbed()) {
         setEmbed();
      } else if (mStep.hasImage()) {
         setImage();
      }
      
      // Initialize the step instructions text and bullets
      mTextAdapter = new StepTextArrayAdapter(mContext, R.id.step_text_list, mStep.getLines());
      mLineList.setAdapter(mTextAdapter);
   }
   
   private void setImage() {
      ArrayList<StepImage> stepImages = mStep.getImages();
     
      // Size the video preview screenshot within the available screen space
      mMainImage.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            String url = (String) v.getTag();

            if (url.equals("") || url.indexOf(".") == 0) return;
            
            Intent intent = new Intent(mContext, FullImageViewActivity.class);
            intent.putExtra(FullImageViewActivity.IMAGE_URL, url);
            startActivity(intent);
         }
      });
     
      // Size the main image and thumbnails to maximize use of screen space
      fitImageToSpace();      

      mMainImage.setVisibility(View.VISIBLE);
      mMainWebView.setVisibility(View.GONE);
      mMainProgress.setVisibility(View.GONE);
      
      mThumbs.setImageSizes(mImageSizes);      
      mThumbs.setMainImage(mMainImage);
      mThumbs.setThumbs(stepImages, mImageManager, mContext);
      mThumbs.setCurrentThumb(stepImages.get(0).getText());
   }
   
   private void setVideo() {
      StepVideo video = mStep.getVideo();
      StepVideoThumbnail thumb = video.getThumbnail();

      // Size the video preview screenshot within the available screen space
      fitVideoToSpace(thumb);
      
      mMainImage.setVisibility(View.VISIBLE);
      mVideoPlayButtonContainer.setVisibility(View.VISIBLE);
      mMainWebView.setVisibility(View.GONE);
      mMainProgress.setVisibility(View.GONE);
      mImageManager.displayImage(thumb.getUrl(), mContext, mMainImage);
      
      // Resize the image view to fit the available space.
      String videoURL = video.getEncodings().get(0).getURL();
      
      mVideoPlayButton.setTag(R.id.guide_step_view_video_url, videoURL);
      
      mVideoPlayButton.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            String url = (String) v.getTag(R.id.guide_step_view_video_url);

            Intent i = new Intent(mContext, VideoViewActivity.class);
            i.putExtra(VideoViewActivity.VIDEO_URL, url);
            startActivity(i);
         }
      });
   }
   
   private void setEmbed() {
      WebSettings settings = mMainWebView.getSettings();
      settings.setUseWideViewPort(true);
      settings.setJavaScriptEnabled(true);
      settings.setSupportZoom(false);
      settings.setLoadWithOverviewMode(true);
      settings.setAppCacheEnabled(true);
      settings.setCacheMode(WebSettings.LOAD_NORMAL);

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
               if (mStep.hasEmbed()) {
                  Intent i = new Intent(mContext, EmbedViewActivity.class);
                  i.putExtra(EmbedViewActivity.HTML, url);
                  startActivity(i);
               }
            }
            return true;
         }
      });

      mMainWebView.stopLoading();
      
      mMainImage.setVisibility(View.GONE);
      mMainWebView.setVisibility(View.INVISIBLE);
      mMainProgress.setVisibility(View.VISIBLE);

      if (mStep.getEmded().hasOembed()) {
         mMainWebView.loadUrl(mStep.getEmded().getOembed().getURL());
         mMainWebView.setTag(mStep.getEmded().getOembed().getURL());
      } else {
         // TODO: find the best place and way to handle the returned
         // oembed
         mEmbedRet = new EmbedRetriever();
         mEmbedRet.execute(mStep.getEmded());
      }
   } 
   
   private void fitVideoToSpace(StepVideoThumbnail video) {
      float width = 0f;
      float height = 0f;    
      float padding = baseStepPadding();

      if (inPortraitMode()) {
         width = mMetrics.widthPixels - padding;
         height = width * ((float)video.getHeight() / (float)video.getWidth());
      } else {
         padding += navigationHeight();
          
         height = ((mMetrics.heightPixels - padding) * 3f) / 5f;
         width = (height * ((float)video.getWidth() / (float)video.getHeight()));
      }

      Log.w("Video Dimensions", width + "x" + height);

      // Set the width and height of the main image
      mMainImage.getLayoutParams().height = (int) (height + .5f);
      mMainImage.getLayoutParams().width = (int) (width + .5f);

      mVideoPlayButtonContainer.getLayoutParams().height = (int) (height + .5f);
      mVideoPlayButtonContainer.getLayoutParams().width = (int) (width + .5f);
   }

   private void fitImageToSpace() {
      float thumbnailHeight = 0f;
      float thumbnailWidth = 0f;
      float width = 0f;
      float height = 0f;

      float padding = baseStepPadding();
      
      padding += viewPadding(R.dimen.guide_thumbnail_padding);

      // Portrait orientation
      if (inPortraitMode()) {
         padding += mResources.getDimensionPixelSize(R.dimen.guide_image_spacing_right);

         // Main image is 4/5ths of the available screen height
         width = (((mMetrics.widthPixels - padding) / 5f) * 4f);
         height = width *  (3f/4f);

         // Screen height minus everything else that occupies horizontal space
         thumbnailWidth = (mMetrics.widthPixels - width - padding);
         thumbnailHeight = thumbnailWidth * (3f/4f);

         mMainProgress.getLayoutParams().height = (int) ((int) (height + .5f));
         mMainProgress.getLayoutParams().width = (int) ((int) (width + .5f) + thumbnailWidth);

      } else {
         int actionBarHeight = mResources.getDimensionPixelSize(
          com.actionbarsherlock.R.dimen.abs__action_bar_default_height);
         int indicatorHeight = ((GuideViewActivity)mContext).getIndicatorHeight();

         padding += mResources.getDimensionPixelSize(R.dimen.guide_image_spacing_bottom);

         // Main image is 4/5ths of the available screen height
         height = (((mMetrics.heightPixels - actionBarHeight - indicatorHeight - padding) / 5f) * 4f);
         width = height * (4f/3f);

         // Screen height minus everything else that occupies vertical space
         thumbnailHeight = (mMetrics.heightPixels - height - actionBarHeight - padding -
          indicatorHeight);
         thumbnailWidth = (thumbnailHeight * (4f/3f));

         mMainProgress.getLayoutParams().height = (int) ((int) (height + .5f) + thumbnailHeight);
         mMainProgress.getLayoutParams().width = (int) ((int) (width + .5f));
      }

      // Set the width and height of the main image
      mMainImage.getLayoutParams().height = (int) (height + .5f);
      mMainImage.getLayoutParams().width = (int) (width + .5f);

      mThumbs.setThumbnailDimensions(thumbnailHeight, thumbnailWidth);
   }

   public class StepTextArrayAdapter extends ArrayAdapter<StepLine> {
      private ArrayList<StepLine> mLines;
      private Context mContext;

      public StepTextArrayAdapter(Context context, int viewResourceId,
       ArrayList<StepLine> lines) {
         super(context, viewResourceId, lines);

         mLines = lines;
         mContext = context;
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
         GuideStepLineView stepLine = (GuideStepLineView)convertView;

         if (stepLine == null) {
            stepLine = new GuideStepLineView(mContext);
         }
         stepLine.setLine(mLines.get(position));
         return stepLine;
      }
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
            if(!isCancelled()) {
               String url = mStep.getEmded().getOembed().getURL();
               mMainWebView.loadUrl(url);
               mMainWebView.setTag(url);
            }
         }
      }
   }
   
   // Helper functions
   
   private float baseStepPadding() {
      float imageBorder = viewPadding(R.dimen.guide_image_padding);
      float pagePadding = viewPadding(R.dimen.page_padding);
      float imagePadding = 0f;
      
      Log.w("pagePadding", pagePadding+"");
      
      if (!inPortraitMode())
         imagePadding = mResources.getDimensionPixelSize(R.dimen.guide_image_spacing_right);

      // padding that's included on every page
      return pagePadding + imageBorder + imagePadding;
   }
   
   private float navigationHeight() {
      int actionBarHeight = mResources.getDimensionPixelSize(
         com.actionbarsherlock.R.dimen.abs__action_bar_default_height);
      int indicatorHeight = ((GuideViewActivity)mContext).getIndicatorHeight();

      return actionBarHeight + indicatorHeight;
   }
   
   private boolean inPortraitMode() {
      return mResources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
   }
   
   private static float viewPadding(int view) {
      return mResources.getDimensionPixelSize(view) * 2f;
   }

   private float dpToPixel(float dp) {
      return dp * (mMetrics.densityDpi / 160f);
   }

   private float pixelToDp(float px) {
      return px / (mMetrics.densityDpi / 160f);
   }
}
