package com.dozuki.ifixit.ui.guide.create;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import com.actionbarsherlock.app.ActionBar;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.ui.IfixitActivity;
import com.dozuki.ifixit.ui.guide.create.StepReorderFragment.StepRearrangeListener;
import com.dozuki.ifixit.ui.guide.view.LoadingFragment;
import com.dozuki.ifixit.util.APIError;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.squareup.otto.Subscribe;
import org.holoeverywhere.app.AlertDialog;

import java.util.ArrayList;

public class StepsActivity extends IfixitActivity implements StepRearrangeListener {
   static final int GUIDE_EDIT_STEP_REQUEST = 0;
   private static final String SHOWING_HELP = "SHOWING_HELP";
   private static final String GUIDE_STEPS_PORTAL_FRAG = "GUIDE_STEPS_PORTAL_FRAG";
   public static String GUIDE_KEY = "GUIDE_KEY";
   private static final String LOADING = "LOADING";
   private ActionBar mActionBar;
   private StepPortalFragment mStepPortalFragment;
   private ArrayList<GuideStep> mStepList;
   private Guide mGuide;
   private boolean mIsLoading;

   /////////////////////////////////////////////////////
   // LIFECYCLE
   /////////////////////////////////////////////////////

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setTheme(((MainApplication) getApplication()).getSiteTheme());

      mActionBar = getSupportActionBar();
      mActionBar.setTitle("");
      mActionBar.setDisplayHomeAsUpEnabled(true);

      if (savedInstanceState != null) {
         // to persist mGuide
         mGuide = (Guide) savedInstanceState.getSerializable(StepsActivity.GUIDE_KEY);
         mIsLoading = savedInstanceState.getBoolean(LOADING);
      }

      setContentView(R.layout.guide_create_steps_root);
      String tag = GUIDE_STEPS_PORTAL_FRAG;

      if (findViewById(R.id.guide_create_fragment_steps_container) != null
       && getSupportFragmentManager().findFragmentByTag(tag) == null) {

         Bundle extras = getIntent().getExtras();

         int guideID = 0;

         if (extras != null) {
            guideID = extras.getInt(StepsActivity.GUIDE_KEY);
         }

         mStepPortalFragment = new StepPortalFragment();
         Bundle fragArgs = new Bundle();
         fragArgs.putInt(GUIDE_KEY, guideID);
         mStepPortalFragment.setArguments(fragArgs);
         mStepPortalFragment.setRetainInstance(true);
         getSupportFragmentManager().beginTransaction()
          .add(R.id.guide_create_fragment_steps_container, mStepPortalFragment, tag).commit();
      } else {
         mStepPortalFragment = (StepPortalFragment) getSupportFragmentManager().findFragmentByTag(tag);
      }

      if (mIsLoading) {
         getSupportFragmentManager().beginTransaction().hide(mStepPortalFragment).addToBackStack(null).commit();
      }
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      savedInstanceState.putSerializable(StepsActivity.GUIDE_KEY, mGuide);
      savedInstanceState.putBoolean(LOADING, mIsLoading);
      super.onSaveInstanceState(savedInstanceState);
   }

   @Override
   public void finish() {
      Intent returnIntent = new Intent();
      returnIntent.putExtra(GuideCreateActivity.GUIDE_KEY, mGuide);
      setResult(RESULT_OK, returnIntent);

      super.finish();
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);

      if (requestCode == GUIDE_EDIT_STEP_REQUEST && resultCode == RESULT_OK) {

         Guide guide = (Guide) data.getSerializableExtra(GuideCreateActivity.GUIDE_KEY);
         if (guide != null) {
            mGuide = guide;
            mStepList = mGuide.getSteps();
         }

      }
   }

   @Override
   public void onReorderComplete(boolean val) {
      ((StepRearrangeListener) getSupportFragmentManager().findFragmentByTag(GUIDE_STEPS_PORTAL_FRAG))
       .onReorderComplete(val);
   }

   @Override
   public boolean finishActivityIfLoggedOut() {
      return true;
   }

   /////////////////////////////////////////////////////
   // NOTIFICATION LISTENERS
   /////////////////////////////////////////////////////

   @Subscribe
   public void onRetrievedGuide(APIEvent.GuideForEdit event) {
      if (!event.hasError()) {
         mGuide = event.getResult();
         mStepList = mGuide.getSteps();
         if (mGuide.getSteps() != null && mGuide.getSteps().size() == 0) {

         }
         hideLoading();
      } else {
         event.setError(APIError.getFatalError(this));
         APIService.getErrorDialog(StepsActivity.this, event.getError(), null).show();
      }
   }

   @Subscribe
   public void onIntroSavedGuide(APIEvent.EditGuide event) {
      if (!event.hasError()) {
         mGuide = event.getResult();
         mStepList = mGuide.getSteps();
         hideLoading();
      } else {
         event.setError(APIError.getFatalError(this));
         APIService.getErrorDialog(StepsActivity.this, event.getError(), null).show();

      }
   }

   public void showLoading() {
      mStepPortalFragment =
       (StepPortalFragment) getSupportFragmentManager().findFragmentByTag(GUIDE_STEPS_PORTAL_FRAG);
      getSupportFragmentManager().beginTransaction()
       .add(R.id.guide_create_fragment_steps_container, new LoadingFragment(), "loading").addToBackStack("loading")
       .commit();
      if (mStepPortalFragment != null) {
         getSupportFragmentManager().beginTransaction().hide(mStepPortalFragment).addToBackStack(null).commit();
      }
      mIsLoading = true;
   }

   public void hideLoading() {
      getSupportFragmentManager().popBackStack("loading", FragmentManager.POP_BACK_STACK_INCLUSIVE);
      mIsLoading = false;
   }


   private AlertDialog createHelpDialog() {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(getString(R.string.media_help_title)).setMessage(getString(R.string.guide_create_steps_help))
       .setPositiveButton(getString(R.string.media_help_confirm), new DialogInterface.OnClickListener() {

          public void onClick(DialogInterface dialog, int id) {
             dialog.cancel();
          }
       });

      return builder.create();
   }

}
