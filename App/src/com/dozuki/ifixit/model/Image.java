package com.dozuki.ifixit.model;

import com.dozuki.ifixit.MainApplication;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class Image implements Serializable {
   private static final long serialVersionUID = 772113480839309007L;

   /**
    * Local images (captured by the camera) will not have an imageid until it's been uploaded and processed by the
    * server.  Until then, we set the imageid to -1 so we can tell which image needs to be updated.
    */
   private static final int LOCAL_IMAGE_ID = -1;
   private static final String TAG = "Image";
   //private final ImageSizes mSizes;

   @SerializedName("id") private int mId;
   @SerializedName("original") private String mPath;

   public Image() {
      this(LOCAL_IMAGE_ID);
   }

   public Image(int id) {
      this(id, "");
   }

   public Image(int id, String path) {
      mId = id;
      mPath = cleanPath(path);
   }

   public void setLocalImage(String path) {
      mId = LOCAL_IMAGE_ID;
      mPath = cleanPath(path);
   }

   public void setId(int id) {
      mId = id;
   }

   public int getId() {
      return mId;
   }

   public void setPath(String path) {
      mPath = cleanPath(path);
   }

   public String getPath() {
      return getPath("");
   }

   public String getPath(String size) {
      if (size.length() != 0 && !size.startsWith(".")) {
         size = size + ".";
      }

      return mPath + size;
   }

   public boolean isLocal() {
      return mId == LOCAL_IMAGE_ID;
   }

   @Override
   public String toString() {
      return "{\n" +
       "path: " + mPath + ",\n" +
       "id: " + mId + "\n" +
       "}";
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof Image)) {
         return false;
      }

      return ((Image)obj).getId() == mId && ((Image)obj).getPath().equals(mPath);
   }

   private String cleanPath(String path) {

      if (MainApplication.inDebug() && path.length() != 0)
         path = path.replaceFirst("https","http");

      return path;
   }
}
