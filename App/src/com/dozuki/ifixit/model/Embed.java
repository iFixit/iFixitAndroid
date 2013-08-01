package com.dozuki.ifixit.model;

import com.dozuki.ifixit.model.guide.OEmbed;

import java.io.Serializable;

public class Embed implements Serializable {

   private static final long serialVersionUID = 1L;
   protected int mWidth;
   protected int mHeight;
   protected OEmbed mOEmbed;
   protected String mType;
   protected String mURL;
   protected String mContentURL;

   public Embed(int width, int height, String type, String url) {
      mWidth = width;
      mHeight = height;
      mType = type;
      mURL = url;
   }

   public String getURL() {
      return mURL;
   }
   
   public int getWidth() {
      return mWidth;
   }
   
   public int getHeight() {
      return mHeight;
   }

   public void addOembed(OEmbed oe) {
      mOEmbed = oe;
   }

   public boolean hasOembed() {
      return mOEmbed != null;
   }

   public OEmbed getOembed() {
      return mOEmbed;
   }

   public String getType() {
      return mType;
   }

   public void setContentURL(String url) {
      mContentURL = url;
   }

   public String getContentURL() {
      return mContentURL;
   }
}
