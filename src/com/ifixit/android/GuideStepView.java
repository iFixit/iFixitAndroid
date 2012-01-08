package com.ifixit.android;

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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class GuideStepView extends LinearLayout {
   public static final double IMG_RESIZE = .80;
   public static final int THUMBNAIL_WIDTH = 160;
   public static final int THUMBNAIL_HEIGHT = 120;
   public static final int MAIN_WIDTH = 720;
   public static final int MAIN_HEIGHT = 540;
   public static final int THUMB_MARGIN = 10;
   public static final int MAIN_MARGIN = 30;
   protected static final String IMAGEID = "imageid";

   private Context mContext;
   private TextView mTitle;
   private GridView mThumbs;
   private Gallery mMainGal;
   private GuideStep mStep;
   private ImageManager mImageManager;
   private ArrayAdapter<StepLine> mAdapter;
   private ListView mLineList;
   
   public GuideStepView(Context context, GuideStep step,
    ImageManager imageManager) {
      super(context);      

      LayoutInflater inflater = (LayoutInflater) context.getSystemService(
       Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.guide_step, this, true);        
      
      mLineList = (ListView)findViewById(R.id.step_text_list);
      mThumbs = (GridView)findViewById(R.id.thumbnail_gallery);
      mMainGal = (Gallery)findViewById(R.id.main_gallery);
      mTitle = (TextView)findViewById(R.id.step_title);

      mContext = context;
      mStep = step;
      mImageManager = imageManager;

      if (step.getTitle().isEmpty())
         mTitle.setText("Step " + step.getStepNum());
      else
         mTitle.setText(step.getTitle());

      mAdapter = new StepTextArrayAdapter((Activity)mContext,
       R.id.step_text_list, mStep.getLines());
      mLineList.setAdapter(mAdapter);
            
      mMainGal.setAdapter(new MainImageAdapter((Activity)mContext, mStep,
       mImageManager));
      mThumbs.setAdapter(new ThumbnailImageAdapter(mContext, mStep,
       imageManager));
      
      mMainGal.setSpacing(MAIN_MARGIN);
      mMainGal.setOnItemClickListener(new OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> parent, View v, int position,
          long id) {            
            Intent intent = new Intent((Activity)mContext,
             FullImageView.class);
            intent.putExtra(IMAGEID,
             (String)mMainGal.getAdapter().getItem(position));
            
            ((Activity)mContext).startActivity(intent);
         }
      });
      
      mThumbs.setOnItemClickListener(new OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> parent, View v, int position,
          long id) {            
            mMainGal.setSelection(position);
         }
      });
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
