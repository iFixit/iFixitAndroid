package com.dozuki.ifixit;

import android.content.Context;

import android.graphics.Bitmap;

import android.util.AttributeSet;

import android.view.LayoutInflater;
import android.view.View;

import android.widget.ImageView;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class LoaderImageZoom extends LoaderImage {
   public LoaderImageZoom(Context context) {
      super(context);
   }

   public LoaderImageZoom(Context context, AttributeSet attrs) {
      super(context, attrs);
   }

   public LoaderImageZoom(Context context, AttributeSet attrs, int def) {
      super(context, attrs, def);
   }

   protected void init(Context context) {
      LayoutInflater inflater = (LayoutInflater)context.getSystemService(
       Context.LAYOUT_INFLATER_SERVICE);

      inflater.inflate(R.layout.loader_image_zoom, this, true);

      mImage = (ImageView)findViewById(R.id.imageViewZoom);
      //mProgressBar = (ProgressBar)findViewById(R.id.loaderProgressBar);
      mImage.setVisibility(View.GONE);
      //mProgressBar.setVisibility(View.VISIBLE);
   }

   public void setImageBitmap(Bitmap bitmap) {
      ((ImageViewTouch)mImage).setImageBitmapReset(bitmap, true);
      mImage.setVisibility(View.VISIBLE);
      //mProgressBar.setVisibility(View.GONE);
   }
}
