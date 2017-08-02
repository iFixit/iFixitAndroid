package com.dozuki.ifixit.ui.topic;

import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.model.topic.TopicLeaf;
import com.dozuki.ifixit.ui.BaseFragment;
import com.dozuki.ifixit.ui.EndlessRecyclerViewScrollListener;
import com.dozuki.ifixit.ui.GuideListRecyclerAdapter;

import java.util.ArrayList;

public class TopicGuideListFragment extends BaseFragment {

   private static final int LIMIT = 20;
   private static final int OFFSET = 0;
   protected static final String SAVED_TOPIC = "SAVED_TOPIC";
   public static final String TOPIC_LEAF_KEY = "TOPIC_LEAF_KEY";
   private TopicLeaf mTopicLeaf;
   private RecyclerView mRecycleView;
   private GridLayoutManager mLayoutManager;
   private EndlessRecyclerViewScrollListener mScrollListener;
   private GuideListRecyclerAdapter mRecycleAdapter;
   private ArrayList<GuideInfo> mGuides;

   /**
    * Required for restoring fragments
    */
   public TopicGuideListFragment() {}

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
      View view = inflater.inflate(R.layout.topic_guide_list, container, false);

      mRecycleView = (RecyclerView)view.findViewById(R.id.topic_guide_grid);
      ViewCompat.setNestedScrollingEnabled(mRecycleView, false);

      mLayoutManager = new GridLayoutManager(inflater.getContext(), 1);

      mRecycleView.setLayoutManager(mLayoutManager);

      ArrayList<GuideInfo> guides = new ArrayList<>();

      boolean hasFeaturedGuides = mTopicLeaf.getFeaturedGuides().size() > 0;
      if (guides.size() == 0 && hasFeaturedGuides) {
         guides = mTopicLeaf.getFeaturedGuides();
      } else if (hasFeaturedGuides) {
         guides.addAll(mTopicLeaf.getFeaturedGuides());
      }

      guides.addAll(mTopicLeaf.getGuides());

      mRecycleAdapter = new GuideListRecyclerAdapter(guides, false);
      mRecycleView.setAdapter(mRecycleAdapter);

      return view;
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      super.onSaveInstanceState(state);

      state.putSerializable(SAVED_TOPIC, mTopicLeaf);
   }
}
