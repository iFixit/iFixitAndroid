package com.dozuki.ifixit.view.model;

import java.io.Serializable;

public class UserImageInfo implements Serializable {
   private static final long serialVersionUID = -6957010569324490644L;

   private String mImageId;
   private String mGuid;
   private String mLocalPath;
   private String mLocalKey;
   private String mWidth;
   private String mHeight;
   private String mRatio;
   private boolean mLoaded;

   public String getKey() {
      return mLocalKey;
   }

   public void setKey(String key) {
      mLocalKey = key;
   }

   public boolean getLoaded() {
      return mLoaded;
   }

   public void setLoaded(boolean loaded) {
      mLoaded = loaded;
   }

   public String getImageid() {
      return mImageId;
   }

   public void setImageid(String imageid) {
      mImageId = imageid;
   }

   public String getlocalPath() {
      return mLocalPath;
   }

   public void setlocalPath(String localPath) {
      mLocalPath = localPath;
   }

   public String getGuid() {
      return mGuid;
   }

   public void setGuid(String guid) {
      mGuid = guid;
   }

   public String getWidth() {
      return mWidth;
   }

   public void setWidth(String width) {
      mWidth = width;
   }

   public String getHeight() {
      return mHeight;
   }

   public void setHeight(String height) {
      mHeight = height;
   }

   public String getRatio() {
      return mRatio;
   }

   public void setRatio(String ratio) {
      mRatio = ratio;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof UserImageInfo)) {
         return false;
      }

      UserImageInfo inf = (UserImageInfo)obj;
      if (mImageId == null || inf.mImageId == null)
         return false;

      return inf.mImageId.equals(mImageId);
   }
}
