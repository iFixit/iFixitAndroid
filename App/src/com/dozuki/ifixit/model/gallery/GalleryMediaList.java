package com.dozuki.ifixit.model.gallery;

import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.model.user.UserImage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GalleryMediaList implements Serializable {
   private static final long serialVersionUID = -771269428461585511L;

   private static final int MIN_WIDTH = 800;
   private static final int MIN_HEIGHT = 600;

   private ArrayList<GalleryImage> mImages;

   public GalleryMediaList() {
      mImages = new ArrayList<GalleryImage>();
   }

   public void addItem(GalleryImage userImage) {
      if (isInvalid(userImage)) return;

      mImages.add(userImage);
      //Collections.sort(mImages, new UserImageComparator());
   }

   public void addItem(int index, GalleryImage userImage) {
      if (isInvalid(userImage)) return;

      mImages.add(index, userImage);
      //Collections.sort(mImages, new UserImageComparator());
   }

   public int size() {
      return mImages.size();
   }

   public void remove(int index) {
      mImages.remove(index);
   }

   public GalleryImage get(int index) {
      return mImages.get(index);
   }

   public void set(int index, GalleryImage image) {
      mImages.set(index, image);
   }

   public ArrayList<GalleryImage> getItems() {
      return mImages;
   }

   public void setItems(ArrayList<UserImage> images) {
      mImages.clear();
      for (UserImage image : images) {
         if (image.getWidth() >= MIN_WIDTH) {
            mImages.add(new GalleryImage(image));
         }
      }

      // Default ordering should be newest to oldest.
      Collections.reverse(mImages);
   }

   public void findAndReplaceByKey(String key, Image replacement) {
      for (GalleryImage image : mImages) {
         Log.w("GalleryMediaList", image.getPath() + " = " + key);
         if (image.getPath().equals(key)) {
            replacement.setPath(image.getPath());
            replacement.setId(image.getId());
            break;
         }
      }
   }

   public boolean hasSelected() {
      for (GalleryImage image : mImages) {
         if (image.isSelected()) {
            return true;
         }
      }

      return false;
   }

   public void clearSelected() {
      for (GalleryImage image : mImages) {
         image.setSelected(false);
      }
   }

   public int countSelected() {
      int selectedCount = 0;
      for (GalleryImage image : mImages) {
         if (image.isSelected()) {
            selectedCount++;
         }
      }

      return selectedCount;
   }

   public int countUploadingImages() {
      // check how many images are being uploaded
      int count = 0;
      for (GalleryImage image : mImages) {
         if (image.isLocal()) {
            count++;
         }
      }
      return count;
   }

   private boolean isInvalid(UserImage image) {
      return (!image.isLocal() &&
       (mImages.contains(image) || image.getWidth() < MIN_WIDTH || image.getHeight() < MIN_HEIGHT));
   }

   private static class UserImageComparator implements Comparator<GalleryImage> {
      public int compare(GalleryImage e1, GalleryImage e2) {
         if (e1.isLocal() && e2.isLocal()) {
            return 0;
         }

         // Keep local images first
         if (e1.isLocal()) {
            return -1;
         }

         if (e2.isLocal()) {
            return 1;
         }

         // Otherwise sort by age
         return e1.getId() - e2.getId();
      }
   }

}
