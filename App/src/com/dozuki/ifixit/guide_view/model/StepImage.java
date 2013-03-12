package com.dozuki.ifixit.guide_view.model;

import java.io.Serializable;

import android.util.Log;
import com.dozuki.ifixit.guide_create.model.ImageObject;

public class StepImage implements Serializable {
   private static final long serialVersionUID = 6728708938023120624L;
   protected int mImageid;
   protected int mOrderby;
   protected String mText;
   protected ImageObject mImageObject = new ImageObject();


    public StepImage()
    {

    }
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

   public void setImageId(int itemId) {
      mImageid = itemId;
   }

    public void setImageObject(ImageObject image) {
        mImageObject = image;
    }

    public ImageObject getImageObject() {
        return mImageObject;
    }
}
