package com.dozuki.ifixit.ui.guide_create;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.ui.IfixitActivity;
import com.dozuki.ifixit.ui.guide_create.GuideCreateStepReorderFragment.StepRearrangeListener;
import com.dozuki.ifixit.ui.guide_view.LoadingFragment;
import com.dozuki.ifixit.util.APIError;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.squareup.otto.Subscribe;
import org.holoeverywhere.app.AlertDialog;

import java.util.ArrayList;

public class GuideCreateStepsActivity extends IfixitActivity implements GuideIntroFragment.GuideCreateIntroListener, StepRearrangeListener {
   static final int GUIDE_EDIT_STEP_REQUEST = 0;
   private static final String SHOWING_HELP = "SHOWING_HELP";
   private static final String GUIDE_STEPS_PORTAL_FRAG = "GUIDE_STEPS_PORTAL_FRAG";
   public static String GUIDE_KEY = "GUIDE_KEY";
   private static final String LOADING = "LOADING";
   private ActionBar mActionBar;
   private GuideCreateStepPortalFragment mStepPortalFragment;
   private ArrayList<GuideStep> mStepList;
   private Guide mGuide;
   private boolean mShowingHelp;
   private boolean mIsLoading;

   public ArrayList<GuideStep> getStepList() {
      return mStepList;
   }

   public void deleteStep(GuideStep step) {
      mStepList.remove(step);
   }

   public void addStep(GuideStep step, int index) {
      mStepList.add(index, step);
   }

   public Guide getGuide() {
      return mGuide;
   }

   @Subscribe
   public void onRetrievedGuide(APIEvent.GuideForEdit event) {
      if (!event.hasError()) {
         mGuide = event.getResult();
         mStepList = mGuide.getSteps();
         if(mGuide.getSteps() != null && mGuide.getSteps().size() == 0) {

         }
         hideLoading();
      } else {
         event.setError(APIError.getFatalError(this));
         APIService.getErrorDialog(GuideCreateStepsActivity.this, event.getError(), null).show();
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
         APIService.getErrorDialog(GuideCreateStepsActivity.this, event.getError(), null).show();

      }
   }

   @SuppressWarnings("unchecked")
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setTheme(((MainApplication) getApplication()).getSiteTheme());
      getSupportActionBar().setTitle(((MainApplication) getApplication()).getSite().mTitle);
      mActionBar = getSupportActionBar();
      mActionBar.setTitle("");
      Bundle extras = getIntent().getExtras();
      int guideID = 0;
      if (extras != null) {
         guideID = extras.getInt(GuideCreateStepsActivity.GUIDE_KEY);
      }
      if (savedInstanceState != null) {
         // to persist mGuide
         mGuide = (Guide) savedInstanceState.getSerializable(GuideCreateStepsActivity.GUIDE_KEY);
         mShowingHelp = savedInstanceState.getBoolean(SHOWING_HELP);
         mIsLoading = savedInstanceState.getBoolean(LOADING);
         if (mShowingHelp) {
            createHelpDialog().show();
         }
      }

      setContentView(R.layout.guide_create_steps_root);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      String tag = GUIDE_STEPS_PORTAL_FRAG;
      if (findViewById(R.id.guide_create_fragment_steps_container) != null
         && getSupportFragmentManager().findFragmentByTag(tag) == null) {
         mStepPortalFragment = new GuideCreateStepPortalFragment();
         Bundle fragArgs = new Bundle();
         fragArgs.putInt(GUIDE_KEY, guideID);
         mStepPortalFragment.setArguments(fragArgs);
         mStepPortalFragment.setRetainInstance(true);
         getSupportFragmentManager().beginTransaction()
            .add(R.id.guide_create_fragment_steps_container, mStepPortalFragment, tag).commit();
      }

      mStepPortalFragment =
         (GuideCreateStepPortalFragment) getSupportFragmentManager().findFragmentByTag(GUIDE_STEPS_PORTAL_FRAG);

      if(mIsLoading) {
         getSupportFragmentManager().beginTransaction().hide(mStepPortalFragment).addToBackStack(null).commit();
      }
   }

   public void showLoading() {
      mStepPortalFragment =
         (GuideCreateStepPortalFragment) getSupportFragmentManager().findFragmentByTag(GUIDE_STEPS_PORTAL_FRAG);
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

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getSupportMenuInflater();
      inflater.inflate(R.menu.step_create_menu, menu);
      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home:
            finish();
            return true;
         case R.id.help_button:
            createHelpDialog().show();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      savedInstanceState.putSerializable(GuideCreateStepsActivity.GUIDE_KEY, mGuide);
      savedInstanceState.putBoolean(SHOWING_HELP, mShowingHelp);
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
      if (requestCode == GUIDE_EDIT_STEP_REQUEST) {
         if (resultCode == RESULT_OK) {
            Guide guide = (Guide) data.getSerializableExtra(GuideCreateActivity.GUIDE_KEY);
            if (guide != null) {
               mGuide = guide;
               mStepList = mGuide.getSteps();
            }
         }
      }
   }

   private AlertDialog createHelpDialog() {
      mShowingHelp = true;
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(getString(R.string.media_help_title)).setMessage(getString(R.string.guide_create_steps_help))
         .setPositiveButton(getString(R.string.media_help_confirm), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
               mShowingHelp = false;
               dialog.cancel();
            }
         });

      AlertDialog dialog = builder.create();
      dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialog) {
            mShowingHelp = false;
         }
      });

      return dialog;
   }

   @Override
   public void onFinishIntroInput(String device, String title, String summary, String intro, String guideType,
      String thing) {
      mGuide.setTitle(title);
      mGuide.setTopic(device);
      mGuide.setSummary(summary);
      mGuide.setIntroduction(intro);

      APIService.call(this, APIService.getEditGuideAPICall(mGuide.getGuideid(),
       device, title, summary, intro, guideType, thing, mGuide.getRevisionid()));
      getSupportFragmentManager().popBackStack();
      showLoading();
   }

   @Override
   public void onReorderComplete(boolean val) {
      ((StepRearrangeListener) getSupportFragmentManager().findFragmentByTag(GUIDE_STEPS_PORTAL_FRAG))
         .onReorderComplete(val);
   }
}
