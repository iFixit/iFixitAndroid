package com.ifixit.android.ifixit;

public class ImageSizes {
   private String mThumb;
   private String mMain;
   private String mFull;

   public ImageSizes(String thumb, String main, String full) {
      mThumb = thumb;
      mMain = main;
      mFull = full;
   }

   public String getThumb() {
      return mThumb;
   }

   public String getMain() {
      return mMain;
   }

   public String getFull() {
      return mFull;
   }
}
