package com.ifixit.android.ifixit;

import java.io.Serializable;

public class StepLine implements Serializable {
   private static final long serialVersionUID = 8535265363779393297L;
   protected String mColor;
   protected int mLevel;
   protected String mText;
   protected boolean hasIcon = false;
   
   public StepLine(String color, int level, String text) {
      if (color.compareTo("icon_reminder") == 0 ||
         color.compareTo("icon_caution") == 0 || 
         color.compareTo("icon_note") == 0) 
      { 
         hasIcon = true;
      } else {
         hasIcon = false;
      }
      
      mColor = color;
      mLevel = level;
      mText = text;
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
   
   public String getColor() {
      return mColor;
   }
   
   public String toString() {
      return "{StepLine: " + mColor + ", " + mLevel + ", " + mText + "}";
   }
}
