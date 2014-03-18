package com.dozuki.ifixit.util;

import android.util.Log;

import com.dozuki.ifixit.App;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * Quick and dirty file cache.
 */
public class FileCache {
   public static String get(String key) {
      File file = getFile(key);

      if (!file.exists()) {
         return null;
      }

      FileInputStream in = null;
      try {
         in = new FileInputStream(file);
         // Read the entire file into a String.
         Scanner scanner = new Scanner(in).useDelimiter("\\A");
         return scanner.hasNext() ? scanner.next() : null;
      } catch (IOException e) {
         Log.e("FileCache", "Get", e);
         return null;
      } finally {
         if (in != null) {
            try {
               in.close();
            } catch (IOException e) {
               Log.e("FileCache", "Closing input", e);
            }
         }
      }
   }

   public static void set(String key, String value) {
      FileOutputStream out = null;
      try {
         out = new FileOutputStream(getFile(key));
         byte[] bytes = value.getBytes();
         out.write(bytes, 0, bytes.length);
      } catch (IOException e) {
         Log.e("FileCache", "Set", e);
      } finally {
         if (out != null) {
            try {
               out.close();
            } catch (IOException e) {
               Log.e("FileCache", "Closing output", e);
            }
         }
      }
   }

   private static File getFile(String key) {
      return new File(getCacheDir(), String.valueOf(key.hashCode()));
   }

   private static File sCacheDir;
   private static File getCacheDir() {
      if (sCacheDir == null) {
         sCacheDir = App.get().getCacheDir();
      }

      return sCacheDir;
   }
}
