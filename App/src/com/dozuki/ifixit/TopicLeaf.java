package com.dozuki.ifixit;

import java.io.Serializable;

import java.util.ArrayList;

public class TopicLeaf implements Serializable {
   private static final long serialVersionUID = 1L;

   private String mName;
   private ArrayList<GuideInfo> mGuides;
   private int mSolutions;
   private String mSolutionsUrl;

   public TopicLeaf(String name) {
      mName = name;
      mGuides = new ArrayList<GuideInfo>();
   }

   public void addGuide(GuideInfo guideInfo) {
      mGuides.add(guideInfo);
   }

   public String getName() {
      return mName;
   }

   public ArrayList<GuideInfo> getGuides() {
      return mGuides;
   }

   public void setSolutionsUrl(String url) {
      mSolutionsUrl = url;
   }

   public void setNumSolutions(int solutions) {
      mSolutions = solutions;
   }

   public String getSolutionsUrl() {
      return mSolutionsUrl;
   }

   public int getNumSolutions() {
      return mSolutions;
   }

   public String toString() {
      return mName + ", " + mGuides;
   }
}
