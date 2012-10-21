package com.dozuki.ifixit.guide_view.model;

import java.io.Serializable;

public class GuidePart implements Serializable {
   private static final long serialVersionUID = 2884598684003517264L;

   protected String mNote;
   protected String mTitle;
   protected String mUrl;
   protected String mThumb;

   public GuidePart(String title, String url, String thumb, String notes) {
      mNote = notes;
      mTitle = title;
      mUrl = url;
      mThumb = thumb;
   }

   public void setTitle(String title) {
      mTitle = title;
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
      return "{GuidePart: " + mTitle + ", " + mThumb +  ", " + mUrl +
       ", " + mNote + "}";
   }
}
