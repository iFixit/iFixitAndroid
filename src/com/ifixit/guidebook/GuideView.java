package com.ifixit.guidebook;

import org.apache.http.client.ResponseHandler;

import android.app.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.support.v4.view.ViewPager;

import android.support.v4.view.ViewPager.OnPageChangeListener;

public class GuideView extends Activity implements OnPageChangeListener {
   private static final String RESPONSE = "RESPONSE";
   
   private ViewPager guidePager;
   private GuidePagerAdapter guideAdapter;
   private Guide mGuide;
   private SpeechCommander mSpeechCommander;
   private int mCurrentPage;
   
   private final Handler mGuideHandler = new Handler() {
      public void handleMessage(Message message) {
         String response = message.getData().getString(RESPONSE);
         mGuide = GuideJSONHelper.parseGuide(response);
         
         if (mGuide != null) {
            setGuide(mGuide);
         }
      }
   };
   
   @Override
   public void onCreate(Bundle savedInstanceState) {
      Bundle extras;
      super.onCreate(savedInstanceState);

      setContentView(R.layout.guide_main);
      extras = getIntent().getExtras();
      getGuide(extras.getInt("guideid"));
      initSpeechRecognizer();
   }

   @Override
   public void onDestroy() {
      super.onDestroy();

      mSpeechCommander.destroy();
   }

   /*@Override
   public void onPause() {
      super.onPause();

      mSpeechCommander.stopListening();
   }

   @Override
   public void onResume() {
      super.onResume();

      mSpeechCommander.startListening();
   }*/

   public void setGuide(Guide guide) {
      guideAdapter = new GuidePagerAdapter(this, mGuide);
      guidePager = (ViewPager) findViewById(R.id.guide_pager);
      guidePager.setAdapter(guideAdapter);
      guidePager.setOnPageChangeListener(this);
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
      guidePager.setCurrentItem(mCurrentPage + 1);
   }

   private void previousStep() {
      guidePager.setCurrentItem(mCurrentPage - 1);
   }

   private void guideHome() {
      guidePager.setCurrentItem(0);
   }

   public void initSpeechRecognizer() {
      mSpeechCommander = new SpeechCommander(this, "com.ifixit.guidebook");

      mSpeechCommander.addCommand("step next", new SpeechCommander.SpeechCommand() {
         public void performCommand() {
            nextStep();
         }
      });

      mSpeechCommander.addCommand("step previous", new SpeechCommander.SpeechCommand() {
         public void performCommand() {
            previousStep();
         }
      });

      mSpeechCommander.addCommand("guide home", new SpeechCommander.SpeechCommand() {
         public void performCommand() {
            guideHome();
         }
      });

      //mSpeechCommander.startListening();
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
