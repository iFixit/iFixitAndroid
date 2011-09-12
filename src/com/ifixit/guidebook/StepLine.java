package com.ifixit.guidebook;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

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
