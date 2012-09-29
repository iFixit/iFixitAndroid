package com.dozuki.ifixit.view.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.dozuki.ifixit.R;

public class FadeInImageView extends ImageView {

   Context mContext;
   boolean mFadeIn;

   public FadeInImageView(Context context, AttributeSet attrs) {
      super(context, attrs);
      // TODO Auto-generated constructor stub
      mContext = context;
      mFadeIn = false;
   }

   public FadeInImageView(Context context, AttributeSet attrs, int integer) {
      super(context, attrs, integer);
      // TODO Auto-generated constructor stub
      mContext = context;
      mFadeIn = false;
   }

   @Override
   public void setImageBitmap(Bitmap bm) {
      super.setImageBitmap(bm);
      if (mFadeIn)
         this.setAnimation(AnimationUtils.loadAnimation(mContext,
            R.anim.fade_in));
      // super.setImageBitmap(bm);
   }

   public void setFadeIn(boolean fade) {
      mFadeIn = fade;
   }

}
