package com.dozuki.ifixit.gallery.ui;

import android.content.Context;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.gallery.model.MediaInfo;
import com.dozuki.ifixit.gallery.model.UserImageInfo;
import com.marczych.androidimagemanager.ImageManager;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.ProgressBar;

public class MediaViewItem extends RelativeLayout {
   public MediaInfo mListRef;
   public String mLocalPath;

   public FadeInImageView mImageview;
   public RelativeLayout mSelectImage;
   private ProgressBar mLoadingBar;
   private Context mContext;
   private ImageManager mImageManager;

   public MediaViewItem(Context context, ImageManager imageManager) {
      super(context);
      mContext = context;
      mImageManager = imageManager;
      mListRef = null;
      mLocalPath = null;
      LayoutInflater inflater = (LayoutInflater)context.getSystemService(
       Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.gallery_cell, this, true);

      mImageview = (FadeInImageView)findViewById(com.dozuki.ifixit.R.id.media_image);
      mSelectImage = (RelativeLayout)findViewById(com.dozuki.ifixit.R.id.selected_image);
      mSelectImage.setVisibility(View.INVISIBLE);
      mLoadingBar = (ProgressBar)findViewById(com.dozuki.ifixit.R.id.gallery_cell_progress_bar);
      mLoadingBar.setVisibility(View.GONE);
   }

   public void setImageItem(String image, Context context, boolean fade) {
      mContext = context;
      mImageview.setFadeIn(fade);
      mImageManager.displayImage(image, (Activity)mContext, mImageview);
   }

   public void setLoading(boolean loading) {
      if (loading) {
         mLoadingBar.setVisibility(View.VISIBLE);
         AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
         alpha.setDuration(0); // Make animation instant.
         alpha.setFillAfter(true); // Persist after the animation ends.
         mImageview.startAnimation(alpha);
      } else {
         mLoadingBar.setVisibility(View.GONE);
         mImageview.clearAnimation();
      }
   }
}
