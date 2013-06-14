package com.dozuki.ifixit.model;

import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.util.JSONHelper;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class APIImage implements Serializable {
   @SerializedName("id") public int mId;
   @SerializedName("original") public String mBaseUrl;

   public APIImage() {
      this(JSONHelper.NULL_INT);
   }

   public APIImage(int id) {
      this(id, "");
   }

   public APIImage(int id, String baseUrl) {
      mId = id;
      if (MainApplication.inDebug() && !baseUrl.isEmpty())
         baseUrl = baseUrl.replaceFirst("https","http");

      mBaseUrl = baseUrl;
   }

   public String getPath(String size) {
      if (!size.startsWith(".")) {
         size = size + ".";
      }

      return mBaseUrl + size;
   }
}
