package com.dozuki.ifixit.util.api;

import java.io.Serializable;

/**
 * Defines an ApiCall that can be performed using Api.call().
 */
public class ApiCall implements Serializable {
   private static final long serialVersionUID = 8782535908621394800L;

   protected ApiEndpoint mEndpoint;
   protected String mQuery;
   protected String mRequestBody;
   protected String mExtraInfo;
   protected String mFilePath;
   protected String mAuthToken;
   protected int mActivityid = -1;

   public ApiCall(ApiEndpoint endpoint, String query) {
      this(endpoint, query, null);
   }

   public ApiCall(ApiEndpoint endpoint, String query, String requestBody) {
      this(endpoint, query, requestBody, null);
   }

   public ApiCall(ApiEndpoint endpoint, String query, String requestBody,
                  String extraInfo) {
      this(endpoint, query, requestBody, extraInfo, null);
   }

   public ApiCall(ApiEndpoint endpoint, String query, String requestBody,
                  String extraInfo, String filePath) {
      mEndpoint = endpoint;
      mQuery = query;
      mRequestBody = requestBody;
      mExtraInfo = extraInfo;
      mFilePath = filePath;
   }
}
