package com.dozuki.ifixit.guide_create.model;

import java.io.Serializable;

public class ImageObject implements Serializable {
   public int mId;
   public String mMini;
   public String mThumbnail;
   public String mStandared;
   public String mMedium;
   public String mLarge;
   public String mOriginal;

   public ImageObject() {

   }

   public ImageObject(int id, String mini, String thumbnail, String standard, String medium,
    String large, String original) {
      mId = id;
      mMini = mini;
      mThumbnail = thumbnail;
      mStandared = standard;
      mMedium = medium;
      mLarge = large;
      mOriginal = original;
   }
}
