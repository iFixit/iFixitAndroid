package com.dozuki.ifixit.view.model;

import java.io.Serializable;

public class UserImageInfo implements Serializable {

   /**
	 * 
	 */
   private static final long serialVersionUID = -6957010569324490644L;
   String mImageId;
   String mGuid;
   String mLocalPath;
   String mLocalKey;
   boolean mLoaded;

   public String getKey() {
      return mLocalKey;
   }

   public void setKey(String key) {
      mLocalKey = key;
   }

   public void setLoaded(boolean loaded) {
      mLoaded = loaded;
   }

   public String getmImageId() {
      return mImageId;
   }

   public void setmImageId(String mImageId) {
      this.mImageId = mImageId;
   }

   public String getlocalPath() {
      return mLocalPath;
   }

   public void setlocalPath(String mLocal) {
      mLocalPath = mLocal;
   }

   public String getmGuid() {
      return mGuid;
   }

   public void setGuid(String mGuid) {
      this.mGuid = mGuid;
   }

   public String getWidth() {
      return mWidth;
   }

   public void setWidth(String mWidth) {
      this.mWidth = mWidth;
   }

   public String getHeight() {
      return mHeight;
   }

   public void setHeight(String mHeight) {
      this.mHeight = mHeight;
   }

   public String getRatio() {
      return mRatio;
   }

   public void setRatio(String mRatio) {
      this.mRatio = mRatio;
   }

   String mWidth;
   String mHeight;
   String mRatio;

   @Override
   public boolean equals(Object obj) {
      if (obj == null)
         return false;
      if (obj == this)
         return true;
      if (obj.getClass() != getClass())
         return false;
      UserImageInfo inf = (UserImageInfo) obj;
      if (this.mImageId == null || inf.mImageId == null)
         return false;
      return (inf.mImageId.equals(this.mImageId));
   }

   public boolean getLoaded() {
      // TODO Auto-generated method stub
      return mLoaded;
   }

}
