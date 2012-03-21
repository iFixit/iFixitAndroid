package com.dozuki.ifixit;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class TopicViewActivity extends SherlockFragmentActivity {
   public static final String TOPIC_KEY = "TOPIC";

   private TopicViewFragment mTopicView;
   protected ImageManager mImageManager;

   @Override
   public void onCreate(Bundle savedState) {
      super.onCreate(savedState);

      setContentView(R.layout.topic_view);
      
      mImageManager = ((MainApplication)getApplication()).getImageManager();
      mTopicView = (TopicViewFragment)getSupportFragmentManager()
       .findFragmentById(R.id.topic_view_fragment);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      if (mTopicView.getTopicNode() == null) {
         mTopicView.setTopicNode((TopicNode)getIntent().
          getSerializableExtra(TOPIC_KEY));
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
