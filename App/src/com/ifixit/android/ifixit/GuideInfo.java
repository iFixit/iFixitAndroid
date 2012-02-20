package com.ifixit.android.ifixit;

public class GuideInfo {
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
      mThumbnail = thumbnail;
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

   public String getSubject(String subject) {
      return mSubject;
   }

   public String getThumbnail(String thumbnail) {
      return mThumbnail;
   }

   public String getTitle(String title) {
      return mTitle;
   }

   public String getType(String type) {
      return mType;
   }

   public String getUrl(String url) {
      return mUrl;
   }

   public String toString() {
      return mGuideid + ", " + mSubject + ", " + mThumbnail + ", " + mTitle +
       ", " + mType + ", " + mUrl;
   }
}
