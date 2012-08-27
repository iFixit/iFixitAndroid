package com.dozuki.ifixit.dozuki.model;

import java.io.Serializable;

public class Site implements Serializable {
   private static final long serialVersionUID = -2798641261277805693L;

   public int mSiteid;
   public String mName;
   public String mDomain;
   public String mTitle;
   public String mTheme; // change to enum?
   public boolean mPublic;
   public boolean mAnswers;
   public String mDescription;

   public Site(int siteid) {
      mSiteid = siteid;
   }

   public String toString() {
      return "{" + mSiteid + " | " + mName + " | " + mDomain + " | " + mTitle +
       " | " + mTheme + " | " + mPublic + " | " + mDescription + " | " +
       mAnswers + "}";
   }
}
