package com.ifixit.android.ifixit;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

public class GuidePagerAdapter extends PagerAdapter {
   private static final int GUIDE_INTRO_POSITION = 0;
   private static final int STEP_OFFSET = 1;

   private Context mContext;
   private Guide mGuide;
   private final ImageManager mImageManager;
   
   public GuidePagerAdapter(Context context, Guide guide,
    ImageManager imageManager) {
      mContext = context;
      mGuide = guide;
      mImageManager = imageManager;
   }
   
   @Override
   public void destroyItem(View collection, int position, Object view) {
      ((ViewPager)collection).removeView((View)view);
   }

   @Override
   public int getCount() {
      if (mGuide != null)
         return mGuide.getNumSteps() + 1;
      else
         return 0;
   }

   @Override
   public Object instantiateItem(View collection, int position) {
      GuideIntroView introView;
      GuideStepView stepView;
      
      if (position == GUIDE_INTRO_POSITION) {
         introView = new GuideIntroView(mContext, mGuide);
         
         /*mImageManager.displayImage(mGuide.getIntroImage() + ".large",
          (Activity)mContext, introView.getImageView());*/
         ((ViewPager) collection).addView(introView);
 
         return introView;
      } else {
         stepView = new GuideStepView(mContext, mGuide.getStep(position -
          STEP_OFFSET),
          mImageManager);
         ((ViewPager)collection).addView(stepView);
         
         return stepView;
      }
   }

   @Override
   public boolean isViewFromObject(View view, Object object) {
      return view == object;
   }

   @Override
   public void restoreState(Parcelable arg0, ClassLoader arg1) {}

   @Override
   public void finishUpdate(View arg0) {}

   @Override
   public Parcelable saveState() {
      return null;
   }

   @Override
   public void startUpdate(View arg0) {}
}
