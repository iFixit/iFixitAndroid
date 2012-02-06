package com.ifixit.android.ifixit;

import java.util.ArrayList;

public class TopicLeaf {
   private String mName;
   private ArrayList<GuideInfo> mGuides;

   public TopicLeaf(String name) {
      mName = name;
      mGuides = new ArrayList<GuideInfo>();
   }

   public void addGuide(GuideInfo guideInfo) {
      mGuides.add(guideInfo);
   }

   public String toString() {
      return mName + ", " + mGuides;
   }
}
