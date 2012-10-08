package com.dozuki.ifixit.dozuki.model;

import java.io.Serializable;

import com.dozuki.ifixit.util.EditDistance;

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

   public boolean search(String query) {
      if (mName.indexOf(query) != -1 ||
       mTitle.toLowerCase().indexOf(query) != -1) {
         // Query is somewhere in title or name.
         return true;
      }

      /**
       * Compare edit distance with the length of the string. This is kinda
       * arbitrary but makes sense because we want more room for error the
       * longer the string and less room for error the shorter the string.
       */
      return EditDistance.editDistance(mName, query) <= (mName.length() / 2);
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
