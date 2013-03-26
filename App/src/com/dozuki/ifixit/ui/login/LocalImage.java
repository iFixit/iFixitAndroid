package com.dozuki.ifixit.ui.login;

import java.io.Serializable;

public class LocalImage implements Serializable {
   private static final long serialVersionUID = 1L;

   public String mImgid;
   public String mPath;

   public LocalImage(String imgid, String path) {
      mImgid = imgid;
      mPath = path;
   }

   public LocalImage(String path) {
      this(null, path);
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof LocalImage)) {
         return false;
      }

      return ((LocalImage)obj).mPath.equals(mPath);
   }
}
