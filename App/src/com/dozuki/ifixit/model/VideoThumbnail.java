package com.dozuki.ifixit.model;

import java.io.Serializable;

public class VideoThumbnail extends Image implements Serializable {
   private static final long serialVersionUID = 0L;

   private int mWidth;
   private int mHeight;

   public VideoThumbnail(int imageid, String url, int width, int height) {
      super(imageid, url);
      mWidth = width;
      mHeight = height;
   }

   public int getWidth() {
      return mWidth;
   }
   
   public int getHeight() {
      return mHeight;
   }
}
