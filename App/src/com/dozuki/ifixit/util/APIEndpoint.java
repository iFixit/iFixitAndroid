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
      "/api/1.0/categories/",
      false,
      false,
      false,
      new ParseResult() {
         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.Categories().setResult(JSONHelper.parseTopics(json));
         }
      },
      new GetEvent() {
         public APIEvent<?> getEvent() {
            return new APIEvent.Categories();
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
      false,
      new ParseResult() {
         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.Guide().setResult(JSONHelper.parseGuide(json));
         }
      },
      new GetEvent() {
         public APIEvent<?> getEvent() {
            return new APIEvent.Guide();
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
      false,
      new ParseResult() {
         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.Topic().setResult(JSONHelper.parseTopicLeaf(json));
         }
      },
      new GetEvent() {
         public APIEvent<?> getEvent() {
            return new APIEvent.Topic();
         }
      }
   ),

   LOGIN(
      "/api/1.0/login/",
      true,
      false,
      true,
      new ParseResult() {
         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.Login().setResult(JSONHelper.parseLoginInfo(json));
         }
      },
      new GetEvent() {
         public APIEvent<?> getEvent() {
            return new APIEvent.Login();
         }
      }
   ),

   REGISTER(
      "/api/1.0/register/",
      true,
      false,
      true,
      new ParseResult() {
         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.Register().setResult(JSONHelper.parseLoginInfo(json));
         }
      },
      new GetEvent() {
         public APIEvent<?> getEvent() {
            return new APIEvent.Register();
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
      true,
      new ParseResult() {
         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.UserImages().setResult(JSONHelper.parseUserImages(json));
         }
      },
      new GetEvent() {
         public APIEvent<?> getEvent() {
            return new APIEvent.UserImages();
         }
      }
   ),

   UPLOAD_IMAGE(
      new CreateUrl() {
         public String create(String query) {
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
      },
      false,
      true,
      true,
      new ParseResult() {
         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.UploadImage().setResult(JSONHelper.parseUploadedImageInfo(json));
         }
      },
      new GetEvent() {
         public APIEvent<?> getEvent() {
            return new APIEvent.UploadImage();
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
      true,
      new ParseResult() {
         public APIEvent<?> parse(String json) throws JSONException {
            // TODO: Actually look at the response?
            return new APIEvent.DeleteImage().setResult("");
         }
      },
      new GetEvent() {
         public APIEvent<?> getEvent() {
            return new APIEvent.DeleteImage();
         }
      }
   ),

   SITES(
      "/api/1.0/sites?limit=1000",
      false,
      false,
      false,
      new ParseResult() {
         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.Sites().setResult(JSONHelper.parseSites(json));
         }
      },
      new GetEvent() {
         public APIEvent<?> getEvent() {
            return new APIEvent.Sites();
         }
      }
   );

   private interface CreateUrl {
      public String create(String query);
   }

   private interface ParseResult {
      public APIEvent<?> parse(String json) throws JSONException;
   }

   private interface GetEvent {
      public APIEvent<?> getEvent();
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
    * Whether or not this is an authenticated endpoint. If true, sends the
    * user's sessionid along in a cookie.
    */
   public final boolean mAuthenticated;

   /**
    * True if this endpoint should always perform POST requests.
    */
   public final boolean mPost;

   /**
    * Used for endpoints to create special URLs.
    */
   private final CreateUrl mCreateUrl;

   /**
    * Used to parse JSON results into the expected form.
    */
   private final ParseResult mParseResult;

   /**
    * Used to obtain a base APIEvent to add data to.
    */
   private final GetEvent mGetEvent;

   private APIEndpoint(String url, boolean https, boolean authenticated,
    boolean post, ParseResult parseResult, GetEvent getEvent) {
      this(url, https, authenticated, post, null, parseResult, getEvent);
   }

   private APIEndpoint(CreateUrl createUrl, boolean https, boolean authenticated,
    boolean post, ParseResult parseResult, GetEvent getEvent) {
      this(null, https, authenticated, post, createUrl, parseResult, getEvent);
   }

   private APIEndpoint(String url, boolean https, boolean authenticated,
    boolean post, CreateUrl createUrl, ParseResult parseResult, GetEvent getEvent) {
      mUrl = url;
      mHttps = https;
      mAuthenticated = authenticated;
      mPost = post;
      mCreateUrl = createUrl;
      mParseResult = parseResult;
      mGetEvent = getEvent;
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

   public APIEvent<?> parseResult(String json) throws JSONException {
      return mParseResult.parse(json).setResponse(json);
   }

   public APIEvent<?> getEvent() {
      return mGetEvent.getEvent();
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
      return mUrl + " | " + mHttps + " | " + mAuthenticated + " | " + ordinal();
   }
}
