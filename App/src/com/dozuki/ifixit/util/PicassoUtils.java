package com.dozuki.ifixit.util;

import android.content.Context;
import com.dozuki.ifixit.util.api.ApiSyncAdapter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;

public class PicassoUtils {
   /**
    * Helper methods for displaying a (potentially) offline image.
    */
   public static RequestCreator displayImage(Context context, String url, boolean offline) {
      return displayImage(Picasso.with(context), url, offline);
   }

   public static RequestCreator displayImage(Picasso picasso, String url, boolean offline) {
      if (offline) {
         return picasso.load(new File(ApiSyncAdapter.getOfflineMediaPath(url)));
      } else {
         return picasso.load(url);
      }
   }
}
