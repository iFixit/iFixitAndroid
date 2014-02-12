package com.dozuki.ifixit.util;

public class ImageSizes {
   private String mStepThumb;
   private String mStepMain;
   private String mStepFull;
   private String mGrid;
   private String mLogo;

   public ImageSizes(String logo, String thumb, String main, String full, String grid) {
      mLogo = logo;
      mStepThumb = thumb;
      mStepMain = main;
      mStepFull = full;
      mGrid = grid;
   }

   public String getLogo() {
      return mLogo;
   }

   public String getThumb() {
      return mStepThumb;
   }

   public String getMain() {
      return mStepMain;
   }

   public String getFull() {
      return mStepFull;
   }

   public String getGrid() {
      return mGrid;
   }
}
