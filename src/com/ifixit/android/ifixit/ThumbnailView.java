package com.ifixit.android.ifixit;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class ThumbnailView extends LinearLayout {

   private ArrayList<LoaderImage> mThumbs;
   private LoaderImage mMainImage;
   private ImageManager mImageManager;
   private Context mContext;
   
   public ThumbnailView(Context context) {
      super(context);
      init(context);
   }

   public ThumbnailView(Context context, AttributeSet attrs) {
      super(context, attrs);
      init(context);
   }

   public ThumbnailView(Context context, AttributeSet attrs, int def) {
      super(context, attrs, def);
      init(context);
   }

   private void init(Context context) {
      LayoutInflater inflater = (LayoutInflater)context.getSystemService(
       Context.LAYOUT_INFLATER_SERVICE);

      inflater.inflate(R.layout.thumbnail_list, this, true);
      
      mThumbs = new ArrayList<LoaderImage>();
      
      mThumbs.add((LoaderImage)findViewById(R.id.thumbnail_1));
      mThumbs.add((LoaderImage)findViewById(R.id.thumbnail_2));
      mThumbs.add((LoaderImage)findViewById(R.id.thumbnail_3));
      
   }
   
   public void setThumbs(ArrayList<StepImage> images, 
      ImageManager imageManager, Context context) {
      
      mImageManager = imageManager;
      mContext = context;
      
      if (!images.isEmpty()) {        
         for (int thumbId = 0; thumbId < images.size(); thumbId++) {
            LoaderImage thumb = mThumbs.get(thumbId);
            thumb.setVisibility(VISIBLE);
            thumb.setTag(images.get(thumbId).mText);
            
            thumb.setOnClickListener(new OnClickListener() {

               @Override
               public void onClick(View v) {
                  String url = (String)v.getTag();
                  mImageManager.displayImage(url+".large", (Activity)mContext, mMainImage);
               }            
            });
            
            mImageManager.displayImage(images.get(thumbId).mText+".large", 
             (Activity)mContext, thumb);
         }
      }
   }
   
   public void setMainImage(LoaderImage mainImg) {
      mMainImage = mainImg;
   }
   
   public void setContext(Context context) {
      mContext = context;
   }
   
   public void setImageManager(ImageManager imageManager) {
      mImageManager = imageManager;
   }
   
   public ArrayList<LoaderImage> getThumbViews() {
      return mThumbs;
   }
}
