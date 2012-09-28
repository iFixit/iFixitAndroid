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

   public static Site getSite(String siteName) {
      Site site = null;

      if (siteName.equals("ifixit")) {
         site = new Site(2);
         site.mName = "ifixit";
         site.mDomain = "www.ifixit.com";
         site.mTitle = "iFixit";
         site.mTheme = "custom";
         site.mPublic = true;
         site.mAnswers = true;
         site.mDescription = "iFixit is the free repair manual you can edit." +
          " We sell tools, parts and upgrades for Apple Mac, iPod, iPhone," +
          " iPad, and MacBook as well as game consoles.";
      } else if (siteName.equals("crucial")) {
         site = new Site(549);
         site.mName = "crucial";
         site.mDomain = "crucial.dozuki.com";
         site.mTitle = "Crucial";
         site.mTheme = "white";
         site.mPublic = true;
         site.mAnswers = true;
         site.mDescription = "Free installation guides for Crucial RAM and" +
          " SSD products.";
      }

      return site;
   }
}
