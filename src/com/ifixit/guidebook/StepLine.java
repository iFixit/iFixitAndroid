package com.ifixit.guidebook;

public class StepLine {
   protected String mColor;
   protected int mLevel;
   protected String mText;

   public void setColor(String color) {
      mColor = color;
   }

   public void setLevel(int level) {
      mLevel = level;
   }

   public void setText(String text) {
      mText = text;
   }

   public String toString() {
      return "{StepLine: " + mColor + ", " + mLevel + ", " + mText + "}";
   }
}
