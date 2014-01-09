package com.dozuki.ifixit.ui.guide.view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;

import java.util.HashMap;
import java.util.Map;

public class GuideViewAdapter extends FragmentStatePagerAdapter {
   private static final int GUIDE_INTRO_POSITION = 0;
   private static final int CONTAINER = R.id.guide_pager;
   private FragmentManager mFragmentManager;
   private Map<Integer, String> mPageLabelMap;

   private int mStepOffset = 1;

   // Default these to a page number that doesn't exist. They will be updated
   // if the guide has tools and parts.
   private int mToolsPosition = -1;
   private int mPartsPosition = -1;

   private Guide mGuide;

   public GuideViewAdapter(FragmentManager fm, Guide guide) {
      super(fm);
      mFragmentManager = fm;
      mGuide = guide;

      mPageLabelMap = new HashMap<Integer, String>();

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
      Fragment fragment;
      String label = "/guide/view/" + mGuide.getGuideid();

      if (position == GUIDE_INTRO_POSITION) {
         label += "/intro";
         fragment = new GuideIntroViewFragment(mGuide);
      } else if (position == mToolsPosition) {
         label += "/tools";
         fragment = GuidePartsToolsViewFragment.newInstance(mGuide.getTools());
      } else if (position == mPartsPosition) {
         label += "/parts";
         fragment = GuidePartsToolsViewFragment.newInstance(mGuide.getParts());
      } else {
         int stepNumber = (position - mStepOffset);
         label += "/" + (stepNumber + 1); // Step title # should be 1 indexed.

         fragment = new GuideStepViewFragment(mGuide.getStep(stepNumber));
      }

      mPageLabelMap.put(position, label);
      return fragment;
   }

   public String getFragmentScreenLabel(int key) {
      return mPageLabelMap.get(key);
   }

   @Override
   public void destroyItem(View container, int position, Object object) {
      super.destroyItem(container, position, object);

      mPageLabelMap.remove(position);
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

   public void swapCurrentPage(Fragment frag) {
      mFragmentManager.beginTransaction().replace(CONTAINER, frag).commit();
      notifyDataSetChanged();
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
