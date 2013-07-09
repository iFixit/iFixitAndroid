package com.dozuki.ifixit.ui.guide.view;

import android.content.Intent;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.ui.BaseActivity;
import com.dozuki.ifixit.ui.guide.create.StepEditActivity;
import com.dozuki.ifixit.ui.topic_view.TopicGuideListFragment;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.util.SpeechCommander;
import com.squareup.otto.Subscribe;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.List;

public class GuideViewActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

   private static final String NEXT_COMMAND = "next";
   private static final String PREVIOUS_COMMAND = "previous";
   private static final String HOME_COMMAND = "home";
   private static final String PACKAGE_NAME = "com.dozuki.ifixit";
   public static final String CURRENT_PAGE = "CURRENT_PAGE";
   public static final String SAVED_GUIDE = "SAVED_GUIDE";
   public static final String SAVED_GUIDEID = "SAVED_GUIDEID";
   public static final String TOPIC_NAME_KEY = "TOPIC_NAME_KEY";
   public static final String FROM_EDIT = "FROM_EDIT_KEY";
   public static final String INBOUND_STEP_ID = "INBOUND_STEP_ID";

   public static final int MENU_EDIT_GUIDE = 2;

   private int mGuideid;
   private Guide mGuide;
   private SpeechCommander mSpeechCommander;
   private int mCurrentPage = -1;
   private ViewPager mPager;
   private TitlePageIndicator mIndicator;
   private int mInboundStepId = -1;
   private GuideViewAdapter mAdapter;

   /////////////////////////////////////////////////////
   // LIFECYCLE
   /////////////////////////////////////////////////////

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.guide_main);

      showLoading(R.id.loading_container);
      mPager = (ViewPager) findViewById(R.id.guide_pager);
      mIndicator = (TitlePageIndicator) findViewById(R.id.guide_step_title_indicator);

      if (savedInstanceState != null) {
         mGuideid = savedInstanceState.getInt(SAVED_GUIDEID);
         mGuide = (Guide) savedInstanceState.getSerializable(SAVED_GUIDE);

         if (mGuide != null) {
            mCurrentPage = savedInstanceState.getInt(CURRENT_PAGE);

            setGuide(mGuide, mCurrentPage);
            mIndicator.setCurrentItem(mCurrentPage);
            mPager.setCurrentItem(mCurrentPage);
         } else {
            getGuide(mGuideid);
         }
      } else {
         Intent intent = getIntent();

         mGuideid = -1;

         // Handle when the activity is started from an external app.  (like a link)
         if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            List<String> segments = intent.getData().getPathSegments();

            try {
               mGuideid = Integer.parseInt(segments.get(2));
            } catch (Exception e) {
               hideLoading();
               Log.e("GuideViewActivity", "Problem parsing guideid out of the path segments");
               return;
            }
         } else {
            extractExtras(intent.getExtras());
         }
      }

      if (mGuide == null) {
         getGuide(mGuideid);
      } else {
         setGuide(mGuide, mCurrentPage);
      }


      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      //initSpeechRecognizer();
   }

   private void extractExtras(Bundle extras) {
      if (extras != null) {

         // We're coming from the Topics GuideList
         if (extras.containsKey(TopicGuideListFragment.GUIDEID)) {
            mGuideid = extras.getInt(TopicGuideListFragment.GUIDEID);

            // We're coming from StepEdit
         } else if (extras.containsKey(GuideViewActivity.SAVED_GUIDEID)) {
            mGuideid = extras.getInt(GuideViewActivity.SAVED_GUIDEID);
         }

         if (extras.containsKey(GuideViewActivity.SAVED_GUIDE)) {
            mGuide = (Guide) extras.getSerializable(GuideViewActivity.SAVED_GUIDE);
         }

         mInboundStepId = extras.getInt(INBOUND_STEP_ID);
         mCurrentPage = extras.getInt(GuideViewActivity.CURRENT_PAGE, 0);

      }
   }

   @Override
   protected void onNewIntent(Intent intent) {
      super.onNewIntent(intent);

      Log.d("GuideViewActivity", "onNewIntent");

      extractExtras(intent.getExtras());

      if (mGuide == null) {
         getGuide(mGuideid);
      } else {
         setGuide(mGuide, mCurrentPage);
      }
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
      /**
       * TODO Figure out why we don't super.onSaveInstanceState(). I think
       * this causes step fragments to not maintain state across orientation
       * changes (selected thumbnail). However, I remember this failing with a
       * call to super.onSavInstanceState(). Investigate.
       */
      state.putSerializable(SAVED_GUIDEID, mGuideid);
      state.putSerializable(SAVED_GUIDE, mGuide);
      state.putInt(CURRENT_PAGE, mCurrentPage);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      menu.add(1, MENU_EDIT_GUIDE, 0, R.string.edit_guide)
       .setIcon(R.drawable.ic_action_edit)
       .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case MENU_EDIT_GUIDE:
            if (mGuide != null) {
               Intent intent = new Intent(this, StepEditActivity.class);
               int stepNum = 0;

               // Take into account the introduction, parts and tools page.
               if (mCurrentPage >= mAdapter.getStepOffset()) {
                  stepNum = mCurrentPage - mAdapter.getStepOffset();
                  Log.d("GuideViewActivity", stepNum+"");
                  // Account for array indexed starting at 1
                  intent.putExtra(StepEditActivity.GUIDE_STEP_NUM_KEY, stepNum + 1);
               }

               int stepGuideid = mGuide.getStep(stepNum).getGuideid();
               // If the step is part of a prerequisite guide, store the parents guideid so that we can get back from
               // editing this prerequisite.
               if (stepGuideid != mGuide.getGuideid()) {
                  intent.putExtra(StepEditActivity.PARENT_GUIDE_ID_KEY, mGuide.getGuideid());
               }
               // We have to pass along the steps guideid to account for prerequisite guides.
               intent.putExtra(StepEditActivity.GUIDE_ID_KEY, stepGuideid);
               intent.putExtra(StepEditActivity.GUIDE_PUBLIC_KEY, mGuide.isPublic());
               intent.putExtra(StepEditActivity.GUIDE_STEP_ID, mGuide.getStep(stepNum).getStepid());
               startActivity(intent);
            }
            break;
         default:
            return (super.onOptionsItemSelected(item));
      }
      return true;
   }

   /////////////////////////////////////////////////////
   // NOTIFICATION LISTENERS
   /////////////////////////////////////////////////////

   @Subscribe
   public void onGuide(APIEvent.ViewGuide event) {
      if (!event.hasError()) {
         if (mGuide == null) {
            Guide guide = event.getResult();
            if (mInboundStepId != -1) {
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
            setGuide(event.getResult(), mCurrentPage);
         }
      } else {
         APIService.getErrorDialog(GuideViewActivity.this, event.getError(),
          APIService.getGuideAPICall(mGuideid)).show();
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

      getSupportActionBar().setTitle(mGuide.getTitle());

      mAdapter = new GuideViewAdapter(this.getSupportFragmentManager(), mGuide);

      mPager.setAdapter(mAdapter);
      mPager.setVisibility(View.VISIBLE);
      mPager.setCurrentItem(currentPage);

      mIndicator.setViewPager(mPager);

      // listen for page changes so we can track the current index
      mIndicator.setOnPageChangeListener(this);
      mIndicator.setCurrentItem(currentPage);
   }

   public void getGuide(int guideid) {
      APIService.call(this, APIService.getGuideAPICall(guideid));
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

   public void onPageScrolled(int arg0, float arg1, int arg2) {
   }

   public void onPageSelected(int currentPage) {
      Log.d("GuideViewActivity", currentPage+"");

      if (mCurrentPage == currentPage) return;

      mCurrentPage = currentPage;
   }
}
