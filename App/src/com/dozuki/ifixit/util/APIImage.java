package com.dozuki.ifixit.util;

import java.io.Serializable;

public class APIImage implements Serializable {
   public final Integer mId;
   public final String mBaseUrl;

   public APIImage() {
      this(null, null);
   }

   public APIImage(Integer id, String baseUrl) {
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
