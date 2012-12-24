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
            return "/api/1.0/categories/";
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
      false,
      false
   ),

   GUIDE(
      new Endpoint() {
         public String createUrl(String query) {
            return "/api/1.0/guide/" + query;
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
      false,
      false
   ),

   TOPIC(
      new Endpoint() {
         public String createUrl(String query) {
            try {
               return "/api/1.0/topic/" + URLEncoder.encode(query, "UTF-8");
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
      false,
      false
   ),

   LOGIN(
      new Endpoint() {
         public String createUrl(String query) {
            return "/api/1.0/login/";
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
      true,
      true
   ),

   REGISTER(
      new Endpoint() {
         public String createUrl(String query) {
            return "/api/1.0/register/";
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
      true,
      true
   ),

   USER_IMAGES(
      new Endpoint() {
         public String createUrl(String query) {
            return "/api/1.0/image/user" + query;
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
      true,
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

            return "/api/1.0/image/upload?file=" + fileName;
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
      true,
      false
   ),

   DELETE_IMAGE(
      new Endpoint() {
         public String createUrl(String query) {
            return "/api/1.0/image/delete" + query;
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
      true,
      false
   ),

   SITES(
      new Endpoint() {
         public String createUrl(String query) {
            return "/api/1.0/sites?limit=1000";
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
      false,
      false
   );

   private interface Endpoint {
      public String createUrl(String query);
      public APIEvent<?> parse(String json) throws JSONException;
      public APIEvent<?> getEvent();
   }

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
    * True if this endpoint should always perform POST requests.
    */
   public final boolean mPost;

   /**
    * True if endpoint must be public. This is primarily for login and register so
    * users can actually log in without being prompted for login repeatedly.
    */
   public final boolean mForcePublic;

   private APIEndpoint(Endpoint endpoint, boolean https, boolean authenticated,
    boolean post, boolean forcePublic) {
      mEndpoint = endpoint;
      mHttps = https;
      mAuthenticated = authenticated;
      mPost = post;
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

      url = mEndpoint.createUrl(query);

      if (url == null) {
         return null;
      }

      return protocol + domain + url;
   }

   public APIEvent<?> parseResult(String json) throws JSONException {
      return mEndpoint.parse(json).setResponse(json);
   }

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
