package com.dozuki.ifixit.model.guide;

public class GuideType {
   public String mTitle;
   public String mType;
   public String mPrompt;

   public GuideType(String type, String title, String prompt) {
      mTitle = title;
      mType = type;
      mPrompt = prompt;
   }
}
