package com.ifixit.guidebook;

public class StepImage {
   protected int mImageid;
   protected int mOrderby;
   protected String mText;

   public StepImage(int imageid) {
      mImageid = imageid;
   }

   public void setOrderby(int orderby) {
      mOrderby = orderby;
   }

   public void setText(String text) {
      mText = text;
   }

   public String toString() {
      return "{StepImage: " + mImageid + ", " + mOrderby + ", " + mText + "}";
   }
}
