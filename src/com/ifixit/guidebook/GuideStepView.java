package com.ifixit.guidebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class GuideStepView extends LinearLayout {
   public static final double IMG_RESIZE = .80;
   public static final int THUMBNAIL_WIDTH = 96;
   public static final int THUMBNAIL_HEIGHT = 72;
   public static final int MAIN_WIDTH = 800;
   public static final int MAIN_HEIGHT = 600;
   public static final int THUMB_MARGIN = 8;
   public static final int MAIN_MARGIN = 30;

   private Context mContext;
   private TextView mTitle;
   private ImageView mImage;
   private Gallery mThumbs;
   private Gallery mMainGal;
   private GuideStep mStep;
   private ImageManager mImageManager;
   private ArrayAdapter<StepLine> mAdapter;
   private ListView mLineList;
   
   public GuideStepView(Context context, GuideStep step, ImageManager imageManager) {
      super(context);      
      mContext = context;
      mStep = step;
      mImageManager = imageManager;

      LayoutInflater inflater = (LayoutInflater) context.getSystemService(
       Context.LAYOUT_INFLATER_SERVICE);

      inflater.inflate(R.layout.guide_step, this, true);        

      mTitle = (TextView) findViewById(R.id.step_title);
      if (step.getTitle().isEmpty()) {
         mTitle.setText("Step " + step.getStepNum());
      }
      else {
         mTitle.setText(step.getTitle());
      }
     
      mAdapter = new StepTextArrayAdapter((Activity)mContext, R.id.step_text_list, mStep.getLines());
      mAdapter.notifyDataSetChanged();
      
      mLineList = (ListView) findViewById(R.id.step_text_list);
      mLineList.setAdapter(mAdapter);

      //int height = ((GuideView) context).getScreenHeight();
      //int width = ((GuideView) context).getScreenWidth();

      //mImage.setMaxHeight((int) Math.floor(height * IMG_RESIZE));
      //mImage.setMaxWidth((int) Math.floor(width * IMG_RESIZE));
      //mImage.setMinimumHeight((int) Math.floor(height * IMG_RESIZE));
      //mImage.setMinimumWidth((int) Math.floor(width * IMG_RESIZE));
      
      //mImageManager.displayImage(mStep.mImages.get(0).mText + ".large",
      //(Activity)mContext, mImage);

      mThumbs = (Gallery)findViewById(R.id.thumbnail_gallery);
      mThumbs.setAdapter(new ThumbnailImageAdapter((Activity)mContext, step,
       mImageManager));
      
      mMainGal = (Gallery)findViewById(R.id.main_gallery);
      mMainGal.setAdapter(new MainImageAdapter((Activity)mContext, step,
       mImageManager));

      mThumbs.setSpacing(THUMB_MARGIN);
      
      if (step.getImages().size() != 0) {
         LayoutParams thumbParams = new LayoutParams(   
          (THUMBNAIL_WIDTH + (THUMB_MARGIN*2)) * step.getImages().size(), 
          LayoutParams.WRAP_CONTENT);

         mThumbs.setLayoutParams(thumbParams);
      }

      mMainGal.setSpacing(MAIN_MARGIN);

      mThumbs.setOnItemClickListener(new OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> parent, View v, int position,
          long id) {            
            mMainGal.setSelection(position);
         }
      });
   }

   public ImageView getImageView() {
      return mImage;
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
         GuideStepLineView row = (GuideStepLineView) convertView;

         if (row == null) {
            row = new GuideStepLineView(mContext);             
         } 
         
         row.setLine(mLines.get(position));
         
         return row;
      }
   }
}
