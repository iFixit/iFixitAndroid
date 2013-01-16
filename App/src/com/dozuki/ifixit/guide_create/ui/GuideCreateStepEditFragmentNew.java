package com.dozuki.ifixit.guide_create.ui;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.model.GuideCreateStepObject;
import com.dozuki.ifixit.guide_view.model.StepImage;

public class GuideCreateStepEditFragmentNew extends Fragment {
  
   public interface GuideStepChangedListener {
      public void onGuideStepChanged();
   }
   
   private static final String GUIDE_STEP_KEY = "GUIDE_STEP_KEY";
   private GuideCreateStepObject mStepObject;
   private GuideCreateEditBulletFragment mEditBulletFrag;
   private GuideCreateEditMediaFragment mEditMediaFrag;
   
   
   
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, 
       Bundle savedInstanceState) {

      View v = inflater.inflate(R.layout.guide_create_step_edit_body_new, container, false);
      Bundle b = getArguments();
      mStepObject = (GuideCreateStepObject) b.getSerializable(GuideCreateStepsEditActivity.GUIDE_STEP_KEY);
      if (savedInstanceState != null) {
         mEditMediaFrag = (GuideCreateEditMediaFragment) getChildFragmentManager().findFragmentById(R.id.guide_create_edit_media_fragment_container);
         mEditBulletFrag = (GuideCreateEditBulletFragment) getChildFragmentManager().findFragmentById(R.id.guide_create_edit_bullet_fragment_container);
         
         mStepObject =  (GuideCreateStepObject) savedInstanceState.getSerializable(GUIDE_STEP_KEY);
         Log.e("save", "lol");
         mStepObject.getLines();
          return v;
      }

      mEditMediaFrag = new GuideCreateEditMediaFragment();

      getChildFragmentManager().beginTransaction()
              .add(R.id.guide_create_edit_media_fragment_container, mEditMediaFrag).commit(); 
      
    
      mEditBulletFrag = new GuideCreateEditBulletFragment();
      getChildFragmentManager().beginTransaction()
              .add(R.id.guide_create_edit_bullet_fragment_container, mEditBulletFrag).commit();
      
       setCopiesForEdit();
       
       return v;
   }



   private void setCopiesForEdit() {
      mEditBulletFrag.setSteps(mStepObject.getLines());
      mEditMediaFrag.setStepTitle(mStepObject.getTitle());
      
      if(mStepObject.getImages().size() > 0) {
         mEditMediaFrag.setImage(GuideCreateEditMediaFragment.IMAGE_KEY_1, mStepObject.getImages().get(0));
      }
      
      if(mStepObject.getImages().size() > 1) {
         mEditMediaFrag.setImage(GuideCreateEditMediaFragment.IMAGE_KEY_2, mStepObject.getImages().get(1));
      }
      
      if(mStepObject.getImages().size() > 2) {
         
         mEditMediaFrag.setImage(GuideCreateEditMediaFragment.IMAGE_KEY_3, mStepObject.getImages().get(2));
      }
      
   }

   public GuideCreateStepObject syncGuideChanges() {
      //lines
      mStepObject.getLines().clear();
      mStepObject.getLines().addAll(mEditBulletFrag.getLines());
      //title
      mStepObject.setTitle(mEditMediaFrag.getTitle());
      //media
      //mStepObject.setImages(mEditMediaFrag.getImageIDs());
      mStepObject.getImages().clear();
      for(StepImage si : mEditMediaFrag.getImageIDs())
      {  
         if(si.getImageid() != GuideCreateEditMediaFragment.NO_IMAGE) {
          mStepObject.addImage(si);
         }
         
      }
      return mStepObject;
   }
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
     // super.onActivityResult(requestCode, resultCode, data);
      mEditMediaFrag.onActivityResult(requestCode, resultCode, data);
   }
   
   
   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
    
      savedInstanceState.putSerializable(GUIDE_STEP_KEY,
         mStepObject);
   }
   

}