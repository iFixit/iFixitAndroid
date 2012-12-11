package com.dozuki.ifixit.test;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.ListView;
import android.widget.TextView;

import com.dozuki.ifixit.dozuki.ui.SiteListActivity;
import com.dozuki.ifixit.topic_view.ui.TopicsActivity;
import com.jayway.android.robotium.solo.*;

import junit.framework.Assert;

import java.util.ArrayList;

public class SiteListActivityTest extends
   ActivityInstrumentationTestCase2<SiteListActivity> {

   private Solo solo;
   private SiteListActivity mActivity;
   private Instrumentation mInstrumentation;
   
   public SiteListActivityTest() {
      super(SiteListActivity.class);
   }

   protected void setUp() throws Exception {
      solo = new Solo(getInstrumentation(), getActivity());
      mInstrumentation = getInstrumentation();
      mActivity = getActivity();
   }
   
   public void testPreConditions() {
      Assert.assertTrue(solo != null);
      Assert.assertTrue(mInstrumentation != null);
      Assert.assertTrue(mActivity != null);
   }
   
   public void testStateDestroy() {
      solo.assertCurrentActivity("Expected SiteListActivity", 
            SiteListActivity.class);

      solo.clickOnButton(0);
      solo.scrollListToLine(0, 5);
      solo.enterText(0, "Testing");
      solo.clearEditText(0);
      boolean hasListView = solo.getCurrentListViews().size() != 0;
      Assert.assertTrue(hasListView);
      Assert.assertTrue(solo.getEditText(0).getText().length() == 0);      
   }
   
   @UiThreadTest
   public void testStatePause() {
      
   }

   public void testSiteListSearch() {
      solo.assertCurrentActivity("Expected SiteListActivity", 
            SiteListActivity.class);

      solo.clickOnButton(0);
      
      String[] sites = {"iFixit", "Crucial"};
   
      for (String site : sites) {
         solo.clickOnEditText(0);
         solo.enterText(0, site);

         Assert.assertTrue(solo.searchText(site, 1));
         String listSiteName = (String)((TextView)solo.getCurrentListViews().get(0)
               .getChildAt(0).findViewById(com.dozuki.ifixit.R.id.site_name)).getText();
         Assert.assertEquals("Expected " + site + " got " + listSiteName , site, listSiteName);

         solo.clearEditText(0);
      }
      
      solo.clickOnEditText(0);
      solo.enterText(0, "DJSFHSDJHFJSHDFKhjkdhsfkjhsdf");
      Assert.assertTrue(solo.searchText(solo.getString(com.dozuki.ifixit.R.string.empty_site_list), 1));
      
      solo.goBack();
   }

   protected void tearDown() throws Exception {
      solo.finishOpenedActivities();
   }
}
