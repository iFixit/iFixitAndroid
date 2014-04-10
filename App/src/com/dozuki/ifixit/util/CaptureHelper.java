package com.dozuki.ifixit.util;

import android.os.Environment;
import android.util.Log;
import com.dozuki.ifixit.App;
import com.dozuki.ifixit.model.dozuki.Site;
import com.google.analytics.tracking.android.MapBuilder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CaptureHelper {
   public static File getAlbumDir() {
      String directoryName = getDirectoryName();
      File storageDir = null;

      if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
         storageDir = new File(Environment.getExternalStoragePublicDirectory(
          Environment.DIRECTORY_PICTURES), directoryName + "/");

         if (!storageDir.mkdirs() && !storageDir.exists()) {
            Log.w("iFixit", "Failed to create directory " + directoryName);
            return null;
         }
      } else {
         App.sendException("CaptureHelper",
          "External storage is not mounted READ/WRITE", new Exception());
      }

      return storageDir;
   }

   private static String getDirectoryName() {
      Site site = App.get().getSite();

      return site.mTitle + "Images";
   }

   public static String getFileName() {
      return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
   }
}
