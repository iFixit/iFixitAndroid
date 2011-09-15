package com.ifixit.guidebook;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ThumbnailImageAdapter extends BaseAdapter {
   private Context mContext;
   private GuideStep mStep;

   private ImageManager mImageManager;

   public ThumbnailImageAdapter(Context context, GuideStep step, ImageManager imageManager) {
      mContext = context;
      mImageManager = imageManager;
      mStep = step;
   }

   public int getCount() {
       return mStep.getImages().size();
   }

   public Object getItem(int position) {
       return mStep.mImages.get(position).mText + ".thumbnail";
   }

   public long getItemId(int position) {
       return mStep.mImages.get(position).mImageid;
   }

   public View getView(int position, View convertView, ViewGroup parent) {
      ImageView imageView;

      if (convertView == null)
         imageView = new ImageView(mContext);
      else 
         imageView = (ImageView)convertView;
  
      imageView.setLayoutParams(new GridView.LayoutParams(
       GuideStepView.THUMBNAIL_WIDTH, GuideStepView.THUMBNAIL_HEIGHT));
      imageView.setScaleType(ImageView.ScaleType.FIT_XY);
    
      mImageManager.displayImage(mStep.mImages.get(position).mText + ".thumbnail",
       (Activity)mContext, imageView);             

      return (View)imageView;
   }
}
