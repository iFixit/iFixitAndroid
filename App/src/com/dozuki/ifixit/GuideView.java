package com.dozuki.ifixit;

import java.util.List;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ifixit.android.imagemanager.ImageManager;
import com.viewpagerindicator.CirclePageIndicator;

public class GuideView extends SherlockFragmentActivity 
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
   private int mCurrentPage;
   protected ImageManager mImageManager;
   private ViewPager mPager;
   private CirclePageIndicator mIndicator;
   protected ProgressBar mProgressBar;

   @Override
   public void onCreate(Bundle savedInstanceState) {
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

      if (savedInstanceState != null) {
         mGuideid = savedInstanceState.getInt(SAVED_GUIDEID);
         Guide guide = (Guide)savedInstanceState.getSerializable(SAVED_GUIDE);
         if (guide != null) {
            setGuide(guide);
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
            mGuideid = extras.getInt(MainActivity.GUIDEID);
         }

         getGuide(mGuideid);
      }

      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      //initSpeechRecognizer();
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      state.putSerializable(SAVED_GUIDEID, mGuideid);
      state.putSerializable(SAVED_GUIDE, mGuide);
      state.putInt(CURRENT_PAGE, mCurrentPage);
   }

   public void setGuide(Guide guide) {
      if (guide == null) {
         displayError();
         return;
      }

      mProgressBar.setVisibility(View.GONE);
      mGuide = guide;

      ActionBar actionBar = getSupportActionBar();
      actionBar.setTitle(mGuide.mTitle);
      
      mGuideAdapter = new GuideViewAdapter(this.getSupportFragmentManager(),
       mImageManager, mGuide);

      mPager.setAdapter(mGuideAdapter);
      mIndicator.setOnPageChangeListener(this);
      mIndicator.setViewPager(mPager);
      mPager.setVisibility(View.VISIBLE);
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

      if (mSpeechCommander != null) {
         mSpeechCommander.stopListening();
         mSpeechCommander.cancel();
      }
   }

   @Override
   public void onResume() {
      super.onResume();

      if (mSpeechCommander != null)
         mSpeechCommander.startListening();
   }

   public int getScreenHeight() {
      Display display = getWindowManager().getDefaultDisplay();
      return display.getHeight();
   }

   public int getScreenWidth() {
      Display display = getWindowManager().getDefaultDisplay();
      return display.getWidth();
   }

   public void getGuide(final int guideid) {
      new APIHelper.APIResponder<Guide>() {
         public void execute() {
            APIHelper.getGuide(GuideView.this, guideid, this);
         }

         public void setResult(Guide guide) {
            setGuide(guide);
         }

         public void error(AlertDialog dialog) {
            dialog.show();
         }
      }.execute();
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

   public void initSpeechRecognizer() {
      if (!SpeechRecognizer.isRecognitionAvailable(getBaseContext()))
         return;

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
      mCurrentPage = page;
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
