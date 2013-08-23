package com.dozuki.ifixit.ui.guide.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.ui.BaseMenuDrawerActivity;
import com.dozuki.ifixit.ui.EndlessScrollListener;
import com.dozuki.ifixit.ui.GuideListAdapter;
import com.dozuki.ifixit.util.APICall;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;

import java.util.ArrayList;

public abstract class GuideListActivity extends BaseMenuDrawerActivity {

   private static final int LIMIT = 20;
   private static final String GRID_STATE = "GRID_STATE";
   private int OFFSET = 0;

   private static final String GUIDES_KEY = "GUIDES_KEY";
   private ArrayList<GuideInfo> mGuides;
   private GridView mGridView;

   private EndlessScrollListener mScrollListener;
   private GuideListAdapter mAdapter;

   protected abstract int getGuideListTitle();
   protected abstract APICall getApiCall(int limit, int offset);

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setTitle(getGuideListTitle());

      setContentView(R.layout.guide_list);

      Parcelable gridState = null;

      if (savedInstanceState != null) {
         mGuides = (ArrayList<GuideInfo>)savedInstanceState.getSerializable(GUIDES_KEY);
         gridState = savedInstanceState.getParcelable(GRID_STATE);
      }

      if (mGuides != null) {
         initGridView(gridState);
      } else {
         APIService.call(this, getApiCall(LIMIT, OFFSET));
         showLoading(R.id.loading_container);
      }
   }

   private void initGridView(Parcelable state) {
      mGridView = (GridView)findViewById(R.id.guide_grid);
      mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> arg0, View view, int position,
          long id) {
            GuideInfo guide = (GuideInfo)mAdapter.getItem(position);
            Intent intent = new Intent(GuideListActivity.this, GuideViewActivity.class);

            intent.putExtra(GuideViewActivity.GUIDEID, guide.mGuideid);
            startActivity(intent);
         }
      });

      mScrollListener = new EndlessScrollListener(mGridView, new EndlessScrollListener.RefreshList() {
         @Override
         public void onRefresh(int pageNumber) {
            OFFSET += LIMIT;

            APIService.call(GuideListActivity.this, getApiCall(LIMIT, OFFSET));
         }
      });

      mGridView.setOnScrollListener(mScrollListener);
      mAdapter = new GuideListAdapter(this, mGuides, false);
      mGridView.setAdapter(mAdapter);
      if (state != null) {
         mGridView.onRestoreInstanceState(state);
      }
   }

   protected void setGuides(APIEvent.Guides event) {
      hideLoading();

      if (!event.hasError()) {
         if (mGuides != null) {
            ArrayList<GuideInfo> guides = event.getResult();
            if (guides.size() > 0) {
               mGuides.addAll(guides);
               mAdapter.addGuides(guides);
               mAdapter.notifyDataSetChanged();

               mScrollListener.notifyMorePages();
            } else {
               mScrollListener.noMorePages();
            }

         } else {
            mGuides = new ArrayList<GuideInfo>(event.getResult());
         }

         if (mGridView == null)
            initGridView(null);
      } else {
         APIService.getErrorDialog(this, event).show();
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
}
