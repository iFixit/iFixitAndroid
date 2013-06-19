package com.dozuki.ifixit.model.user;

import com.dozuki.ifixit.model.Image;

import java.io.Serializable;

public class UserImage extends Image implements Serializable {
   private static final long serialVersionUID = 272113480839309007L;

   private int mWidth;
   private int mHeight;
   private String mRatio;
   private String mMarkup;
   private String mExif;

   public UserImage() {
      super();
      mWidth = 0;
      mHeight = 0;
      mRatio = "";
      mMarkup = "";
      mExif = "";
   }

   public UserImage(int id, String path, int width, int height, String ratio, String markup, String exif) {
      super(id, path);

      mWidth = width;
      mHeight = height;
      mRatio = ratio;
      mMarkup = markup;
      mExif = exif;
   }

   public void setMarkup(String markup) {
      mMarkup = markup;
   }

   public String getMarkup() {
      return mMarkup;
   }

   public int getHeight() {
      return mHeight;
   }

   public void setHeight(int height) {
      mHeight = height;
   }

   public int getWidth() {
      return mWidth;
   }

   public void setWidth(int width) {
      mWidth = width;
   }

   public void setRatio(String ratio) {
      mRatio = ratio;
   }

   public String getRatio() {
      return mRatio;
   }
}
