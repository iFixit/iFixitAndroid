package com.dozuki.ifixit;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.ifixit.android.imagemanager.ImageManager;

public class GuideStepViewFragment extends SherlockFragment {
   protected static final String IMAGE_URL = "IMAGE_URL";

   private TextView mTitle;
   private ThumbnailView mThumbs;
   private ImageView mMainImage;
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

      mMainImage = (ImageView)view.findViewById(R.id.main_image);
      mMainImage.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            String url = (String)v.getTag();

            if (url.equals("") || url.indexOf(".") == 0) {
               return;
            }

            Intent intent = new Intent(getActivity(), FullImageView.class);
            intent.putExtra(IMAGE_URL, url);

            startActivity(intent);
         }
      });
      
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
      
      float thumbPadding = dpToPixel(
       resources.getDimension(R.dimen.guide_thumbnail_padding), context) * 2f;
      float mainPadding = dpToPixel(
       resources.getDimension(R.dimen.guide_image_padding), context) * 2f;
      float pagePadding = dpToPixel(
       resources.getDimension(R.dimen.page_padding), context) * 2f;

      // padding that's included on every page 
      float padding = pagePadding + mainPadding;
      
      Log.w("Screen Height", screenHeight+"");
      Log.w("Screen Width", screenWidth+"");
      Log.w("Screen Padding", padding+"");
      int indicatorHeight = ((GuideView)context).getIndicatorHeight();
      Log.w("Indicator Height", indicatorHeight+"");
      
      // Unbelievably horrible hack that fixes a problem when getIndicatorHeight() 
      // returns 0 after a orientation change, causing the Main image view 
      // to calculate to large and the thumbnails are hidden by the 
      // CircleIndicator.
      // TODO: Figure out why this is actually happening and the right way to do
      //       this.
      if (indicatorHeight == 0) {
         indicatorHeight = 49;
      }

      Log.w("Screen Orientation", resources.getConfiguration().orientation+"");
      // Portrait orientation
      if (resources.getConfiguration().orientation == 
       Configuration.ORIENTATION_PORTRAIT) {
         padding += dpToPixel(resources.getDimension(
          R.dimen.guide_image_spacing_right), context);

         width = (((screenWidth - padding) / 5f) * 4f);
         height = width * 0.75f;
         
         Log.w("Main Image Height", height+"");
         Log.w("Main Image Width", width+"");

         thumbnailWidth = (screenWidth - width - padding - thumbPadding);
         thumbnailHeight = thumbnailWidth * (3f/4f);

      } else {
         int actionBarHeight = resources.getDimensionPixelSize(
          com.actionbarsherlock.R.dimen.abs__action_bar_default_height);
         padding += dpToPixel(resources.getDimension(
          R.dimen.guide_image_spacing_bottom), context);
         height = (((screenHeight - actionBarHeight - padding - thumbPadding)
          / 4f) * 3f);
         width = height * (4f/3f);
      
         Log.w("Main Image Height", height+"");
         Log.w("Main Image Width", width+"");

         thumbnailHeight = (height / 4f);
         thumbnailWidth = (thumbnailHeight * (4f/3f));
      }

      Log.w("Thumbnail Height", thumbnailHeight+"");
      Log.w("Thumbnail Width", thumbnailWidth+"");
      mMainImage.getLayoutParams().height = (int) (height + .5f);
      mMainImage.getLayoutParams().width = (int) (width + .5f);

      mThumbs = (ThumbnailView)view.findViewById(R.id.thumbnails);
      mThumbs.setMainImage(mMainImage);
      mThumbs.setThumbnailDimensions(thumbnailHeight, thumbnailWidth);
      
      if (mStep != null) {
         setStep();
      }

      return view;
   }
   

   public static float dpToPixel(float dp,Context context){
       Resources resources = context.getResources();
       DisplayMetrics metrics = resources.getDisplayMetrics();
       float px = dp * (metrics.densityDpi/160f);
       return px;
   }

   public static float pixelToDp(float px,Context context){
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
      mThumbs.setThumbs(mStep.mImages, mImageManager, getActivity());

      // Might be a problem if there are no images for a step...
      mThumbs.setCurrentThumb(mStep.mImages.get(0).getText());
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
}
