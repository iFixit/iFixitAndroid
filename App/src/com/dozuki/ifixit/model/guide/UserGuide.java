package com.dozuki.ifixit.model.guide;

import com.dozuki.ifixit.util.APIImage;

import java.io.Serializable;

public class UserGuide implements Serializable {
   private static final long serialVersionUID = 3712835236938894378L;

   protected int mGuideid;
   protected String mSubject;
   protected String mTopic;
   protected String mTitle;
   protected String mSummary;
   protected String mIntro;
   protected String mType;
   protected String mDevice;
   protected boolean mPublic;
   protected int mUserid;
   protected String mUsername;
   protected APIImage mImage = new APIImage();
   protected boolean mEditMode; // save state for edit drop down
   protected Integer mRevisionId;

   public UserGuide(int guideid, String subject, String topic, String title, boolean public_, int userid,
      String username) {
      mGuideid = guideid;
      mSubject = subject;
      mTopic = topic;
      mTitle = title;
      mPublic = public_;
      mUserid = userid;
      mUsername = username;
   }

   public UserGuide() {

   }

   public String getTitle() {
      return mTitle;
   }

   public int getGuideid() {
      return mGuideid;
   }

   public void setGuideid(int gID) {
      mGuideid = gID;
   }

   public void setTitle(String title) {
      mTitle = title;
   }

   public void setTopic(String topic) {
      mTopic = topic;
   }

   public void setEditMode(boolean editMode) {
      mEditMode = editMode;
   }

   public boolean getEditMode() {
      return mEditMode;
   }

   public boolean getPublished() {
      return mPublic;
   }

   public void setPublished(boolean pub) {
      mPublic = pub;
   }

   public void setSubject(String sub) {
      mSubject = sub;
   }

   public void setUserName(String user) {
      mUsername = user;
   }

   public void setUserid(int userid) {
      mUserid = userid;
   }

   public void setImage(APIImage image) {
      mImage = image;
   }

   public APIImage getImage() {
      return mImage;
   }

   public void setSummary(String sum) {
      mSummary = sum;
   }

   public void setIntroduction(String intro) {
      mIntro = intro;
   }

   public void setDevice(String device) {
      mDevice = device;
   }

   public void setType(String type) {
      mType = type;
   }

   public String getTopic() {
      return mTopic;
   }

   public String getType() {
      return mType;
   }

   public String getSubject() {
      return mSubject;
   }

   public String getSummary() {
      return mSummary;
   }

   public String getIntro() {
      return mIntro;
   }

   public Integer getRevisionid() {
      return mRevisionId;
   }

   public void setRevisionid(Integer id) {
      mRevisionId = id;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (!(o instanceof UserGuide)) {
         return false;
      }
      UserGuide lhs = (UserGuide) o;
      return mGuideid == lhs.mGuideid;
   }

}
