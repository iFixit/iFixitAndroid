package com.dozuki.ifixit.view.model;

import java.io.Serializable;

public class User implements Serializable {
   private static final long serialVersionUID = 6209686573978334361L;

   String userId;
   String username;
   String imageId;
   String session;

   public String getUserId() {
      return userId;
   }

   public void setUserId(String userId) {
      this.userId = userId;
   }

   public String getUsername() {
      return username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public String getImageId() {
      return imageId;
   }

   public void setImageId(String imageId) {
      this.imageId = imageId;
   }

   public String getSession() {
      return session;
   }

   public void setSession(String session) {
      this.session = session;
   }
}
