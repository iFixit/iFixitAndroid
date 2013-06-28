package com.dozuki.ifixit.ui;

import android.widget.AbsListView;
import android.widget.GridView;

public class EndlessScrollListener implements AbsListView.OnScrollListener {

   private GridView gridView;
   private boolean isLoading;
   private boolean hasMorePages;
   private int pageNumber = 0;
   private RefreshList refreshList;


   private boolean isRefreshing;

   public EndlessScrollListener(GridView gridView, RefreshList refreshList) {
      this.gridView = gridView;
      this.isRefreshing = false;
      this.isLoading = false;
      this.hasMorePages = true;
      this.refreshList = refreshList;
   }

   @Override
   public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
      // List hasn't been populated yet, don't do anything
      if (totalItemCount == 0) return;

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
   public void onScrollStateChanged(AbsListView view, int scrollState) { }

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
