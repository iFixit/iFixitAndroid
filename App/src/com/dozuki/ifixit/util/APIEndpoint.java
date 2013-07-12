package com.dozuki.ifixit.util;

import android.util.Log;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.model.dozuki.Site;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Defines all APIEndpoints.
 */
public enum APIEndpoint {
   CATEGORIES(
      new Endpoint() {
         public String createUrl(String query) {
            return "categories";
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.Categories().setResult(JSONHelper.parseTopics(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.Categories();
         }
      },
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
            return new APIEvent.ViewGuide().setResult(JSONHelper.parseGuide(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.ViewGuide();
         }
      },
      false,
      "GET",
      false
   ),

   GUIDES(
      new Endpoint() {
         public String createUrl(String query) {
            return "guides" + query;
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.Guides().setResult(JSONHelper.parseGuides(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.Guides();
         }
      },
      false,
      "GET",
      false
   ),

   TOPIC(
      new Endpoint() {
         public String createUrl(String query) {
            try {
               return "categories/" + URLEncoder.encode(query, "UTF-8");
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
      "GET",
      false
   ),

   ALL_TOPICS(
    new Endpoint() {
       public String createUrl(String query) {
          return "categories/all?limit=100000";
       }

       public APIEvent<?> parse(String json) throws JSONException {
          return new APIEvent.TopicList().setResult(JSONHelper.parseAllTopics(json));
       }

       public APIEvent<?> getEvent() {
          return new APIEvent.TopicList();
       }
    },
    false,
    "GET",
    false
   ),

   LOGIN(
      new Endpoint() {
         public String createUrl(String query) {
            return "user/token";
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.Login().setResult(JSONHelper.parseLoginInfo(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.Login();
         }
      },
      false,
      "POST",
      true
   ),

   LOGOUT(
      new Endpoint() {
         public String createUrl(String query) {
            return "user/token";
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.Logout();
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.Logout();
         }
      },
      true,
      "DELETE",
      false
   ),

   REGISTER(
      new Endpoint() {
         public String createUrl(String query) {
            return "users";
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.Register().setResult(JSONHelper.parseLoginInfo(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.Register();
         }
      },
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
      true,
      "GET",
      false
   ),
   USER_VIDEOS(
      new Endpoint() {
         public String createUrl(String query) {
            return "user/media/videos" + query;
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.UserVideos().setResult(JSONHelper.parseUserVideos(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.UserVideos();
         }
      },
      true,
      "GET",
      false
   ),

   USER_FAVORITES(
    new Endpoint() {
       public String createUrl(String query) {
          return "user/favorites/guides";
       }

       public APIEvent<?> parse(String json) throws JSONException {
          return new APIEvent.UserFavorites().setResult(JSONHelper.parseUserFavorites(json));
       }

       public APIEvent<?> getEvent() {
          return new APIEvent.UserFavorites();
       }
    },
    true,
    "GET",
    false
   ),

   USER_EMBEDS(
      new Endpoint() {
         public String createUrl(String query) {
            return "user/media/embeds" + query;
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.UserEmbeds().setResult(JSONHelper.parseUserEmbeds(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.UserVideos();
         }
      },
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
            return new APIEvent.UploadImage().setResult(JSONHelper.parseUploadedImage(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.UploadImage();
         }
      },
      true,
      "POST",
      false
   ),

   UPLOAD_STEP_IMAGE(
    new Endpoint() {
       public String createUrl(String query) {
          String fileName;

          try {
             fileName = URLEncoder.encode(getFileNameFromFilePath(query), "UTF-8");
          } catch (UnsupportedEncodingException e) {
             // Provide a default file name.
             fileName = "uploaded_image.jpg";
          }

          return "user/media/images?cropToRatio=FOUR_THREE&file=" + fileName;
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
          return new APIEvent.UploadStepImage().setResult(JSONHelper.parseUploadedImage(json));
       }

       public APIEvent<?> getEvent() {
          return new APIEvent.UploadStepImage();
       }
    },
    true,
    "POST",
    false
   ),

   COPY_IMAGE(
       new Endpoint() {
          public String createUrl(String query) {
             return "user/media/images/" + query;
          }

          public APIEvent<?> parse(String json) throws JSONException {
             // TODO: Actually look at the response?
             return new APIEvent.DeleteImage().setResult("");
          }

          public APIEvent<?> getEvent() {
             return new APIEvent.DeleteImage();
          }
       },
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
      true,
      "DELETE",
      false
   ),

   USER_GUIDES(
      new Endpoint() {
         public String createUrl(String query) {
            return "user/guides?limit=10000";
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.UserGuides().setResult(JSONHelper.parseUserGuides(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.UserGuides();
         }
      },
      true,
      "GET",
      false
   ),

   GUIDE_FOR_EDIT(
      new Endpoint() {
         public String createUrl(String query) {
            return "guides/" + query + "?unpatrolled&excludePrerequisiteSteps";
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.GuideForEdit().setResult(JSONHelper.parseGuide(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.GuideForEdit();
         }
      },
      true,
      "GET",
      false
   ),

   CREATE_GUIDE(
      new Endpoint() {
         public String createUrl(String query) {
            return "guides";
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.CreateGuide().setResult(JSONHelper.parseGuide(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.CreateGuide();
         }
      },
      true,
      "POST",
      false
   ),

   EDIT_GUIDE(
      new Endpoint() {
         public String createUrl(String query) {
            return "guides/" + query;
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.EditGuide().setResult(JSONHelper.parseGuide(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.EditGuide();
         }
      },
      true,
      "PATCH",
      false
   ),
   DELETE_GUIDE(
      new Endpoint() {
         public String createUrl(String query) {
            return "guides/" + query;
         }

         public APIEvent<?> parse(String json) throws JSONException {
           return new APIEvent.DeleteGuide().setResult(json);
         }

         public APIEvent<?> getEvent() {
           return new APIEvent.DeleteGuide();
         }
      },
      true,
      "DELETE",
      false
   ),

   UPDATE_GUIDE(
      new Endpoint() {
         public String createUrl(String query) {
            return "guides/" + query;
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return null;
            //return new APIEvent.CreateGuide().setResult(JSONHelper.parseUserGuide(json));
         }

         public APIEvent<?> getEvent() {
            return null;
           // return new APIEvent.CreateGuide();
         }
      },
      true,
      "PATCH",
      false
   ),
   PUBLISH_GUIDE(
      new Endpoint() {
         public String createUrl(String query) {
            return "guides/" + query;
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.PublishStatus().setResult(JSONHelper.parseGuide(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.PublishStatus();
         }
      },
      true,
      "PUT",
      false
   ),

   UNPUBLISH_GUIDE(
      new Endpoint() {
         public String createUrl(String query) {
            return "guides/" +  query + "/public";
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.PublishStatus().setResult(JSONHelper.parseGuide(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.PublishStatus();
         }
      },
      true,
      "DELETE",
      false
   ),

   REORDER_GUIDE_STEPS(
      new Endpoint() {
         public String createUrl(String query) {
            return "guides/" +  query;
         }

         public APIEvent<?> parse(String json) throws JSONException {
            return new APIEvent.StepReorder().setResult(JSONHelper.parseGuide(json));
         }

         public APIEvent<?> getEvent() {
            return new APIEvent.StepReorder();
         }
      },
      true,
      "PUT",
      false
   ),

   ADD_GUIDE_STEP(new Endpoint() {
      public String createUrl(String query) {
         return "guides/" + query;
      }

      public APIEvent<?> parse(String json) throws JSONException {
         return new APIEvent.StepAdd().setResult(JSONHelper.parseGuide(json));
      }

      public APIEvent<?> getEvent() {
         return new APIEvent.StepAdd();
      }
   }, true, "POST", false),

   UPDATE_GUIDE_STEP(new Endpoint() {
      public String createUrl(String query) {
         return "guides/" + query;
      }

      public APIEvent<?> parse(String json) throws JSONException {
         return new APIEvent.StepSave().setResult(JSONHelper.parseStep(new JSONObject(json), 0));
      }

      public APIEvent<?> getEvent() {
         return new APIEvent.StepSave();
      }
   }, true, "PATCH", false),

   DELETE_GUIDE_STEP(new Endpoint() {
      public String createUrl(String query) {
         return "guides/" + query;
      }

      public APIEvent<?> parse(String json) throws JSONException {
         return new APIEvent.StepRemove().setResult(JSONHelper.parseGuide(json));
      }

      public APIEvent<?> getEvent() {
         return new APIEvent.StepRemove();
      }
   }, true, "DELETE", false),

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
      "GET",
      false
   ),

   SITE_INFO(
    new Endpoint() {
       public String createUrl(String query) {
          return "sites/info";
       }

       public APIEvent<?> parse(String json) throws JSONException {
          return new APIEvent.SiteInfo().setResult(JSONHelper.parseSiteInfo(json));
       }

       public APIEvent<?> getEvent() {
          return new APIEvent.SiteInfo();
       }
    },
    true,  // Must be authenticated to force GuideIntroActivity to require login.
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
      "GET",
      false
   );

   /**
    * Current version of the API to use.
    */
   private static final String API_VERSION = "2.0";

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
    * Whether or not this is an authenticated endpoint. If true, sends the
    * user's auth token along in a header.
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

   private APIEndpoint(Endpoint endpoint, boolean authenticated,
    String method, boolean forcePublic) {
      mEndpoint = endpoint;
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

   /**
    * Returns an absolute URL for this endpoint for the given site and query.
    */
   public String getUrl(Site site, String query) {
      String domain;
      String protocol;
      String url;

      if (site != null) {
         domain = site.getAPIDomain();
      } else {
         domain = "www.ifixit.com";
      }

      protocol = "https";
      url = String.format("%s://%s/api/%s/%s", protocol, domain, API_VERSION, mEndpoint.createUrl(query));

      if (MainApplication.inDebug()) {
         Log.d("APIEndpoint", "API Request URL: " + url);
      }

      return url;
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

   public String toString() {
      return mAuthenticated + " | " + ordinal();
   }
}
