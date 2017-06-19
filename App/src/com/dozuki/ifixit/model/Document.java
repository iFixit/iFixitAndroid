package com.dozuki.ifixit.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Document implements Serializable {
   private static final long serialVersionUID = 1L;

   public String text;
   @SerializedName("url")
   public String relativeUrl;
   @SerializedName("download_url")
   public String url;
   @SerializedName("documentid")
   public int id;

}
