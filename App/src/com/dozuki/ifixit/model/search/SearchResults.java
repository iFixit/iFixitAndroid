package com.dozuki.ifixit.model.search;

import android.support.v7.util.SortedList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class SearchResults implements Serializable {
   private static final long serialVersionUID = -3423222443335L;

   public String mQuery;
   public int mLimit;
   public int mOffset;
   public int mTotalResults;
   public boolean mHasMoreResults;
   public ArrayList<SearchResult> mResults = new ArrayList<>();

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      SearchResults that = (SearchResults) o;

      if (mLimit != that.mLimit) return false;
      if (mOffset != that.mOffset) return false;
      if (mTotalResults != that.mTotalResults) return false;
      if (mHasMoreResults != that.mHasMoreResults) return false;
      if (!mQuery.equals(that.mQuery)) return false;
      return mResults.equals(that.mResults);
   }

   @Override
   public int hashCode() {
      int result = mQuery.hashCode();
      result = 31 * result + mLimit;
      result = 31 * result + mOffset;
      result = 31 * result + mTotalResults;
      result = 31 * result + (mHasMoreResults ? 1 : 0);
      result = 31 * result + mResults.hashCode();
      return result;
   }
}
