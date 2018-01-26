package com.dozuki.ifixit.util;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.model.dozuki.Site;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CaptureHelper {

   public static final int CAMERA_REQUEST_CODE = 1888;
   public static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

   public static File createImageFile(Activity activity) throws IOException {
      // Create an image file name
      String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
      String imageFileName = "JPEG_" + timeStamp + "_";
      File storageDir = getAlbumDir(activity);
      File image = File.createTempFile(
       imageFileName,  /* prefix */
       ".jpg",         /* suffix */
       storageDir      /* directory */
      );

      return image;
   }

   private static File getAlbumDir(Activity activity) {
      String directoryName = getDirectoryName();
      File storageDir = null;

      if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
         storageDir = new File(activity.getExternalFilesDir(
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
      return site.mName + "Images";
   }

   public static String getFileName() {
      return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
   }
}
