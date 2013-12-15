package com.dozuki.ifixit.model.guide;

import java.io.Serializable;

public class OEmbed implements Serializable {

   private String mHTML;
   private String mURL;
   private String mThumbnail;
   private int mWidth;
   private int mHeight;
   private String mTitle;
   private String mProviderUrl;
   private String mType;

   private static final long serialVersionUID = -885749451236840644L;

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
