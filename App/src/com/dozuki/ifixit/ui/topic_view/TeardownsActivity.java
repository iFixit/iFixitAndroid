package com.dozuki.ifixit.ui.topic_view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.ui.BaseActivity;
import com.dozuki.ifixit.ui.GuideListAdapter;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class TeardownsActivity extends BaseActivity {

   private static final int LIMIT = 200;
   private int OFFSET = 0;

   private static final String GUIDES_KEY = "GUIDES_KEY";
   private ArrayList<GuideInfo> mGuides;
   private GridView mGridView;

   private Context mContext;

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
            GuideInfo guide = mGuides.get(position);
            Intent intent = new Intent(mContext, GuideViewActivity.class);

            intent.putExtra(GuideViewActivity.SAVED_GUIDEID, guide.mGuideid);
            startActivity(intent);
         }
      });

      mGridView.setOnScrollListener(new EndlessScrollListener(mGridView, new EndlessScrollListener.RefreshList() {
         @Override
         public void onRefresh(int pageNumber) {
            OFFSET += LIMIT;

            APIService.call((TeardownsActivity) mContext, APIService.getTeardowns(LIMIT, OFFSET));
         }
      }));

      mGridView.setAdapter(new GuideListAdapter(this, mGuides));
   }

   @Subscribe
   public void onGuides(APIEvent.Guides event) {
      if (!event.hasError()) {
         if (mGuides != null) {
            mGuides.addAll(event.getResult());
         } else {
            mGuides = new ArrayList<GuideInfo>(event.getResult());
         }
         initGridView();
      } else {
         APIService.getErrorDialog(this, event.getError(), null).show();
      }
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      state.putSerializable(GUIDES_KEY, mGuides);
   }

   public static class EndlessScrollListener implements AbsListView.OnScrollListener {

      private GridView gridView;
      private boolean isLoading;
      private boolean hasMorePages;
      private int pageNumber = 0;
      private RefreshList refreshList;
      private boolean isRefreshing;

      public EndlessScrollListener(GridView gridView, RefreshList refreshList) {
         this.gridView = gridView;
         this.isLoading = false;
         this.hasMorePages = true;
         this.refreshList = refreshList;
      }

      @Override
      public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
         if (gridView.getLastVisiblePosition() + 1 == totalItemCount && !isLoading) {
            isLoading = true;
            if (hasMorePages && !isRefreshing) {
               isRefreshing = true;
               refreshList.onRefresh(pageNumber);
            }
         } else {
            isLoading = false;
         }

      }

      @Override
      public void onScrollStateChanged(AbsListView view, int scrollState) {

      }

      public void noMorePages() {
         this.hasMorePages = false;
      }

      public void notifyMorePages() {
         isRefreshing = false;
         pageNumber = pageNumber + 1;
      }

      public interface RefreshList {
         public void onRefresh(int pageNumber);
      }
   }

}
