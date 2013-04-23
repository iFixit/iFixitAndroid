package com.dozuki.ifixit.ui.guide.create;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.GuideStep;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;


public class StepEditFragment extends Fragment {

   private static final String GUIDE_STEP_KEY = "GUIDE_STEP_KEY";
   private GuideStep mStepObject;
   private StepEditLinesFragment mEditBulletFrag;
   private StepEditMediaFragment mEditMediaFrag;


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

      if (savedInstanceState != null) {
         Log.w(getTag(), "SavedInstanceState is not null");
         mEditMediaFrag = (StepEditMediaFragment) getChildFragmentManager()
          .findFragmentById(R.id.guide_create_edit_media_fragment_container);
         mEditBulletFrag = (StepEditLinesFragment) getChildFragmentManager()
          .findFragmentById(R.id.guide_create_edit_bullet_fragment_container);

      } else {
         Log.w(getTag(), "SavedInstanceState is null");

         mEditMediaFrag = new StepEditMediaFragment();
         mEditBulletFrag = new StepEditLinesFragment();

         mEditBulletFrag.setRetainInstance(true);

         getChildFragmentManager()
          .beginTransaction()
          .add(R.id.guide_create_edit_media_fragment_container, mEditMediaFrag)
          .add(R.id.guide_create_edit_bullet_fragment_container, mEditBulletFrag)
          .commit();

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
      mEditBulletFrag.setStepNumber(mStepObject.getStepNum());
      mEditMediaFrag.setImages(mStepObject.getImages());
   }

   public GuideStep getGuideChanges() {

      mStepObject.setLines(mEditBulletFrag.getLines());
      mStepObject.setTitle(mEditBulletFrag.getTitle());
      mStepObject.setImages(mEditMediaFrag.getImages());

      return mStepObject;
   }

   public void setMediaResult(int requestCode, int resultCode, Intent data) {
      mEditMediaFrag.onActivityResult(requestCode, resultCode, data);
   }

   public GuideStep getStepObject() {
      return mStepObject;
   }

   public void setGuideStep(GuideStep guideCreateStepObject) {
      mStepObject = guideCreateStepObject;
      setCopiesForEdit();
   }

}
