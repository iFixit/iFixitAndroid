package com.dozuki.ifixit.util;

import java.net.URLEncoder;

import org.json.JSONException;

import android.util.Log;

import com.dozuki.ifixit.dozuki.model.Site;

/**
 * Defines all APIEndpoints.
 */
public enum APIEndpoint {
   CATEGORIES(
      "/api/1.0/categories/",
      false,
      false,
      0,
      "com.dozuki.ifixit.api.categories",
      new ParseResult() {
         public Object parse(String json) throws JSONException {
            return JSONHelper.parseTopics(json);
         }
      }
   ),
   GUIDE(
      new CreateUrl() {
         public String create(String query) {
            return "/api/1.0/guide/" + query;
         }
      },
      false,
      false,
      1,
      "com.dozuki.ifixit.api.guide",
      new ParseResult() {
         public Object parse(String json) throws JSONException {
            return JSONHelper.parseGuide(json);
         }
      }
   ),
   TOPIC(
      new CreateUrl() {
         public String create(String query) {
            try {
               return "/api/1.0/topic/" +
                URLEncoder.encode(query, "UTF-8");
            } catch (Exception e) {
               Log.w("iFixit", "Encoding error: " + e.getMessage());
               return null;
            }
         }
      },
      false,
      false,
      2,
      "com.dozuki.ifixit.api.topic",
      new ParseResult() {
         public Object parse(String json) throws JSONException {
            return JSONHelper.parseTopicLeaf(json);
         }
      }
   ),
   LOGIN(
      "/api/1.0/login/",
      true,
      false,
      3,
      "com.dozuki.ifixit.api.login",
      new ParseResult() {
         public Object parse(String json) throws JSONException {
            return JSONHelper.parseLoginInfo(json);
         }
      }
   ),
   REGISTER(
      "/api/1.0/register/",
      true,
      false,
      4,
      "com.dozuki.ifixit.api.register",
      new ParseResult() {
         public Object parse(String json) throws JSONException {
            return JSONHelper.parseLoginInfo(json);
         }
      }
   ),
   USER_IMAGES(
      "/api/1.0/image/user/",
      false,
      true,
      5,
      "com.dozuki.ifixit.api.user_images",
      new ParseResult() {
         public Object parse(String json) throws JSONException {
            return JSONHelper.parseUserImages(json);
         }
      }
   ),
   UPLOAD_IMAGE(
      "/api/1.0/image/upload/",
      false,
      true,
      6,
      "com.dozuki.ifixit.api.upload_image",
      new ParseResult() {
         public Object parse(String json) throws JSONException {
            return JSONHelper.parseUploadedImageInfo(json);
         }
      }
   ),
   DELETE_IMAGE(
      "/api/1.0/image/delete/",
      false,
      true,
      7,
      "com.dozuki.ifixit.api.delete_image",
      new ParseResult() {
         public Object parse(String json) throws JSONException {
            // TODO: Actually look at the response?
            return "";
         }
      }
   ),
   SITES(
      "/api/1.0/sites?limit=1000",
      false,
      false,
      8,
      "com.dozuki.ifixit.api.sites",
      new ParseResult() {
         public Object parse(String json) throws JSONException {
            return JSONHelper.parseSites(json);
         }
      }
   );

   private interface CreateUrl {
      public String create(String query);
   }

   private interface ParseResult {
      public Object parse(String json) throws JSONException;
   }

   /**
    * The relative URL for the API endpoint.
    */
   private final String mUrl;

   /**
    * Whether or not to use https://.
    */
   public final boolean mHttps;

   /**
    * Whether or not this is an authenticated endpoint.
    */
   public final boolean mAuthenticated;

   /**
    * Integer used to uniquely identify this endpoint.
    */
   public final int mTarget;

   /**
    * Action used for broadcast receivers.
    */
   public final String mAction;

   /**
    * Used for endpoints to create special URLs.
    */
   private final CreateUrl mCreateUrl;

   /**
    * Used to parse JSON results into the expected form.
    */
   private final ParseResult mParseResult;

   private APIEndpoint(String url, boolean https, boolean authenticated,
    int target, String action, ParseResult parseResult) {
      this(url, https, authenticated, target, action, null, parseResult);
   }

   private APIEndpoint(CreateUrl createUrl, boolean https, boolean authenticated,
    int target, String action, ParseResult parseResult) {
      this(null, https, authenticated, target, action, createUrl, parseResult);
   }

   private APIEndpoint(String url, boolean https, boolean authenticated,
    int target, String action, CreateUrl createUrl, ParseResult parseResult) {
      mUrl = url;
      mHttps = https;
      mAuthenticated = authenticated;
      mTarget = target;
      mAction = action;
      mCreateUrl = createUrl;
      mParseResult = parseResult;
   }


   public String getUrl(Site site) {
      return getUrl(site, null);
   }

   public String getUrl(Site site, String query) {
      String domain;
      String protocol;
      String url;

      if (site != null) {
         domain = site.mDomain;
      } else {
         domain = "www.ifixit.com";
      }

      if (mHttps) {
         protocol = "https://";
      } else {
         protocol = "http://";
      }

      if (mCreateUrl != null) {
         url = mCreateUrl.create(query);

         if (url == null) {
            return null;
         }
      } else {
         url = mUrl;
      }

      return protocol + domain + url;
   }

   public Object parseResult(String json) throws JSONException {
      return mParseResult.parse(json);
   }

   public static APIEndpoint getByTarget(int target) {
      for (APIEndpoint endpoint : APIEndpoint.values()) {
         if (endpoint.mTarget == target) {
            return endpoint;
         }
      }

      return null;
   }
}
