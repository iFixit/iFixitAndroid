package com.ifixit.guidebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class ThumbnailImageAdapter extends BaseAdapter {

   private int mGalleryItemBackground;
   private Context mContext;
   private GuideStep mStep;

   private ImageManager mImageManager;

   public ThumbnailImageAdapter(Context context, GuideStep step) {
       
      mContext = context;
      mStep = step;
       
      TypedArray attr = mContext.obtainStyledAttributes(R.styleable.thumbnail_gallery);
      mGalleryItemBackground = attr.getResourceId(
              R.styleable.thumbnail_gallery_android_galleryItemBackground, 0);
      attr.recycle();
      
      mImageManager = new ImageManager(mContext);
   }

   public int getCount() {
       return mStep.getImages().size();
   }

   public Object getItem(int position) {
       return position;
   }

   public long getItemId(int position) {
       return position;
   }

   public View getView(int position, View convertView, ViewGroup parent) {
      ImageView imageView;
      if (convertView == null) 
         imageView = new ImageView(mContext);
      else 
         imageView = (ImageView) convertView;
      
      imageView.setLayoutParams(new Gallery.LayoutParams(96, 72));
      imageView.setScaleType(ImageView.ScaleType.FIT_XY);
      
      mImageManager.displayImage(mStep.mImages.get(position).mText + ".thumbnail",
       (Activity)mContext, imageView);             
        
      return (View)imageView;
   }

}
