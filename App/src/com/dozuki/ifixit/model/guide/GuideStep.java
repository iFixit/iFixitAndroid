package com.dozuki.ifixit.model.guide;

import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.APIImage;

import java.io.Serializable;
import java.util.ArrayList;

public class GuideStep implements Serializable {
   private static final long serialVersionUID = 2884598684003517264L;

   /**
    * Guide that this step originates from. Can either be the guide currently
    * being viewed or one of its prerequisites.
    */
   protected int mGuideid;

   /**
    * The unique stepid that identifies this step.
    */
   protected int mStepid;

   /**
    * The step's revisionid or null if this is a new step.
    */
   protected Integer mRevisionid;

   /**
    * The step's 1-indexed orderby in the original guide.
    */
   protected int mOrderby;

   /**
    * 1-indexed step number for this step. This changes if prerequisites
    * are included in the guide.
    */
   protected int mStepNum;

   protected String mTitle;
   protected ArrayList<StepImage> mImages;
   protected ArrayList<APIImage> mAPIImages;
   protected ArrayList<StepLine> mLines;
   protected StepVideo mVideo;
   private Embed mEmbed;

   public GuideStep(int stepNum) {
      mStepNum = stepNum;
      mImages = new ArrayList<StepImage>();
      mAPIImages = new ArrayList<APIImage>();
      mLines = new ArrayList<StepLine>();
   }

   public void setGuideid(int guideid) {
      mGuideid = guideid;
   }

   public void setStepid(int stepid) {
      mStepid = stepid;
   }

   public void setRevisionid(Integer revisionid) {
      mRevisionid = revisionid;
   }

   public void setOrderby(int orderby) {
      mOrderby = orderby;
   }

   public void setTitle(String title) {
      mTitle = title;
   }

   public String getTitle() {
      if (mTitle.length() == 0) {
         mTitle = MainApplication.get().getResources().getString(R.string.step) + " " + mStepNum;
      }
      
      return mTitle;
   }

   public int getStepNum() {
      return mStepNum;
   }
   
   public boolean hasImage() {
      return mImages.size() > 0;
   }

   public boolean hasAPIImage() {
      return mAPIImages.size() > 0;
   }

   public void addImage(StepImage image) {
      mImages.add(image);
   }

   public void addAPIImage(APIImage image) {
      mAPIImages.add(image);
   }

   public ArrayList<StepImage> getImages() {
      return mImages;
   }

   public ArrayList<APIImage> getAPIImages() {
      return mAPIImages;
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


   public void addVideo(StepVideo stepvid) {
      mVideo = stepvid;
   }

   public boolean hasVideo() {
      return mVideo != null;
   }

   public StepVideo getVideo() {
      return mVideo;
   }

   public void addEmbed(Embed parseEmbed) {
      mEmbed = parseEmbed;
   }

   public boolean hasEmbed() {
      return mEmbed != null;
   }

   public Embed getEmbed() {
      return mEmbed;
   }

   public String toString() {
      return "{GuideStep: " + mGuideid + ", " + mStepid + ", " + mRevisionid + ", " +
       mOrderby + ", " + mStepNum + ", " + mTitle +  ", " + mLines + ", " + mImages + "}";
   }


   public Integer getRevisionid() {
      return mRevisionid;
   }

   public Integer getStepid() {
      return mStepid;
   }
}
