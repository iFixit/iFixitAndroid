package com.dozuki.ifixit.guide_view.model;

import com.dozuki.ifixit.util.APIImage;

import java.io.Serializable;

public class GuideInfo implements Serializable {
   private static final long serialVersionUID = 1L;

   public int mGuideid;
   public int mRevisionid;
   public int mModifiedDate;
   public int mPrereqModifiedDate;
   public String mType;
   public String mTopic;
   public String mSubject;
   public String mTitle;
   public boolean mPublic;
   public APIImage mImage;

   public GuideInfo(int guideid) {
      mGuideid = guideid;
   }
   public String toString() {
      return mGuideid + ", " + mSubject + ", " + mImage + ", " + mTitle +
       ", " + mType;
   }
}
