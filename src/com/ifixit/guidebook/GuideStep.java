package com.ifixit.guidebook;

import java.util.ArrayList;

public class GuideStep {
   protected int mStepNum;
   protected String mTitle;
   protected ArrayList<StepImage> mImages;
   protected ArrayList<StepLine> mLines;

   public GuideStep(int stepNum) {
      mStepNum = stepNum;
      mImages = new ArrayList<StepImage>();
      mLines = new ArrayList<StepLine>();
   }

   public void setTitle(String title) {
      mTitle = title;
   }

   public String getTitle() {
      return mTitle;
   }
   
   public int getStepNum() {
      return mStepNum;
   }

   public void addImage(StepImage image) {
      mImages.add(image);
   }

   public void addLine(StepLine line) {
      mLines.add(line);
   }
   
   public String getText() {
      return mLines.get(0).mText;
   }

   public String toString() {
      return "{GuideStep: " + mStepNum + ", " + mTitle +  ", " + mLines +
       ", " + mImages + "}";
   }
}
