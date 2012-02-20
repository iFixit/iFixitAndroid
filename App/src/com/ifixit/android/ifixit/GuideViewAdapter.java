package com.ifixit.android.ifixit;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.View;

import com.viewpagerindicator.TitleProvider;

public class GuideViewAdapter extends FragmentPagerAdapter 
 implements TitleProvider {
   private static final int GUIDE_INTRO_POSITION = 0;
   private static final int STEP_OFFSET = 1;

   private Guide mGuide;
   private ImageManager mImageManager;
   
   
   public GuideViewAdapter(FragmentManager fm, ImageManager im, Guide guide) {
      super(fm);
      mImageManager = im;
      mGuide = guide;
   }
      
   @Override
   public int getCount() {
      if (mGuide != null)
         return mGuide.getNumSteps() + 1;
      else
         return 0;
   }


   @Override
   public Fragment getItem(int position) {
      
	   GuideIntroViewFragment introView;
      GuideStepViewFragment stepView;
      Log.w("GuideViewAdapter getItem()", "Postion: " + position);
      
      if (position == GUIDE_INTRO_POSITION) {
         introView = new GuideIntroViewFragment(mImageManager, mGuide);
         
         return introView;
      } else {
         stepView = new GuideStepViewFragment(mImageManager, mGuide.getStep(position-STEP_OFFSET));
         
         return stepView;
      }
   }

   @Override
   public String getTitle(int position) {
	   return mGuide.getStep(position).mTitle;
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
