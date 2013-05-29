package com.dozuki.ifixit.model.guide;

import com.dozuki.ifixit.model.APIImage;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class GuideInfo implements Serializable {
   private static final long serialVersionUID = 1L;

   @SerializedName("guideid")
   public int mGuideid;
   @SerializedName("revisionid")
   public int mRevisionid;
   @SerializedName("modified_date")
   public int mModifiedDate;
   @SerializedName("prereq_modified_date")
   public int mPrereqModifiedDate;
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


   public GuideInfo(int guideid) {
      mGuideid = guideid;
   }

   public boolean hasSubject() {
      List<String> hasSubject = Arrays.asList(new String[]{"repair", "installation", "disassembly"});
      List<String> noSubject = Arrays.asList(new String[]{"technique", "maintenance", "teardown"});

      if (mSubject == null) {
         return false;
      }
      return !mSubject.equals("") && hasSubject.contains(mType.toLowerCase());
   }

   public boolean hasFlag(String flagid) {
      return false;
   }

   public String toString() {
      return mGuideid + ", " + mSubject + ", " + mImage + ", " + mTitle +
       ", " + mType + ", " + mFlags.toString();
   }
}
