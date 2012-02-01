package com.ifixit.android.ifixit;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class GuideStepView extends LinearLayout {
   
   protected static final String IMAGEID = "imageid";

   private Context mContext;
   private TextView mTitle;
   private ThumbnailView mThumbs;
   private LoaderImage mMainImage;
   private GuideStep mStep;
   private ImageManager mImageManager;
   private ArrayAdapter<StepLine> mAdapter;
   private ListView mLineList;
   
   public GuideStepView(Context context, GuideStep step,
    ImageManager imageManager) {
      super(context);      
      mContext = context;

      LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
       Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.guide_step, this, true);        
      
      mLineList = (ListView)findViewById(R.id.step_text_list);
      mTitle = (TextView)findViewById(R.id.step_title);
      
      mThumbs = (ThumbnailView)findViewById(R.id.thumbnails);
      mMainImage = (LoaderImage)findViewById(R.id.main_image);
      mThumbs.setMainImage(mMainImage);
      
      
      mStep = step;
      mImageManager = imageManager;

      if (step.getTitle().length() == 0)
         mTitle.setText("Step " + step.getStepNum());
      else
         mTitle.setText(step.getTitle());

      mAdapter = new StepTextArrayAdapter((Activity)mContext,
       R.id.step_text_list, mStep.getLines());
      mLineList.setAdapter(mAdapter);
      
      // Might be a problem if there are no images for a step...
      mImageManager.displayImage(step.mImages.get(0).getText() +".large",
       (Activity)mContext, mMainImage);       
      
      mThumbs.setThumbs(step.mImages, mImageManager, (Activity)mContext);
      
      mMainImage.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            //Intent intent = new Intent((Activity)mContext, FullImageView.class);
            //intent.putExtra(IMAGE, (LoaderImage)v;
           
            //((Activity)mContext).startActivity(intent);
         }
      });
      
   }
   
   public void setMainImage(String url) {
      mImageManager.displayImage(url, (Activity)mContext, mMainImage);
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
