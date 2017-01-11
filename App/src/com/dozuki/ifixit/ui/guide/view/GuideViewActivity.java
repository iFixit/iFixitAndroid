package com.dozuki.ifixit.ui.guide.view;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Comment;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.user.LoginEvent;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.ui.BaseMenuDrawerActivity;
import com.dozuki.ifixit.ui.guide.CommentsActivity;
import com.dozuki.ifixit.ui.guide.create.GuideIntroActivity;
import com.dozuki.ifixit.ui.guide.create.StepEditActivity;
import com.dozuki.ifixit.ui.guide.create.StepsActivity;
import com.dozuki.ifixit.util.CheatSheet;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiDatabase;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.squareup.otto.Subscribe;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;

public class GuideViewActivity extends BaseMenuDrawerActivity implements
 ViewPager.OnPageChangeListener {

   private static final int DEFAULT_INBOUND_STEPID = -1;

   private static final String TAG = "GuideViewActivity";
   private static final String FAVORITING = "FAVORITING";
   private static final String IS_OFFLINE_GUIDE = "IS_OFFLINE_GUIDE";
   public static final String CURRENT_PAGE = "CURRENT_PAGE";
   public static final String SAVED_GUIDE = "SAVED_GUIDE";
   public static final String GUIDEID = "GUIDEID";
   public static final String TOPIC_NAME_KEY = "TOPIC_NAME_KEY";
   public static final String FROM_EDIT = "FROM_EDIT_KEY";
   public static final String INBOUND_STEP_ID = "INBOUND_STEP_ID";
   public static final String COMMENTS_TAG = "COMMENTS_TAG";
   private static final int COMMENT_REQUEST = 0;

   private int mGuideid;
   private Guide mGuide;
   private int mCurrentPage = -1;
   private int mStepOffset = 1;
   private ViewPager mPager;
   private TitlePageIndicator mIndicator;
   private int mInboundStepId = DEFAULT_INBOUND_STEPID;
   private GuideViewAdapter mAdapter;
   private boolean mFavoriting = false;
   private boolean mIsOfflineGuide;
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
      mIndicator = (TitlePageIndicator) findViewById(R.id.guide_step_title_indicator);

      if (savedInstanceState != null) {
         mGuideid = savedInstanceState.getInt(GUIDEID);
         mFavoriting = savedInstanceState.getBoolean(FAVORITING);
         mIsOfflineGuide = savedInstanceState.getBoolean(IS_OFFLINE_GUIDE);

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
         fetchGuideFromApi(mGuideid);
      }
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
      fetchGuideFromApi(mGuideid);
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      super.onSaveInstanceState(state);

      state.putInt(GUIDEID, mGuideid);
      state.putSerializable(SAVED_GUIDE, mGuide);
      state.putInt(CURRENT_PAGE, mCurrentPage);
      state.putBoolean(FAVORITING, mFavoriting);
      state.putBoolean(IS_OFFLINE_GUIDE, mIsOfflineGuide);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.guide_view_menu, menu);

      MenuItem item = menu.findItem(R.id.comments);
      View commentsButtonView = MenuItemCompat.getActionView(item);

      if (commentsButtonView != null) {
         commentsButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (mGuide != null) {
                  ArrayList<Comment> comments;
                  int stepIndex = getStepIndex(), contextid;
                  String title, context;

                  // If we're in one of the introduction pages, show guide comments.
                  if (GuideViewActivity.this.notOnStep(stepIndex)) {
                     comments = mGuide.getComments();
                     title = getString(R.string.guide_comments);
                     context = "guide";
                     contextid = mGuide.getGuideid();
                  } else {
                     comments = mGuide.getStep(stepIndex).getComments();
                     contextid = mGuide.getStep(stepIndex).getStepid();
                     context = "step";
                     title = getString(R.string.step_number_comments, stepIndex + 1);
                  }

                  startActivityForResult(CommentsActivity.viewGuideComments(getApplicationContext(), comments, title,
                   context, contextid, mGuide.getGuideid()), COMMENT_REQUEST);
               }
            }
         });

         CheatSheet.setup(commentsButtonView, R.string.view_comments);
      }

      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (requestCode == COMMENT_REQUEST) {
         Bundle extras = data.getExtras();
         if (resultCode == RESULT_OK && extras != null) {
            ArrayList<Comment> comments = (ArrayList<Comment>)extras.getSerializable(COMMENTS_TAG);
            int stepIndex = getStepIndex();

            if (notOnStep(stepIndex)) {
               mGuide.setComments(comments);
            } else {
               mGuide.getStep(stepIndex).setComments(comments);
            }

            updateCommentCounts();
         }
      } else {
         super.onActivityResult(requestCode, resultCode, data);
      }
   }

   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      MenuItem comments = menu.findItem(R.id.comments);
      MenuItem favoriteGuide = menu.findItem(R.id.favorite_guide);
      MenuItem reloadGuide = menu.findItem(R.id.reload_guide);
      MenuItem editGuide = menu.findItem(R.id.edit_guide);

      View commentsView = MenuItemCompat.getActionView(comments);
      if (commentsView != null) {
         TextView countView = ((TextView)commentsView.findViewById(R.id.comment_count));

         if (mGuide != null) {
            int stepIndex = getStepIndex();

            int commentCount = 0;
            if (notOnStep(stepIndex)) {
               commentCount = mGuide.getCommentCount();
            } else if (mGuide.getNumSteps() < stepIndex) {
               commentCount = mGuide.getStep(stepIndex).getCommentCount();
            }

            if (countView != null) {
               countView.setText(commentCount + "");
            }
         }
      }

      boolean favorited = mGuide != null && mGuide.isFavorited();
      favoriteGuide.setIcon(favorited ? R.drawable.ic_action_favorite_filled :
       R.drawable.ic_action_favorite_empty);
      favoriteGuide.setEnabled(!mFavoriting && mGuide != null);
      favoriteGuide.setTitle(favorited ? R.string.unfavorite_guide : R.string.favorite_guide);

      reloadGuide.setEnabled(mGuide != null);
      editGuide.setEnabled(mGuide != null);

      if (mIsOfflineGuide) {
         reloadGuide.setVisible(false);
         editGuide.setVisible(false);
         favoriteGuide.setVisible(false);
      }

      return super.onPrepareOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.edit_guide:
            if (mGuide != null) {
               App.sendEvent("menu_action", "button_press", "edit_guide", (long)mGuide.getGuideid());

               // If the user is on the introduction, take them to edit the introduction fields.
               if (mCurrentPage < mAdapter.getStepOffset() ||
                (mCurrentPage - mAdapter.getStepOffset()) >= mGuide.getNumSteps()) {
                  Intent intent = new Intent(this, GuideIntroActivity.class);
                  intent.putExtra(StepsActivity.GUIDE_KEY, mGuide);
                  intent.putExtra(GuideIntroActivity.STATE_KEY, true);
                  startActivity(intent);
               } else {
                  Intent intent = new Intent(this, StepEditActivity.class);
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
            fetchGuideFromApi(mGuideid);
            return true;
         case R.id.comments:
            ArrayList<Comment> comments;
            int stepIndex = getStepIndex(), contextid;
            String title, context;

            // If we're in one of the introduction pages, show guide comments.
            if (notOnStep(stepIndex)) {
               comments = mGuide.getComments();
               title = getString(R.string.guide_comments);
               context = "guide";
               contextid = mGuide.getGuideid();
            } else {
               comments = mGuide.getStep(stepIndex).getComments();
               title = getString(R.string.step_number_comments, stepIndex + 1);
               context = "step";
               contextid = mGuide.getStep(stepIndex).getStepid();
            }

            startActivity(CommentsActivity.viewComments(this, comments, title, context,
             contextid));

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

   private int getStepIndex() {
      return (mCurrentPage - mAdapter.getStepOffset());
   }

   /////////////////////////////////////////////////////
   // NOTIFICATION LISTENERS
   /////////////////////////////////////////////////////

   @Subscribe
   public void onGuide(ApiEvent.ViewGuide event) {
      if (App.get().isUserLoggedIn() &&
       (event.mStoredResponse || event.hasError())) {
         // Attempt to use an offline guide if it isn't a live response.
         fetchOfflineGuide(mGuideid, event);
      } else {
         displayApiEvent(event);
      }
   }

   private void displayApiEvent(ApiEvent.ViewGuide event) {
      if (!event.hasError()) {
         if (mGuide == null) {
            Guide guide = event.getResult();
            mCurrentPage = calculateInitialPage(guide);
            setGuide(guide, mCurrentPage);
         }
      } else {
         Api.getErrorDialog(this, event).show();
      }
   }

   private int calculateInitialPage(Guide guide) {
      if (mInboundStepId != DEFAULT_INBOUND_STEPID) {
         for (int i = 0; i < guide.getSteps().size(); i++) {
            if (mInboundStepId == guide.getStep(i).getStepid()) {
               int stepOffset = 1;
               if (guide.getNumTools() != 0) stepOffset++;
               if (guide.getNumParts() != 0) stepOffset++;

               // Account for the introduction, parts and tools pages
               return i + stepOffset;
            }
         }
      }

      // Default to the first page.
      return 0;
   }

   @Subscribe
   public void onFavorite(ApiEvent.FavoriteGuide event) {
      mFavoriting = false;
      if (!event.hasError()) {
         boolean favorited = event.getResult();

         App.sendEvent("ui_action", "button_press",
          "guide_view_" + (favorited ? "" : "un") + "favorited", null);

         if (mGuide != null) {
            mGuide.setFavorited(favorited);
         }

         toast(favorited ? R.string.favorited : R.string.unfavorited,
          Toast.LENGTH_SHORT);

         // Force a sync to make it show up in the offline guides list immediately.
         App.get().requestSync(/* force */ true);
      } else {
         Api.getErrorDialog(this, event).show();
      }

      supportInvalidateOptionsMenu();
   }

   public void onLogin(LoginEvent.Login event) {
      super.onLogin(event);

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

      App.sendScreenView("/guide/view/" + mGuide.getGuideid());

      String guideTitle = mGuide.getTitle();
      setTitle(guideTitle);

      mAdapter = new GuideViewAdapter(getSupportFragmentManager(), mGuide,
       mIsOfflineGuide);

      mPager.setAdapter(mAdapter);
      mPager.setVisibility(View.VISIBLE);
      mPager.setCurrentItem(currentPage);

      mIndicator.setViewPager(mPager);

      // listen for page changes so we can track the current index
      mIndicator.setOnPageChangeListener(this);
      mIndicator.setCurrentItem(currentPage);

      // Enable menu items and update comment count.
      supportInvalidateOptionsMenu();
   }

   private void fetchGuideFromApi(int guideid) {
      showLoading(R.id.loading_container);
      Api.call(this, ApiCall.guide(guideid));
   }

   /**
    * Retrieves the guide from the database and displays the result. The user must
    * be logged in because offline guides are stored per user. The provided ApiEvent
    * is used for display if the guide is not found. This will result in either a
    * cached version of the API response or a guide not found dialog.
    */
   private void fetchOfflineGuide(final int guideid, final ApiEvent.ViewGuide event) {
      final App app = App.get();
      final User user = app.getUser();

      // Can't get offline guide if the user isn't logged in.
      if (user == null) {
         throw new IllegalStateException("Can't fetch offline guide for logged out user.");
      }

      new AsyncTask<String, Void, Guide>() {
         @Override
         protected Guide doInBackground(String... params) {
            return ApiDatabase.get(app).getOfflineGuide(app.getSite(), user, guideid);
         }

         @Override
         protected void onPostExecute(Guide guide) {
            if (guide != null) {
               App.sendEvent("ui_action", "button_press", "offline_guide_view", null);
               mIsOfflineGuide = true;
               mCurrentPage = calculateInitialPage(guide);
               setGuide(guide, mCurrentPage);
            } else {
               App.sendEvent("ui_action", "button_press", "offline_guide_not_found", null);
               displayApiEvent(event);
            }
         }
      }.execute();
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

   public void onPageScrollStateChanged(int arg0) { }

   public void onPageScrolled(int arg0, float arg1, int arg2) { }

   public void onPageSelected(int currentPage) {
      mCurrentPage = currentPage;

      updateCommentCounts();
      App.sendScreenView(mAdapter.getFragmentScreenLabel(currentPage));
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

   /**
    * Invalidates the menu to update the comment count.
    */
   private void updateCommentCounts() {
      supportInvalidateOptionsMenu();
   }

   private boolean notOnStep(int stepIndex) {
      return stepIndex < 0 || stepIndex >= mGuide.getNumSteps();
   }
}
