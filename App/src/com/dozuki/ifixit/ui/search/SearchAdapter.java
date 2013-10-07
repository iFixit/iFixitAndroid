package com.dozuki.ifixit.ui.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.dozuki.ifixit.model.search.SearchResult;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends BaseAdapter {

   private List<SearchResult> mResults;

   private LayoutInflater mInflater;

   public SearchAdapter(List<SearchResult> results, Context context) {
      mResults = results;
      mInflater = LayoutInflater.from(context);
   }

   public void setSearchResults(ArrayList<SearchResult> results) {
      mResults = results;
   }

   @Override
   public int getCount() {
      return mResults.size();
   }

   @Override
   public SearchResult getItem(int position) {
      return mResults.get(position);
   }

   @Override
   public long getItemId(int position) {
      return position;
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent) {
      return getItem(position).buildView(convertView, mInflater, parent);
   }
}
