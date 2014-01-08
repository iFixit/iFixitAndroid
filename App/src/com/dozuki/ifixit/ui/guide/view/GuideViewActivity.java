package com.dozuki.ifixit.ui.guide.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Comment;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.ui.BaseMenuDrawerActivity;
import com.dozuki.ifixit.ui.guide.CommentsFragment;
import com.dozuki.ifixit.ui.guide.create.GuideIntroActivity;
import com.dozuki.ifixit.ui.guide.create.StepEditActivity;
import com.dozuki.ifixit.ui.guide.create.StepsActivity;
import com.dozuki.ifixit.util.SpeechCommander;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiError;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.google.analytics.tracking.android.Tracker;
import com.squareup.otto.Subscribe;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.List;

public class GuideViewActivity extends BaseMenuDrawerActivity implements
 ViewPager.OnPageChangeListener {

   private static final int DEFAULT_INBOUND_STEPID = -1;

   private static final String NEXT_COMMAND = "next";
   private static final String PREVIOUS_COMMAND = "previous";
   private static final String HOME_COMMAND = "home";
   private static final String PACKAGE_NAME = "com.dozuki.ifixit";
   public static final String CURRENT_PAGE = "CURRENT_PAGE";
   public static final String SAVED_GUIDE = "SAVED_GUIDE";
   public static final String GUIDEID = "GUIDEID";
   public static final String DOMAIN = "DOMAIN";
   public static final String TOPIC_NAME_KEY = "TOPIC_NAME_KEY";
   public static final String FROM_EDIT = "FROM_EDIT_KEY";
   public static final String INBOUND_STEP_ID = "INBOUND_STEP_ID";
   private static final String COMMENTS_TAG = "COMMENTS_TAG";

   private int mGuideid;
   private Guide mGuide;
   private SpeechCommander mSpeechCommander;
   private int mCurrentPage = -1;
   private int mStepOffset = 1;
   private ViewPager mPager;
   private TitlePageIndicator mIndicator;
   private int mInboundStepId = DEFAULT_INBOUND_STEPID;
   private GuideViewAdapter mAdapter;
   private String mDomain;

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
      mIndicator = (TitlePageIndicator) findViewById(R.id.guide_step_title_indicator);

      if (savedInstanceState != null) {
         mGuideid = savedInstanceState.getInt(GUIDEID);
         mDomain = savedInstanceState.getString(DOMAIN);

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

   private void handleActionViewIntent(Intent intent) {
      List<String> segments = intent.getData().getPathSegments();

      try {
         mGuideid = Integer.parseInt(segments.get(2));
      } catch (Exception e) {
         hideLoading();
         Log.e("GuideViewActivity", "Problem parsing guideid out of the path segments", e);

         MainApplication.getGaTracker().send(MapBuilder.createException(
          new StandardExceptionParser(this, null).getDescription(
           Thread.currentThread().getName(), e), false).build());

         displayGuideNotFoundDialog();
         return;
      }

      Site currentSite = MainApplication.get().getSite();
      mDomain = intent.getData().getHost();
      if (currentSite.hostMatches(mDomain)) {
         // Load the guide for the current site.
         getGuide(mGuideid);
         return;
      }

      // Set site to dozuki before API call.
      MainApplication.get().setSite(Site.getSite("dozuki"));

      showLoading(R.id.loading_container);
      Api.call(this, ApiCall.sites());
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
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getSupportMenuInflater().inflate(R.menu.guide_view_menu, menu);

      MenuItem item = menu.findItem(R.id.comments);
      item.getActionView().setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if (mGuide != null) {
               ArrayList<Comment> comments;
               int stepIndex = getStepIndex(), stepid = -1, guideid = mGuide.getGuideid();
               String title;

               // If we're in one of the introduction pages, show guide comments.
               if (stepIndex < 0) {
                  comments = mGuide.getComments();
                  title = getString(R.string.guide_comments);
               } else {
                  comments = mGuide.getStep(stepIndex).getComments();
                  stepid = mGuide.getStep(stepIndex).getStepid();
                  title = getString(R.string.step_number_comments, stepIndex + 1);
               }

               CommentsFragment frag = CommentsFragment.newInstance(comments, title, guideid, stepid);
               frag.setRetainInstance(true);
               frag.show(getSupportFragmentManager(), COMMENTS_TAG);
            }
         }
      });

      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      MenuItem item = menu.findItem(R.id.comments);
      TextView countView = ((TextView) item.getActionView().findViewById(R.id.comment_count));

      if (mGuide != null) {
         int stepIndex = getStepIndex();

         int commentCount = 0;
         if (stepIndex < 0) {
            commentCount = mGuide.getCommentCount();
         } else {
            commentCount = mGuide.getStep(stepIndex).getCommentCount();
         }

         Log.d("GuideStep", "onPrepareOptionsMenu " + commentCount);

         if (countView != null) {
            countView.setText(commentCount + "");
         }
      }

      return super.onPrepareOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.edit_guide:
            if (mGuide != null) {
               MainApplication.getGaTracker().send(MapBuilder.createEvent("menu_action", "button_press",
                "edit_guide", (long) mGuide.getGuideid()).build());

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
            getGuide(mGuideid);
            return true;
         case R.id.comments:
            ArrayList<Comment> comments;
            int stepIndex = (mCurrentPage - (mStepOffset + 1));
            String title;

            // If we're in one of the introduction pages, show guide comments.
            if (stepIndex < 0) {
               comments = mGuide.getComments();
               title = getString(R.string.guide_comments);
            } else {
               comments = mGuide.getStep(stepIndex).getComments();
               title = getString(R.string.step_number_comments, stepIndex + 1);
            }

            CommentsFragment frag = CommentsFragment.newInstance(comments, title, mGuide.getGuideid(),
             mGuide.getStep(stepIndex).getStepid());
            frag.setRetainInstance(true);
            frag.show(getSupportFragmentManager(), COMMENTS_TAG);
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   private int getStepIndex() {
      return (mCurrentPage - (mStepOffset + 1));
   }

   /////////////////////////////////////////////////////
   // NOTIFICATION LISTENERS
   /////////////////////////////////////////////////////

   @Subscribe
   public void onSites(ApiEvent.Sites event) {
      if (!event.hasError()) {
         Site selectedSite = null;
         for (Site site : event.getResult()) {
            if (site.hostMatches(mDomain)) {
               selectedSite = site;
               break;
            }
         }

         if (selectedSite != null) {
            // Set the site and then fetch the guide.
            MainApplication.get().setSite(selectedSite);

            // Recreating the Activity forces it to be recreated with the appropriate
            // theme and fetch the guide from the correct site. mDomain needs to be
            // reset otherwise the guide won't be fetched (end of onCreate()).
            mDomain = null;
            recreate();
         } else {
            Exception e = new Exception();
            Log.e("GuideViewActivity", "Didn't find site!", e);

            MainApplication.getGaTracker().send(MapBuilder.createException(
             new StandardExceptionParser(this, null).getDescription(
              Thread.currentThread().getName(), e), false).build());

            displayGuideNotFoundDialog();
         }
      } else {
         Api.getErrorDialog(this, event).show();
      }
   }

   @Subscribe
   public void onGuide(ApiEvent.ViewGuide event) {
      if (!event.hasError()) {
         if (mGuide == null) {
            Guide guide = event.getResult();
            if (mInboundStepId != DEFAULT_INBOUND_STEPID) {
               for (int i = 0; i < guide.getSteps().size(); i++) {
                  if (mInboundStepId == guide.getStep(i).getStepid()) {
                     mStepOffset = 1;
                     if (guide.getNumTools() != 0) mStepOffset++;
                     if (guide.getNumParts() != 0) mStepOffset++;

                     // Account for the introduction, parts and tools pages
                     mCurrentPage = i + mStepOffset;
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

      Tracker tracker = MainApplication.getGaTracker();

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

      // Update the comment count
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

      // Update comment count in the menu
      supportInvalidateOptionsMenu();

      String label = mAdapter.getFragmentScreenLabel(currentPage);
      Tracker tracker = MainApplication.getGaTracker();
      tracker.set(Fields.SCREEN_NAME, label);
      tracker.send(MapBuilder.createAppView().build());
   }

   private void displayGuideNotFoundDialog() {
      Api.getErrorDialog(this, new ApiEvent.ViewGuide().
       setCode(404).
       setError(ApiError.getByStatusCode(404))).show();
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
