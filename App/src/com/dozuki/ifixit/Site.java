package com.dozuki.ifixit;

import java.io.Serializable;

public class Site implements Serializable {
   private static final long serialVersionUID = -2798641261277805693L;

   protected int mSiteid;
   protected String mName;
   protected String mDomain;
   protected String mTitle;
   protected String mTheme; // change to enum?
   protected boolean mPublic;
   protected String mDescription;

   public Site(int siteid) {
      mSiteid = siteid;
   }

   public String toString() {
      return "{" + mSiteid + " | " + mName + " | " + mDomain + " | " + mTitle +
       " | " + mTheme + " | " + mPublic + " | " + mDescription + "}";
   }
}
