package com.ifixit.android.ifixit;

import java.io.Serializable;

public class GuideInfo implements Serializable {
   private static final long serialVersionUID = 1L;

   private int mGuideid;
   private String mSubject;
   private String mThumbnail;
   private String mTitle;
   private String mType;
   private String mUrl;

   public GuideInfo(int guideid) {
      mGuideid = guideid;
   }

   public void setSubject(String subject) {
      mSubject = subject;
   }

   public void setThumbnail(String thumbnail) {
      mThumbnail = thumbnail+".standard";
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

   public String getThumbnail() {
      return mThumbnail;
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
      return mGuideid + ", " + mSubject + ", " + mThumbnail + ", " + mTitle +
       ", " + mType + ", " + mUrl;
   }
}
