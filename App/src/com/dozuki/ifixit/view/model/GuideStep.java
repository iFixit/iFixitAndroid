package com.dozuki.ifixit.view.model;

import java.io.Serializable;
import java.util.ArrayList;

public class GuideStep implements Serializable {
   private static final long serialVersionUID = 2884598684003517264L;

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
   
   public ArrayList<StepImage> getImages() {
      return mImages;
   }
   
   public ArrayList<StepLine> getLines() {
      return mLines;
   }

   public void addLine(StepLine line) {
      mLines.add(line);
   }
   
   public StepLine getLine(int pos) {
      return mLines.get(pos);
   }
   
   public String toString() {
      return "{GuideStep: " + mStepNum + ", " + mTitle +  ", " + mLines +
       ", " + mImages + "}";
   }
}
