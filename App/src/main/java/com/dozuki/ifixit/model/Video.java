package com.dozuki.ifixit.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Video implements Serializable {

   private static final long serialVersionUID = 2L;
   protected ArrayList<VideoEncoding> mEncodings = new ArrayList<VideoEncoding>();
   protected VideoThumbnail mThumbnail;
   private int id;
   private String filename;
   private int width;
   private int height;
   private int duration;

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

   public int getHeight() {
      return height;
   }

   public void setHeight(int height) {
      this.height = height;
   }

   public int getWidth() {
      return width;
   }

   public void setWidth(int width) {
      this.width = width;
   }

   public String getFilename() {
      return filename;
   }

   public void setFilename(String filename) {
      this.filename = filename;
   }

   public int getId() {
      return id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public void setDuration(int duration) {
      this.duration = duration;
   }
}
