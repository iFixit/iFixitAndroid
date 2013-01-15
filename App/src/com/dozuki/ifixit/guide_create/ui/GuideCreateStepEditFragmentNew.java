package com.dozuki.ifixit.guide_create.ui;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.model.GuideCreateStepObject;

public class GuideCreateStepEditFragmentNew extends Fragment {
  
   private GuideCreateStepObject mStepObject;
   private GuideCreateEditBulletFragment mEditBulletFrag;
   private GuideCreateEditMediaFragment mEditMediaFrag;
   
   
   
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, 
       Bundle savedInstanceState) {

      View v = inflater.inflate(R.layout.guide_create_step_edit_body_new, container, false);
      
      if (savedInstanceState != null) {
         mEditMediaFrag = (GuideCreateEditMediaFragment) getChildFragmentManager().findFragmentById(R.id.guide_create_edit_media_fragment_container);
         mEditBulletFrag = (GuideCreateEditBulletFragment) getChildFragmentManager().findFragmentById(R.id.guide_create_edit_bullet_fragment_container);
         
          return v;
      }

      mEditMediaFrag = new GuideCreateEditMediaFragment();

      getChildFragmentManager().beginTransaction()
              .add(R.id.guide_create_edit_media_fragment_container, mEditMediaFrag).commit();
      
      Bundle b = getArguments();
      mStepObject = (GuideCreateStepObject) b.getSerializable(GuideCreateStepsEditActivity.GUIDE_STEP_KEY);
      mStepObject.getLines().size();
      Bundle args = new Bundle();
      mEditBulletFrag = new GuideCreateEditBulletFragment();
      mEditBulletFrag.setSteps(mStepObject);
      args.putSerializable(GuideCreateStepsEditActivity.GUIDE_STEP_KEY, mStepObject);
      mEditBulletFrag.setArguments(args);
      getChildFragmentManager().beginTransaction()
              .add(R.id.guide_create_edit_bullet_fragment_container, mEditBulletFrag).commit();
      
      
     
       return v;
   }



   public void syncGuideChanges() {
      mStepObject.setLines(mEditBulletFrag.getLines());
      mStepObject.setImages(mEditMediaFrag.getImageIDs());
      mStepObject.setTitle(mEditMediaFrag.getTitle());
      //API Call
   }

}