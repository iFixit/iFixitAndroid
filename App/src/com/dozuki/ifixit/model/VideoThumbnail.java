package com.dozuki.ifixit.model;

import java.io.Serializable;
import java.util.ArrayList;

public class VideoThumbnail extends Image implements Serializable {
   private static final long serialVersionUID = 0L;

   private String mGuid;
   private int mImageid;
   private String mUrl;
   private ArrayList<String> mSizes;
   private String mRatio;
   private int mWidth;
   private int mHeight;

   public VideoThumbnail(String guid, int imageid, String url, String ratio, int width, int height) {
      mGuid = guid;
      mImageid = imageid;
      mUrl = url;
      mRatio = ratio;
      mWidth = width;
      mHeight = height;
   }
   
   public String getUrl() {
      return mUrl;
   }

   public String getUrl(String size) {
      if (!size.startsWith(".")) {
         mUrl += ".";
      }

      return mUrl + size;
   }

   public int getWidth() {
      return mWidth;
   }
   
   public int getHeight() {
      return mHeight;
   }
}
