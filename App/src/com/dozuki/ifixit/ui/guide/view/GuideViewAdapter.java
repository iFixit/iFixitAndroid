package com.dozuki.ifixit.ui.guide.view;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;

public class GuideViewAdapter extends FragmentStatePagerAdapter {
   private static final int GUIDE_INTRO_POSITION = 0;

   private int mStepOffset = 1;

   // Default these to a page number that doesn't exist. They will be updated
   // if the guide has tools and parts.
   private int mToolsPosition = -1;
   private int mPartsPosition = -1;

   private Guide mGuide;

   public GuideViewAdapter(FragmentManager fm, Guide guide) {
      super(fm);
      mGuide = guide;

      if (guideHasTools()) {
         mToolsPosition = mStepOffset;
         mStepOffset++;
      }

      if (guideHasParts()) {
         mPartsPosition = mStepOffset;
         mStepOffset++;
      }
   }

   @Override
   public int getCount() {
      if (mGuide != null) {
         return mGuide.getNumSteps() + mStepOffset;
      } else {
         return 0;
      }
   }

   @Override
   public Fragment getItem(int position) {
      if (position == GUIDE_INTRO_POSITION) {
         return new GuideIntroViewFragment(mGuide);
      } else if (position == mToolsPosition) {
         return new GuidePartsToolsViewFragment(mGuide.getTools());
      } else if (position == mPartsPosition) {
         return new GuidePartsToolsViewFragment(mGuide.getParts());
      } else {
         return new GuideStepViewFragment(mGuide.getStep(position - mStepOffset));
      }
   }

   @Override
   public CharSequence getPageTitle(int position) {
      if (position == GUIDE_INTRO_POSITION) {
         return MainApplication.get().getString(R.string.introduction);
      } else if (position == mToolsPosition) {
         return MainApplication.get().getString(R.string.requiredTools);
      } else if (position == mPartsPosition) {
         return MainApplication.get().getString(R.string.requiredParts);
      } else {
         return MainApplication.get().getString(R.string.step_number, (position - mStepOffset) + 1);
      }
   }

   public int getStepOffset() {
      return mStepOffset;
   }

   private boolean guideHasTools() {
      return mGuide.getNumTools() != 0;
   }

   private boolean guideHasParts() {
      return mGuide.getNumParts() != 0;
   }
}
