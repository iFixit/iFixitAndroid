package com.dozuki.ifixit.guide_create.ui;

import java.util.ArrayList;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import org.holoeverywhere.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.model.GuideCreateObject;
import com.dozuki.ifixit.guide_create.ui.GuideIntroFragment.GuideCreateIntroListener;
import com.dozuki.ifixit.util.IfixitActivity;

public class GuideCreateActivity extends IfixitActivity implements GuideCreateIntroListener {
   static final int GUIDE_STEP_LIST_REQUEST = 0;
   public static int TASK_ID = -1;
   private static final String SHOWING_HELP = "SHOWING_HELP";
   private static String GUIDE_OBJECT_KEY = "GUIDE_OBJECT_KEY";
   private static String GUIDE_PORTAL_FRAGMENT_TAG = "GUIDE_PORTAL_FRAGMENT_TAG";
   private static String GUIDE_INTRO_FRAGMENT_TAG = "GUIDE_INTRO_FRAGMENT_TAG";
   public static String GUIDE_KEY = "GUIDE_KEY";
   public static int GuideItemID = 0;
   private ActionBar mActionBar;
   private GuidePortalFragment mGuidePortal;
   private ArrayList<GuideCreateObject> mGuideList;
   private boolean mShowingHelp;

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

   public ArrayList<GuideCreateObject> getGuideList() {
      return mGuideList;
   }

   public void addGuide(GuideCreateObject guide) {
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
      prepareNavigationSpinner(mActionBar);
      TASK_ID = this.getTaskId();
      this.getSupportActionBar().setSelectedNavigationItem(CREATE_GUIDES);

      mGuideList = new ArrayList<GuideCreateObject>();
      if (savedInstanceState != null) {
         mGuideList = (ArrayList<GuideCreateObject>) savedInstanceState.getSerializable(GUIDE_OBJECT_KEY);
         mShowingHelp = savedInstanceState.getBoolean(SHOWING_HELP);
         if (mShowingHelp)
            createHelpDialog().show();
      }

      setContentView(R.layout.guide_create);

      getSupportFragmentManager().addOnBackStackChangedListener(getListener());
      if (findViewById(R.id.guide_create_fragment_container) != null
         && getSupportFragmentManager().findFragmentByTag(GUIDE_PORTAL_FRAGMENT_TAG) == null) {
         mGuidePortal = new GuidePortalFragment();
         getSupportFragmentManager().beginTransaction()
            .add(R.id.guide_create_fragment_container, mGuidePortal, GUIDE_PORTAL_FRAGMENT_TAG).commit();
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
      savedInstanceState.putBoolean(SHOWING_HELP, mShowingHelp);
      super.onSaveInstanceState(savedInstanceState);
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
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      if (requestCode == GUIDE_STEP_LIST_REQUEST) {
         if (resultCode == RESULT_OK) {
            GuideCreateObject guide = (GuideCreateObject) data.getSerializableExtra(GUIDE_KEY);
            if (guide != null) {
               mGuideList.set(mGuideList.indexOf(guide), guide);
            }
         }
      }
   }

   @Override
   public void onFinishIntroInput(String device, String title, String summary, String intro, String guideType,
      String thing) {

      GuideCreateObject guideObject = new GuideCreateObject(GuideItemID++);
      guideObject.setTitle(title);
      guideObject.setTopic(device);
      guideObject.setSummary(summary);
      guideObject.setIntroduction(intro);

      getGuideList().add(guideObject);
      // APIService.call((Activity) getActivity(),
      // APIService.getCreateGuideAPICall(device, title, summary, intro, guideType, thing));

      getSupportFragmentManager().popBackStack();

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

   @Override
   public void onResume() {
      super.onResume();

      this.getSupportActionBar().setSelectedNavigationItem(1);
   }
}
