package com.dozuki.ifixit.ui.guide.create;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import com.actionbarsherlock.view.Menu;
import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.ui.BaseMenuDrawerActivity;
import com.dozuki.ifixit.ui.LoadingFragment;
import com.dozuki.ifixit.ui.guide.create.StepReorderFragment.StepRearrangeListener;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.squareup.otto.Subscribe;

public class StepsActivity extends BaseMenuDrawerActivity implements StepRearrangeListener {
   static final int GUIDE_EDIT_STEP_REQUEST = 0;
   private static final String GUIDE_STEPS_PORTAL_FRAG = "GUIDE_STEPS_PORTAL_FRAG";
   public static String GUIDE_KEY = "GUIDE_KEY";
   public static String GUIDE_ID_KEY = "GUIDE_ID_KEY";
   public static String GUIDE_PUBLIC_KEY = "GUIDE_PUBLIC_KEY";

   private StepPortalFragment mStepPortalFragment;
   private Guide mGuide;
   private boolean mIsLoading;

   /////////////////////////////////////////////////////
   // LIFECYCLE
   /////////////////////////////////////////////////////

   @Override
   public void onCreate(Bundle savedInstanceState) {
      int guideid = 0;

      super.onCreate(savedInstanceState);

      if (savedInstanceState != null) {
         // to persist mGuide
         mGuide = (Guide) savedInstanceState.getSerializable(StepsActivity.GUIDE_KEY);
         mIsLoading = savedInstanceState.getBoolean(LOADING);
      }

      Bundle extras = getIntent().getExtras();
      if (extras != null) {
         mGuide = (Guide) extras.getSerializable(StepsActivity.GUIDE_KEY);
         guideid = extras.getInt(StepsActivity.GUIDE_ID_KEY, 0);
         if (guideid == 0 && mGuide != null) {
            guideid = mGuide.getGuideid();
         }
      }

      setContentView(R.layout.guide_create_steps_root);

      if (findViewById(R.id.guide_create_fragment_steps_container) != null
       && getSupportFragmentManager().findFragmentByTag(GUIDE_STEPS_PORTAL_FRAG) == null) {

         mStepPortalFragment = new StepPortalFragment();
         Bundle fragArgs = new Bundle();
         fragArgs.putInt(GUIDE_ID_KEY, guideid);
         if (mGuide != null) {
            fragArgs.putSerializable(GUIDE_KEY, mGuide);
         }
         mStepPortalFragment.setArguments(fragArgs);
         mStepPortalFragment.setRetainInstance(true);
         getSupportFragmentManager().beginTransaction()
          .add(R.id.guide_create_fragment_steps_container, mStepPortalFragment, GUIDE_STEPS_PORTAL_FRAG).commit();
      } else {
         mStepPortalFragment = (StepPortalFragment) getSupportFragmentManager()
          .findFragmentByTag(GUIDE_STEPS_PORTAL_FRAG);
      }

      if (mIsLoading) {
         getSupportFragmentManager().beginTransaction().hide(mStepPortalFragment).addToBackStack(null).commit();
      }
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);

      savedInstanceState.putSerializable(StepsActivity.GUIDE_KEY, mGuide);
      savedInstanceState.putBoolean(LOADING, mIsLoading);
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
         }
      }
   }

   @Override
   public void onReorderComplete(boolean reodered) {
      ((StepRearrangeListener) getSupportFragmentManager().findFragmentByTag(GUIDE_STEPS_PORTAL_FRAG))
       .onReorderComplete(reodered);
   }

   @Override
   public boolean finishActivityIfLoggedOut() {
      return true;
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getSupportMenuInflater().inflate(R.menu.step_list_menu, menu);

      return super.onCreateOptionsMenu(menu);
   }

   /////////////////////////////////////////////////////
   // NOTIFICATION LISTENERS
   /////////////////////////////////////////////////////

   @Subscribe
   public void onRetrievedGuide(ApiEvent.GuideForEdit event) {
      if (!event.hasError()) {
         mGuide = event.getResult();
         App.getGaTracker().set(Fields.SCREEN_NAME, "/user/guides/" + mGuide.getGuideid());
         App.getGaTracker().send(MapBuilder.createAppView().build());

         hideLoading();
      } else {
         Api.getErrorDialog(StepsActivity.this, event).show();
      }
   }

   @Subscribe
   public void onIntroSavedGuide(ApiEvent.EditGuide event) {
      if (!event.hasError()) {
         mGuide = event.getResult();
         hideLoading();
      } else {
         Api.getErrorDialog(StepsActivity.this, event).show();
      }
   }

   /////////////////////////////////////////////////////
   // HELPERS
   /////////////////////////////////////////////////////

   public void showLoading() {
      showLoading(R.id.guide_create_fragment_steps_container);
   }

   @Override
   public void showLoading(int container) {
      mStepPortalFragment =
       (StepPortalFragment) getSupportFragmentManager().findFragmentByTag(GUIDE_STEPS_PORTAL_FRAG);
      getSupportFragmentManager().beginTransaction()
       .add(container, new LoadingFragment(), LOADING).addToBackStack(LOADING)
       .commit();
      if (mStepPortalFragment != null) {
         getSupportFragmentManager().beginTransaction().hide(mStepPortalFragment).addToBackStack(null).commit();
      }
      mIsLoading = true;
   }

   @Override
   public void hideLoading() {
      getSupportFragmentManager().popBackStack(LOADING, FragmentManager.POP_BACK_STACK_INCLUSIVE);
      mIsLoading = false;
   }
}
