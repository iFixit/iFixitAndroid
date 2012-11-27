package com.dozuki.ifixit.guide_view.model;

import java.io.Serializable;

import android.util.Log;

public class OEmbed implements Serializable {

   private String mHTML;
   private String mURL;
   private String mThumbnail;

   private static final long serialVersionUID = -885749451286840644L;

   public OEmbed(String html, String URL, String thumbnail) {
      mHTML = html;
      mURL = URL;
      mThumbnail = thumbnail;
   }

   public String getThumbnail() {
      return mThumbnail;
   }

   public String getURL() {
      return mURL;
   }

   public String getHtml() {
      return mHTML;
   }

}
