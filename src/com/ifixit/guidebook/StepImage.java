package com.ifixit.guidebook;

import java.io.Serializable;

public class StepImage implements Serializable {
   private static final long serialVersionUID = 6728708938023120624L;
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

   public String getText() {
      return mText;
   }

   public int getImageid() {
      return mImageid;
   }

   public String toString() {
      return "{StepImage: " + mImageid + ", " + mOrderby + ", " + mText + "}";
   }
}
