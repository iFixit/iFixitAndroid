package com.dozuki.ifixit.ui.guide.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.user.LoginEvent;
import com.dozuki.ifixit.ui.BaseMenuDrawerActivity;
import com.dozuki.ifixit.ui.guide.create.GuideIntroActivity;
import com.dozuki.ifixit.ui.guide.create.StepEditActivity;
import com.dozuki.ifixit.ui.guide.create.StepsActivity;
import com.dozuki.ifixit.util.SpeechCommander;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.squareup.otto.Subscribe;
import com.viewpagerindicator.TitlePageIndicator;

public class GuideViewActivity extends BaseMenuDrawerActivity implements
 ViewPager.OnPageChangeListener {

   private static final int DEFAULT_INBOUND_STEPID = -1;

   private static final String NEXT_COMMAND = "next";
   private static final String PREVIOUS_COMMAND = "previous";
   private static final String HOME_COMMAND = "home";
   private static final String PACKAGE_NAME = "com.dozuki.ifixit";
   private static final String FAVORITING = "FAVORITING";
   public static final String CURRENT_PAGE = "CURRENT_PAGE";
   public static final String SAVED_GUIDE = "SAVED_GUIDE";
   public static final String GUIDEID = "GUIDEID";
   public static final String DOMAIN = "DOMAIN";
   public static final String TOPIC_NAME_KEY = "TOPIC_NAME_KEY";
   public static final String FROM_EDIT = "FROM_EDIT_KEY";
   public static final String INBOUND_STEP_ID = "INBOUND_STEP_ID";

   private int mGuideid;
   private Guide mGuide;
   private SpeechCommander mSpeechCommander;
   private int mCurrentPage = -1;
   private ViewPager mPager;
   private TitlePageIndicator mIndicator;
   private int mInboundStepId = DEFAULT_INBOUND_STEPID;
   private GuideViewAdapter mAdapter;
   private boolean mFavoriting = false;
   private Toast mToast;

   /////////////////////////////////////////////////////
   // LIFECYCLE
   /////////////////////////////////////////////////////

   public static Intent viewGuideid(Context context, int guideid) {
      Intent intent = new Intent(context, GuideViewActivity.class);
      intent.putExtra(GUIDEID, guideid);
      return intent;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.guide_main);

      mPager = (ViewPager) findViewById(R.id.guide_pager);
      mPager.setSaveEnabled(false);
      mIndicator = (TitlePageIndicator) findViewById(R.id.guide_step_title_indicator);

      if (savedInstanceState != null) {
         mGuideid = savedInstanceState.getInt(GUIDEID);
         mFavoriting = savedInstanceState.getBoolean(FAVORITING);

         if (savedInstanceState.containsKey(SAVED_GUIDE)) {
            mGuide = (Guide) savedInstanceState.getSerializable(SAVED_GUIDE);
         }

         if (mGuide != null) {
            mCurrentPage = savedInstanceState.getInt(CURRENT_PAGE);

            setGuide(mGuide, mCurrentPage);
            mIndicator.setCurrentItem(mCurrentPage);
            mPager.setCurrentItem(mCurrentPage);
         }
      } else {
         extractExtras(getIntent().getExtras());
      }

      if (mGuide != null) {
         setGuide(mGuide, mCurrentPage);
      } else {
         getGuide(mGuideid);
      }

      //initSpeechRecognizer();
   }

   private void extractExtras(Bundle extras) {
      if (extras != null) {
         if (extras.containsKey(GUIDEID)) {
            mGuideid = extras.getInt(GUIDEID);
         }

         if (extras.containsKey(GuideViewActivity.SAVED_GUIDE)) {
            mGuide = (Guide) extras.getSerializable(GuideViewActivity.SAVED_GUIDE);
         }

         mInboundStepId = extras.getInt(INBOUND_STEP_ID, DEFAULT_INBOUND_STEPID);
         mCurrentPage = extras.getInt(GuideViewActivity.CURRENT_PAGE, 0);
      }
   }

   @Override
   protected void onNewIntent(Intent intent) {
      super.onNewIntent(intent);

      // Reset everything to default values since we're getting a new intent - forces the view to refresh.
      mGuide = null;
      mCurrentPage = -1;
      mInboundStepId = -1;

      extractExtras(intent.getExtras());
      getGuide(mGuideid);
   }

   @Override
   public void onDestroy() {
      super.onDestroy();

      if (mSpeechCommander != null) {
         mSpeechCommander.destroy();
      }
   }

   @Override
   public void onPause() {
      super.onPause();

      if (mSpeechCommander != null) {
         mSpeechCommander.stopListening();
         mSpeechCommander.cancel();
      }
   }

   @Override
   public void onResume() {
      super.onResume();

      if (mSpeechCommander != null) {
         mSpeechCommander.startListening();
      }
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      super.onSaveInstanceState(state);

      state.putInt(GUIDEID, mGuideid);
      state.putSerializable(SAVED_GUIDE, mGuide);
      state.putInt(CURRENT_PAGE, mCurrentPage);
      state.putBoolean(FAVORITING, mFavoriting);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getSupportMenuInflater().inflate(R.menu.guide_view_menu, menu);
      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      MenuItem favoriteGuide = menu.findItem(R.id.favorite_guide);

      boolean favorited = mGuide != null ? mGuide.isFavorited() : false;
      favoriteGuide.setIcon(favorited ? R.drawable.ic_action_favorite_filled :
       R.drawable.ic_action_favorite_empty);
      favoriteGuide.setEnabled(!mFavoriting && mGuide != null);
      favoriteGuide.setTitle(favorited ? R.string.unfavorite_guide : R.string.favorite_guide);

      return super.onPrepareOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.edit_guide:
            if (mGuide != null) {
               App.getGaTracker().send(MapBuilder.createEvent("menu_action", "button_press",
                "edit_guide", (long)mGuide.getGuideid()).build());

               Intent intent;
               // If the user is on the introduction, take them to edit the introduction fields.
               if (mCurrentPage == 0) {
                  intent = new Intent(this, GuideIntroActivity.class);
                  intent.putExtra(StepsActivity.GUIDE_KEY, mGuide);
                  intent.putExtra(GuideIntroActivity.STATE_KEY, true);
                  startActivity(intent);
               } else {
                  intent = new Intent(this, StepEditActivity.class);
                  int stepNum = 0;

                  // Take into account the introduction, parts and tools page.
                  if (mCurrentPage >= mAdapter.getStepOffset()) {
                     stepNum = mCurrentPage - mAdapter.getStepOffset();
                     // Account for array indexed starting at 1
                     intent.putExtra(StepEditActivity.GUIDE_STEP_NUM_KEY, stepNum + 1);
                  }

                  int stepGuideid = mGuide.getStep(stepNum).getGuideid();
                  // If the step is part of a prerequisite guide, store the parents
                  // guideid so that we can get back from editing this prerequisite.
                  if (stepGuideid != mGuide.getGuideid()) {
                     intent.putExtra(StepEditActivity.PARENT_GUIDE_ID_KEY, mGuide.getGuideid());
                  }
                  // We have to pass along the steps guideid to account for prerequisite guides.
                  intent.putExtra(StepEditActivity.GUIDE_ID_KEY, stepGuideid);
                  intent.putExtra(StepEditActivity.GUIDE_PUBLIC_KEY, mGuide.isPublic());
                  intent.putExtra(StepEditActivity.GUIDE_STEP_ID, mGuide.getStep(stepNum).getStepid());
                  startActivity(intent);
               }
            }
            return true;
         case R.id.reload_guide:
            // Set guide to null to force a refresh of the guide object.
            mGuide = null;
            supportInvalidateOptionsMenu();
            getGuide(mGuideid);
            return true;
         case R.id.favorite_guide:
            // Current favorite state.
            boolean favorited = mGuide == null ? false : mGuide.isFavorited();
            mFavoriting = true;

            Api.call(this, ApiCall.favoriteGuide(mGuideid, !favorited));
            supportInvalidateOptionsMenu();

            if (App.get().isUserLoggedIn()) {
               // Only Toast if the user is logged in. Otherwise it happens
               // in the login success event handler.
               toast(favorited ? R.string.unfavoriting :
                R.string.favoriting, Toast.LENGTH_LONG);
            }
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   /////////////////////////////////////////////////////
   // NOTIFICATION LISTENERS
   /////////////////////////////////////////////////////

   @Subscribe
   public void onGuide(ApiEvent.ViewGuide event) {
      if (!event.hasError()) {
         if (mGuide == null) {
            Guide guide = event.getResult();
            if (mInboundStepId != DEFAULT_INBOUND_STEPID) {
               for (int i = 0; i < guide.getSteps().size(); i++) {
                  if (mInboundStepId == guide.getStep(i).getStepid()) {
                     int stepOffset = 1;
                     if (guide.getNumTools() != 0) stepOffset++;
                     if (guide.getNumParts() != 0) stepOffset++;

                     // Account for the introduction, parts and tools pages
                     mCurrentPage = i + stepOffset;
                     break;
                  }
               }
            }
            setGuide(guide, mCurrentPage);
         }
      } else {
         Api.getErrorDialog(this, event).show();
      }
   }

   @Subscribe
   public void onFavorite(ApiEvent.FavoriteGuide event) {
      mFavoriting = false;
      if (!event.hasError()) {
         boolean favorited = event.getResult();

         if (mGuide != null) {
            mGuide.setFavorited(favorited);
         }

         toast(favorited ? R.string.favorited : R.string.unfavorited,
          Toast.LENGTH_SHORT);
      } else {
         Api.getErrorDialog(this, event).show();
      }

      supportInvalidateOptionsMenu();
   }

   public void onLogin(LoginEvent.Login event) {
      if (mFavoriting) {
         toast(mGuide.isFavorited() ? R.string.unfavoriting :
          R.string.favoriting, Toast.LENGTH_LONG);
      }
   }

   public void onCancelLogin(LoginEvent.Cancel event) {
      // Always reset this because there is no way that the user can be
      // favoriting a guide right now.
      mFavoriting = false;
      supportInvalidateOptionsMenu();
   }

   /////////////////////////////////////////////////////
   // HELPERS
   /////////////////////////////////////////////////////

   private void setGuide(Guide guide, int currentPage) {
      hideLoading();

      if (guide == null) {
         Log.wtf("GuideViewActivity", "Guide is not set.  This should be impossible");
         return;
      }

      mGuide = guide;

      Tracker tracker = App.getGaTracker();

      tracker.set(Fields.SCREEN_NAME, "/guide/view/" + mGuide.getGuideid());
      tracker.send(MapBuilder.createAppView().build());

      String guideTitle = mGuide.getTitle();
      setTitle(guideTitle);

      mAdapter = new GuideViewAdapter(this.getSupportFragmentManager(), mGuide);

      mPager.setAdapter(mAdapter);
      mPager.setVisibility(View.VISIBLE);
      mPager.setCurrentItem(currentPage);

      mIndicator.setViewPager(mPager);

      // listen for page changes so we can track the current index
      mIndicator.setOnPageChangeListener(this);
      mIndicator.setCurrentItem(currentPage);

      supportInvalidateOptionsMenu();
   }

   public void getGuide(int guideid) {
      showLoading(R.id.loading_container);
      Api.call(this, ApiCall.guide(guideid));
   }

   private void nextStep() {
      mIndicator.setCurrentItem(mCurrentPage + 1);
   }

   private void previousStep() {
      mIndicator.setCurrentItem(mCurrentPage - 1);
   }

   private void guideHome() {
      mIndicator.setCurrentItem(0);
   }

   /**
    * Displays a toast with the given values and clears any existing Toasts
    * if they exist.
    */
   private void toast(int string, int duration) {
      if (mToast == null) {
         mToast = Toast.makeText(this, string, duration);
      }

      mToast.setText(string);
      mToast.setDuration(duration);

      mToast.show();
   }

   @SuppressWarnings("unused")
   private void initSpeechRecognizer() {
      if (!SpeechRecognizer.isRecognitionAvailable(getBaseContext())) {
         return;
      }

      mSpeechCommander = new SpeechCommander(this, PACKAGE_NAME);

      mSpeechCommander.addCommand(NEXT_COMMAND, new SpeechCommander.Command() {
         public void performCommand() {
            nextStep();
         }
      });

      mSpeechCommander.addCommand(PREVIOUS_COMMAND,
       new SpeechCommander.Command() {
          public void performCommand() {
             previousStep();
          }
       });

      mSpeechCommander.addCommand(HOME_COMMAND, new SpeechCommander.Command() {
         public void performCommand() {
            guideHome();
         }
      });

      mSpeechCommander.startListening();
   }

   public void onPageScrollStateChanged(int arg0) { }

   public void onPageScrolled(int arg0, float arg1, int arg2) { }

   public void onPageSelected(int currentPage) {
      mCurrentPage = currentPage;

      String label = mAdapter.getFragmentScreenLabel(currentPage);
      Tracker tracker = App.getGaTracker();
      tracker.set(Fields.SCREEN_NAME, label);
      tracker.send(MapBuilder.createAppView().build());
   }

   @Override
   public void showLoading(int id) {
      View container = findViewById(id);
      if (container != null) {
         container.setVisibility(View.VISIBLE);
      }

      super.showLoading(id);
   }

   @Override
   public void hideLoading() {
      super.hideLoading();

      View container = findViewById(R.id.loading_container);
      if (container != null) {
         container.setVisibility(View.GONE);
      }
   }
}
