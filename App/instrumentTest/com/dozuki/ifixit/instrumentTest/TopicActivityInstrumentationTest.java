package com.dozuki.ifixit.instrumentTest;

import android.test.ActivityInstrumentationTestCase2;

import com.dozuki.ifixit.ui.topic_view.TopicActivity;

public class TopicActivityInstrumentationTest extends
 ActivityInstrumentationTestCase2<TopicActivity> {
   private TopicActivity mTopicActivity;

   public TopicActivityInstrumentationTest() {
      super(TopicActivity.class);
   }

   protected void setUp() throws Exception {
      super.setUp();

      mTopicActivity = getActivity();
   }

   /**
    * Dummy test for now.
    */
   public void testTitle() {
      assertEquals(mTopicActivity.getTitle(), "iFixit");
   }
}

