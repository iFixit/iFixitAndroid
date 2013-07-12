package com.dozuki.ifixit.model.dozuki;

import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.GuideType;
import com.dozuki.ifixit.util.EditDistance;

import java.io.Serializable;
import java.util.ArrayList;

public class Site implements Serializable {
   private static final long serialVersionUID = -2798641261277805693L;

   private static final String DEV_SERVER = "";

   public int mSiteid;
   public String mName;
   public String mDomain;
   public String mTitle;
   public String mTheme; // change to enum?
   public boolean mPublic;
   public boolean mAnswers;
   public String mDescription;
   public boolean mStandardAuth;
   public String mSsoUrl;
   public boolean mPublicRegistration;
   public String mCustomDomain;
   public String mStoreUrl;

   public ArrayList<GuideType> mGuideTypes;

   public Site(int siteid) {
      mSiteid = siteid;
   }

   public boolean search(String query) {
      if (mTitle.toLowerCase().contains(query)) {
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

   public String getOpenIdLoginUrl() {
      return "https://" + mDomain + "/Guide/login/openid?host=";
   }

   /**
    * Returns the resourceid for the current site's object name.
    */
   public int getObjectName() {
      if (mName.equals("ifixit")) {
         return R.string.devices;
      } else {
         return R.string.topics;
      }
   }

   public ArrayList<String> getGuideTypes() {
      ArrayList<String> types = new ArrayList<String>();

      for (GuideType type : mGuideTypes) {
         types.add(capitalize(type.mType));
      }

      return types;
   }

   public String siteName() {
      return capitalize(mName);
   }

   public String getAPIDomain() {
      String domain;
      if (MainApplication.inDebug()) {
         if (mName.equals("ifixit")) {
            domain = DEV_SERVER;
         } else {
            domain = mName + "." + DEV_SERVER;
         }
      } else {
         domain = mDomain;
      }

      return domain;
   }

   public int theme() {
      // Put custom site themes here.
      if (mName.equals("ifixit")) {
         return R.style.Theme_iFixit;
      } else {
         // We don't have a custom theme for the site - check for generic theme.
         if (mTheme.equals("custom")) {
            // Site has a custom theme but we don't have one implemented yet.
            return R.style.Theme_Dozuki;
         } else if (mTheme.equals("green")) {
            return R.style.Theme_Dozuki_Green;
         } else if (mTheme.equals("blue")) {
            return R.style.Theme_Dozuki_Blue;
         } else if (mTheme.equals("white")) {
            return R.style.Theme_Dozuki_White;
         } else if (mTheme.equals("orange")) {
            return R.style.Theme_Dozuki_Orange;
         } else if (mTheme.equals("black")) {
            return R.style.Theme_Dozuki_Grey;
         }
      }

      return R.style.Theme_Dozuki;
   }

   // Used only for custom apps, where we don't have a call to get the site info.
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
         site.mStandardAuth = true;
         site.mSsoUrl = null;
         site.mPublicRegistration = true;
      } else if (siteName.equals("dozuki")) {
         site = new Site(5);
         site.mName = "dozuki";
         site.mDomain = "www.dozuki.com";
         site.mTitle = "Dozuki";
         site.mTheme = "custom";
         site.mPublic = true;
         site.mAnswers = true;
         site.mDescription = "Using the Dozuki platform: How-to guides and other useful information.";
         site.mStandardAuth = true;
         site.mSsoUrl = null;
         site.mPublicRegistration = true;
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
         site.mStandardAuth = true;
         site.mSsoUrl = null;
         site.mPublicRegistration = false;
      }

      return site;
   }

   @Override
   public String toString() {
      return "{" + mSiteid + " | " + mName + " | " + mDomain + " | " + mTitle +
       " | " + mTheme + " | " + mPublic + " | " + mDescription + " | " +
       mAnswers + " | " + mStandardAuth + " | " + mSsoUrl + " | " +
       mPublicRegistration + "|" + mGuideTypes.toString() + "}";
   }

   private String capitalize(String word) {
      return Character.toUpperCase(word.charAt(0)) + word.substring(1);
   }
}
