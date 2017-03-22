package com.dozuki.ifixit.model.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public interface SearchResult {
   public View buildView(View v, LayoutInflater inflater, ViewGroup container);
   public int getLayout();
   public String getType();
}
