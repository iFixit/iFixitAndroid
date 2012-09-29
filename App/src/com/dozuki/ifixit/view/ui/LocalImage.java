package com.dozuki.ifixit.view.ui;

import java.io.Serializable;

public class LocalImage implements Serializable {
   private static final long serialVersionUID = 1L;

   String imgId;
   String path;

   public LocalImage(String id, String p) {
      imgId = id;
      path = p;
   }

   public LocalImage(String p) {
      path = p;
      imgId = null;
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

      return (inf.path.equals(this.path));
   }
}
