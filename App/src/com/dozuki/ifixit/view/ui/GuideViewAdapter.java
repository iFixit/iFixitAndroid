package com.dozuki.ifixit.view.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.dozuki.ifixit.view.model.Guide;
import com.ifixit.android.imagemanager.ImageManager;

public class GuideViewAdapter extends
		FragmentStatePagerAdapter {
	private static final int GUIDE_INTRO_POSITION = 0;
	private static final int STEP_OFFSET = 1;

	private Guide mGuide;
	private ImageManager mImageManager;

	public GuideViewAdapter(FragmentManager fm,
			ImageManager im, Guide guide) {
		super(fm);
		mImageManager = im;
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
		GuideIntroViewFragment introView;
		GuideStepViewFragment stepView;

		if (position == GUIDE_INTRO_POSITION) {
			introView = new GuideIntroViewFragment(mGuide);

			return introView;
		} else {
			stepView = new GuideStepViewFragment(
					mImageManager, mGuide.getStep(position
							- STEP_OFFSET));

			return stepView;
		}
	}
}
