package com.ifixit.guidebook;

import android.app.Activity;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

public class GuidePagerAdapter extends PagerAdapter {

   private Context mContext;
   private Guide mGuide;
   private ImageManager mImageManager;
   
   public GuidePagerAdapter(Context context, Guide guide) {
      mContext = context;
      mGuide = guide;
      mImageManager = new ImageManager(mContext);
   }
   
   @Override
   public void destroyItem(View collection, int position, Object view) {

      ((ViewPager) collection).removeView((View) view);
      
   }

   @Override
   public void finishUpdate(View arg0) {
      // TODO Auto-generated method stub
      
   }

   @Override
   public int getCount() {
      return mGuide.getNumSteps()+1;
   }

   @Override
   public Object instantiateItem(View collection, int position) {
      
      GuideIntroView introView;
      GuideStepView stepView;
      
      if (position == 0) {
         introView = new GuideIntroView(mContext, mGuide);    

         mImageManager.displayImage(
          "http://guide-images.ifixit.net/igi/jpSRuNYPcGo6fkiD.large",
          (Activity)mContext, introView.getImageView());
         ((ViewPager) collection).addView(introView);

         return introView;
      } else {
         stepView = new GuideStepView(mContext, mGuide.getStep(position - 1));
         ((ViewPager) collection).addView(stepView);
         
         return stepView;
      }
   }

   @Override
   public boolean isViewFromObject(View view, Object object) {
      return view==((View)object);
   }

   @Override
   public void restoreState(Parcelable arg0, ClassLoader arg1) {
      // TODO Auto-generated method stub
      
   }

   @Override
   public Parcelable saveState() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void startUpdate(View arg0) {
      // TODO Auto-generated method stub
      
   }

}
