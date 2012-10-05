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
      "com.dozuki.ifixit.api.register",
      new ParseResult() {
         public Object parse(String json) throws JSONException {
            return JSONHelper.parseLoginInfo(json);
         }
      }
   ),

   USER_IMAGES(
      new CreateUrl() {
         public String create(String query) {
            return "/api/1.0/image/user" + query;
         }
      },
      false,
      true,
      "com.dozuki.ifixit.api.user_images",
      new ParseResult() {
         public Object parse(String json) throws JSONException {
            return JSONHelper.parseUserImages(json);
         }
      }
   ),

   UPLOAD_IMAGE(
      new CreateUrl() {
         public String create(String query) {
            return "/api/1.0/image/upload?file=" + query;
         }
      },
      false,
      true,
      "com.dozuki.ifixit.api.upload_image",
      new ParseResult() {
         public Object parse(String json) throws JSONException {
            return JSONHelper.parseUploadedImageInfo(json);
         }
      }
   ),

   DELETE_IMAGE(
      new CreateUrl() {
         public String create(String query) {
            return "/api/1.0/image/delete" + query;
         }
      },
      false,
      true,
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
    String action, ParseResult parseResult) {
      this(url, https, authenticated, action, null, parseResult);
   }

   private APIEndpoint(CreateUrl createUrl, boolean https, boolean authenticated,
    String action, ParseResult parseResult) {
      this(null, https, authenticated, action, createUrl, parseResult);
   }

   private APIEndpoint(String url, boolean https, boolean authenticated,
    String action, CreateUrl createUrl, ParseResult parseResult) {
      mUrl = url;
      mHttps = https;
      mAuthenticated = authenticated;
      mAction = action;
      mCreateUrl = createUrl;
      mParseResult = parseResult;
   }

   /**
    * Returns a unique integer for this endpoint.
    *
    * This value is passed around in Intents to identify what request is being
    * made. It will also be used in the database to store results.
    */
   protected int getTarget() {
      /**
       * Just use the enum's unique integer that auto increments from 0.
       */
      return ordinal();
   }

   public String getUrl(Site site) {
      return getUrl(site, null);
   }

   /**
    * Returns an absolute URL for this endpoint for the given site and query.
    */
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
         if (endpoint.ordinal() == target) {
            return endpoint;
         }
      }

      return null;
   }

   public String toString() {
      return mUrl + " | " + mHttps + " | " + mAuthenticated + " | " +
       ordinal() + " | " + mAction;
   }
}
