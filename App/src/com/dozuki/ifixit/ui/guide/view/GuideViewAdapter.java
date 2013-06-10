package com.dozuki.ifixit.ui.guide.view;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.dozuki.ifixit.model.guide.Guide;

public class GuideViewAdapter extends FragmentStatePagerAdapter {
   private static final int GUIDE_INTRO_POSITION = 0;
   private static final int STEP_OFFSET = 1;

   private Guide mGuide;

   public GuideViewAdapter(FragmentManager fm, Guide guide) {
      super(fm);
      mGuide = guide;
   }

   @Override
   public int getCount() {
      if (mGuide != null) {
         return mGuide.getNumSteps() + STEP_OFFSET;
      } else {
         return 0;
      }
   }

   @Override
   public Fragment getItem(int position) {
      Fragment introView;
      Fragment stepView;

      if (position == GUIDE_INTRO_POSITION) {
         introView = new GuideIntroViewFragment(mGuide);

         return introView;
      } else {
         stepView = new GuideStepViewFragment(mGuide.getStep(position - STEP_OFFSET));

         return stepView;
      }
   }
}
