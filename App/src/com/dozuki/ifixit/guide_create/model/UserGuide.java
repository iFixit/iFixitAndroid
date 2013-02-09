package com.dozuki.ifixit.guide_create.model;

import java.io.Serializable;

public class UserGuide implements Serializable {
   private static final long serialVersionUID = 3712835236938894378L;

   protected int mGuideid;
   protected String mSubject;
   protected String mTopic;
   protected String mTitle;
   protected boolean mPublic;
   protected int mUserid;
   protected String mUsername;
   protected String mImage;

   public UserGuide(int guideid, String subject, String topic, String title,
    boolean public_, int userid, String username, String image) {
      mGuideid = guideid;
      mSubject = subject;
      mTopic = topic;
      mTitle = title;
      mPublic = public_;
      mUserid = userid;
      mUsername = username;
      mImage = image;
   }
}
