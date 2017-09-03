package com.dozuki.ifixit.model.dozuki;

import android.content.res.Resources;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.BuildConfig;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.model.guide.GuideType;
import com.dozuki.ifixit.util.EditDistance;
import com.dozuki.ifixit.util.Utils;

import java.io.Serializable;
import java.util.ArrayList;

public class Site implements Serializable {
   private static final long serialVersionUID = -2998341267277845644L;

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
   public String mCustomDomain = "";
   public String mStoreUrl;
   public Image mLogo;

   public String mObjectNameSingular;
   public String mObjectNamePlural;
   public String mGoogleOAuth2Clientid;

   public String[] hasSubject = {"Repair", "Installation", "Replacement", "Disassembly"};
   public String[] noSubject = {"Technique", "How-to", "Maintenance", "Teardown"};

   public ArrayList<GuideType> mGuideTypes;
   private boolean mBarcodeScanner = false;
   public boolean mHasTitlePictures = false;

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
      return "https://" + getAPIDomain() + "/Guide/login/openid?host=";
   }

   public boolean checkForGoogleLogin() {
      // Google login is only supported for iFixit for now so we shouldn't
      // check for it or even initialize the GoogleApiClient on any other site.
      return isIfixit();
   }

   public boolean hasGoogleLogin() {
      // We can't support google login in the Dozuki app because the package name is
      // tied to a client id in the same project as the site's project.
      return !(App.isDozukiApp() || mGoogleOAuth2Clientid == null || mGoogleOAuth2Clientid.length() == 0);
   }

   /**
    * Returns the resourceid for the current site's object name.
    */
   public String getObjectName() {
      return mObjectNameSingular;
   }

   public String getObjectNamePlural() {
      return mObjectNamePlural;
   }

   public ArrayList<String> getGuideTypes() {
      ArrayList<String> types = new ArrayList<String>();

      for (GuideType type : mGuideTypes) {
         types.add(Utils.capitalize(type.mType));
      }

      return types;
   }

   public String[] getGuideTypesArray() {
      String[] typesArr = new String[mGuideTypes.size()];

      return getGuideTypes().toArray(typesArr);
   }

   public void setBarcodeScanner(boolean enabled) {
      mBarcodeScanner = enabled;
   }

   public boolean barcodeScanningEnabled() {
      return mBarcodeScanner;
   }

   /**
    * Returns true if the user should be automatically reauthenticated if their
    * auth token expires.
    */
   public boolean reauthenticateOnLogout() {
      return isIfixit();
   }

   public boolean hasSubject(String type) {
      for (String t : hasSubject) {
         if (t.equals(type)) {
            return true;
         }
      }

      return false;
   }

   public String getAPIDomain() {
      String domain;
      if (App.inDebug()) {
         if (isIfixit()) {
            domain = BuildConfig.DEV_SERVER;
         } else {
            domain = mName + "." + BuildConfig.DEV_SERVER;
         }
      } else {
         domain = mDomain;
      }

      return domain;
   }

   /**
    * Returns true if the provided host is for this Site.
    */
   public boolean hostMatches(String host) {
      return mDomain.equals(host) || mCustomDomain.equals(host);
   }

   public int transparentTheme() {
      // If the site has a transparent theme, use that.
      if (isIfixit()) {
         return R.style.Theme_iFixit_TransparentActionBar;
      } else if (isPepsi()) {
         return R.style.Theme_CharlesSmith_TransparentActionBar;
      } else if (isAristocrat()) {
         return R.style.Theme_Aristocrat_TransparentActionBar;
      } else {
         // We don't have a custom theme for the site - check for generic theme.
         if (mTheme.equals("custom")) {
            // Site has a custom theme but we don't have one implemented yet.
            return R.style.Theme_Dozuki_TransparentActionBar;
         } else if (mTheme.equals("green")) {
            return R.style.Theme_Dozuki_Green_TransparentActionBar;
         } else if (mTheme.equals("blue")) {
            return R.style.Theme_iFixit_TransparentActionBar;
         } else if (mTheme.equals("white")) {
            return R.style.Theme_Dozuki_White_TransparentActionBar;
         } else if (mTheme.equals("orange")) {
            return R.style.Theme_Dozuki_Orange_TransparentActionBar;
         } else if (mTheme.equals("black")) {
            return R.style.Theme_Dozuki_Black_TransparentActionBar;
         }
      }

      return R.style.Theme_Base_TransparentActionBar;
   }

   public int theme() {
      // Put custom site themes here.
      if (isIfixit()) {
         return R.style.Theme_iFixit;
      } else if (isAccustream()) {
         return R.style.Theme_Accustream;
      } else if (isDripAssist()) {
         return R.style.Theme_DripAssist;
      } else if (isPVA()) {
         return R.style.Theme_PVA;
      } else if (isOscaro()) {
         return R.style.Theme_Oscaro;
      } else if (isPepsi()) {
         return R.style.Theme_CharlesSmith;
      } else if (isAristocrat()) {
         return R.style.Theme_Aristocrat;
      } else {
         // We don't have a custom theme for the site - check for generic theme.
         if (mTheme.equals("custom")) {
            // Site has a custom theme but we don't have one implemented yet.
            return R.style.Theme_Dozuki;
         } else if (mTheme.equals("green")) {
            return R.style.Theme_Dozuki_Green;
         } else if (mTheme.equals("blue")) {
            return R.style.Theme_iFixit;
         } else if (mTheme.equals("white")) {
            return R.style.Theme_Dozuki_White;
         } else if (mTheme.equals("orange")) {
            return R.style.Theme_Dozuki_Orange;
         } else if (mTheme.equals("black")) {
            return R.style.Theme_Dozuki_Black;
         }
      }

      return R.style.Theme_Dozuki;
   }


   // Used only for custom apps, where we don't have a call to get the site info.
   public static Site getSite(String siteName) {
      Site site = null;
      Resources res = App.get().getResources();

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
         site.mObjectNamePlural = res.getString(R.string.devices);
         site.mObjectNameSingular = res.getString(R.string.device);
      } else if (siteName.equals("aristocrat")) {
         site = new Site(3995);
         site.mName = "aristocrat";
         site.mDomain = "aristocrat.dozuki.com";
         site.mTitle = "Aristocrat Resource Center";
         site.mTheme = "custom";
         site.mPublic = false;
         site.mAnswers = true;
         site.mDescription = "";
         site.mStandardAuth = false;
         site.mBarcodeScanner = false;
         site.mSsoUrl = "http://aristocrat.dozuki.com/Login";
         site.mPublicRegistration = false;
         site.mHasTitlePictures = true;
         site.mObjectNamePlural = res.getString(R.string.categories);
         site.mObjectNameSingular = res.getString(R.string.category);
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
         site.mObjectNamePlural = res.getString(R.string.categories);
         site.mObjectNameSingular = res.getString(R.string.category);
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
         site.mObjectNamePlural = res.getString(R.string.categories);
         site.mObjectNameSingular = res.getString(R.string.category);
      } else if (siteName.equals("accustream")) {
         site = new Site(1145);
         site.mName = "accustream";
         site.mDomain = "accustream.dozuki.com";
         site.mCustomDomain = "assist.hypertherm.com";
         site.mTitle = "Hypertherm Waterjet Mobile Assistant";
         site.mTheme = "white";
         site.mPublic = true;
         site.mAnswers = false;
         site.mDescription = "Hypertherm Waterjet Mobile Assistant provides step-by-step guides for setting up, maintaining, repairing and troubleshooting your waterjet system including the high pressure pump, cutting head, on/off valve, abrasive delivery system and high pressure tubing.  Guides exist for equipment supplied by all major waterjet OEMs: Hypertherm HyPrecision™, KMT, Flow, OMAX and Jet Edge.";
         site.mStandardAuth = true;
         site.mSsoUrl = null;
         site.mPublicRegistration = true;
         site.mBarcodeScanner = true;
         site.mObjectNamePlural = res.getString(R.string.categories);
         site.mObjectNameSingular = res.getString(R.string.category);
      } else if (siteName.equals("pva")) {
         site = new Site(3335);
         site.mName = "pva";
         site.mDomain = "pva.dozuki.com";
         site.mTitle = "PVA Support Hub";
         site.mTheme = "white";
         site.mPublic = true;
         site.mAnswers = false;
         site.mDescription = "Welcome to our electronic support portal. Below you will find an assortment of guides that will lead you step by step through various tasks associated with service, applications, maintenance, etc., for PVA equipment.";
         site.mStandardAuth = true;
         site.mSsoUrl = null;
         site.mPublicRegistration = true;
         site.mObjectNamePlural = res.getString(R.string.categories);
         site.mObjectNameSingular = res.getString(R.string.category);
      } else if (siteName.equals("dripassist")) {
         site = new Site(3366);
         site.mName = "dripassist";
         site.mDomain = "dripassist.dozuki.com";
         site.mTitle = "DripAssist";
         site.mTheme = "white";
         site.mPublic = true;
         site.mAnswers = false;
         site.mDescription = "";
         site.mStandardAuth = true;
         site.mSsoUrl = null;
         site.mPublicRegistration = false;
         site.mObjectNamePlural = res.getString(R.string.categories);
         site.mObjectNameSingular = res.getString(R.string.category);
      } else if (siteName.equals("oscaro")) {
         site = new Site(3293);
         site.mName = "oscaro";
         site.mDomain = "oscaro.dozuki.com";
         site.mCustomDomain = "tutoriels.oscaro.com";
         site.mTitle = "Tutoriels Oscaro.com";
         site.mTheme = "white";
         site.mPublic = true;
         site.mAnswers = false;
         site.mDescription = "Des fiches pratiques en vidéos et photos pour l’entretien et la réparation de votre véhicule. Toutes marques auto, modèles de voitures et tous types de pièces.";
         site.mStandardAuth = true;
         site.mSsoUrl = null;
         site.mPublicRegistration = false;
         site.mObjectNamePlural = res.getString(R.string.categories);
         site.mObjectNameSingular = res.getString(R.string.category);
      } else if (siteName.equals("charlessmith")) {
         site = new Site(3558);
         site.mName = "charlessmith";
         site.mDomain = "charlessmith.dozuki.com";
         site.mTitle = "PepsiCo International";
         site.mTheme = "custom";
         site.mPublic = false;
         site.mAnswers = false;
         site.mDescription = "Operation & Maintenance of Your Postmix Dispenser";
         site.mStandardAuth = true;
         site.mSsoUrl = null;
         site.mPublicRegistration = false;
         site.mObjectNamePlural = res.getString(R.string.categories);
         site.mObjectNameSingular = res.getString(R.string.category);
      }

      return site;
   }

   @Override
   public String toString() {
      return "{" + mSiteid + " | " + mName + " | " + mDomain + " | " + mTitle +
       " | " + mTheme + " | " + mPublic + " | " + mDescription + " | " +
       mAnswers + " | " + mStandardAuth + " | " + mSsoUrl + " | " +
       mPublicRegistration + "}";
   }

   public boolean actionBarUsesIcon() {
      return isAccustream() || isIfixit() || isDripAssist() || isPVA() || isOscaro() || isPepsi() || isAristocrat();
   }

   public boolean isAristocrat() {
      return mName.equals("aristocrat");
   }

   public boolean isPepsi() {
      return mName.equals("charlessmith");
   }

   public boolean isOscaro() {
      return mName.equals("oscaro");
   }

   public boolean isPVA() {
      return mName.equals("pva");
   }

   public boolean isDripAssist() {
      return mName.equals("dripassist");
   }

   public boolean isAccustream() {
      return mName.equals("accustream");
   }

   public boolean isIfixit() {
      return mName.equals("ifixit");
   }

   public boolean isDozuki() {
      return mName.equals("dozuki");
   }

   public Image getLogo() {
      return mLogo;
   }

   public int getGuideListItemOptions(boolean isPublic) {
      if (!isPublic) {
         return isIfixit() ? R.array.guide_list_item_options : R.array.guide_list_item_options_with_delete;
      } else {
         return isIfixit() ? R.array.guide_list_item_options_unpublish : R.array.guide_list_item_options_with_delete_unpublish;
      }
   }
}
