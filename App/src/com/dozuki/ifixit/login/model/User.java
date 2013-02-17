package com.dozuki.ifixit.login.model;

import java.io.Serializable;

public class User implements Serializable {
   private static final long serialVersionUID = 6209686573978334361L;

   private String mUserid;
   private String mUsername;
   private String mImageid;
   private String mAuthToken;

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

   public String getAuthToken() {
      return mAuthToken;
   }

   public void setAuthToken(String authToken) {
      mAuthToken = authToken;
   }
}
