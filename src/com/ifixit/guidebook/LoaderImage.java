package com.ifixit.guidebook;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class LoaderImage extends RelativeLayout {
   protected ImageView mImage;
   protected ProgressBar mProgressBar;

   public LoaderImage(Context context) {
      super(context);
      init(context);
   }

   public LoaderImage(Context context, AttributeSet attrs) {
      super(context, attrs);
      init(context);
   }

   public LoaderImage(Context context, AttributeSet attrs, int def) {
      super(context, attrs, def);
      init(context);
   }

   private void init(Context context) {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(
       Context.LAYOUT_INFLATER_SERVICE);

      inflater.inflate(R.layout.loader_image, this, true);        

      mImage = (ImageView)findViewById(R.id.imageView);
      mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
      mImage.setVisibility(View.GONE);
      mProgressBar.setVisibility(View.VISIBLE);
   }

   public void setScaleType(ImageView.ScaleType scaleType) {
      mImage.setScaleType(scaleType);
   }

   public void setImageResource(int resource) {
      mImage.setImageResource(resource);
      mImage.setVisibility(View.VISIBLE);
      mProgressBar.setVisibility(View.GONE);
   }

   public void setImageBitmap(Bitmap bitmap) {
      mImage.setImageBitmap(bitmap);
      mImage.setVisibility(View.VISIBLE);
      mProgressBar.setVisibility(View.GONE);
   }
}
