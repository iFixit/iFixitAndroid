package com.dozuki.ifixit.util;

public class ImageSizes {
   private String mThumb;
   private String mMain;
   private String mFull;
   private String mGrid;
   private String mLogo;

   public ImageSizes(String logo, String thumb, String main, String full, String grid) {
      mLogo = logo;
      mThumb = thumb;
      mMain = main;
      mFull = full;
      mGrid = grid;
   }

   public String getLogo() {
      return mLogo;
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

   public String getGrid() {
      return mGrid;
   }
}
