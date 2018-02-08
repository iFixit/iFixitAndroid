package com.dozuki.ifixit.ui.topic;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.model.topic.TopicLeaf;
import com.dozuki.ifixit.ui.BaseFragment;
import com.dozuki.ifixit.ui.EndlessRecyclerViewScrollListener;
import com.dozuki.ifixit.ui.GuideListRecyclerAdapter;
import com.dozuki.ifixit.ui.WikiListRecyclerAdapter;

import java.util.ArrayList;

public class TopicRelatedWikisFragment extends BaseFragment {

   private static final int LIMIT = 20;
   private static final int OFFSET = 0;
   protected static final String SAVED_TOPIC = "SAVED_TOPIC";
   public static final String TOPIC_LEAF_KEY = "TOPIC_LEAF_KEY";
   private TopicLeaf mTopicLeaf;
   private RecyclerView mRecycleView;
   private GridLayoutManager mLayoutManager;
   private EndlessRecyclerViewScrollListener mScrollListener;
   private WikiListRecyclerAdapter mRecycleAdapter;

   /**
    * Required for restoring fragments
    */
   public TopicRelatedWikisFragment() {}

   @Override
   public void onCreate(Bundle savedState) {
      super.onCreate(savedState);

      mTopicLeaf = (TopicLeaf)this.getArguments().getSerializable(TOPIC_LEAF_KEY);

      if (savedState != null && mTopicLeaf == null) {
         mTopicLeaf = (TopicLeaf)savedState.getSerializable(SAVED_TOPIC);
      }
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.topic_wiki_list, container, false);

      mRecycleView = (RecyclerView)view.findViewById(R.id.topic_wiki_grid);
      mLayoutManager = new GridLayoutManager(inflater.getContext(), 1);
      mRecycleView.setLayoutManager(mLayoutManager);
      mRecycleAdapter = new WikiListRecyclerAdapter(getContext(), mTopicLeaf.getRelatedWikis(), false);
      mRecycleView.setAdapter(mRecycleAdapter);

      return view;
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      super.onSaveInstanceState(state);

      state.putSerializable(SAVED_TOPIC, mTopicLeaf);
   }
}
