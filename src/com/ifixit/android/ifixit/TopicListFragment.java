package com.ifixit.android.ifixit;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

public class TopicListFragment extends ListFragment {
   public interface TopicSelectedListener {
      public void onTopicSelected(Topic topic);
   }

   private static final String CURRENT_TOPIC = "CURRENT_TOPIC";

   private TopicSelectedListener topicSelectedListener;
   private Topic mTopic;
   private TopicListAdapter mTopicAdapter;
   private Context mContext;

   /**
    * Required for restoring fragments
    */
   public TopicListFragment() {}

   public TopicListFragment(Topic topic) {
      mTopic = topic;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (savedInstanceState != null) {
         mTopic = (Topic)savedInstanceState.getSerializable(
          CURRENT_TOPIC);
      }

      mTopicAdapter = new TopicListAdapter(mContext);
      setTopic(mTopic);
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putSerializable(CURRENT_TOPIC, mTopic);
   }

   @Override
   public void onListItemClick(ListView l, View v, int position, long id) {
      topicSelectedListener.onTopicSelected(
       mTopic.getChildren().get(position));
   }

   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);

      try {
         topicSelectedListener = (TopicSelectedListener)activity;
         mContext = (Context)activity;
      } catch (ClassCastException e) {
         throw new ClassCastException(activity.toString() +
          " must implement TopicSelectedListener");
      }
   }

   private void setTopic(Topic topic) {
      mTopic = topic;
      mTopicAdapter.setTopic(mTopic);
      setListAdapter(mTopicAdapter);
   }
}
