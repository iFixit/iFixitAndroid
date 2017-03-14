package com.dozuki.ifixit.ui.guide.view;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.ui.BaseFragment;
import com.dozuki.ifixit.ui.guide.StepEmbedFragment;
import com.dozuki.ifixit.ui.guide.StepVideoFragment;

public class GuideStepViewFragment extends BaseFragment {

   private static final String GUIDE_STEP_KEY = "GUIDE_STEP_KEY";
   private static final int MEDIA_CONTAINER = R.id.guide_step_media;
   private static final String STEP_IMAGE_FRAGMENT_TAG = "STEP_IMAGE_FRAGMENT_TAG";
   private static final String STEP_VIDEO_FRAGMENT_TAG = "STEP_VIDEO_FRAGMENT_TAG";
   private static final String STEP_EMBED_FRAGMENT_TAG = "STEP_EMBED_FRAGMENT_TAG";

   private static final String VIDEO_TYPE = "video";
   private static final String IMAGE_TYPE = "image";
   private static final String EMBED_TYPE = "embed";

   private GuideStep mStep;

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      Bundle bundle = this.getArguments();

      mStep = (GuideStep)bundle.getSerializable("STEP_KEY");
      boolean mIsOfflineGuide = bundle.getBoolean("OFFLINE_KEY");

      View view = inflater.inflate(R.layout.guide_step, container, false);

      StepImageFragment mImageFrag;
      StepVideoFragment mVideoFrag;
      StepEmbedFragment mEmbedFrag;
      StepLinesFragment mLinesFrag;
      if (savedInstanceState != null) {
         mStep = (GuideStep) savedInstanceState.getSerializable(GUIDE_STEP_KEY);
         String stepType = mStep.type();

         if (stepType.equals(VIDEO_TYPE)) {
            mVideoFrag = (StepVideoFragment) getChildFragmentManager().findFragmentByTag(STEP_VIDEO_FRAGMENT_TAG);
         } else if (stepType.equals(EMBED_TYPE)) {
            mEmbedFrag = (StepEmbedFragment) getChildFragmentManager().findFragmentByTag(STEP_EMBED_FRAGMENT_TAG);
         } else if (stepType.equals(IMAGE_TYPE)) {
            mImageFrag = (StepImageFragment) getChildFragmentManager().findFragmentByTag(STEP_IMAGE_FRAGMENT_TAG);
         }

         mLinesFrag = (StepLinesFragment) getChildFragmentManager().findFragmentById(R.id.guide_step_lines);

      } else {
         String stepType = mStep.type();
         mLinesFrag = new StepLinesFragment();
         mLinesFrag.setRetainInstance(true);
         Bundle linesArgs = new Bundle();

         linesArgs.putSerializable(StepLinesFragment.GUIDE_STEP, mStep);

         mLinesFrag.setArguments(linesArgs);

         FragmentTransaction ft = getChildFragmentManager()
          .beginTransaction()
          .add(R.id.guide_step_lines, mLinesFrag);

         if (stepType.equals(VIDEO_TYPE)) {
            mVideoFrag = StepVideoFragment.newInstance(mStep.getVideo(), mIsOfflineGuide);
            ft.add(MEDIA_CONTAINER, mVideoFrag, STEP_VIDEO_FRAGMENT_TAG);
         } else if (stepType.equals(EMBED_TYPE)) {
            mEmbedFrag = StepEmbedFragment.newInstance(mStep.getEmbed(), mIsOfflineGuide);
            ft.add(MEDIA_CONTAINER, mEmbedFrag, STEP_EMBED_FRAGMENT_TAG);
         } else if (stepType.equals(IMAGE_TYPE)) {
            mImageFrag = StepImageFragment.newInstance(mStep.getImages(), mIsOfflineGuide);
            ft.add(MEDIA_CONTAINER, mImageFrag, STEP_IMAGE_FRAGMENT_TAG);
         }

         ft.commit();
      }

      return view;
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putSerializable(GUIDE_STEP_KEY, mStep);
   }
}
