package com.dozuki.ifixit.ui.topic_view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.ui.BaseActivity;
import com.dozuki.ifixit.ui.EndlessScrollListener;
import com.dozuki.ifixit.ui.GuideListAdapter;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class TeardownsActivity extends BaseActivity {

   private static final int LIMIT = 20;
   private int OFFSET = 0;

   private static final String GUIDES_KEY = "GUIDES_KEY";
   private ArrayList<GuideInfo> mGuides;
   private GridView mGridView;

   private Context mContext;
   private EndlessScrollListener mScrollListener;
   private GuideListAdapter mAdapter;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setTitle(getString(R.string.teardowns));

      mContext = this;

      setContentView(R.layout.teardowns);

      if (savedInstanceState != null) {
         mGuides = (ArrayList<GuideInfo>) savedInstanceState.getSerializable(GUIDES_KEY);
         initGridView();
      } else {
         APIService.call(this, APIService.getTeardowns(LIMIT, OFFSET));
      }
   }

   private void initGridView() {
      mGridView = (GridView) findViewById(R.id.guide_grid);
      mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> arg0, View view, int position,
          long id) {
            GuideInfo guide = (GuideInfo)mAdapter.getItem(position);
            Intent intent = new Intent(mContext, GuideViewActivity.class);

            intent.putExtra(GuideViewActivity.SAVED_GUIDEID, guide.mGuideid);
            startActivity(intent);
         }
      });

      mScrollListener = new EndlessScrollListener(mGridView, new EndlessScrollListener.RefreshList() {
         @Override
         public void onRefresh(int pageNumber) {
            OFFSET += LIMIT;

            APIService.call((TeardownsActivity) mContext, APIService.getTeardowns(LIMIT, OFFSET));
         }
      });

      mGridView.setOnScrollListener(mScrollListener);
      mAdapter = new GuideListAdapter(this, mGuides);
      mGridView.setAdapter(mAdapter);
   }

   @Subscribe
   public void onGuides(APIEvent.Guides event) {
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
            initGridView();
      } else {
         APIService.getErrorDialog(this, event.getError(), null).show();
      }
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      state.putSerializable(GUIDES_KEY, mGuides);
   }
}
