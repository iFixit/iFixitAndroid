package com.dozuki.ifixit.guide_view.model;

import com.dozuki.ifixit.util.APIImage;

import java.io.Serializable;

public class GuideInfo implements Serializable {
   private static final long serialVersionUID = 1L;

   private int mGuideid;
   private int mRevisionid;
   private int mModifiedDate;
   private int mPrereqModifiedDate;
   private String mType;
   private String mTopic;
   private String mSubject;
   private String mTitle;
   private boolean mPublic;
   private APIImage mImage;

   public GuideInfo(int guideid) {
      mGuideid = guideid;
   }

   public void setRevisionid(int revisionid) {
      mRevisionid = revisionid;
   }

   public void setModifiedDate(int modifiedDate) {
      mModifiedDate = modifiedDate;
   }

   public void setPrereqModifiedDate(int prereqModifiedDate) {
      mPrereqModifiedDate = prereqModifiedDate;
   }

   public void setSubject(String subject) {
      mSubject = subject;
   }

   public void setTopic(String topic) {
      mTopic = topic;
   }

   public void setPublic(boolean isPublic) {
      mPublic = isPublic;
   }

   public void setImage(APIImage image) {
      mImage = image;
   }

   public void setTitle(String title) {
      mTitle = title;
   }

   public void setType(String type) {
      mType = type;
   }


   public int getGuideid() {
      return mGuideid;
   }

   public String getSubject() {
      return mSubject;
   }

   public APIImage getImage() {
      return mImage;
   }

   public String getTitle() {
      return mTitle;
   }

   public String getType() {
      return mType;
   }

   public String toString() {
      return mGuideid + ", " + mSubject + ", " + mImage + ", " + mTitle +
       ", " + mType;
   }
}
