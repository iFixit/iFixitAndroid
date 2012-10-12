package com.dozuki.ifixit.view.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class UserImageList implements Serializable {
   private static final long serialVersionUID = 7067096480019401662L;

   private User mUser;
   private ArrayList<UserImageInfo> mImages;

   public UserImageList() {
      mImages = new ArrayList<UserImageInfo>();
   }

   public User getUser() {
      return mUser;
   }

   public void addImage(UserImageInfo userImageInfo) {
      if (mImages.contains(userImageInfo))
         return;
      mImages.add(userImageInfo);
      Collections.sort(mImages, new UserImageInfoComparator());
   }

   public void setUser(User mUser) {
      this.mUser = mUser;
   }

   public ArrayList<UserImageInfo> getImages() {
      return mImages;
   }

   public void setImages(ArrayList<UserImageInfo> mImages) {
      this.mImages = mImages;
   }

   static class UserImageInfoComparator implements Comparator<UserImageInfo> {

      public int compare(UserImageInfo e1, UserImageInfo e2) {

         if (e1.getImageid() == null && e2.getImageid() == null)
            return 0;

         if (e1.getImageid() == null)
            return 1;

         if (e2.getImageid() == null)
            return -1;

         return (int) (Long.parseLong(e1.getImageid()) - Long.parseLong(e2
            .getImageid()));

      }

   }
}
