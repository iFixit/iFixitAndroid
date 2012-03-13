package com.ifixit.android.ifixit;

public class ImageSizes {
   private String mThumb;
   private String mMain;
   private String mFull;
   private String mGrid;

   public ImageSizes(String thumb, String main, String full, String grid) {
      mThumb = thumb;
      mMain = main;
      mFull = full;
      mGrid = grid;
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
