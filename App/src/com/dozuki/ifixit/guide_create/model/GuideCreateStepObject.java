package com.dozuki.ifixit.guide_create.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.dozuki.ifixit.guide_view.model.GuideStep;
import com.dozuki.ifixit.guide_view.model.StepImage;
import com.dozuki.ifixit.guide_view.model.StepLine;

public class GuideCreateStepObject implements Serializable {

   /**
	 * 
	 */
   private static final long serialVersionUID = -2019322123419333278L;
   protected int mStepNum;
   protected String mTitle;
   protected boolean mEditMode; // save state for edit drop down
   protected ArrayList<StepImage> mImages;
   protected ArrayList<StepLine> mLines;

   public GuideCreateStepObject(int stepNum) {
      mStepNum = stepNum;
      mImages = new ArrayList<StepImage>();
      mLines = new ArrayList<StepLine>();
   }

   public GuideCreateStepObject(GuideStep gs) {
      mStepNum = gs.getStepNum();
      mImages = gs.getImages();

      mLines = gs.getLines();
      mTitle = gs.getTitle();
   }

   public void setEditMode(boolean editMode) {
      mEditMode = editMode;
   }

   public boolean getEditMode() {
      return mEditMode;
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
      return "{GuideStep: " + mStepNum + ", " + mTitle + ", " + mLines + ", " + mImages + "}";
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (!(o instanceof GuideCreateStepObject)) {
         return false;
      }
      GuideCreateStepObject lhs = (GuideCreateStepObject) o;
      return mStepNum == lhs.mStepNum;
   }

   public void setImages(ArrayList<StepImage> imageIDs) {
      mImages = imageIDs;
   }

   public void setLines(ArrayList<StepLine> lines) {
      mLines = lines;
   }
}
