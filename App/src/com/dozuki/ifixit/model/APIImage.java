package com.dozuki.ifixit.model;

import com.dozuki.ifixit.util.JSONHelper;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class APIImage implements Serializable {
   @SerializedName("id") public int mId;
   @SerializedName("original") public String mBaseUrl;

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
