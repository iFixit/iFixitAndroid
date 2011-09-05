package com.ifixit.guidebook;

import android.content.Context;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;


public class GuidePagerAdapter extends PagerAdapter {

   private Context mContext;
   private Guide mGuide;
   
   public GuidePagerAdapter(Context context, Guide guide) {
      mContext = context;
      mGuide = guide;

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

      
      // TODO: Figure out what view we want (Intro or Guide Step) and 
      // inflate GuideIntroView or GuideStepView (respectively) to the page.
      
      if (position == 0) {
         introView = new GuideIntroView(mContext, mGuide);    
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
