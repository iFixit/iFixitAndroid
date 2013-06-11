package com.dozuki.ifixit.model;

public class Flag {
   private String mTitle;
   private int mId;
   private String mText;
   private APIImage mThumbnail;

   public Flag(int id) {
      mId = id;
   }

   public Flag(int id, String title) {
      mId = id;
      mTitle = title;
   }

   public Flag(int id, String title, String text) {
      mId = id;
      mTitle = title;
      mText = text;
   }

   public String getTitle() {
      return mTitle;
   }

   public String getText() {
      return mText;
   }

   public int getId() {
      return mId;
   }
}
