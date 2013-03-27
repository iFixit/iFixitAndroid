package com.dozuki.ifixit.ui.guide_create;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.model.guide.StepLine;
import com.dozuki.ifixit.model.guide.UserGuide;
import com.dozuki.ifixit.ui.IfixitActivity;
import com.dozuki.ifixit.ui.guide_view.LoadingFragment;
import com.dozuki.ifixit.util.APIError;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.squareup.otto.Subscribe;
import org.holoeverywhere.app.Fragment;

import java.util.ArrayList;

public class GuideCreateActivity extends IfixitActivity implements GuideIntroFragment.GuideCreateIntroListener {
   static final int GUIDE_STEP_LIST_REQUEST = 0;
   static int GUIDE_STEP_EDIT_REQUEST = 1;
   public static int TASK_ID = -1;
   private static final String SHOWING_HELP = "SHOWING_HELP";
   private static final String SHOWING_DELETE = "SHOWING_DELETE";
   private static final String GUIDE_FOR_DELETE = "GUIDE_FOR_DELETE";
   private static String GUIDE_OBJECT_KEY = "GUIDE_OBJECT_KEY";
   private static String GUIDE_PORTAL_FRAGMENT_TAG = "GUIDE_PORTAL_FRAGMENT_TAG";
   private static String GUIDE_INTRO_FRAGMENT_TAG = "GUIDE_INTRO_FRAGMENT_TAG";
   public static String GUIDE_KEY = "GUIDE_KEY";
   private static final String LOADING = "LOADING";
   private ActionBar mActionBar;
   private GuidePortalFragment mGuidePortal;
   private ArrayList<UserGuide> mGuideList = new ArrayList<UserGuide>();
   private boolean mShowingHelp;
   private UserGuide mGuideForDelete;
   private boolean mShowingDelete;
   private boolean mIsLoading;

   private OnBackStackChangedListener getListener() {
      OnBackStackChangedListener result = new OnBackStackChangedListener() {
         public void onBackStackChanged() {
            FragmentManager manager = getSupportFragmentManager();

            if (manager != null) {
               Fragment currFrag = (Fragment) manager.findFragmentById(R.id.guide_create_fragment_container);

               currFrag.onResume();
            }
         }
      };

      return result;
   }

   public ArrayList<UserGuide> getGuideList() {
      return mGuideList;
   }

   public void addGuide(UserGuide guide) {
      mGuideList.add(guide);
   }

   @SuppressWarnings("unchecked")
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setTheme(((MainApplication) getApplication()).getSiteTheme());
      getSupportActionBar().setTitle(((MainApplication) getApplication()).getSite().mTitle);
      mActionBar = getSupportActionBar();
      mActionBar.setTitle("");
      prepareNavigationSpinner(mActionBar, CREATE_GUIDES);
      TASK_ID = this.getTaskId();
      if (savedInstanceState != null) {
         mGuideList = (ArrayList<UserGuide>) savedInstanceState.getSerializable(GUIDE_OBJECT_KEY);
         mShowingHelp = savedInstanceState.getBoolean(SHOWING_HELP);
         mShowingDelete = savedInstanceState.getBoolean(SHOWING_DELETE);
         mGuideForDelete = (UserGuide) savedInstanceState.getSerializable(GUIDE_FOR_DELETE);
         if (mShowingHelp) {
            createHelpDialog().show();
         }
         mIsLoading = savedInstanceState.getBoolean(LOADING);
      }

      setContentView(R.layout.guide_create);

      getSupportFragmentManager().addOnBackStackChangedListener(getListener());
      if (findViewById(R.id.guide_create_fragment_container) != null
         && getSupportFragmentManager().findFragmentByTag(GUIDE_PORTAL_FRAGMENT_TAG) == null) {
         mGuidePortal = new GuidePortalFragment();
         getSupportFragmentManager().beginTransaction()
            .add(R.id.guide_create_fragment_container, mGuidePortal, GUIDE_PORTAL_FRAGMENT_TAG).commit();
      }
      else
      {
         mGuidePortal = (GuidePortalFragment)getSupportFragmentManager().findFragmentByTag(GUIDE_PORTAL_FRAGMENT_TAG);
      }
      
      if (mShowingDelete && mGuideForDelete != null)    {
         createDeleteDialog(mGuideForDelete).show();
      }

      getSupportActionBar().setDisplayHomeAsUpEnabled(false);

   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {

      MenuInflater inflater = getSupportMenuInflater();
      inflater.inflate(R.menu.guide_create_menu, menu);

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
      savedInstanceState.putSerializable(GUIDE_OBJECT_KEY, mGuideList);
      savedInstanceState.putSerializable(GUIDE_FOR_DELETE, mGuideForDelete);
      savedInstanceState.putBoolean(SHOWING_HELP, mShowingHelp);
      savedInstanceState.putBoolean(SHOWING_DELETE, mShowingDelete);
      savedInstanceState.putBoolean(LOADING, mIsLoading);
      super.onSaveInstanceState(savedInstanceState);
   }

   @Subscribe
   public void onGuideCreated(APIEvent.CreateGuide event) {
      if (!event.hasError()) {
         UserGuide userGuide = new UserGuide();
         Guide guideObject = event.getResult();
         mGuidePortal.toggleNoGuidesText(false);
         userGuide.setGuideid(guideObject.getGuideid());
         userGuide.setImage(guideObject.getIntroImage());
         userGuide.setTitle(guideObject.getTitle());
         userGuide.setPublished(guideObject.getPublic());
         userGuide.setRevisionid(guideObject.getRevisionid());
         mGuideList.add(userGuide);
         launchStepEditOnNewGuide(guideObject);
      } else {
         event.setError(APIError.getFatalError(this));
         APIService.getErrorDialog(this, event.getError(), null).show();
      }
   }


   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      if (requestCode == GUIDE_STEP_LIST_REQUEST || requestCode == GUIDE_STEP_EDIT_REQUEST) {
         if (resultCode == RESULT_OK) {
            Guide guide = (Guide) data.getSerializableExtra(GUIDE_KEY);
            if(guide == null) {
               return;
            }
            for (UserGuide g : mGuideList) {
               if (g.getGuideid() == guide.getGuideid()) {
                  g.setRevisionid(guide.getRevisionid());
                  g.setTitle(guide.getTitle());
                  break;
               }
            }
         }
      }
   }

   @Subscribe
   public void onPublishStatus(APIEvent.PublishStatus event) {
      if (!event.hasError()) {
         Guide guide = event.getResult();
         for (UserGuide g : mGuideList) {
            if (g.getGuideid() == guide.getGuideid()) {
               g.setRevisionid(guide.getRevisionid());
               break;
            }
         }
         hideLoading();
      } else {
         event.setError(APIError.getFatalError(this));
         APIService.getErrorDialog(this, event.getError(), null).show();
      }
   }

   @Subscribe
   public void onDeleteGuide(APIEvent.DeleteGuide event) {
      if (!event.hasError()) {
         getGuideList().remove(mGuideForDelete);
         if (getGuideList().isEmpty())  {
            mGuidePortal.toggleNoGuidesText(true);
         }
         mGuideForDelete = null;
         hideLoading();
         mGuidePortal.invalidateViews();
      } else {
         event.setError(APIError.getFatalError(this));
         APIService.getErrorDialog(this, event.getError(), null).show();
      }
   }

   public void showLoading() {
      getSupportFragmentManager().beginTransaction()
         .add(R.id.guide_create_fragment_container, new LoadingFragment(), "loading").addToBackStack("loading")
         .commit();

      if (mGuidePortal != null) {
         getSupportFragmentManager().beginTransaction().hide(mGuidePortal).addToBackStack(null).commit();
      }
      mIsLoading = true;
   }

   public void hideLoading() {
      getSupportFragmentManager().popBackStack("loading", FragmentManager.POP_BACK_STACK_INCLUSIVE);
      mIsLoading = false;
   }

   public void createGuide() {
      if (mGuideList == null)
         return;
      launchGuideCreateIntro();
   }

   private void launchGuideCreateIntro() {
      GuideIntroFragment newFragment = new GuideIntroFragment();
      newFragment.setGuideOBject(null);
      FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
      transaction.replace(R.id.guide_create_fragment_container, newFragment);
      transaction.addToBackStack(GUIDE_INTRO_FRAGMENT_TAG);
      transaction.commitAllowingStateLoss();
   }

   @Override
   public void onFinishIntroInput(String device, String title, String summary,
    String intro, String guideType, String subject) {
      getSupportFragmentManager().popBackStack();
      showLoading();
      UserGuide guideObject = new UserGuide();//(GuideItemID++);
      guideObject.setTitle(title);
      guideObject.setTopic(device);
      guideObject.setType(guideType);
      guideObject.setDevice(device);
      guideObject.setSummary(summary);
      guideObject.setSubject(subject);
      guideObject.setIntroduction(intro);
      APIService.call(this, APIService.getCreateGuideAPICall(guideObject));
   }

   public void launchStepEditOnNewGuide(Guide guideObject) {
      Intent intent = new Intent(this, GuideCreateStepsEditActivity.class);
      intent.putExtra(GuideCreateActivity.GUIDE_KEY,  guideObject);
      GuideStep item = new GuideStep(GuideCreateStepPortalFragment.STEP_ID++);
      item.setStepNum(0);
      item.setTitle(GuideCreateStepPortalFragment.DEFAULT_TITLE);
      item.addLine(new StepLine());
      ArrayList<GuideStep> initialStepList = new ArrayList<GuideStep>();
      initialStepList.add(item);
      intent.putExtra(GuideCreateStepsEditActivity.GUIDE_STEP_LIST_KEY, initialStepList);
      intent.putExtra(GuideCreateStepsEditActivity.GUIDE_STEP_KEY, 0);
      startActivityForResult(intent, GUIDE_STEP_EDIT_REQUEST);
   }

   @Override
   public void onResume() {
      super.onResume();
      hideLoading();
      this.getSupportActionBar().setSelectedNavigationItem(CREATE_GUIDES);
   }

   @Override
   public void onPause() {
      super.onPause();
   }


   @Override
   protected void onDestroy() {
      super.onDestroy();
      TASK_ID = -1;
   }

   private AlertDialog createHelpDialog() {
      mShowingHelp = true;
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(getString(R.string.media_help_title)).setMessage(getString(R.string.guide_create_help))
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

   public AlertDialog createDeleteDialog(UserGuide item) {
      mGuideForDelete = item;
      mShowingDelete = true;
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(getString(R.string.confirm_delete_title))
         .setMessage(getString(R.string.confirm_delete_body) + " \"" + mGuideForDelete.getTitle() + "\"?")
         .setPositiveButton(getString(R.string.confirm_delete_confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               APIService.call(GuideCreateActivity.this, APIService.getRemoveGuideAPICall(mGuideForDelete));
               showLoading();
               mShowingDelete = false;
               dialog.cancel();
            }
         }).setNegativeButton(getString(R.string.confirm_delete_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               mShowingDelete = false;
               mGuideForDelete = null;
            }
         });

      AlertDialog dialog = builder.create();
      dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialog) {
            mShowingDelete = false;
         }
      });

      return dialog;
   }

}
