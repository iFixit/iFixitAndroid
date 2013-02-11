package com.dozuki.ifixit.guide_create.ui;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.model.GuideCreateObject;
import com.dozuki.ifixit.guide_create.model.GuideCreateStepObject;
import com.dozuki.ifixit.guide_create.ui.GuideCreateStepReorderFragment.StepRearrangeListener;
import com.dozuki.ifixit.guide_create.ui.GuideIntroFragment.GuideCreateIntroListener;
import com.dozuki.ifixit.util.IfixitActivity;

public class GuideCreateStepsActivity extends IfixitActivity implements GuideCreateIntroListener, StepRearrangeListener {
   static final int GUIDE_EDIT_STEP_REQUEST = 0;
   private static final String SHOWING_HELP = "SHOWING_HELP";
   private static final String GUIDE_STEPS_PORTAL_FRAG = "GUIDE_STEPS_PORTAL_FRAG";
   public static String GUIDE_KEY = "GUIDE_KEY";
   public static String NEW_GUIDE_KEY = "NEW_GUIDE_KEY";
   private ActionBar mActionBar;
   private GuideCreateStepPortalFragment mStepPortalFragment;
   private ArrayList<GuideCreateStepObject> mStepList;
   private GuideCreateObject mGuide;
   private boolean mShowingHelp;

   public ArrayList<GuideCreateStepObject> getStepList() {
      return mStepList;
   }

   public void deleteStep(GuideCreateStepObject step) {
      mStepList.remove(step);
   }

   public void addStep(GuideCreateStepObject step, int index) {
      mStepList.add(index, step);
   }

   public GuideCreateObject getGuide() {
      return mGuide;
   }

   @SuppressWarnings("unchecked")
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setTheme(((MainApplication) getApplication()).getSiteTheme());
      getSupportActionBar().setTitle(((MainApplication) getApplication()).getSite().mTitle);
      mActionBar = getSupportActionBar();
      mActionBar.setTitle("");
      if (Build.VERSION.SDK_INT > 10) {
         prepareNavigationSpinner(mActionBar, CREATE_GUIDES);
         this.getSupportActionBar().setSelectedNavigationItem(CREATE_GUIDES);
      }
      Bundle extras = getIntent().getExtras();
      if (extras != null) {
         mGuide = (GuideCreateObject) extras.getSerializable(GuideCreateStepsActivity.GUIDE_KEY);
         if (mGuide.getSteps() == null)
            mGuide.setStepList(new ArrayList<GuideCreateStepObject>());
         mStepList = mGuide.getSteps();
      }
      if (savedInstanceState != null) {
         mStepList = mGuide.getSteps();
         mShowingHelp = savedInstanceState.getBoolean(SHOWING_HELP);
         if (mShowingHelp)
            createHelpDialog().show();
      }

      setContentView(R.layout.guide_create_steps_root);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      String tag = GUIDE_STEPS_PORTAL_FRAG;
      if (findViewById(R.id.guide_create_fragment_steps_container) != null
         && getSupportFragmentManager().findFragmentByTag(tag) == null) {
         mStepPortalFragment = new GuideCreateStepPortalFragment(mGuide);
         mStepPortalFragment.setRetainInstance(true);
         getSupportFragmentManager().beginTransaction()
            .add(R.id.guide_create_fragment_steps_container, mStepPortalFragment, tag).commit();
      }
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
            GuideCreateObject guide = (GuideCreateObject) data.getSerializableExtra(GuideCreateActivity.GUIDE_KEY);
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
      // TODO PATCH GUIDE
      // APIService.call((Activity) getActivity(),
      // APIService.getCreateGuideAPICall(device, title, summary, intro, guideType, thing));
      getSupportFragmentManager().popBackStack();
   }

   public void onResume() {
      super.onResume();

      if (Build.VERSION.SDK_INT > 10) {
         this.getSupportActionBar().setSelectedNavigationItem(CREATE_GUIDES);
      }
   }

   @Override
   public void onReorderComplete() {
      ((StepRearrangeListener) getSupportFragmentManager().findFragmentByTag(GUIDE_STEPS_PORTAL_FRAG))
         .onReorderComplete();
   }

   void launchStepEdit(ArrayList<GuideCreateStepObject> stepList, int curStep) {
      Intent intent = new Intent(this, GuideCreateStepsEditActivity.class);
      intent.putExtra(GuideCreateActivity.GUIDE_KEY, mGuide);
      intent.putExtra(GuideCreateStepsEditActivity.GUIDE_STEP_LIST_KEY, stepList);
      intent.putExtra(GuideCreateStepsEditActivity.GUIDE_STEP_KEY, curStep);
      startActivityForResult(intent, GuideCreateStepsActivity.GUIDE_EDIT_STEP_REQUEST);
   }

   void launchStepEdit(GuideCreateStepObject curStep) {
      ArrayList<GuideCreateStepObject> stepList = new ArrayList<GuideCreateStepObject>();
      stepList.addAll(mGuide.getSteps());
      stepList.add(curStep);
      launchStepEdit(stepList, stepList.size() - 1);
   }

   void launchStepEdit(int curStep) {
      ArrayList<GuideCreateStepObject> stepList = new ArrayList<GuideCreateStepObject>();
      stepList.addAll(mGuide.getSteps());
      launchStepEdit(stepList, curStep);
   }
}
