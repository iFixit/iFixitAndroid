package com.dozuki.ifixit.model;


import java.io.Serializable;

public class Item implements Serializable {
   private static final long serialVersionUID = 2884598684003517264L;

   public enum ItemType {
      PART,
      TOOL
   }

   protected ItemType mType;
   protected String mNote;
   protected String mTitle;
   protected String mQuantity;
   protected String mUrl;
   protected String mThumb;

   public Item(ItemType type, String title, String quantity, String url,
    String thumb, String notes) {
      mType = type;
      mNote = notes;
      mTitle = title;
      mQuantity = quantity;
      mUrl = url;
      mThumb = thumb;
   }

   public ItemType getType() {
      return mType;
   }

   public void setTitle(String title) {
      mTitle = title;
   }

   public String getQuantity() {
      return mQuantity;
   }

   public String getTitle() {
      return mTitle;
   }

   public void setUrl(String url) {
      mUrl = url;
   }

   public String getUrl() {
      return mUrl;
   }

   public void setThumb(String thumb) {
      mThumb = thumb;
   }

   public String getThumb() {
      return mThumb;
   }

   public void setNote(String note) {
      mNote = note;
   }

   public String getNote() {
      return mNote;
   }

   public String toString() {
      return "{Item: " + mTitle + ", " + mThumb + ", " + mUrl +
       ", " + mNote + "}";
   }
}
