package com.dozuki.ifixit.util;

import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

public class Utils {
   public static void stripImageView(ImageView view) {
      if ( view.getDrawable() instanceof BitmapDrawable) {
         ((BitmapDrawable)view.getDrawable()).getBitmap().recycle();
      }

      if (view.getDrawable() != null)
         view.getDrawable().setCallback(null);

      view.setImageDrawable(null);
      view.getResources().flushLayoutCache();
      view.destroyDrawingCache();
   }
}
