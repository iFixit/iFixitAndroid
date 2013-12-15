package com.dozuki.ifixit.model.search;

import java.io.Serializable;
import java.util.ArrayList;

public class SearchResults implements Serializable {
   private static final long serialVersionUID = -3423222443335L;

   public String mQuery;
   public int mLimit;
   public int mOffset;
   public int mTotalResults;
   public boolean mHasMoreResults;
   public ArrayList<SearchResult> mResults;

   public SearchResults() {
      mResults = new ArrayList<SearchResult>();
   }
}
