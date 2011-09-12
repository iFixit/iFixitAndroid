package com.ifixit.guidebook;

import java.io.Serializable;

import java.util.ArrayList;

import android.util.Log;

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
   
   public String getText() {
      String lineText = new String();
      Log.w("Step Line Count", "" + mLines.size());
      
      int prevLevel = 0;
      
      for (StepLine line : mLines) {
         if (line.getLevel() > prevLevel) {
            lineText = lineText +"<p><b>" + line.getText() +"</b></p>";
         } else {
            lineText = lineText +"<p>" + line.getText() +"</p>";
         }
         Log.w("Step Line", "" + lineText);
      }
      return lineText;
   }

   public String toString() {
      return "{GuideStep: " + mStepNum + ", " + mTitle +  ", " + mLines +
       ", " + mImages + "}";
   }
}
