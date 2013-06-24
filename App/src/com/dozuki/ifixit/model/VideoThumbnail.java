package com.dozuki.ifixit.model;

import java.io.Serializable;

public class VideoThumbnail extends Image implements Serializable {
   private static final long serialVersionUID = 0L;

   private String mRatio;
   private int mWidth;
   private int mHeight;

   public VideoThumbnail(int imageid, String url, String ratio, int width, int height) {
      super(imageid, url);
      mRatio = ratio;
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
