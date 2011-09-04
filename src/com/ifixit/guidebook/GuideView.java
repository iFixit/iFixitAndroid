package com.ifixit.guidebook;

import org.apache.http.client.ResponseHandler;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

public class GuideView extends Activity {
   
   private ViewPager guidePager;
   private GuidePagerAdapter guideAdapter;
   private Guide mGuide;
   
   private static final String RESPONSE = "RESPONSE";
   
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
     
   }
   
   public void setGuide(Guide guide) {
      guideAdapter = new GuidePagerAdapter(this, mGuide);
      guidePager = (ViewPager) findViewById(R.id.guide_pager);
      guidePager.setAdapter(guideAdapter);

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
}
