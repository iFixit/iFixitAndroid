package com.dozuki.ifixit.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Video implements Serializable {

   private static final long serialVersionUID = 2L;
   protected ArrayList<VideoEncoding> mEncodings = new ArrayList<VideoEncoding>();
   protected VideoThumbnail mThumbnail;

   public Video() {
      mEncodings = new ArrayList<VideoEncoding>();
   }

   public void addEncoding(VideoEncoding parseVideoEncoding) {
      mEncodings.add(parseVideoEncoding);
   }

   public void setThumbnail(VideoThumbnail thumb) {
      mThumbnail = thumb;
   }

   public VideoThumbnail getThumbnail() {
      return mThumbnail;
   }

   public ArrayList<VideoEncoding> getEncodings() {
      return mEncodings;
   }   
}
