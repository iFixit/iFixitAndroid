package com.ifixit.android.ifixit;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ThumbnailImageAdapter extends BaseAdapter {
   private Context mContext;
   private GuideStep mStep;
   private ImageManager mImageManager;

   public ThumbnailImageAdapter(Context context, GuideStep step,
    ImageManager imageManager) {
      mContext = context;
      mImageManager = imageManager;
      mStep = step;
   }

   public int getCount() {
      return mStep.getImages().size();
   }

   public Object getItem(int position) {
      return mStep.mImages.get(position).mText + ".large";
   }

   public long getItemId(int position) {
      return mStep.mImages.get(position).mImageid;
   }

   public View getView(int position, View convertView, ViewGroup parent) {
      LoaderImage imageView;

      if (convertView == null)
         imageView = new LoaderImage(mContext);
      else 
         imageView = (LoaderImage)convertView;

      imageView.setLayoutParams(new GridView.LayoutParams(
       GuideStepView.THUMBNAIL_WIDTH, GuideStepView.THUMBNAIL_HEIGHT));
      imageView.setScaleType(ImageView.ScaleType.FIT_XY);

      mImageManager.displayImage(mStep.mImages.get(position).mText + ".large",
       (Activity)mContext, imageView);             

      return imageView;
   }
}
