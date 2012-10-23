package com.dozuki.ifixit.guide_view.ui;

import java.util.List;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_view.model.Guide;
import com.dozuki.ifixit.topic_view.ui.TopicGuideListFragment;
import com.dozuki.ifixit.util.APIEndpoint;
import com.dozuki.ifixit.util.APIReceiver;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.util.SpeechCommander;
import com.ifixit.android.imagemanager.ImageManager;
import com.viewpagerindicator.CirclePageIndicator;
import com.dozuki.ifixit.util.Error;

public class GuideViewActivity extends SherlockFragmentActivity
 implements OnPageChangeListener {
   private static final int MAX_LOADING_IMAGES = 9;
   private static final int MAX_STORED_IMAGES = 9;
   private static final int MAX_WRITING_IMAGES = 10;
   private static final String CURRENT_PAGE = "CURRENT_PAGE";
   private static final String SAVED_GUIDE = "SAVED_GUIDE";
   private static final String SAVED_GUIDEID = "SAVED_GUIDEID";
   private static final String NEXT_COMMAND = "next";
   private static final String PREVIOUS_COMMAND = "previous";
   private static final String HOME_COMMAND = "home";
   private static final String PACKAGE_NAME = "com.dozuki.ifixit";

   private GuideViewAdapter mGuideAdapter;
   private int mGuideid;
   private Guide mGuide;
   private SpeechCommander mSpeechCommander;
   private int mCurrentPage = -1;
   private ImageManager mImageManager;
   private ViewPager mPager;
   private CirclePageIndicator mIndicator;
   private ProgressBar mProgressBar;
   private ImageView mNextPageImage;

   private APIReceiver mApiReceiver = new APIReceiver() {
      public void onSuccess(Object result, Intent intent) {
         if (mGuide == null) {
            setGuide((Guide)result, 0);
         }
      }

      public void onFailure(Error error, Intent intent) {
         APIService.getErrorDialog(GuideViewActivity.this, error,
          APIService.getGuideIntent(GuideViewActivity.this, mGuideid)).show();
      }
   };

   @Override
   public void onCreate(Bundle savedInstanceState) {
      setTheme(((MainApplication)getApplication()).getSiteTheme());
      setTitle("");
      super.onCreate(savedInstanceState);

      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
       WindowManager.LayoutParams.FLAG_FULLSCREEN);

      setContentView(R.layout.guide_main);

      mImageManager = ((MainApplication)getApplication()).getImageManager();
      mImageManager.setMaxLoadingImages(MAX_LOADING_IMAGES);
      mImageManager.setMaxStoredImages(MAX_STORED_IMAGES);
      mImageManager.setMaxWritingImages(MAX_WRITING_IMAGES);
      mPager = (ViewPager)findViewById(R.id.guide_pager);
      mIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
      mProgressBar = (ProgressBar)findViewById(R.id.progress_bar);
      mNextPageImage = (ImageView)findViewById(R.id.next_page_image);

      if (savedInstanceState != null) {
         mGuideid = savedInstanceState.getInt(SAVED_GUIDEID);
         Guide guide = (Guide)savedInstanceState.getSerializable(SAVED_GUIDE);
         if (guide != null) {
            setGuide(guide, savedInstanceState.getInt(CURRENT_PAGE));
            mIndicator.setCurrentItem(savedInstanceState.getInt(CURRENT_PAGE));
         } else {
            getGuide(mGuideid);
         }
      } else {
         Intent intent = getIntent();
         mGuideid = -1;

         if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            List<String> segments = intent.getData().getPathSegments();

            try {
               mGuideid = Integer.parseInt(segments.get(2));
            } catch (Exception e) {
               displayError();
               Log.e("iFixit", "Problem parsing guide");
            }
         } else {
            Bundle extras = intent.getExtras();
            mGuideid = extras.getInt(TopicGuideListFragment.GUIDEID);
         }

         getGuide(mGuideid);
      }

      mNextPageImage.setOnTouchListener(new View.OnTouchListener() {
         public boolean onTouch(View v, MotionEvent event) {
            if (mCurrentPage == 0) {
               nextStep();

               return true;
            }

            return false;
         }
      });

      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      //initSpeechRecognizer();
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

   public void setGuide(Guide guide, int page) {
      if (guide == null) {
         displayError();
         return;
      }

      mProgressBar.setVisibility(View.GONE);
      mGuide = guide;

      ActionBar actionBar = getSupportActionBar();
      actionBar.setTitle(mGuide.getTitle());

      mGuideAdapter = new GuideViewAdapter(this.getSupportFragmentManager(),
       mImageManager, mGuide);

      mPager.setAdapter(mGuideAdapter);
      mIndicator.setOnPageChangeListener(this);
      mIndicator.setViewPager(mPager);

      final float density = getResources().getDisplayMetrics().density;
      mIndicator.setBackgroundColor(0x00FFFFFF);
      mIndicator.setRadius(6 * density);
      mIndicator.setPageColor(0xFFFFFFFF);
      mIndicator.setFillColor(0xFF444444);
      mIndicator.setStrokeColor(0xFF000000);
      mIndicator.setStrokeWidth((int)(1.5 * density));

      mPager.setVisibility(View.VISIBLE);

      onPageSelected(page);
   }

   @Override
   public void onDestroy() {
      super.onDestroy();

      if (mSpeechCommander != null)
         mSpeechCommander.destroy();
   }

   @Override
   public void onPause() {
      super.onPause();

      try {
         unregisterReceiver(mApiReceiver);
      } catch (IllegalArgumentException e) {
         // Do nothing. This happens in the unlikely event that
         // unregisterReceiver has been called already.
      }

      if (mSpeechCommander != null) {
         mSpeechCommander.stopListening();
         mSpeechCommander.cancel();
      }
   }

   @Override
   public void onResume() {
      super.onResume();

      IntentFilter filter = new IntentFilter();
      filter.addAction(APIEndpoint.GUIDE.mAction);
      registerReceiver(mApiReceiver, filter);

      if (mSpeechCommander != null) {
         mSpeechCommander.startListening();
      }
   }

   public void getGuide(final int guideid) {
      mNextPageImage.setVisibility(View.GONE);

      startService(APIService.getGuideIntent(this, guideid));
   }

   private void displayError() {
      mProgressBar.setVisibility(View.GONE);
      // TODO Display error
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

   public int getIndicatorHeight() {
      return mIndicator.getHeight();
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

   @Override
   public void onPageScrollStateChanged(int arg0) {}

   @Override
   public void onPageScrolled(int arg0, float arg1, int arg2) {}

   @Override
   public void onPageSelected(int page) {
      if (mCurrentPage == page) {
         return;
      }

      mCurrentPage = page;
      final int visibility;
      Animation anim;

      if (mCurrentPage == 0) {
         anim = new AlphaAnimation(0.00f, 1.00f);
         visibility = View.VISIBLE;
      } else if (mCurrentPage == 1) {
         anim = new AlphaAnimation(1.00f, 0.00f);
         visibility = View.GONE;
      } else {
         mNextPageImage.setVisibility(View.GONE);
         return;
      }

      if (anim != null && mNextPageImage.getVisibility() != visibility) {
         anim.setDuration(400);
         anim.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {}

            public void onAnimationRepeat(Animation animation) {}

            public void onAnimationEnd(Animation animation) {
               mNextPageImage.setVisibility(visibility);
            }
         });

         mNextPageImage.startAnimation(anim);
      }
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home:
            finish();

            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }
}
