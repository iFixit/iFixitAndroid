package com.dozuki.ifixit.view.model;

import java.io.Serializable;

public class UploadedImageInfo implements Serializable {
   private static final long serialVersionUID = -4960272337013311382L;

   String mGuid;
   String mImageid;
   String status;

   public String getmGuid() {
      return mGuid;
   }

   public void setmGuid(String mGuid) {
      this.mGuid = mGuid;
   }

   public String getmImageid() {
      return mImageid;
   }

   public void setmImageid(String mImageid) {
      this.mImageid = mImageid;
   }

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }
}
