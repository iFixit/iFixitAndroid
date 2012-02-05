package com.ifixit.android.ifixit;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class TopicViewActivity extends FragmentActivity {
   public static final String TOPIC_KEY = "TOPIC";

   private Topic mTopic;
   private TopicViewFragment mTopicView;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (getResources().getConfiguration().orientation ==
       Configuration.ORIENTATION_LANDSCAPE) {
         finish();
         return;
      }

      setContentView(R.layout.topic_view);
      mTopicView = (TopicViewFragment)getSupportFragmentManager()
       .findFragmentById(R.id.topic_view_fragment);
      mTopic = (Topic)getIntent().getSerializableExtra(TOPIC_KEY);

      mTopicView.setTopic(mTopic);
   }
}
