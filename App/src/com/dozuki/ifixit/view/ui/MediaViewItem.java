package com.dozuki.ifixit.view.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.dozuki.ifixit.view.model.UserImageInfo;
import com.ifixit.android.imagemanager.ImageManager;

public class MediaViewItem extends RelativeLayout {
   FadeInImageView imageview;
   RelativeLayout selectImage;
   ProgressBar loadingBar;
   public UserImageInfo listRef;
   private Context mContext;
   int id;
   private ImageManager mImageManager;
   public String localPath;

   public MediaViewItem(Context context, ImageManager imageManager) {
      super(context);
      mContext = context;
      mImageManager = imageManager;
      listRef = null;
      localPath = null;
      LayoutInflater inflater =
         (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(com.dozuki.ifixit.R.layout.gallery_cell, this, true);

      imageview =
         (FadeInImageView) findViewById(com.dozuki.ifixit.R.id.media_image);
      selectImage =
         (RelativeLayout) findViewById(com.dozuki.ifixit.R.id.selected_image);
      selectImage.setVisibility(View.INVISIBLE);
      loadingBar =
         (ProgressBar) findViewById(com.dozuki.ifixit.R.id.gallery_cell_progress_bar);
      loadingBar.setVisibility(View.INVISIBLE);
   }

   public void setImageItem(String image, Context context, boolean fade) {
      mContext = context;
      imageview.setFadeIn(fade);
      mImageManager.displayImage(image, (Activity) mContext, imageview);
   }

   public void setLoading(boolean loading) {
      if (loading) {
         loadingBar.setVisibility(View.VISIBLE);
         AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
         alpha.setDuration(0); // Make animation instant
         alpha.setFillAfter(true); // Tell it to persist after the animation
         // ends
         imageview.startAnimation(alpha);
      } else {
         loadingBar.setVisibility(View.INVISIBLE);
         imageview.clearAnimation();
      }
   }
}
