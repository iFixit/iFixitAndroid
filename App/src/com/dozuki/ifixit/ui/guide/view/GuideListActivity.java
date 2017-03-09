package com.dozuki.ifixit.ui.guide.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.ui.BaseMenuDrawerActivity;
import com.dozuki.ifixit.ui.EndlessRecyclerViewScrollListener;
import com.dozuki.ifixit.ui.EndlessScrollListener;
import com.dozuki.ifixit.ui.GuideListAdapter;
import com.dozuki.ifixit.ui.GuideListRecyclerAdapter;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.dozuki.ifixit.util.api.Api;

import java.util.ArrayList;

public abstract class GuideListActivity extends BaseMenuDrawerActivity implements GuideListRecyclerAdapter.ItemClickListener {

   private static final int LIMIT = 20;
   private static final String GRID_STATE = "GRID_STATE";
   private int OFFSET = 0;

   private static final String GUIDES_KEY = "GUIDES_KEY";
   private ArrayList<GuideInfo> mGuides;
   private GridView mGridView;

   private EndlessRecyclerViewScrollListener mScrollListener;
   private GuideListAdapter mAdapter;
   private RecyclerView mRecycleView;
   private GridLayoutManager mLayoutManager;
   private GuideListRecyclerAdapter mRecycleAdapter;

   protected abstract int getGuideListTitle();
   protected abstract ApiCall getApiCall(int limit, int offset);

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      super.setDrawerContent(R.layout.guide_list);

      setTitle(getGuideListTitle());

      Parcelable gridState = null;

      if (savedInstanceState != null) {
         mGuides = (ArrayList<GuideInfo>)savedInstanceState.getSerializable(GUIDES_KEY);
         gridState = savedInstanceState.getParcelable(GRID_STATE);
      }

      if (mGuides == null) {
         mGuides = new ArrayList<GuideInfo>();
      }

      initRecycleGridView();

      if (mGuides.size() <= 0) {
         Api.call(this, getApiCall(LIMIT, OFFSET));
         showLoading(R.id.loading_container);
      }
   }

   private void initRecycleGridView() {
      mRecycleView = (RecyclerView)findViewById(R.id.guide_recycler_view);
      mLayoutManager = new GridLayoutManager(this, 1);

      mRecycleView.setLayoutManager(mLayoutManager);

      mScrollListener = new EndlessRecyclerViewScrollListener(mLayoutManager) {
         @Override
         public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
            // Triggered only when new data needs to be appended to the list
            OFFSET += LIMIT;
            Api.call(GuideListActivity.this, getApiCall(LIMIT, OFFSET));
         }
      };

      mRecycleView.addOnScrollListener(mScrollListener);

      mRecycleAdapter = new GuideListRecyclerAdapter(mGuides, false);
      mRecycleAdapter.setClickListener(this);

      mRecycleView.setAdapter(mRecycleAdapter);
   }

   protected void setGuides(ApiEvent.Guides event) {
      hideLoading();
      if (!event.hasError()) {
         ArrayList<GuideInfo> guides = event.getResult();

         Log.i("API", "Number of new Guides: " + guides.size());
         if (guides.size() > 0) {
            mGuides.addAll(guides);
         }

         Log.i("API", "Number of Total Guides: " + mGuides.size());

         mRecycleAdapter.setGuides(mGuides);
         mRecycleAdapter.notifyDataSetChanged();
      } else {
         Api.getErrorDialog(this, event).show();
      }
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      super.onSaveInstanceState(state);

      if (mGridView != null)
         state.putParcelable(GRID_STATE, mGridView.onSaveInstanceState());

      if (mGuides != null)
         state.putSerializable(GUIDES_KEY, mGuides);
   }

   @Override
   public void onItemClick(View view, int position) {
      GuideInfo guide = (GuideInfo)mAdapter.getItem(position);
      Intent intent = new Intent(GuideListActivity.this, GuideViewActivity.class);

      intent.putExtra(GuideViewActivity.GUIDEID, guide.mGuideid);
      startActivity(intent);
   }
}
