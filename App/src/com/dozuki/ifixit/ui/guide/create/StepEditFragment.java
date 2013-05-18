package com.dozuki.ifixit.ui.guide.create;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.GuideStep;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;


public class StepEditFragment extends Fragment {

   private static final String GUIDE_STEP_KEY = "GUIDE_STEP_KEY";
   private static final String STEP_EMBED_FRAGMENT_TAG = "STEP_EMBED_FRAGMENT_TAG";
   private static final String STEP_VIDEO_FRAGMENT_TAG = "STEP_VIDEO_FRAGMENT_TAG";
   private static final String STEP_IMAGE_FRAGMENT_TAG = "STEP_IMAGE_FRAGMENT_TAG";

   private GuideStep mStepObject;
   private StepEditLinesFragment mEditBulletFrag;
   private StepEditImageFragment mEditImageFrag;
   private StepEditVideoFragment mEditVideoFrag;
   private StepEditEmbedFragment mEditEmbedFrag;


   static StepEditFragment newInstance(GuideStep step) {
      StepEditFragment frag = new StepEditFragment();
      Bundle args = new Bundle();
      args.putSerializable(GUIDE_STEP_KEY, step);
      frag.setArguments(args);
      return frag;
   }

   /////////////////////////////////////////////////////
   // LIFECYCLE
   /////////////////////////////////////////////////////

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {

      View v = inflater.inflate(R.layout.guide_create_step_edit_body, container, false);
      Bundle b = getArguments();
      mStepObject = (GuideStep) b.getSerializable(StepsEditActivity.GUIDE_STEP_KEY);
      String stepType = mStepObject.type();
      Log.w("StepEditFragment", stepType);

      if (savedInstanceState != null) {
         mStepObject = (GuideStep) savedInstanceState.getSerializable(GUIDE_STEP_KEY);

         if (stepType.equals("video")) {
            mEditVideoFrag = (StepEditVideoFragment) getChildFragmentManager().findFragmentByTag(
             STEP_VIDEO_FRAGMENT_TAG);
         } else if (stepType.equals("embed")) {
            mEditEmbedFrag = (StepEditEmbedFragment) getChildFragmentManager().findFragmentByTag(
             STEP_EMBED_FRAGMENT_TAG);
         } else if (stepType.equals("image")) {
            mEditImageFrag = (StepEditImageFragment) getChildFragmentManager().findFragmentByTag(
             STEP_IMAGE_FRAGMENT_TAG);
         }

         mEditBulletFrag = (StepEditLinesFragment) getChildFragmentManager()
          .findFragmentById(R.id.guide_create_edit_bullet_fragment_container);

      } else {

         mEditBulletFrag = new StepEditLinesFragment();
         mEditBulletFrag.setRetainInstance(true);

         FragmentTransaction ft = getChildFragmentManager()
          .beginTransaction()
          .add(R.id.guide_create_edit_bullet_fragment_container, mEditBulletFrag);

         if (stepType.equals("video")) {
            ft.add(R.id.guide_create_edit_media_fragment_container, new StepEditVideoFragment(),
             STEP_VIDEO_FRAGMENT_TAG);
         } else if (stepType.equals("embed")) {
            ft.add(R.id.guide_create_edit_media_fragment_container, new StepEditEmbedFragment(),
             STEP_IMAGE_FRAGMENT_TAG);
         } else if (stepType.equals("image")) {
            ft.add(R.id.guide_create_edit_media_fragment_container, new StepEditImageFragment(),
             STEP_IMAGE_FRAGMENT_TAG);
         }

         ft.commit();
      }

      setCopiesForEdit(stepType);

      return v;
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);

      savedInstanceState.putSerializable(GUIDE_STEP_KEY, mStepObject);
   }

   /////////////////////////////////////////////////////
   // HELPERS
   /////////////////////////////////////////////////////

   private void setCopiesForEdit(String stepType) {
      mEditBulletFrag.setSteps(mStepObject.getLines());
      mEditBulletFrag.setStepTitle(mStepObject.getTitle());
      mEditBulletFrag.setStepNumber(mStepObject.getStepNum());
      if (stepType.equals("video")) {
         mEditVideoFrag.setVideo(mStepObject.getVideo());
      } else if (stepType.equals("image")) {
         mEditImageFrag.setImages(mStepObject.getImages());
      }
   }

   public GuideStep getGuideChanges() {

      mStepObject.setLines(mEditBulletFrag.getLines());
      mStepObject.setTitle(mEditBulletFrag.getTitle());
      mStepObject.setImages(mEditImageFrag.getImages());

      return mStepObject;
   }

   public void setMediaResult(int requestCode, int resultCode, Intent data) {
      mEditImageFrag.onActivityResult(requestCode, resultCode, data);
   }
}
