package com.dozuki.ifixit.guide_view.model;

import java.io.Serializable;
import java.util.ArrayList;

public class StepVideoThumbnail implements Serializable {
   private static final long serialVersionUID = 0L;

   private String mGuid;
   private int mImageid;
   private String mUrl;
   private ArrayList<String> mSizes;
   private String mRatio;
   private int mWidth;
   private int mHeight;

   public StepVideoThumbnail(String guid, int imageid, String url, String ratio, int width, int height) {
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

   public int getWidth() {
      return mWidth;
   }
   
   public int getHeight() {
      return mHeight;
   }
}
