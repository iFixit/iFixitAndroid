package com.dozuki.ifixit.model.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.dozuki.ifixit.R;

public class SearchResult implements Searchable {
   public String mType;

   @Override
   public View buildView(View v, LayoutInflater inflater, ViewGroup container) {
      return null;
   }

   @Override
   public int getLayout() {
      return R.layout.search_row;
   }
}
