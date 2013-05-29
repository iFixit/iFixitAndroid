package com.dozuki.ifixit.ui.guide.create;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.ui.guide.StepVideoFragment;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;


public class StepEditFragment extends Fragment {

   private static final String GUIDE_STEP_KEY = "GUIDE_STEP_NUM_KEY";
   private static final String STEP_EMBED_FRAGMENT_TAG = "STEP_EMBED_FRAGMENT_TAG";
   private static final String STEP_VIDEO_FRAGMENT_TAG = "STEP_VIDEO_FRAGMENT_TAG";
   private static final String STEP_IMAGE_FRAGMENT_TAG = "STEP_IMAGE_FRAGMENT_TAG";

   private static final String VIDEO_TYPE = "video";
   private static final String IMAGE_TYPE = "image";
   private static final String EMBED_TYPE = "embed";

   private GuideStep mStepObject;
   private String mStepType;
   private StepEditLinesFragment mEditBulletFrag;
   private StepEditImageFragment mEditImageFrag;
   private StepVideoFragment mEditVideoFrag;
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
      mStepObject = (GuideStep) b.getSerializable(StepEditActivity.GUIDE_STEP_NUM_KEY);
      mStepType = mStepObject.type();
      Log.w("StepEditFragment", mStepType);
      mEditEmbedFrag =  new StepEditEmbedFragment();

      if (savedInstanceState != null) {
         mStepObject = (GuideStep) savedInstanceState.getSerializable(GUIDE_STEP_KEY);

         if (mStepType.equals(VIDEO_TYPE)) {
            mEditVideoFrag = (StepVideoFragment) getChildFragmentManager().findFragmentByTag(
             STEP_VIDEO_FRAGMENT_TAG);
         } else if (mStepType.equals(EMBED_TYPE)) {
            mEditEmbedFrag = (StepEditEmbedFragment) getChildFragmentManager().findFragmentByTag(
             STEP_EMBED_FRAGMENT_TAG);
         } else if (mStepType.equals(IMAGE_TYPE)) {
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

         if (mStepType.equals(VIDEO_TYPE)) {
            Bundle videoArgs = new Bundle();

            Log.w("StepEditFragment", mStepObject.toString());
            videoArgs.putSerializable(StepVideoFragment.GUIDE_VIDEO_KEY, mStepObject.getVideo());
            mEditVideoFrag = new StepVideoFragment();
            mEditVideoFrag.setArguments(videoArgs);

            ft.add(R.id.guide_create_edit_media_fragment_container, mEditVideoFrag,
             STEP_VIDEO_FRAGMENT_TAG);
         } else if (mStepType.equals(EMBED_TYPE)) {
            ft.add(R.id.guide_create_edit_media_fragment_container, mEditEmbedFrag,
             STEP_IMAGE_FRAGMENT_TAG);
         } else if (mStepType.equals(IMAGE_TYPE)) {
            mEditImageFrag = new StepEditImageFragment();
            ft.add(R.id.guide_create_edit_media_fragment_container, mEditImageFrag,
             STEP_IMAGE_FRAGMENT_TAG);
         }

         ft.commit();
      }

      setCopiesForEdit();

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

   private void setCopiesForEdit() {
      mEditBulletFrag.setSteps(mStepObject.getLines());
      mEditBulletFrag.setStepTitle(mStepObject.getTitle());
      if (mStepType.equals(IMAGE_TYPE)) {
         mEditImageFrag.setImages(mStepObject.getImages());
      }
   }

   public GuideStep getGuideChanges() {

      mStepObject.setLines(mEditBulletFrag.getLines());
      mStepObject.setTitle(mEditBulletFrag.getTitle());
      if (mStepType.equals(IMAGE_TYPE)) {
         mStepObject.setImages(mEditImageFrag.getImages());
      }

      return mStepObject;
   }

   public void setMediaResult(int requestCode, int resultCode, Intent data) {
      mEditImageFrag.onActivityResult(requestCode, resultCode, data);
   }
}
