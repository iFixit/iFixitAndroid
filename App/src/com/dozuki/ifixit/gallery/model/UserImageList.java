package com.dozuki.ifixit.gallery.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class UserImageList implements  UserMediaList {
   private static final long serialVersionUID = 7067096480019401662L;

   private ArrayList<MediaInfo> mImages;

   public UserImageList() {
      mImages = new ArrayList<MediaInfo>();
   }

   public void addItem(MediaInfo userImageInfo) {
      if (mImages.contains(userImageInfo))
         return;
      mImages.add(userImageInfo);
      Collections.sort(mImages, new UserImageInfoComparator());
   }

   public ArrayList<MediaInfo> getItems() {
      return mImages;
   }

   public void setItems(ArrayList<MediaInfo> images) {
      mImages = images;
   }

   private static class UserImageInfoComparator implements
    Comparator<MediaInfo> {
      public int compare(MediaInfo e1, MediaInfo e2) {
         if (e1.getItemId() == null && e2.getItemId() == null)
            return 0;

         if (e1.getItemId() == null)
            return 1;

         if (e2.getItemId() == null)
            return -1;

         return Integer.parseInt(e1.getItemId()) - Integer.parseInt(
          e2.getItemId());
      }
   }



  
}
