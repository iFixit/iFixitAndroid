package com.dozuki.ifixit.guide_view.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
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
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_view.model.Embed;
import com.dozuki.ifixit.guide_view.model.GuideStep;
import com.dozuki.ifixit.guide_view.model.OEmbed;
import com.dozuki.ifixit.guide_view.model.StepLine;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.JSONHelper;
import com.ifixit.android.imagemanager.ImageManager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

public class GuideStepViewFragment extends SherlockFragment {
   private TextView mTitle;
   private ThumbnailView mThumbs;
   private ImageView mMainImage;
   private WebView mMainWebView;
   private GuideStep mStep;
   private ImageManager mImageManager;
   private StepTextArrayAdapter mTextAdapter;
   private ListView mLineList;
   private Typeface mFont;
   private ImageSizes mImageSizes;

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
      View view = inflater.inflate(R.layout.guide_step, container, false);
      mFont = Typeface.createFromAsset(getActivity().getAssets(),
       "fonts/Ubuntu-B.ttf");

      mLineList = (ListView)view.findViewById(R.id.step_text_list);
      mTitle = (TextView)view.findViewById(R.id.step_title);
      mTitle.setTypeface(mFont);

      mMainImage = (ImageView) view.findViewById(R.id.main_image);
      mMainWebView = (WebView) view.findViewById(R.id.main_web_view);
      mMainWebView.getSettings().setUseWideViewPort(true);
      mMainWebView.getSettings().setJavaScriptEnabled(true);
      mMainWebView.getSettings().setSupportZoom(false);
      mMainWebView.getSettings().setLoadWithOverviewMode(true);
      mMainWebView.setWebViewClient(new WebViewClient() {

         public void onPageFinished(WebView view, String url) {
            // add loading and hide
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
                  // if (mStep.getEmded().getType().equals("VIDEO")) {
                  Intent i = new Intent(getActivity(), EmbedViewActivity.class);
                  i.putExtra(EmbedViewActivity.HTML, url);
                  startActivity(i);
                  // }
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

      if (mStep != null) {
         setStep();
      }

      return view;
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
      }

      // Set the width and height of the main image
      mMainImage.getLayoutParams().height = (int)(height + .5f);
      mMainImage.getLayoutParams().width = (int)(width + .5f);
      
      mMainWebView.getLayoutParams().height = (int)(height + .5f);
      mMainWebView.getLayoutParams().width = (int) ((int)(width + .5f));

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
      if (mStep.getTitle().length() == 0) {
         mTitle.setText(getActivity().getString(R.string.step) + " " +
          mStep.getStepNum());
      } else {
         mTitle.setText(mStep.getTitle());
      }

      mTextAdapter = new StepTextArrayAdapter(getActivity(),
       R.id.step_text_list, mStep.getLines());
      mLineList.setAdapter(mTextAdapter);

      mThumbs.setImageSizes(mImageSizes);
      mMainWebView.setVisibility(View.GONE);
      if (mStep.hasVideo()) {
         mMainWebView.setVisibility(View.GONE);
         mImageManager.displayImage(mStep.getVideo().getThumbnail(), getActivity(), mMainImage);
         mMainImage.setTag(mStep.getVideo().getEncodings().get(0).getURL());

      } else
         if (mStep.hasEmbed()) {
            if (mStep.getEmded().hasOembed()) {

               mMainWebView.setVisibility(View.VISIBLE);
               mMainWebView.loadUrl(mStep.getEmded().getOembed().getURL());
               mMainWebView.setTag(mStep.getEmded().getOembed().getURL());
            } else {
               // TODO: find the best place and way to handle the returned
               // oembed
               new EmbedRetriever().execute(mStep.getEmded());
            }

         } else
            if (mStep.getImages().size() > 0) {
               mMainWebView.setVisibility(View.GONE);
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
         } finally {

            return oe;
         }

      }

      protected void onPostExecute(OEmbed embed) {
         if (embed != null) {
            // TODO: decide if this is ok. Most likely because the setStep
            // function isnt intensive
            setStep();
         }
      }

   }

}
