package com.ifixit.android.ifixit;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class MainImageAdapter extends BaseAdapter {
   private Context mContext;
   private GuideStep mStep;
   private ImageManager mImageManager;

   public MainImageAdapter(Context context, GuideStep step, 
    ImageManager imageManager) {
      mContext = context;
      mStep = step;
      mImageManager = imageManager;
   }

   public int getCount() {
       return mStep.getImages().size();
   }

   public String getItem(int position) {
       return mStep.mImages.get(position).getText();
   }

   public long getItemId(int position) {
       return mStep.mImages.get(position).getImageid();
   }

   public View getView(int position, View convertView, ViewGroup parent) {
      LoaderImage imageView;

      if (convertView == null) 
         imageView = new LoaderImage(mContext);
      else 
         imageView = (LoaderImage)convertView;
      
      imageView.setLayoutParams(new Gallery.LayoutParams(
       GuideStepView.MAIN_WIDTH, GuideStepView.MAIN_HEIGHT));
      imageView.setScaleType(ImageView.ScaleType.FIT_XY);
      
      mImageManager.displayImage(mStep.mImages.get(position).getText() +
       ".large", (Activity)mContext, imageView);             
        
      return imageView;
   }
}
