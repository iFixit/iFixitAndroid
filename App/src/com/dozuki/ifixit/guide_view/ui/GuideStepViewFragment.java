package com.dozuki.ifixit.guide_view.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_view.model.Embed;
import com.dozuki.ifixit.guide_view.model.GuideStep;
import com.dozuki.ifixit.guide_view.model.OEmbed;
import com.dozuki.ifixit.guide_view.model.StepLine;
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

   public GuideStepViewFragment() {

   }

   public GuideStepViewFragment(ImageManager im, GuideStep step) {
      mStep = step;
      mImageManager = im;
   }

   @Override
   public void onCreate(Bundle savedState) {
      getActivity().setTheme(((MainApplication)getActivity().getApplication()).
       getSiteTheme());

      super.onCreate(savedState);

      if (mImageManager == null) {
         mImageManager = ((MainApplication)getActivity().getApplication()).
          getImageManager();
      }

      mImageSizes = ((MainApplication)getActivity().getApplication()).
       getImageSizes();
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {

      if (mMainWebView != null) {
         mMainWebView.destroy();
      }

      View view = inflater.inflate(R.layout.guide_step, container, false);
      mFont = Typeface.createFromAsset(getActivity().getAssets(),
       "fonts/Ubuntu-B.ttf");

      mLineList = (ListView)view.findViewById(R.id.step_text_list);
      mTitle = (TextView)view.findViewById(R.id.step_title);
      mTitle.setTypeface(mFont);

      mMainProgress = (RelativeLayout) view.findViewById(R.id.progress_bar_guide_step);
      mVideoPlayButtonContainer = (RelativeLayout) view.findViewById(R.id.video_play_button_container);
      mMainImage = (ImageView) view.findViewById(R.id.main_image);
      mMainWebView = (WebView) view.findViewById(R.id.main_web_view);

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
                  Intent i = new Intent(getActivity(), EmbedViewActivity.class);
                  i.putExtra(EmbedViewActivity.HTML, url);
                  startActivity(i);
               }
            }
            return true;
         }
      });

      mMainImage.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            String url = (String) v.getTag();

            if (url.equals("") || url.indexOf(".") == 0) {
               return;
            }

            if (!mStep.hasVideo() && !mStep.hasEmbed()) {
               Intent intent = new Intent(getActivity(), FullImageViewActivity.class);
               intent.putExtra(FullImageViewActivity.IMAGE_URL, url);
               startActivity(intent);
            } else {
               Intent i = new Intent(getActivity(), VideoViewActivity.class);
               i.putExtra(VideoViewActivity.VIDEO_URL, url);
               startActivity(i);
            }
         }
      });

      // MUST BE BEFORE fitImagesToSpace(), DONT MOVE
      mThumbs = (ThumbnailView)view.findViewById(R.id.thumbnails);

      // Resize and fit thumbnails and main image to available screen space
      fitImagesToSpace();

      mThumbs.setMainImage(mMainImage);

      if (savedInstanceState != null) {
         mMainWebView.restoreState(savedInstanceState);
      }

      if (mStep != null) {
         setStep();
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

   public void fitImagesToSpace() {
      Activity context = getActivity();
      Resources resources = context.getResources();
      DisplayMetrics metrics = new DisplayMetrics();
      context.getWindowManager().getDefaultDisplay().getMetrics(metrics);

      float screenHeight = metrics.heightPixels;
      float screenWidth = metrics.widthPixels;
      float thumbnailHeight = 0f;
      float thumbnailWidth = 0f;
      float height = 0f;
      float width = 0f;

      float thumbPadding = resources.getDimensionPixelSize(
       R.dimen.guide_thumbnail_padding) * 2f;
      float mainPadding = resources.getDimensionPixelSize(
       R.dimen.guide_image_padding) * 2f;
      float pagePadding = resources.getDimensionPixelSize(
       R.dimen.page_padding) * 2f;

      // padding that's included on every page
      float padding = pagePadding + mainPadding + thumbPadding;

      // Portrait orientation
      if (resources.getConfiguration().orientation ==
       Configuration.ORIENTATION_PORTRAIT) {
         padding += resources.getDimensionPixelSize(
          R.dimen.guide_image_spacing_right);

         // Main image is 4/5ths of the available screen height
         width = (((screenWidth - padding) / 5f) * 4f);
         height = width *  (3f/4f);

         // Screen height minus everything else that occupies horizontal space
         thumbnailWidth = (screenWidth - width - padding);
         thumbnailHeight = thumbnailWidth * (3f/4f);

         mMainWebView.getLayoutParams().height = (int) ((int) (height + .5f));
         mMainWebView.getLayoutParams().width = (int) ((int) (width + .5f) + thumbnailWidth);
         mMainProgress.getLayoutParams().height = (int) ((int) (height + .5f));
         mMainProgress.getLayoutParams().width = (int) ((int) (width + .5f)+ thumbnailWidth);

      } else {
         int actionBarHeight = resources.getDimensionPixelSize(
          com.actionbarsherlock.R.dimen.abs__action_bar_default_height);
         int indicatorHeight = ((GuideViewActivity)context).getIndicatorHeight();

         // Unbelievably horrible hack that fixes a problem when
         // getIndicatorHeight() returns 0 after a orientation change, causing the
         // Main image view to calculate to large and the thumbnails are hidden by
         // the CircleIndicator.
         // TODO: Figure out why this is actually happening and the right way to do
         //       this.
         if (indicatorHeight == 0) {
            indicatorHeight = 49;
         }

         padding += resources.getDimensionPixelSize(
          R.dimen.guide_image_spacing_bottom);

         // Main image is 4/5ths of the available screen height
         height = (((screenHeight - actionBarHeight - indicatorHeight - padding)
          / 5f) * 4f);
         width = height * (4f/3f);

         // Screen height minus everything else that occupies vertical space
         thumbnailHeight = (screenHeight - height - actionBarHeight - padding -
          indicatorHeight);
         thumbnailWidth = (thumbnailHeight * (4f/3f));

         mMainWebView.getLayoutParams().height = (int) ((int) (height + .5f) + thumbnailHeight);
         mMainWebView.getLayoutParams().width = (int) ((int) (width + .5f));
         mMainProgress.getLayoutParams().height = (int) ((int) (height + .5f) + thumbnailHeight);
         mMainProgress.getLayoutParams().width = (int) ((int) (width + .5f));
      }

      // Set the width and height of the main image
      mMainImage.getLayoutParams().height = (int) (height + .5f);
      mMainImage.getLayoutParams().width = (int) (width + .5f);
      mVideoPlayButtonContainer.getLayoutParams().height = mMainImage.getLayoutParams().height;
      mVideoPlayButtonContainer.getLayoutParams().width = mMainImage.getLayoutParams().width;

      mThumbs.setThumbnailDimensions(thumbnailHeight, thumbnailWidth);
   }

   public static float dpToPixel(float dp, Context context) {
      Resources resources = context.getResources();
      DisplayMetrics metrics = resources.getDisplayMetrics();
      float px = dp * (metrics.densityDpi / 160f);
      return px;
   }

   public static float pixelToDp(float px, Context context) {
      Resources resources = context.getResources();
      DisplayMetrics metrics = resources.getDisplayMetrics();
      float dp = px / (metrics.densityDpi / 160f);
      return dp;
   }

   public void setStep() {
      //stop the load of the 
      mMainWebView.stopLoading();

      if (mStep.getTitle().length() == 0) {
         mTitle.setText(getActivity().getString(R.string.step) + " " + mStep.getStepNum());
      } else {
         mTitle.setText(mStep.getTitle());
      }

      mTextAdapter = new StepTextArrayAdapter(getActivity(),
       R.id.step_text_list, mStep.getLines());
      mLineList.setAdapter(mTextAdapter);

      mThumbs.setImageSizes(mImageSizes);
      if (mStep.hasVideo()) {
         mMainImage.setVisibility(View.VISIBLE);
         mVideoPlayButtonContainer.setVisibility(View.VISIBLE);
         mMainWebView.setVisibility(View.GONE);
         mMainProgress.setVisibility(View.GONE);
         mImageManager.displayImage(mStep.getVideo().getThumbnail(), getActivity(), mMainImage);
         mMainImage.setTag(mStep.getVideo().getEncodings().get(0).getURL());

      } else if (mStep.hasEmbed()) {
         mMainImage.setVisibility(View.GONE);
         mMainWebView.setVisibility(View.INVISIBLE);
         mMainProgress.setVisibility(View.VISIBLE);
         mVideoPlayButtonContainer.setVisibility(View.GONE);
         if (mStep.getEmded().hasOembed()) {
            mMainWebView.loadUrl(mStep.getEmded().getOembed().getURL());
            mMainWebView.setTag(mStep.getEmded().getOembed().getURL());
         } else {
            // TODO: find the best place and way to handle the returned
            // oembed
            mEmbedRet = new EmbedRetriever();
            mEmbedRet.execute(mStep.getEmded());
         }
      } else if (mStep.getImages().size() > 0) {
         mMainImage.setVisibility(View.VISIBLE);
         mMainWebView.setVisibility(View.GONE);
         mMainProgress.setVisibility(View.GONE);
         mVideoPlayButtonContainer.setVisibility(View.GONE);
         // Might be a problem if there are no images for a step...
         mThumbs.setThumbs(mStep.getImages(), mImageManager, getActivity());
         mThumbs.setCurrentThumb(mStep.getImages().get(0).getText());
      }
   }

   public void setImageManager(ImageManager im) {
      mImageManager = im;
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
}
