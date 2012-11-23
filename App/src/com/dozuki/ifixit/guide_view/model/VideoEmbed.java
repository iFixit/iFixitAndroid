package com.dozuki.ifixit.guide_view.model;

import java.io.Serializable;

public class VideoEmbed implements Serializable {

   private static final long serialVersionUID = 1L;
   protected int mWidth;
   protected int mHeight;
   protected String mType;
   protected String mURL;

   public VideoEmbed(int width, int height, String type, String url) {
      mWidth = width;
      mHeight = height;
      mType = type;
      mURL = url;
   }

   public String getURL() {
      return mURL;
   }

}
