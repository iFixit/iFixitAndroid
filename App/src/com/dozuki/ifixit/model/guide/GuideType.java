package com.dozuki.ifixit.model.guide;

import java.io.Serializable;

public class GuideType implements Serializable {
   private static final long serialVersionUID = -8948485049734934973L;

   public String mTitle;
   public String mType;
   public String mPrompt;

   public GuideType(String type, String title, String prompt) {
      mTitle = title;
      mType = type;
      mPrompt = prompt;
   }
}
