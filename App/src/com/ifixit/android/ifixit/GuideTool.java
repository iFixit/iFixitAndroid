package com.ifixit.android.ifixit;

import java.io.Serializable;

public class GuideTool implements Serializable {
   private static final long serialVersionUID = 2884598684003517264L;

   protected String mNote;
   protected String mTitle;
   protected String mUrl;
   protected String mThumb;

   public GuideTool() {
      mNote = mThumb = mUrl = mTitle = "";
   }

   public GuideTool(String title, String url) {
      mNote = mThumb = "";

      mTitle = title;
      mUrl = url;
   }

   public GuideTool(String title, String url, String thumb) {
      mNote = "";

      mTitle = title;
      mUrl = url;
      mThumb = thumb;
   }

   public GuideTool(String title, String url, String thumb, String notes) {
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
      return "{GuideTools: " + mTitle + ", " + mThumb +  ", " + mUrl +
       ", " + mNote + "}";
   }
}
