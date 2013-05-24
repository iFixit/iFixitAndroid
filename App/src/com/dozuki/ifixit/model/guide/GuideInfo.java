package com.dozuki.ifixit.model.guide;

import com.dozuki.ifixit.model.APIImage;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class GuideInfo implements Serializable {
   private static final long serialVersionUID = 1L;

   @SerializedName("guideid")
   public int mGuideid;
   @SerializedName("revisionid")
   public int mRevisionid;
   @SerializedName("modified_date")
   public int mModifiedDate;
   @SerializedName("prereq_modified_date")
   public transient int mPrereqModifiedDate = 0;
   @SerializedName("type")
   public String mType;
   @SerializedName("category")
   public String mTopic;
   @SerializedName("subject")
   public String mSubject;
   @SerializedName("title")
   public String mTitle;
   @SerializedName("public")
   public boolean mPublic;
   @SerializedName("flags")
   public String[] mFlags;
   @SerializedName("image")
   public APIImage mImage;

   public transient boolean mEditMode = false;

   private String[] hasSubject = {"repair", "installation", "disassembly"};
   private String[] noSubject = {"technique", "maintenance", "teardown"};

   public GuideInfo(int guideid) {
      mGuideid = guideid;
   }

   public boolean hasSubject() {
      return !mSubject.equals("") && contains(mSubject, hasSubject);
   }

   public boolean hasFlag(String flagid) {
      return contains(flagid, mFlags);
   }

   private boolean contains(String needle, String[] haystack) {
      for (int i = 0; i < haystack.length; i++) {
         if (haystack[i].equals(needle)) {
            return true;
         }
      }
      return false;
   }

   public String toString() {
      return mGuideid + ", " + mSubject + ", " + mImage + ", " + mTitle +
       ", " + mType;
   }
}
