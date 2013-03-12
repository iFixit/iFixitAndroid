package com.dozuki.ifixit.guide_view.model;

import java.io.Serializable;

public class StepLine implements Serializable {
   private static final long serialVersionUID = 8535265363779393297L;
   /**
    * Lineid that identifies this stepline. Can be null if this line hasn't been
    * saved yet.
    */
   protected Integer mLineid;
   protected String mColor;
   protected int mLevel;
   protected String mText;
   protected boolean hasIcon = false;

   public StepLine(Integer lineid, String color, int level, String text) {
      if (color.equals("icon_reminder") ||
          color.equals("icon_caution") ||
          color.equals("icon_note")) {
         hasIcon = true;
      } else {
         hasIcon = false;
      }

      mLineid = lineid;
      mColor = color;
      mLevel = level;
      mText = text;
   }

   public void setLineid(Integer lineid) {
      mLineid = lineid;
   }

   public void setColor(String color) {
      mColor = color;
   }

   public void setLevel(int level) {
      mLevel = level;
   }

   public void setText(String text) {
      mText = text;
   }

   public String getText() {
      return mText;
   }

   public int getLevel() {
      return mLevel;
   }

   public boolean hasIcon() {
      return hasIcon;
   }

   public String getColor() {
      return mColor;
   }

   public String toString() {
      return "{StepLine: " + mLineid + ", " + mColor + ", " + mLevel + ", " + mText + "}";
   }

   public int getLineId() {
      return mLineid;
   }
}
