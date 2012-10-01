package com.dozuki.ifixit.util;

/**
 * Defines all APIEndpoints.
 */
public enum APIEndpoint {
   CATEGORIES(
      "/api/1.0/categories/",
      false,
      false,
      0,
      "com.dozuki.ifixit.api.categories"
   ),
   GUIDE(
      "/api/1.0/guide/",
      false,
      false,
      1,
      "com.dozuki.ifixit.api.guide"
   ),
   TOPIC(
      "/api/1.0/topic/",
      false,
      false,
      2,
      "com.dozuki.ifixit.api.topic"
   ),
   LOGIN(
      "/api/1.0/login/",
      true,
      false,
      3,
      "com.dozuki.ifixit.api.login"
   ),
   REGISTER(
      "/api/1.0/register/",
      true,
      false,
      4,
      "com.dozuki.ifixit.api.register"
   ),
   USER_IMAGES(
      "/api/1.0/image/user/",
      false,
      true,
      5,
      "com.dozuki.ifixit.api.user_images"
   ),
   UPLOAD_IMAGE(
      "/api/1.0/image/upload/",
      false,
      true,
      6,
      "com.dozuki.ifixit.api.upload_image"
   ),
   DELETE_IMAGE(
      "/api/1.0/image/delete/",
      false,
      true,
      7,
      "com.dozuki.ifixit.api.delete_image"
   );

   private APIEndpoint(String url, boolean https, boolean authenticated,
    int target, String action) {
      mUrl = url;
      mHttps = https;
      mAuthenticated = authenticated;
      mTarget = target;
      mAction = action;
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
}
