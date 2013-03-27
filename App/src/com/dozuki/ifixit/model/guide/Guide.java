package com.dozuki.ifixit.model.guide;

import com.dozuki.ifixit.util.APIImage;

import java.io.Serializable;
import java.util.ArrayList;

public class Guide implements Serializable {
   private static final long serialVersionUID = -1965203088124961695L;

   protected int mGuideid;
   /**
    * Guide's revisionid or null if this is a new guide.
    */
   protected Integer mRevisionid;
   protected String mTitle;
   protected boolean mPublic;
   protected String mTopic;
   protected String mAuthor;
   protected String mTimeRequired;
   protected String mDifficulty;
   protected String mIntroductionRendered;
   protected String mIntroductionRaw;
   protected String mSubject;
   protected APIImage mIntroImage;
   protected String mSummary;
   protected ArrayList<GuideStep> mSteps;
   protected ArrayList<GuideTool> mTools;
   protected ArrayList<GuidePart> mParts;
   protected boolean mEditMode = false;

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

   public void setStepList(ArrayList<GuideStep> steps) {
      mSteps = steps;
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

   public void deleteStep(GuideStep step) {
      mSteps.remove(step);
   }

   public void setEditMode(boolean editMode) {
      mEditMode = editMode;
   }

   public boolean getEditMode() {
      return mEditMode;
   }

   public void setPublic(boolean isPublic) {
      mPublic = isPublic;
   }

   public boolean getPublic() {
      return mPublic;
   }

   public void setGuideid(int guideid) {
      mGuideid = guideid;
   }


   public int getNumSteps() {
      return mSteps.size();
   }

   public ArrayList<GuideStep> getSteps() {
      return mSteps;
   }

   public GuideStep getStep(int position) {
      return mSteps.get(position);
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

   public void setIntroductionRendered(String introductionRendered) {
      mIntroductionRendered = introductionRendered;
   }

   public void setIntroductionRaw(String introductionRaw) {
      mIntroductionRaw = introductionRaw;
   }

   public String getIntroductionRaw() {
      return mIntroductionRaw;
   }

   public String getIntroductionRendered() {
      return mIntroductionRendered;
   }

   public void setIntroImage(APIImage image) {
      mIntroImage = image;
   }

   public APIImage getIntroImage() {
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

   public void setRevisionid(Integer revisionid) {
      mRevisionid = revisionid;
   }

   public Integer getRevisionid() {
      return mRevisionid;
   }

   public ArrayList<GuideStep> getStepList() {
      return mSteps;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (!(o instanceof Guide)) {
         return false;
      }
      Guide lhs = (Guide) o;
      return mGuideid == lhs.mGuideid;
   }

   public void sync(GuideStep changedStep, int position) {

      if(mSteps.contains(changedStep)) {
         mSteps.get(position).setTitle(changedStep.getTitle());
         mSteps.get(position).setImages(changedStep.getImages());
         mSteps.get(position).setLines(changedStep.getLines());
         return;
      }

      mSteps.add(position, changedStep);
   }

   public String toString() {
      return "{" + mGuideid + "\n" + mRevisionid + "\n" + mTitle + "\n" + mTopic + "\n" +
       mAuthor + "\n" + mTimeRequired + "\n" + mDifficulty + "\n" + mIntroductionRendered + "\n" +
       mSummary + "\n\n" + mSteps + "\n" + mSummary + "}";
   }
}
