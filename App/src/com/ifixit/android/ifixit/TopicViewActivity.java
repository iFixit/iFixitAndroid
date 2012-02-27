package com.ifixit.android.ifixit;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class TopicViewActivity extends FragmentActivity {
   public static final String TOPIC_KEY = "TOPIC";

   private TopicViewFragment mTopicView;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.topic_view);

      mTopicView = (TopicViewFragment)getSupportFragmentManager()
       .findFragmentById(R.id.topic_view_fragment);

      // We can handle the fragments side by side in the previous activity
      // so lets go back there
      if ((mTopicView == null || !mTopicView.isInLayout()) &&
       getResources().getConfiguration().orientation ==
       Configuration.ORIENTATION_LANDSCAPE) {
         finish();
         return;
      }

      mTopicView.setTopicNode((TopicNode)getIntent().
       getSerializableExtra(TOPIC_KEY));
   }
}
