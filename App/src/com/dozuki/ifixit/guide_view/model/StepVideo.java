package com.dozuki.ifixit.guide_view.model;

import java.io.Serializable;
import java.util.ArrayList;

public class StepVideo implements Serializable {

   private static final long serialVersionUID = 2L;
   protected ArrayList<VideoEncoding> mEncodings = new ArrayList<VideoEncoding>();
   protected StepVideoThumbnail mThumbnail;

   public void addEncoding(VideoEncoding parseVideoEncoding) {
      mEncodings.add(parseVideoEncoding);
   }

   public void setThumbnail(StepVideoThumbnail thumb) {
      mThumbnail = thumb;
   }

   public StepVideoThumbnail getThumbnail() {
      return mThumbnail;
   }

   public ArrayList<VideoEncoding> getEncodings() {
      return mEncodings;
   }   
}
