package com.dozuki.ifixit.model.search;

import java.io.Serializable;
import java.util.ArrayList;

public class Search implements Serializable {
   private static final long serialVersionUID = -3423222443335L;

   public String mQuery;
   public int mLimit;
   public int mOffset;
   public int mTotalResults;
   public boolean mHasMoreResults;
   public ArrayList<Searchable> mResults;

   public Search() {
      mResults = new ArrayList<Searchable>();
   }
}
