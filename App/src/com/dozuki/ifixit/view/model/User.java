package com.dozuki.ifixit.view.model;

import java.io.Serializable;

public class User implements Serializable {
   private static final long serialVersionUID = 6209686573978334361L;

   private String mUserid;
   private String mUsername;
   private String mImageid;
   private String mSession;

   public String getUserId() {
      return mUserid;
   }

   public void setUserid(String userid) {
      mUserid = userid;
   }

   public String getUsername() {
      return mUsername;
   }

   public void setUsername(String username) {
      mUsername = username;
   }

   public String getImageid() {
      return mImageid;
   }

   public void setImageid(String imageid) {
      mImageid = imageid;
   }

   public String getSession() {
      return mSession;
   }

   public void setSession(String session) {
      mSession = session;
   }
}
