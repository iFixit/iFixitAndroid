package com.dozuki.ifixit.view.model;

import java.io.Serializable;

public class UploadedImageInfo implements Serializable {
   private static final long serialVersionUID = -4960272337013311382L;

   private String mGuid;
   private String mImageid;
   private String mStatus;

   public String getmGuid() {
      return mGuid;
   }

   public void setmGuid(String guid) {
      mGuid = guid;
   }

   public String getmImageid() {
      return mImageid;
   }

   public void setmImageid(String imageid) {
      mImageid = imageid;
   }

   public String getStatus() {
      return mStatus;
   }

   public void setStatus(String status) {
      mStatus = status;
   }
}
