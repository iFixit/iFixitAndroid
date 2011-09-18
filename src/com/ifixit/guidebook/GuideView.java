package com.ifixit.guidebook;

import org.apache.http.client.ResponseHandler;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.SpeechRecognizer;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.ProgressBar;

public class GuideView extends Activity implements OnPageChangeListener {
   private static final String RESPONSE = "RESPONSE";
   
   private ViewPager mGuidePager;
   private ProgressBar mProgressBar;
   private GuidePagerAdapter mGuideAdapter;
   private Guide mGuide;
   private SpeechCommander mSpeechCommander;
   private int mCurrentPage;
   
   private final Handler mGuideHandler = new Handler() {
      public void handleMessage(Message message) {
         String response = message.getData().getString(RESPONSE);
         Guide guide = GuideJSONHelper.parseGuide(response);
         
         if (guide != null) {
            setGuide(guide);
         }
      }
   };
   
   @Override
   public void onCreate(Bundle savedInstanceState) {
      Bundle extras;
      super.onCreate(savedInstanceState);
      setContentView(R.layout.guide_main);

      mGuidePager = (ViewPager)findViewById(R.id.guide_pager);
      mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
      mGuidePager.setVisibility(View.GONE);
      mProgressBar.setVisibility(View.VISIBLE);

      extras = getIntent().getExtras();
      getGuide(extras.getInt(GuidebookActivity.GUIDEID));
      //initSpeechRecognizer();
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
      mGuideAdapter = new GuidePagerAdapter(this, mGuide);
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

            helper.performGet("http://www.ifixit.com/api/guide/" + guideid);
         }
      }.start();
   }

   private void nextStep() {
      mGuidePager.setCurrentItem(mCurrentPage + 1);
      //setStep();
   }

   private void previousStep() {
      mGuidePager.setCurrentItem(mCurrentPage - 1);
      //setStep();
   }
   
  /* private void setStep() {
      imageAdapter = new GuideImagePagerAdapter(this, mGuide.getStep(mCurrentPage));
      imagePager = (ViewPager) findViewById(R.id.image_pager);
      imagePager.setAdapter(imageAdapter);
      imagePager.setOnPageChangeListener(this);
   }*/

   private void guideHome() {
      mGuidePager.setCurrentItem(0);
   }

   public void initSpeechRecognizer() {
      if (!SpeechRecognizer.isRecognitionAvailable(getBaseContext()))
         return;
      
      mSpeechCommander = new SpeechCommander(this, "com.ifixit.guidebook");

      mSpeechCommander.addCommand("next", new SpeechCommander.SpeechCommand() {
         public void performCommand() {
            nextStep();
         }
      });

      mSpeechCommander.addCommand("previous", new SpeechCommander.SpeechCommand() {
         public void performCommand() {
            previousStep();
         }
      });

      mSpeechCommander.addCommand("home", new SpeechCommander.SpeechCommand() {
         public void performCommand() {
            guideHome();
         }
      });

      mSpeechCommander.startListening();
   }
   
   public void viewGuideHome() {
      Intent intent = new Intent(this, GuidebookActivity.class);

      startActivity(intent);
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
       MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.guide_menu, menu);
       return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
       // Handle item selection
       switch (item.getItemId()) {
       case R.id.guide_item:
           viewGuideHome();
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
   }
}
