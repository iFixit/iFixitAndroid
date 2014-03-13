package com.dozuki.ifixit.util;

import android.content.Context;

import com.dozuki.ifixit.util.api.ApiSyncAdapter;
import com.squareup.okhttp.HttpResponseCache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PicassoUtils {
   private static Picasso singleton = null;

   public static Picasso with(Context context) {
      // Mimicking Picasso's new OkHttpLoader(context), but with our custom OkHttpClient
      if (singleton == null) {
         OkHttpClient client = Utils.createOkHttpClient();
         try {
            client.setResponseCache(createResponseCache(context));
         } catch (IOException ignored) {
            // Ignored
            // throw new RuntimeException();
         }
         singleton = new Picasso.Builder(context).downloader(new OkHttpDownloader(client)).build();
      }
      return singleton;
   }

   private static File createDefaultCacheDir(Context context) {
      try {
         final Class<?> clazz = Class.forName("com.squareup.picasso.Utils");
         final Method method = clazz.getDeclaredMethod("createDefaultCacheDir", Context.class);
         method.setAccessible(true);
         return (File) method.invoke(null, context);
      } catch (ClassNotFoundException e) {
         throw new RuntimeException(e); // shouldn't happen
      } catch (NoSuchMethodException e) {
         throw new RuntimeException(e); // shouldn't happen
      } catch (InvocationTargetException e) {
         throw new RuntimeException(e); // shouldn't happen
      } catch (IllegalAccessException e) {
         throw new RuntimeException(e); // shouldn't happen
      }
   }

   private static long calculateDiskCacheSize(File dir) {
      try {
         final Class<?> clazz = Class.forName("com.squareup.picasso.Utils");
         final Method method = clazz.getDeclaredMethod("calculateDiskCacheSize", File.class);
         method.setAccessible(true);
         return (Long) method.invoke(null, dir);
      } catch (ClassNotFoundException e) {
         throw new RuntimeException(e); // shouldn't happen
      } catch (NoSuchMethodException e) {
         throw new RuntimeException(e); // shouldn't happen
      } catch (InvocationTargetException e) {
         throw new RuntimeException(e); // shouldn't happen
      } catch (IllegalAccessException e) {
         throw new RuntimeException(e); // shouldn't happen
      }
   }

   private static HttpResponseCache createResponseCache(Context context) throws IOException {
      File cacheDir = createDefaultCacheDir(context);
      long maxSize = calculateDiskCacheSize(cacheDir);
      return new HttpResponseCache(cacheDir, maxSize);
   }

   /**
    * Helper methods for displaying a (potentially) offline image.
    */
   public static RequestCreator displayImage(Context context, String url, boolean offline) {
      return displayImage(with(context), url, offline);
   }

   public static RequestCreator displayImage(Picasso picasso, String url, boolean offline) {
      if (offline) {
         return picasso.load(new File(ApiSyncAdapter.getOfflineMediaPath(url)));
      } else {
         return picasso.load(url);
      }
   }
}
