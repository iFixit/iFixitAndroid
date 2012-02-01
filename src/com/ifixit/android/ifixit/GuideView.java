package com.ifixit.android.ifixit;

import org.apache.http.client.ResponseHandler;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.SpeechRecognizer;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

public class GuideView extends Activity implements OnPageChangeListener {
   private static final String RESPONSE = "RESPONSE";
   private static final String API_URL = "http://www.ifixit.com/api/0.1/guide/";
   private static final String NEXT_COMMAND = "next";
   private static final String PREVIOUS_COMMAND = "previous";
   private static final String HOME_COMMAND = "home";
   private static final String PACKAGE_NAME = "com.ifixit.android.ifixit";
   
   private ViewPager mGuidePager;
   private ProgressBar mProgressBar;
   private GuidePagerAdapter mGuideAdapter;
   private Guide mGuide;
   private SpeechCommander mSpeechCommander;
   private int mCurrentPage;
   protected ImageManager mImageManager;
   
   private final Handler mGuideHandler = new Handler() {
      public void handleMessage(Message message) {
         String response = message.getData().getString(RESPONSE);
         Guide guide = GuideJSONHelper.parseGuide(response);

         if (guide != null) {
            setGuide(guide);
         }
         else {
            Log.e("iFixit", "Guide is null (response: " + response + ")");
         }
      }
   };
   
   @Override
   public void onCreate(Bundle savedInstanceState) {
      Bundle extras;

      super.onCreate(savedInstanceState);
      setContentView(R.layout.guide_main);

      mImageManager = ((MainApplication)getApplication()).getImageManager();
      mGuidePager = (ViewPager)findViewById(R.id.guide_pager);
      mGuideAdapter = new GuidePagerAdapter(this, null, mImageManager);
      mGuidePager.setAdapter(mGuideAdapter);
      mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
      mGuidePager.setVisibility(View.GONE);
      mProgressBar.setVisibility(View.VISIBLE);

      extras = getIntent().getExtras();
      getGuide(extras.getInt(MainActivity.GUIDEID));
      initSpeechRecognizer();
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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

   public void setGuide(Guide guide) {
      mGuide = guide;
      mGuideAdapter = new GuidePagerAdapter(this, mGuide, mImageManager);
      mGuidePager.setAdapter(mGuideAdapter);
      mGuidePager.setOnPageChangeListener(this);

      mProgressBar.setVisibility(View.GONE);
      mGuidePager.setVisibility(View.VISIBLE);
   }

   public void getGuide(final int guideid) {
      final ResponseHandler<String> responseHandler =
       HTTPRequestHelper.getResponseHandlerInstance(mGuideHandler);

      new Thread() {
         public void run() {
            HTTPRequestHelper helper = new HTTPRequestHelper(responseHandler);

            helper.performGet(API_URL + guideid);
         }
      }.start();
   }

   private void nextStep() {
      mGuidePager.setCurrentItem(mCurrentPage + 1);
   }

   private void previousStep() {
      mGuidePager.setCurrentItem(mCurrentPage - 1);
   }
   
   private void guideHome() {
      mGuidePager.setCurrentItem(0);
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
   public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getMenuInflater();

      if (APICompatibility.hasActionBar())
         inflater.inflate(R.menu.guide_menu, menu);
      else
         inflater.inflate(R.menu.guide_menu_no_action_bar, menu);

      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      menu.findItem(R.id.previous_step).setEnabled(mCurrentPage > 0);
      menu.findItem(R.id.next_step).setEnabled(mGuide == null ||
       mCurrentPage < mGuide.getNumSteps());

      return super.onPrepareOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.more_guides:
            finish();
            return true;
         case R.id.guide_home:
            guideHome();
            return true;
         case R.id.previous_step:
            previousStep();
            return true;
         case R.id.next_step:
            nextStep();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }
   
   @Override
   public void onPageScrollStateChanged(int arg0) {}
   @Override
   public void onPageScrolled(int arg0, float arg1, int arg2) {}

   @Override
   public void onPageSelected(int page) {
      mCurrentPage = page;

      if (APICompatibility.hasActionBar())
         invalidateOptionsMenu();
   }
}
