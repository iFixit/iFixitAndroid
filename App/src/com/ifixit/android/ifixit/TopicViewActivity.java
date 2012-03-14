package com.ifixit.android.ifixit;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.content.Intent;

import android.content.res.Configuration;
import android.os.Bundle;

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

      TopicLeaf topicLeaf = null;
      if (savedState != null) {
         topicLeaf = (TopicLeaf)savedState.getSerializable(
          TOPIC_KEY);
      }

      // We can handle the fragments side by side in the previous activity
      // so lets go back there
      if ((mTopicView == null || !mTopicView.isInLayout()) &&
       getResources().getConfiguration().orientation ==
       Configuration.ORIENTATION_LANDSCAPE) {
         if (topicLeaf != null) {
            Intent intent = new Intent();
            Bundle extras = new Bundle();

            extras.putSerializable(TOPIC_KEY, topicLeaf);
            intent.putExtras(extras);
            setResult(TopicsActivity.TOPIC_RESULT, intent);
         } else {
            setResult(TopicsActivity.NO_TOPIC_RESULT);
         }

         finish();
         return;
      }

      if (topicLeaf != null) {
         mTopicView.setTopicLeaf(topicLeaf);
      } else {
         mTopicView.setTopicNode((TopicNode)getIntent().
          getSerializableExtra(TOPIC_KEY));
      }
   }
   
   public ImageManager getImageManager() {
	   return mImageManager;
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      super.onSaveInstanceState(state);

      state.putSerializable(TOPIC_KEY, mTopicView.getTopicLeaf());
   }
}
