package com.dozuki.ifixit;

import com.dozuki.ifixit.ui.topic_view.TopicActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
public class TopicActivityTest {
   private TopicActivity mActivity;

   @Before
   public void setup() {
      mActivity = Robolectric.buildActivity(TopicActivity.class).get();
   }

   @Test
   public void shouldFail() {
      assertTrue(Robolectric.httpRequestWasMade("https://mzych.cominor.com/api/2.0/categories"));
   }
}
