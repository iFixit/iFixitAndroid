package com.dozuki.ifixit.util;

import java.io.Serializable;

/**
 * Defines an APICall that can be performed using APIService.call().
 */
public class APICall implements Serializable {
   private static final long serialVersionUID = 8782535908621394800L;

   protected APIEndpoint mEndpoint;
   protected String mQuery;
   protected String mRequestBody;
   protected String mExtraInfo;
   protected String mFilePath;

   public APICall(APIEndpoint endpoint, String query) {
      this(endpoint, query, null);
   }

   public APICall(APIEndpoint endpoint, String query, String requestBody) {
      this(endpoint, query, requestBody, null);
   }

   public APICall(APIEndpoint endpoint, String query, String requestBody,
    String extraInfo) {
      this(endpoint, query, requestBody, extraInfo, null);
   }

   public APICall(APIEndpoint endpoint, String query, String requestBody,
    String extraInfo, String filePath) {
      mEndpoint = endpoint;
      mQuery = query;
      mRequestBody = requestBody;
      mExtraInfo = extraInfo;
      mFilePath = filePath;
   }
}
