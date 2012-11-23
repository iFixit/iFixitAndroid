package com.dozuki.ifixit.guide_view.model;

import java.io.Serializable;
import java.util.ArrayList;

import android.os.Bundle;

public class StepVideo implements Serializable {

   /**
	 * 
	 */
   private static final long serialVersionUID = 1L;
   protected ArrayList<VideoEncoding> mEncodings = new ArrayList<VideoEncoding>();
   protected String mThumbnail;

   public void addEncoding(VideoEncoding parseVideoEncoding) {
      mEncodings.add(parseVideoEncoding);
   }

   public void setThumbnail(String url) {
      mThumbnail = url;

   }

   public String getThumbnail() {
      return mThumbnail;
   }

   public ArrayList<VideoEncoding> getEncodings() {

      return mEncodings;
   }
}
