package com.dozuki.ifixit.ui.guide.create;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.ui.IfixitActivity;
import com.dozuki.ifixit.ui.guide.create.StepReorderFragment.StepRearrangeListener;
import com.dozuki.ifixit.ui.guide.view.LoadingFragment;
import com.dozuki.ifixit.util.APIError;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.squareup.otto.Subscribe;
import org.holoeverywhere.app.AlertDialog;

public class StepsActivity extends IfixitActivity implements StepRearrangeListener {
   static final int GUIDE_EDIT_STEP_REQUEST = 0;
   private static final String SHOWING_HELP = "SHOWING_HELP";
   private static final String GUIDE_STEPS_PORTAL_FRAG = "GUIDE_STEPS_PORTAL_FRAG";
   public static final int MENU_STEP_ADD = 2;
   public static final int MENU_EDIT_INTRO = 3;
   public static final int MENU_REARRANGE_STEPS = 4;
   public static String GUIDE_KEY = "GUIDE_KEY";
   public static String GUIDE_ID_KEY = "GUIDE_ID_KEY";
   public static String GUIDE_PUBLIC_KEY = "GUIDE_PUBLIC_KEY";

   private static final String LOADING = "LOADING";
   private ActionBar mActionBar;
   private StepPortalFragment mStepPortalFragment;
   private Guide mGuide;
   private boolean mIsLoading;
   private boolean mGuidePublic;

   /////////////////////////////////////////////////////
   // LIFECYCLE
   /////////////////////////////////////////////////////

   @Override
   public void onCreate(Bundle savedInstanceState) {
      int guideid = 0;

      super.onCreate(savedInstanceState);

      mActionBar = getSupportActionBar();
      mActionBar.setDisplayHomeAsUpEnabled(true);

      if (savedInstanceState != null) {
         // to persist mGuide
         mGuide = (Guide) savedInstanceState.getSerializable(StepsActivity.GUIDE_KEY);
         mGuidePublic = mGuide.isPublic();
         mIsLoading = savedInstanceState.getBoolean(LOADING);
      }

      Bundle extras = getIntent().getExtras();
      if (extras != null) {
         mGuide = (Guide) extras.getSerializable(StepsActivity.GUIDE_KEY);
         mGuidePublic = extras.getBoolean(StepEditActivity.GUIDE_PUBLIC_KEY);
         if (mGuide != null) {
            mGuidePublic = mGuide.isPublic();
         }
         guideid = extras.getInt(StepsActivity.GUIDE_ID_KEY, 0);
         if (guideid == 0) {
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
            mGuidePublic = mGuide.isPublic();
         } else {
            mGuidePublic = data.getBooleanExtra(StepEditActivity.GUIDE_PUBLIC_KEY, guide.isPublic());
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

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      menu
       .add(1, MENU_STEP_ADD, 0, R.string.add_step)
       .setIcon(R.drawable.ic_action_add)
       .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
      menu
       .add(2, MENU_EDIT_INTRO, 0, R.string.edit_step_intro)
       .setIcon(R.drawable.ic_action_edit)
       .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
      menu
       .add(3, MENU_REARRANGE_STEPS, 0, R.string.reorder_steps)
       .setIcon(R.drawable.ic_dialog_arrange_bullets_light)
       .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);

      if (mGuidePublic) {
         menu
          .add(4, StepEditActivity.MENU_VIEW_GUIDE, 0, R.string.view_guide)
          .setIcon(R.drawable.ic_action_book)
          .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
      }

      return super.onCreateOptionsMenu(menu);
   }

   /////////////////////////////////////////////////////
   // NOTIFICATION LISTENERS
   /////////////////////////////////////////////////////

   @Subscribe
   public void onRetrievedGuide(APIEvent.GuideForEdit event) {
      if (!event.hasError()) {
         mGuide = event.getResult();
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
         hideLoading();
      } else {
         event.setError(APIError.getFatalError(this));
         APIService.getErrorDialog(StepsActivity.this, event.getError(), null).show();
      }
   }

   /////////////////////////////////////////////////////
   // HELPERS
   /////////////////////////////////////////////////////

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
