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

      StepImageFragment imageFrag;
      StepVideoFragment videoFrag;
      StepEmbedFragment embedFrag;
      StepLinesFragment linesFrag;
      if (savedInstanceState != null) {
         mStep = (GuideStep) savedInstanceState.getSerializable(GUIDE_STEP_KEY);
         String stepType = mStep.type();

         if (stepType.equals(VIDEO_TYPE)) {
            videoFrag = (StepVideoFragment) getChildFragmentManager().findFragmentByTag(STEP_VIDEO_FRAGMENT_TAG);
         } else if (stepType.equals(EMBED_TYPE)) {
            embedFrag = (StepEmbedFragment) getChildFragmentManager().findFragmentByTag(STEP_EMBED_FRAGMENT_TAG);
         } else if (stepType.equals(IMAGE_TYPE)) {
            imageFrag = (StepImageFragment) getChildFragmentManager().findFragmentByTag(STEP_IMAGE_FRAGMENT_TAG);
         }

         linesFrag = (StepLinesFragment) getChildFragmentManager().findFragmentById(R.id.guide_step_lines);

      } else {
         String stepType = mStep.type();
         linesFrag = new StepLinesFragment();
         linesFrag.setRetainInstance(true);
         Bundle linesArgs = new Bundle();

         linesArgs.putSerializable(StepLinesFragment.GUIDE_STEP, mStep);

         linesFrag.setArguments(linesArgs);

         FragmentTransaction ft = getChildFragmentManager()
          .beginTransaction()
          .add(R.id.guide_step_lines, linesFrag);

         if (stepType.equals(VIDEO_TYPE)) {
            videoFrag = StepVideoFragment.newInstance(mStep.getVideo(), mIsOfflineGuide);
            ft.add(MEDIA_CONTAINER, videoFrag, STEP_VIDEO_FRAGMENT_TAG);
         } else if (stepType.equals(EMBED_TYPE)) {
            embedFrag = StepEmbedFragment.newInstance(mStep.getEmbed(), mIsOfflineGuide);
            ft.add(MEDIA_CONTAINER, embedFrag, STEP_EMBED_FRAGMENT_TAG);
         } else if (stepType.equals(IMAGE_TYPE)) {
            imageFrag = StepImageFragment.newInstance(mStep.getImages(), mIsOfflineGuide);
            ft.add(MEDIA_CONTAINER, imageFrag, STEP_IMAGE_FRAGMENT_TAG);
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
