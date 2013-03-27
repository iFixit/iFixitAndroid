package com.dozuki.ifixit.util;

import java.io.Serializable;

public class APIImage implements Serializable {
   public int mId;
   public String mBaseUrl;

   public APIImage() {
      this(JSONHelper.NULL_INT, null);
   }

   public APIImage(int id, String baseUrl) {
      mId = id;
      mBaseUrl = baseUrl;
   }

   public String getSize(String size) {
      if (mBaseUrl == null) {
         return "";
      }

      return mBaseUrl + size;
   }
}
