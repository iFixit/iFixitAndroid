package com.dozuki.ifixit.gallery.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.dozuki.ifixit.R;

public class FadeInImageView extends ImageView {
   private Context mContext;
   private boolean mFadeIn;

   public FadeInImageView(Context context, AttributeSet attrs) {
      super(context, attrs);
      mContext = context;
      mFadeIn = false;
   }

   public FadeInImageView(Context context, AttributeSet attrs, int integer) {
      super(context, attrs, integer);
      mContext = context;
      mFadeIn = false;
   }

   @Override
   public void setImageBitmap(Bitmap bm) {
      super.setImageBitmap(bm);
      if (mFadeIn) {
         setAnimation(AnimationUtils.loadAnimation(mContext,
          R.anim.fade_in));
      }
   }

   public void setFadeIn(boolean fade) {
      mFadeIn = fade;
   }
}
