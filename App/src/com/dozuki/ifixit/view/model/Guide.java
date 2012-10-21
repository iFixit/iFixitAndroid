package com.dozuki.ifixit.view.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Guide implements Serializable {
   private static final long serialVersionUID = -1965203088124961695L;

   protected int mGuideid;
   protected String mTitle;
   protected String mTopic;
   protected String mAuthor;
   protected String mTimeRequired;
   protected String mDifficulty;
   protected String mIntroduction;
   protected String mSubject;
   protected String mIntroImage;
   protected String mSummary;
   protected ArrayList<GuideStep> mSteps;
   protected ArrayList<GuideTool> mTools;
   protected ArrayList<GuidePart> mParts;

   public Guide(int guideid) {
      mGuideid = guideid;
      mSteps = new ArrayList<GuideStep>();
      mTools = new ArrayList<GuideTool>();
      mParts = new ArrayList<GuidePart>();
   }

   public void addTool(GuideTool tool) {
      mTools.add(tool);
   }

   public int getNumTools() {
      return mTools.size();
   }

   public GuideTool getTool(int position) {
      return mTools.get(position);
   }

   public String getToolsFormatted(String title) {
      String formattedTools = title + ": <br />";
      for (GuideTool t : mTools) {
         formattedTools += "<a href=\"" + t.getUrl() + "\">"+ t.getTitle() +
          "</a><br />";
      }

      return formattedTools;
   }

   public void addPart(GuidePart part) {
      mParts.add(part);
   }

   public int getNumParts() {
      return mParts.size();
   }

   public GuidePart getPart(int position) {
      return mParts.get(position);
   }

   public String getPartsFormatted(String title) {
      String formattedPart = title + ": <br />";
      for (GuidePart t : mParts) {
         formattedPart += "<a href=\"" + t.getUrl() + "\">"+ t.getTitle() +
          "</a><br />";
      }

      return formattedPart;
   }

   public void addStep(GuideStep step) {
      mSteps.add(step);
   }

   public int getNumSteps() {
      return mSteps.size();
   }

   public GuideStep getStep(int position) {
      return mSteps.get(position);
   }

   public void setGuideid(int guideid) {
      mGuideid = guideid;
   }

   public int getGuideid() {
      return mGuideid;
   }

   public void setTitle(String title) {
      mTitle = title;
   }

   public String getTitle() {
      return mTitle;
   }

   public String getDisplayTitle() {
      if (!(mSubject.equals("null") || mSubject.length() == 0)) {
         return mSubject;
      } else {
         return mTitle;
      }
   }

   public void setTopic(String topic) {
      mTopic = topic;
   }

   public String getTopic() {
      return mTopic;
   }

   public void setAuthor(String author) {
      mAuthor = author;
   }

   public String getAuthor() {
      return mAuthor;
   }

   public void setTimeRequired(String timeRequired) {
      mTimeRequired = timeRequired;
   }

   public String getTimeRequired() {
      return mTimeRequired;
   }

   public void setDifficulty(String difficulty) {
      mDifficulty = difficulty;
   }

   public String getDifficulty() {
      return mDifficulty;
   }

   public void setIntroduction(String introduction) {
      mIntroduction = introduction;
   }

   public String getIntroduction() {
      return mIntroduction;
   }

   public void setIntroImage(String url) {
      mIntroImage = url;
   }

   public String getIntroImage() {
      return mIntroImage;
   }

   public void setSummary(String summary) {
      mSummary = summary;
   }

   public String getSummary() {
      return mSummary;
   }

   public String getSubject() {
      return mSubject;
   }

   public void setSubject(String subject) {
      mSubject = subject;
   }

   public String toString() {
      return "{" + mGuideid + "\n" + mTitle + "\n" + mTopic + "\n" + mAuthor +
       "\n" + mTimeRequired + "\n" + mDifficulty + "\n" + mIntroduction + "\n"
       + mSummary + "\n\n" + mSteps + "\n" + mSummary + "}";
   }
}
