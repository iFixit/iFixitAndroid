package com.dozuki.ifixit.guide_view.model;

import java.io.Serializable;

public class VideoEncoding implements Serializable {

   private static final long serialVersionUID = -6244973891206389939L;
   protected int mWidth;
   protected int mHeight;
   protected String mFormat;
   protected String mURL;

   public VideoEncoding(int width, int height, String url, String format) {
      mWidth = width;
      mHeight = height;
      mFormat = format;
      mURL = url;
   }

   public String getURL() {
      return mURL;
   }
}
