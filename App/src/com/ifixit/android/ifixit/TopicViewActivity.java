package com.ifixit.android.ifixit;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class TopicViewActivity extends FragmentActivity {
   public static final String TOPIC_KEY = "TOPIC";

   private TopicViewFragment mTopicView;

   @Override
   public void onCreate(Bundle savedState) {
      super.onCreate(savedState);

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

      TopicLeaf topicLeaf = null;
      if (savedState != null) {
         topicLeaf = (TopicLeaf)savedState.getSerializable(
          TOPIC_KEY);
      }

      if (topicLeaf != null) {
         mTopicView.setTopicLeaf(topicLeaf);
      } else {
         mTopicView.setTopicNode((TopicNode)getIntent().
          getSerializableExtra(TOPIC_KEY));
      }
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      super.onSaveInstanceState(state);

      state.putSerializable(TOPIC_KEY, mTopicView.getTopicLeaf());
   }
}
