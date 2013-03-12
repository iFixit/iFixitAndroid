package com.dozuki.ifixit.guide_create.model;

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
   protected ImageObject mImage = new ImageObject();
   protected boolean mEditMode; // save state for edit drop down

   public UserGuide(int guideid, String subject, String topic, String title,
                    boolean public_, int userid, String username) {
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

   public void setImageObject(ImageObject image) {
      mImage = image;
   }

   public ImageObject getImageObject() {
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


}
