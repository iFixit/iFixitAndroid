package com.dozuki.ifixit.guide_create.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.dozuki.ifixit.guide_view.model.Guide;
import com.dozuki.ifixit.guide_view.model.GuideStep;

public class GuideCreateObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -356754234536788271L;
   private static final String stepList = null;
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
	protected boolean mEditMode;
	protected boolean mPublished;
	protected ArrayList<GuideCreateStepObject> mStepList;

	public GuideCreateObject(int guideid) {
		mGuideid = guideid;
	}

   public GuideCreateObject(Guide guide) {
      mStepList = new ArrayList<GuideCreateStepObject>();
      mGuideid = guide.getGuideid();
      mTitle = guide.getTitle();
      mTopic = guide.getTopic();
      mAuthor = guide.getAuthor();
      mTimeRequired = guide.getTimeRequired();
      mDifficulty = guide.getDifficulty();
      mIntroduction = guide.getIntroduction();
      mSubject = guide.getSubject();
      mIntroImage = guide.getIntroImage();
      mSummary = guide.getSummary();

      for (GuideStep gs : guide.getStepList()) {
         mStepList.add(new GuideCreateStepObject(gs));
      }
   }

	public void setStepList(ArrayList<GuideCreateStepObject> stepList)
	{
		mStepList = stepList;
	}
	
	public void deleteStep(GuideCreateStepObject step)
	{
		mStepList.remove(step);
	}
	
	public ArrayList<GuideCreateStepObject> getSteps()
	{
		return mStepList;
	}

	public void setEditMode(boolean editMode) {
		mEditMode = editMode;
	}

	public boolean getEditMode() {
		return mEditMode;
	}
	
	public void setPublished(boolean  published) {
		 mPublished =  published;
	}

	public boolean getPublished() {
		return  mPublished;
	}

	public void setGuideid(int guideid) {
		mGuideid = guideid;
	}

	public int getGuideid() {
		return mGuideid;
	}

	public void setTitle(String title) {
		if (title.isEmpty())
			title = "Default Title";
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
		return "{" + mGuideid + "\n" + mTitle + "\n" + mTopic + "\n" + mAuthor
				+ "\n" + mTimeRequired + "\n" + mDifficulty + "\n"
				+ mIntroduction + "\n" + mSummary + "\n" + mSummary + "}";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof GuideCreateObject)) {
			return false;
		}
		GuideCreateObject lhs = (GuideCreateObject) o;
		return mGuideid == lhs.mGuideid;
	}

   public void sync(GuideCreateStepObject changedStep) {
      for (GuideCreateStepObject so : mStepList) {
          if(so.equals(changedStep))
          {
             so = changedStep;
             return;
          }
      }
      
      mStepList.add(changedStep);

   }
}
