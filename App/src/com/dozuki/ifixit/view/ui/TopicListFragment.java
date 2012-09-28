package com.dozuki.ifixit.view.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.view.model.TopicNode;
import com.dozuki.ifixit.view.model.TopicSelectedListener;
import com.ifixit.android.sectionheaders.SectionHeadersAdapter;
import com.ifixit.android.sectionheaders.SectionListView;

public class TopicListFragment extends SherlockFragment implements
   TopicSelectedListener, OnItemClickListener {
   private static final String CURRENT_TOPIC = "CURRENT_TOPIC";

   private TopicSelectedListener topicSelectedListener;
   private TopicNode mTopic;
   private SectionHeadersAdapter mTopicAdapter;
   private Context mContext;
   private SectionListView mListView;

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
         mTopic = (TopicNode) savedInstanceState.getSerializable(CURRENT_TOPIC);
      }

   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
      View view =
         inflater.inflate(R.layout.topic_list_fragment, container, false);

      mListView = (SectionListView) view.findViewById(R.id.topicList);
      mListView.getListView().setOnItemClickListener(this);

      setTopic(mTopic);

      return view;
   }

   private void setupTopicAdapter() {
      mTopicAdapter = new SectionHeadersAdapter();
      ArrayList<TopicNode> generalInfo = new ArrayList<TopicNode>();
      ArrayList<TopicNode> nonLeaves = new ArrayList<TopicNode>();
      ArrayList<TopicNode> leaves = new ArrayList<TopicNode>();
      TopicListAdapter adapter;

      for (TopicNode topic : mTopic.getChildren()) {
         if (topic.isLeaf()) {
            leaves.add(topic);
         } else {
            nonLeaves.add(topic);
         }
      }

      Comparator<TopicNode> comparator = new Comparator<TopicNode>() {
         public int compare(TopicNode first, TopicNode second) {
            return first.getName().compareTo(second.getName());
         }
      };

      Collections.sort(nonLeaves, comparator);
      Collections.sort(leaves, comparator);

      if (!mTopic.isRoot()) {
         generalInfo.add(new TopicNode(mTopic.getName()));
         adapter =
            new TopicListAdapter(mContext,
               mContext.getString(R.string.generalInformation), generalInfo);
         adapter.setTopicSelectedListener(this);
         mTopicAdapter.addSection(adapter);
      }

      if (nonLeaves.size() > 0) {
         adapter =
            new TopicListAdapter(mContext,
               mContext.getString(R.string.categories), nonLeaves);
         adapter.setTopicSelectedListener(this);
         mTopicAdapter.addSection(adapter);
      }

      if (leaves.size() > 0) {
         MainApplication app = (MainApplication)getActivity().getApplication();

         adapter = new TopicListAdapter(mContext,
          app.getSiteObjectName(), leaves);
         adapter.setTopicSelectedListener(this);
         mTopicAdapter.addSection(adapter);
      }
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putSerializable(CURRENT_TOPIC, mTopic);
   }

   public void onItemClick(AdapterView<?> adapterView, View view, int position,
      long id) {
      mTopicAdapter.onItemClick(null, view, position, id);
   }

   public void onTopicSelected(TopicNode topic) {
      topicSelectedListener.onTopicSelected(topic);
   }

   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);

      try {
         topicSelectedListener = (TopicSelectedListener) activity;
         mContext = (Context) activity;
      } catch (ClassCastException e) {
         throw new ClassCastException(activity.toString()
            + " must implement TopicSelectedListener");
      }
   }

   private void setTopic(TopicNode topic) {
      mTopic = topic;
      setupTopicAdapter();
      mListView.setAdapter(mTopicAdapter);
   }
}
