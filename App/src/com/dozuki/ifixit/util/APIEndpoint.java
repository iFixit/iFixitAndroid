package com.dozuki.ifixit.util;

import android.util.Log;

import com.dozuki.ifixit.dozuki.model.Site;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Defines all APIEndpoints.
 */
public enum APIEndpoint {
   CATEGORIES(
      new Endpoint() {
         public String createUrl(String query) {
            return "categories/";
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.Categories().setResult(JSONHelper.parseTopics(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.Categories();
         }
      },
      false,
      false,
      "GET",
      false
   ),

   GUIDE(
      new Endpoint() {
         public String createUrl(String query) {
            return "guides/" + query;
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.Guide().setResult(JSONHelper.parseGuide(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.Guide();
         }
      },
      false,
      false,
      "GET",
      false
   ),

   TOPIC(
      new Endpoint() {
         public String createUrl(String query) {
            try {
               return "topics/" + URLEncoder.encode(query, "UTF-8");
            } catch (Exception e) {
               Log.w("iFixit", "Encoding error: " + e.getMessage());
               return null;
            }
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.Topic().setResult(JSONHelper.parseTopicLeaf(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.Topic();
         }
      },
      false,
      false,
      "GET",
      false
   ),

   LOGIN(
      new Endpoint() {
         public String createUrl(String query) {
            return "login/";
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.Login().setResult(JSONHelper.parseLoginInfo(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.Login();
         }
      },
      true,
      false,
      "POST",
      true
   ),

   REGISTER(
      new Endpoint() {
         public String createUrl(String query) {
            return "register/";
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.Register().setResult(JSONHelper.parseLoginInfo(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.Register();
         }
      },
      true,
      false,
      "POST",
      true
   ),

   USER_IMAGES(
      new Endpoint() {
         public String createUrl(String query) {
            return "user/media/images" + query;
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.UserImages().setResult(JSONHelper.parseUserImages(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.UserImages();
         }
      },
      false,
      true,
      "GET",
      false
   ),

   UPLOAD_IMAGE(
      new Endpoint() {
         public String createUrl(String query) {
            String fileName;

            try {
               fileName = URLEncoder.encode(getFileNameFromFilePath(query), "UTF-8");
            } catch (UnsupportedEncodingException e) {
               // Provide a default file name.
               fileName = "uploaded_image.jpg";
            }

            return "user/media/images?file=" + fileName;
         }

         private String getFileNameFromFilePath(String filePath) {
            int index = filePath.lastIndexOf('/');

            if (index == -1) {
               /**
                * filePath doesn't have a '/' in it. That's weird but just
                * return the whole file path.
                */
               return filePath;
            }

            return filePath.substring(index + 1);
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.UploadImage().setResult(JSONHelper.parseUploadedImageInfo(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.UploadImage();
         }
      },
      false,
      true,
      "POST",
      false
   ),

   DELETE_IMAGE(
      new Endpoint() {
         public String createUrl(String query) {
            return "user/media/images" + query;
         }

         public APIEvent<?> parse(String json) throws JSONException {
            // TODO: Actually look at the response?
            return new APIEvent.DeleteImage().setResult("");
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.DeleteImage();
         }
      },
      false,
      true,
      "DELETE",
      false
   ),

   SITES(
      new Endpoint() {
         public String createUrl(String query) {
            return "sites?limit=1000";
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.Sites().setResult(JSONHelper.parseSites(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.Sites();
         }
      },
      false,
      false,
      "GET",
      false
   ),

   USER_INFO(
      new Endpoint() {
         public String createUrl(String query) {
            return "user";
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.UserInfo().setResult(JSONHelper.parseLoginInfo(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.UserInfo();
         }
      },
      false,
      false,
      "GET",
      false
   );

   /**
    * Current version of the API to use.
    */
   private static final String API_VERSION = "1.1";

   /**
    * Defines various methods that each endpoint must provide.
    */
   private interface Endpoint {
      /**
       * Returns the end of a URL that defines this endpoint.
       *
       * The full URL is then: protocol + domain + "/api/" + api version + "/" + createUrl
       */
      public String createUrl(String query);

      /**
       * Returns an APIEvent given the JSON response of the request.
       */
      public APIEvent<?> parse(String json) throws JSONException;

      /**
       * Returns an empty APIEvent that is used for events for this endpoint.
       *
       * This is typically used to put errors into so they still get to the right place.
       */
      public APIEvent<?> getEvent();
   }

   /**
    * Endpoint's functionality.
    */
   private final Endpoint mEndpoint;

   /**
    * Whether or not to use https://.
    */
   public final boolean mHttps;

   /**
    * Whether or not this is an authenticated endpoint. If true, sends the
    * user's sessionid along in a cookie.
    */
   public final boolean mAuthenticated;

   /**
    * Request method for this endpoint e.g. GET, POST, DELETE
    */
   public final String mMethod;

   /**
    * True if endpoint must be public. This is primarily for login and register so
    * users can actually log in without being prompted for login repeatedly.
    */
   public final boolean mForcePublic;

   private APIEndpoint(Endpoint endpoint, boolean https, boolean authenticated,
    String method, boolean forcePublic) {
      mEndpoint = endpoint;
      mHttps = https;
      mAuthenticated = authenticated;
      mMethod = method;
      mForcePublic = forcePublic;
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

      url = "/api/" + API_VERSION + "/" + mEndpoint.createUrl(query);

      return protocol + domain + url;
   }

   public APIEvent<?> parseResult(String json) throws JSONException {
      return mEndpoint.parse(json).setResponse(json);
   }

   /**
    * Returns a "plain" event that is the correct type for this endpoint.
    */
   public APIEvent<?> getEvent() {
      return mEndpoint.getEvent();
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
      return mHttps + " | " + mAuthenticated + " | " + ordinal();
   }
}
