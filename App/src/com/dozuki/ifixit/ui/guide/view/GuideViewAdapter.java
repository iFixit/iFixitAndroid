package com.dozuki.ifixit.ui.guide.view;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;
import com.google.analytics.tracking.android.EasyTracker;

public class GuideViewAdapter extends FragmentStatePagerAdapter {
   private static final int GUIDE_INTRO_POSITION = 0;
   private static final int GUIDE_TOOL_POSITION = 1;
   private static final int GUIDE_PARTS_POSITION = 2;
   private int mStepOffset = 1;

   private Guide mGuide;

   public GuideViewAdapter(FragmentManager fm, Guide guide) {
      super(fm);
      mGuide = guide;
      if (guideHasParts()) mStepOffset++;
      if (guideHasTools()) mStepOffset++;
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

      switch (position) {
         case GUIDE_INTRO_POSITION:
            EasyTracker.getTracker().sendView(mGuide.getTitle() + " Introduction");
            return new GuideIntroViewFragment(mGuide);
         case GUIDE_TOOL_POSITION:
            if (!guideHasTools()) break;

            EasyTracker.getTracker().sendView(mGuide.getTitle() + " Tools");
            return new GuidePartsToolsViewFragment(mGuide.getTools());
         case GUIDE_PARTS_POSITION:
            if (!guideHasParts()) break;

            EasyTracker.getTracker().sendView(mGuide.getTitle() + " Parts");
            return new GuidePartsToolsViewFragment(mGuide.getParts());
      }

      int stepNumber = (position - mStepOffset);
      EasyTracker.getTracker()
       .sendView(mGuide.getTitle() + " Step #" + (stepNumber + 1)); // Step title # should be 1 indexed.

      return new GuideStepViewFragment(mGuide.getStep(stepNumber));
   }

   @Override
   public CharSequence getPageTitle(int position) {
      switch (position) {
         case GUIDE_INTRO_POSITION:
            return MainApplication.get().getString(R.string.introduction);
         case GUIDE_TOOL_POSITION:
            if (!guideHasTools()) break;

            return MainApplication.get().getString(R.string.requiredTools);
         case GUIDE_PARTS_POSITION:
            if (!guideHasParts()) break;

            return MainApplication.get().getString(R.string.requiredParts);
      }

      // The step number must account for if there are parts or tools pages and the introduction page,
      // and also should be 1 indexed.
      return MainApplication.get().getString(R.string.step_number, (position - mStepOffset) + 1);
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
