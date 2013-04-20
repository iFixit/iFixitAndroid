package com.dozuki.ifixit.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;


public class CaptureHelper {
   public static final String IMAGE_PREFIX = "IFIXIT_GALLERY";

   public static File getAlbumDir() {
      File storageDir = null;
      if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
         storageDir = new File(Environment.getExternalStoragePublicDirectory(
          Environment.DIRECTORY_PICTURES), "iFixitImages/");

         if (storageDir != null && !storageDir.mkdirs() && !storageDir.exists()) {
            Log.w("iFixit", "Failed to create directory iFixitImages");
            return null;
         }
      } else {
         Log.w("iFixit", "External storage is not mounted READ/WRITE.");
      }

      return storageDir;
   }
}
