package com.ifixit.android.ifixit;

import java.util.ArrayList;
import org.apache.http.client.ResponseHandler;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class TopicsActivity extends FragmentActivity implements
 TopicListFragment.TopicSelectedListener {
   private static final String TOPICS_API_URL =
    "http://www.ifixit.com/api/0.1/areas/";
   private static final String RESPONSE = "RESPONSE";
   private static final String ROOT_TOPIC = "ROOT_TOPIC";

   private boolean mDualPane;
   private TopicViewFragment mTopicView;
   private TopicNode mTopic;

   private final Handler mTopicsHandler = new Handler() {
      public void handleMessage(Message message) {
         String response = message.getData().getString(RESPONSE);
         ArrayList<TopicNode> topics = JSONHelper.parseTopics(response);

         if (topics != null) {
            mTopic = new TopicNode();
            mTopic.addAllTopics(topics);
            onTopicSelected(mTopic);
         }
         else {
            Log.e("iFixit", "Topics is null (response: " + response + ")");
         }
      }
   };

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.topics);
      mTopicView = (TopicViewFragment)getSupportFragmentManager()
       .findFragmentById(R.id.topic_view_fragment);
      mDualPane = mTopicView != null && mTopicView.isInLayout();

      if (savedInstanceState != null) {
         mTopic = (TopicNode)savedInstanceState.getSerializable(ROOT_TOPIC);
      } else {
         getTopicHierarchy();
      }
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putSerializable(ROOT_TOPIC, mTopic);
   }

   @Override
   public void onTopicSelected(TopicNode topic) {
      if (topic.isLeaf()) {
         if (mDualPane) {
            mTopicView.setTopicNode(topic);
         }
         else {
            Intent intent = new Intent(this, TopicViewActivity.class);
            Bundle bundle = new Bundle();

            bundle.putSerializable(TopicViewActivity.TOPIC_KEY, topic);
            intent.putExtras(bundle);
            startActivity(intent);
         }
      } else {
         FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
         TopicListFragment newFragment = new TopicListFragment(topic);
         
         ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
          R.anim.slide_in_left, R.anim.slide_out_right);
         ft.replace(R.id.topic_list_fragment, newFragment);

         if (!topic.isRoot()) {
            ft.addToBackStack(null);
         }

         ft.commit();
      }
   }

   private void getTopicHierarchy() {
      final ResponseHandler<String> responseHandler =
       HTTPRequestHelper.getResponseHandlerInstance(mTopicsHandler);

      new Thread() {
         public void run() {
            HTTPRequestHelper helper = new HTTPRequestHelper(responseHandler);

            helper.performGet(TOPICS_API_URL);
         }
      }.start();
   }
}
