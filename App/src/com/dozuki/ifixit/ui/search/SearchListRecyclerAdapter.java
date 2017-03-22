package com.dozuki.ifixit.ui.search;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.dozuki.ifixit.model.search.SearchResult;

import java.util.Comparator;
import java.util.List;

public class SearchListRecyclerAdapter extends RecyclerView.Adapter<SearchListRecyclerAdapter.ViewHolder> {

   private final Comparator<SearchResult> mComparator;
   private final LayoutInflater mInflator;
   private SortedList<SearchResult> mResults = new SortedList<>(SearchResult.class, new SortedList.Callback<SearchResult>() {
      @Override
      public int compare(SearchResult a, SearchResult b) {
         return mComparator.compare(a, b);
      }

      @Override
      public void onInserted(int position, int count) {
         notifyItemRangeInserted(position, count);
      }

      @Override
      public void onRemoved(int position, int count) {
         notifyItemRangeRemoved(position, count);
      }

      @Override
      public void onMoved(int fromPosition, int toPosition) {
         notifyItemMoved(fromPosition, toPosition);
      }

      @Override
      public void onChanged(int position, int count) {
         notifyItemRangeChanged(position, count);
      }

      @Override
      public boolean areContentsTheSame(SearchResult oldItem, SearchResult newItem) {
         return oldItem.equals(newItem);
      }

      @Override
      public boolean areItemsTheSame(SearchResult item1, SearchResult item2) {
         return item1 == item2;
      }
   });

   public SearchListRecyclerAdapter(Context context) {
      mInflator = LayoutInflater.from(context);

      mComparator = new Comparator<SearchResult>() {
         @Override
         public int compare(SearchResult o1, SearchResult o2) {
            return o1.getType().compareTo(o2.getType());
         }
      };
   }

   @Override
   public int getItemViewType(int position) {
      return mResults.get(position).getLayout();
   }

   @Override
   public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View v = mInflator.inflate(viewType, parent, false);
      ViewHolder vh = new ViewHolder(v, parent);
      return vh;
   }

   @Override
   public void onBindViewHolder(ViewHolder holder, int position) {
      holder.setItem(mResults.get(position));
   }

   @Override
   public int getItemCount() {
      return mResults.size();
   }

   public void add(SearchResult result) {
      mResults.add(result);
   }

   public void remove(SearchResult result) {
      mResults.remove(result);
   }

   public void add(List<SearchResult> results) {
      mResults.addAll(results);
   }

   public void remove(List<SearchResult> results) {
      mResults.beginBatchedUpdates();
      for (SearchResult result : results) {
         mResults.remove(result);
      }
      mResults.endBatchedUpdates();
   }

   public void clear() {
      mResults.clear();
   }

   public void replaceAll(List<SearchResult> results) {
      mResults.beginBatchedUpdates();
      for (int i = mResults.size() - 1; i >= 0; i--) {
         final SearchResult result = mResults.get(i);
         if (!results.contains(result)) {
            mResults.remove(result);
         }
      }
      mResults.addAll(results);
      mResults.endBatchedUpdates();
   }

   public SearchResult getItem(int position) {
      return mResults.get(position);
   }

   public static class ViewHolder extends RecyclerView.ViewHolder {
      private LayoutInflater mInflator;
      private ViewGroup mParent;
      private View mItemView;

      public ViewHolder(View v, ViewGroup parent) {
         super(v);
         mItemView = v;
         mParent = parent;
         mInflator = LayoutInflater.from(parent.getContext());
      }

      public void setItem(SearchResult result) {
         result.buildView(mItemView, mInflator, mParent);
      }
   }
}
