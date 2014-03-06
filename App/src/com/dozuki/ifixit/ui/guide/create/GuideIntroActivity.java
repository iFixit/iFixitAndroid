package com.dozuki.ifixit.ui.guide.create;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FixedFragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.model.guide.StepLine;
import com.dozuki.ifixit.model.guide.wizard.AbstractWizardModel;
import com.dozuki.ifixit.model.guide.wizard.EditTextPage;
import com.dozuki.ifixit.model.guide.wizard.GuideTitlePage;
import com.dozuki.ifixit.model.guide.wizard.ModelCallbacks;
import com.dozuki.ifixit.model.guide.wizard.Page;
import com.dozuki.ifixit.model.guide.wizard.TopicNamePage;
import com.dozuki.ifixit.ui.BaseMenuDrawerActivity;
import com.dozuki.ifixit.ui.guide.create.wizard.PageFragmentCallbacks;
import com.dozuki.ifixit.ui.guide.create.wizard.ReviewFragment;
import com.dozuki.ifixit.ui.guide.create.wizard.StepPagerStrip;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiError;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.dozuki.ifixit.util.api.Api;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class GuideIntroActivity extends BaseMenuDrawerActivity implements
 PageFragmentCallbacks, ReviewFragment.Callbacks, ModelCallbacks {
   public static final int GUIDE_STEP_EDIT_REQUEST = 1;
   public static final String STATE_KEY = "STATE_KEY";

   private ViewPager mPager;
   private FormWizardPagerAdapter mPagerAdapter;
   private AbstractWizardModel mWizardModel;

   private Button mNextButton;
   private Button mPrevButton;

   private List<Page> mCurrentPageSequence;
   private StepPagerStrip mStepPagerStrip;

   private Bundle mWizardModelBundle;
   private Guide mGuide;

   private boolean mEditingAfterReview;
   private boolean mEditIntroState = false;
   private boolean mConsumePageSelectedEvent;

   private View.OnClickListener mNextButtonClickListener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
         if (mPager.getCurrentItem() == mCurrentPageSequence.size()) {
            showLoading(R.id.intro_loading_container, getString(R.string.saving));

            Bundle bundle = mWizardModel.save();
            if (mEditIntroState) {
               Api.call(GuideIntroActivity.this, ApiCall.editGuide(bundle,
                mGuide.getGuideid(), mGuide.getRevisionid()));
            } else {
               Api.call(GuideIntroActivity.this, ApiCall.createGuideFromBundle(bundle));
            }

         } else {
            if (mEditingAfterReview) {
               mPager.setCurrentItem(mPagerAdapter.getCount() - 1);
            } else {
               mPager.setCurrentItem(mPager.getCurrentItem() + 1);
            }
         }
      }
   };

   private View.OnClickListener mPrevButtonClickListener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
         mPager.setCurrentItem(mPager.getCurrentItem() - 1);
      }
   };

   private ViewPager.SimpleOnPageChangeListener mPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
         mStepPagerStrip.setCurrentPage(position);

         if (mConsumePageSelectedEvent) {
            mConsumePageSelectedEvent = false;
            return;
         }

         mEditingAfterReview = false;
         updateBottomBar();
      }
   };

   private StepPagerStrip.OnPageSelectedListener mPageSelectedListener = new StepPagerStrip.OnPageSelectedListener() {
      @Override
      public void onPageStripSelected(int position) {
         position = Math.min(mPagerAdapter.getCount() - 1, position);
         if (mPager.getCurrentItem() != position) {
            mPager.setCurrentItem(position);
         }
      }
   };

   /////////////////////////////////////////////////////
   // LIFECYCLE
   /////////////////////////////////////////////////////

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.guide_create_intro);

      if (App.get().isScreenLarge()) {
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
      } else {
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      }

      Bundle extras = getIntent().getExtras();
      if (extras != null) {
         mGuide = (Guide) extras.getSerializable(StepsActivity.GUIDE_KEY);
         mEditIntroState = extras.getBoolean(GuideIntroActivity.STATE_KEY);
         mWizardModelBundle = buildIntroBundle();
      } else if (savedInstanceState != null) {
         mGuide = (Guide) savedInstanceState.getSerializable(StepsActivity.GUIDE_KEY);
         mWizardModelBundle = savedInstanceState.getBundle("model");
      }

      if (App.get().getSite().mGuideTypes == null) {
         Api.call(this, ApiCall.siteInfo());
      } else {
         initWizard();
      }

      App.getGaTracker().set(Fields.SCREEN_NAME, "/user/guides/" + mGuide.getGuideid() + "/details");
      App.getGaTracker().send(MapBuilder.createAppView().build());
   }

   private Bundle buildIntroBundle() {
      Bundle bundle = new Bundle();
      App app = App.get();
      String type = mGuide.getType().toLowerCase();
      String subjectBundleKey;

      Bundle topicBundle = new Bundle();
      topicBundle.putString(TopicNamePage.TOPIC_DATA_KEY, mGuide.getTopic());

      Bundle typeBundle = new Bundle();
      typeBundle.putString(Page.SIMPLE_DATA_KEY, type);

      Bundle titleBundle = new Bundle();
      titleBundle.putString(GuideTitlePage.TITLE_DATA_KEY, mGuide.getTitle());

      Bundle summaryBundle = new Bundle();
      summaryBundle.putString(EditTextPage.TEXT_DATA_KEY, mGuide.getSummary());

      Bundle introductionBundle = new Bundle();
      introductionBundle.putString(EditTextPage.TEXT_DATA_KEY, mGuide.getIntroductionRaw());

      Bundle subjectBundle = new Bundle();
      subjectBundle.putString(EditTextPage.TEXT_DATA_KEY, mGuide.getSubject());

      if (type.equals("replacement") || type.equals("introduction") || type.equals("disassembly")
       || type.equals("repair")) {
         subjectBundleKey = GuideIntroWizardModel.HAS_SUBJECT_KEY + ":" + app.getString(R.string
          .guide_intro_wizard_guide_subject_title);
      } else {
         subjectBundleKey = GuideIntroWizardModel.NO_SUBJECT_KEY + ":" + app.getString(R.string
          .guide_intro_wizard_guide_subject_title);
      }

      String topicBundleKey = app.getString(R.string.guide_intro_wizard_guide_topic_title, app.getTopicName());

      bundle.putBundle(subjectBundleKey, subjectBundle);
      bundle.putBundle(app.getString(R.string.guide_intro_wizard_guide_type_title), typeBundle);
      bundle.putBundle(topicBundleKey, topicBundle);
      bundle.putBundle(app.getString(R.string.guide_intro_wizard_guide_title_title), titleBundle);
      bundle.putBundle(app.getString(R.string.guide_intro_wizard_guide_introduction_title), introductionBundle);
      bundle.putBundle(app.getString(R.string.guide_intro_wizard_guide_summary_title), summaryBundle);

      return bundle;
   }

   protected void initWizard() {
      mWizardModel = mEditIntroState ?
       new GuideIntroEditWizardModel(this) :
       new GuideIntroWizardModel(this);

      if (mWizardModelBundle != null) {
         mWizardModel.load(mWizardModelBundle);
      }

      mWizardModel.registerListener(this);
      mCurrentPageSequence = mWizardModel.getCurrentPageSequence();

      mPager = (ViewPager) findViewById(R.id.pager);
      mStepPagerStrip = (StepPagerStrip) findViewById(R.id.strip);

      mNextButton = (Button) findViewById(R.id.next_button);
      mPrevButton = (Button) findViewById(R.id.prev_button);

      mStepPagerStrip.setOnPageSelectedListener(mPageSelectedListener);

      mPagerAdapter = new FormWizardPagerAdapter(getSupportFragmentManager());
      mPager.setAdapter(mPagerAdapter);
      mPager.setOnPageChangeListener(mPageChangeListener);

      mNextButton.setOnClickListener(mNextButtonClickListener);
      mPrevButton.setOnClickListener(mPrevButtonClickListener);

      onPageTreeChanged();
      updateBottomBar();

      // If we're editing an existing guides details, start at the Review page.
      if (mEditIntroState) {
         mPager.setCurrentItem(mPagerAdapter.getCount() - 1);
      }
   }


   @Override
   public void onPageTreeChanged() {
      mCurrentPageSequence = mWizardModel.getCurrentPageSequence();
      recalculateCutOffPage();
      mStepPagerStrip.setPageCount(mCurrentPageSequence.size() + 1); // + 1 = review step
      mPagerAdapter.notifyDataSetChanged();
      updateBottomBar();
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      // Null check is required to prevent null pointer exceptions when a user is not logged in and cancels login.
      if (mWizardModel != null) {
         mWizardModel.unregisterListener(this);
      }
   }

   @Override
   protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      if (mWizardModel != null) {
         outState.putBundle("model", mWizardModel.save());
      }
      outState.putSerializable(StepsActivity.GUIDE_KEY, mGuide);
      outState.putBoolean(GuideIntroActivity.STATE_KEY, mEditIntroState);
   }

   @Override
   public AbstractWizardModel onGetModel() {
      return mWizardModel;
   }

   @Override
   public void onEditScreenAfterReview(String key) {
      for (int i = mCurrentPageSequence.size() - 1; i >= 0; i--) {
         if (mCurrentPageSequence.get(i).getKey().equals(key)) {
            mConsumePageSelectedEvent = true;
            mEditingAfterReview = true;
            mPager.setCurrentItem(i);
            updateBottomBar();
            break;
         }
      }
   }

   @Override
   public boolean finishActivityIfLoggedOut() {
      return true;
   }

   @Override
   public void onPageDataChanged(Page page) {
      if (page.isRequired()) {
         if (recalculateCutOffPage()) {
            mPagerAdapter.notifyDataSetChanged();
            updateBottomBar();
         }
      }
   }

   @Override
   public Page onGetPage(String key) {
      return mWizardModel.findByKey(key);
   }

   /////////////////////////////////////////////////////
   // NOTIFICATION LISTENERS
   /////////////////////////////////////////////////////

   @Subscribe
   public void onSiteInfo(ApiEvent.SiteInfo event) {
      if (!event.hasError()) {
         App.get().setSite(event.getResult());

         initWizard();
      } else {
         Api.getErrorDialog(this, event).show();
      }
   }

   @Subscribe
   public void onGuideCreated(ApiEvent.CreateGuide event) {
      if (!event.hasError()) {
         Guide guide = event.getResult();

         GuideStep step = new GuideStep();
         step.addLine(new StepLine());

         ArrayList<GuideStep> initialStepList = new ArrayList<GuideStep>();
         initialStepList.add(step);

         guide.setStepList(initialStepList);
         hideLoading();

         Intent intent = new Intent(this, StepEditActivity.class);
         intent.putExtra(StepsActivity.GUIDE_KEY, guide);
         intent.putExtra(StepEditActivity.GUIDE_STEP_NUM_KEY, 0);
         startActivityForResult(intent, GUIDE_STEP_EDIT_REQUEST);
         finish();
      } else {
         hideChildren(false);
         Api.getErrorDialog(this, event).show();
      }
   }

   @Subscribe
   public void onGuideEdited(ApiEvent.EditGuide event) {
      if (!event.hasError()) {
         Guide guide = event.getResult();
         hideLoading();

         Intent intent = new Intent(this, StepsActivity.class);
         intent.putExtra(StepsActivity.GUIDE_KEY, guide);
         startActivityForResult(intent, GUIDE_STEP_EDIT_REQUEST);
         finish();
      } else {
         if (event.getError().mType == ApiError.Type.CONFLICT) {
            mGuide = event.getResult();
            mWizardModelBundle = buildIntroBundle();
            mWizardModel.load(mWizardModelBundle);
            onPageTreeChanged();

            hideLoading();
         }

         Api.getErrorDialog(this, event).show();
      }
   }

   /////////////////////////////////////////////////////
   // ADAPTERS
   /////////////////////////////////////////////////////

   public class FormWizardPagerAdapter extends FixedFragmentStatePagerAdapter {
      private int mCutOffPage;
      private Fragment mPrimaryItem;

      public FormWizardPagerAdapter(FragmentManager fm) {
         super(fm);
      }

      @Override
      public Fragment getItem(int i) {
         if (i >= mCurrentPageSequence.size()) {
            return new ReviewFragment();
         }

         return mCurrentPageSequence.get(i).createFragment();
      }

      @Override
      public int getItemPosition(Object object) {
         // TODO: Be smarter about this.
         if (object == mPrimaryItem) {
            // Re-use the current fragment (its position never changes).
            return POSITION_UNCHANGED;
         }

         return POSITION_NONE;
      }

      @Override
      public void setPrimaryItem(ViewGroup container, int position, Object object) {
         super.setPrimaryItem(container, position, object);
         mPrimaryItem = (Fragment) object;
      }

      @Override
      public int getCount() {
         return Math.min(mCutOffPage + 1, mCurrentPageSequence.size() + 1);
      }

      public void setCutOffPage(int cutOffPage) {
         if (cutOffPage < 0) {
            cutOffPage = Integer.MAX_VALUE;
         }
         mCutOffPage = cutOffPage;
      }

      public int getCutOffPage() {
         return mCutOffPage;
      }
   }

   /////////////////////////////////////////////////////
   // HELPERS
   /////////////////////////////////////////////////////

   private boolean recalculateCutOffPage() {
      // Cut off the pager adapter at first required page that isn't completed
      int cutOffPage = mCurrentPageSequence.size() + 1;
      for (int i = 0; i < mCurrentPageSequence.size(); i++) {
         Page page = mCurrentPageSequence.get(i);
         if (page.isRequired() && !page.isCompleted()) {
            cutOffPage = i;
            break;
         }
      }

      if (mPagerAdapter.getCutOffPage() != cutOffPage) {
         mPagerAdapter.setCutOffPage(cutOffPage);
         return true;
      }

      return false;
   }

   private void updateBottomBar() {
      int position = mPager.getCurrentItem();
      if (position == mCurrentPageSequence.size()) {
         mNextButton.setText(R.string.finish);
         mNextButton.setBackgroundResource(R.drawable.wizard_finish_background);
         mNextButton.setTextAppearance(this, R.style.WizardFinishTextAppearance);
      } else {
         mNextButton.setText(mEditingAfterReview
          ? R.string.review
          : R.string.next);
         mNextButton.setBackgroundResource(R.drawable.wizard_selectable_item_background);
         TypedValue v = new TypedValue();
         getTheme().resolveAttribute(android.R.attr.textAppearanceMedium, v, true);
         mNextButton.setTextAppearance(this, v.resourceId);
         mNextButton.setEnabled(position != mPagerAdapter.getCutOffPage());
      }

      mPrevButton.setVisibility(position <= 0 ? View.INVISIBLE : View.VISIBLE);
   }

   private void hideChildren(boolean hide) {
      int visibility = hide ? View.GONE : View.VISIBLE;

      mStepPagerStrip.setVisibility(visibility);
      mNextButton.setVisibility(visibility);
      mPrevButton.setVisibility(visibility);
   }

   @Override
   public void showLoading(int container, String message) {
      hideChildren(true);

      if (findViewById(container).getVisibility() == View.GONE) {
         findViewById(container).setVisibility(View.VISIBLE);
      }

      super.showLoading(container, message);
   }

   @Override
   public void hideLoading() {
      super.hideLoading();

      findViewById(R.id.intro_loading_container).setVisibility(View.GONE);

      hideChildren(false);
   }
}
