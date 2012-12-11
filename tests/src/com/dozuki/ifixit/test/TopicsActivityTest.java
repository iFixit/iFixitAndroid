package com.dozuki.ifixit.test;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.dozuki.ui.SiteListActivity;
import com.dozuki.ifixit.topic_view.ui.TopicsActivity;
import com.jayway.android.robotium.solo.Solo;

import junit.framework.Assert;

public class TopicsActivityTest extends ActivityInstrumentationTestCase2<TopicsActivity> {
   private Solo solo;
   private TopicsActivity mActivity;
   private Instrumentation mInstrumentation;

   public TopicsActivityTest(String name) {
      super(TopicsActivity.class);
   }

   protected void setUp() throws Exception {
      solo = new Solo(getInstrumentation(), getActivity());
   }

   protected void testActionBar() {
      solo.clickOnActionBarItem(R.id.gallery_button);
      Assert.assertTrue(solo.searchText("Login", 1));
   }
   
   protected void tearDown() throws Exception {
      solo.finishOpenedActivities();
   }

}
