package com.dozuki.ifixit.ui.topic;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.model.topic.TopicLeaf;
import com.dozuki.ifixit.ui.BaseFragment;
import com.dozuki.ifixit.ui.EndlessRecyclerViewScrollListener;
import com.dozuki.ifixit.ui.GuideListAdapter;
import com.dozuki.ifixit.ui.GuideListRecyclerAdapter;
import com.dozuki.ifixit.ui.guide.view.GuideListActivity;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.util.api.Api;

import java.util.ArrayList;

public class TopicGuideListFragment extends BaseFragment implements GuideListRecyclerAdapter.ItemClickListener {

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
      mLayoutManager = new GridLayoutManager(inflater.getContext(), 1);

      mRecycleView.setLayoutManager(mLayoutManager);

      mRecycleAdapter = new GuideListRecyclerAdapter(mTopicLeaf.getGuides(), false);
      mRecycleAdapter.setClickListener(this);

      mRecycleView.setAdapter(mRecycleAdapter);

      return view;
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      super.onSaveInstanceState(state);

      state.putSerializable(SAVED_TOPIC, mTopicLeaf);
   }

   @Override
   public void onItemClick(View view, int position) {
      GuideInfo guide = mTopicLeaf.getGuides().get(position);
      Intent intent = new Intent(getContext(), GuideViewActivity.class);
      intent.putExtra(GuideViewActivity.GUIDEID, guide.mGuideid);
      startActivity(intent);
   }
}
