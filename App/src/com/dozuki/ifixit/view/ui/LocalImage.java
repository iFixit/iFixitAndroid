package com.dozuki.ifixit.view.ui;

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
      if (obj == null)
         return false;
      if (obj == this)
         return true;
      if (obj.getClass() != getClass())
         return false;
      LocalImage inf = (LocalImage) obj;

      return inf.mPath.equals(this.mPath);
   }
}
