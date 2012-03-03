package com.ifixit.android.ifixit;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.ifixit.android.sectionheaders.SectionHeadersAdapter;

public class TopicListFragment extends ListFragment {
   public interface TopicSelectedListener {
      public void onTopicSelected(TopicNode topic);
   }

   private static final String CURRENT_TOPIC = "CURRENT_TOPIC";

   private TopicSelectedListener topicSelectedListener;
   private TopicNode mTopic;
   private SectionHeadersAdapter mTopicAdapter;
   private Context mContext;

   /**
    * Required for restoring fragments
    */
   public TopicListFragment() {}

   public TopicListFragment(TopicNode topic) {
      mTopic = topic;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (savedInstanceState != null) {
         mTopic = (TopicNode)savedInstanceState.getSerializable(
          CURRENT_TOPIC);
      }

      setTopic(mTopic);
   }

   private void setupTopicAdapter() {
      mTopicAdapter = new SectionHeadersAdapter();
      ArrayList<TopicNode> generalInfo = new ArrayList<TopicNode>();
      ArrayList<TopicNode> nonLeaves = new ArrayList<TopicNode>();
      ArrayList<TopicNode> leaves = new ArrayList<TopicNode>();

      for (TopicNode topic : mTopic.getChildren()) {
         if (topic.isLeaf()) {
            leaves.add(topic);
         } else {
            nonLeaves.add(topic);
         }
      }

      // TODO add these to strings.xml

      if (!mTopic.isRoot()) {
         generalInfo.add(makeTopicLeaf(mTopic));
         mTopicAdapter.addSection(new TopicListAdapter(mContext,
          "General Information", generalInfo));
      }

      if (nonLeaves.size() > 0) {
         mTopicAdapter.addSection(new TopicListAdapter(mContext,
          "Categories", nonLeaves));
      }

      if (leaves.size() > 0) {
         mTopicAdapter.addSection(new TopicListAdapter(mContext,
          "Devices", leaves));
      }
   }

   private TopicNode makeTopicLeaf(TopicNode topic) {
      return new TopicNode(topic.getName());
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putSerializable(CURRENT_TOPIC, mTopic);
   }

   @Override
   public void onListItemClick(ListView l, View v, int position, long id) {
      /*
      topicSelectedListener.onTopicSelected(
       mTopic.getChildren().get(position));
       */
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

   private void setTopic(TopicNode topic) {
      mTopic = topic;
      setupTopicAdapter();
      setListAdapter(mTopicAdapter);
   }
}
