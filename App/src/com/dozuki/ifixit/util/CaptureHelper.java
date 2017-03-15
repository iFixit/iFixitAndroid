package com.dozuki.ifixit.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.ui.guide.create.StepEditActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CaptureHelper {

   public static final int CAMERA_REQUEST_CODE = 1888;
   public static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

   public static File createImageFile(Activity activity) throws IOException {
      // Create an image file name
      String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
      String imageFileName = "JPEG_" + timeStamp + "_";
      File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
      File image = File.createTempFile(
       imageFileName,  /* prefix */
       ".jpg",         /* suffix */
       storageDir      /* directory */
      );

      return image;
   }

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
