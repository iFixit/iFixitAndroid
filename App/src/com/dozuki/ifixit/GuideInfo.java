package com.dozuki.ifixit;

import java.io.Serializable;

public class GuideInfo implements Serializable {
   private static final long serialVersionUID = 1L;

   private int mGuideid;
   private String mSubject;
   private String mImage;
   private String mTitle;
   private String mType;
   private String mUrl;

   public GuideInfo(int guideid) {
      mGuideid = guideid;
   }

   public void setSubject(String subject) {
      mSubject = subject;
   }

   public void setImage(String image) {
      mImage = image;
   }

   public void setTitle(String title) {
      mTitle = title;
   }

   public void setType(String type) {
      mType = type;
   }

   public void setUrl(String url) {
      mUrl = url;
   }

   public int getGuideid() {
      return mGuideid;
   }

   public String getSubject() {
      return mSubject;
   }

   public String getImage() {
      return mImage;
   }

   public String getTitle() {
      return mTitle;
   }

   public String getType() {
      return mType;
   }

   public String getUrl() {
      return mUrl;
   }

   public String toString() {
      return mGuideid + ", " + mSubject + ", " + mImage + ", " + mTitle +
       ", " + mType + ", " + mUrl;
   }
}
