package com.dozuki.ifixit.model.guide;

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
   protected String mTextRendered;
   protected String mTextRaw;
   protected boolean hasIcon = false;

   public StepLine() {
      this(null, "black", 0, "", "");
   }

   public StepLine(Integer lineid, String color, int level, String textRaw, String textRendered) {
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
      mTextRaw = textRaw;
      mTextRendered = textRendered;
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

   public void setTextRendered(String text) {
      mTextRendered = text;
   }

   public String getTextRendered() {
      return mTextRendered;
   }

   public String getTextRaw() {
      return mTextRaw;
   }

   public void setTextRaw(String textRaw) {
      mTextRaw = textRaw;
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
      return "{StepLine: " + mLineid + ", " + mColor + ", " + mLevel + ", " + mTextRendered + "}";
   }

   public Integer getLineId() {
      return mLineid;
   }
}
