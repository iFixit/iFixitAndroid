package com.dozuki.ifixit.util;

import android.content.Context;
import com.squareup.okhttp.HttpResponseCache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpLoader;
import com.squareup.picasso.Picasso;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;

public class PicassoUtils {
   private static Picasso singleton = null;

   public static Picasso with(Context context) {
      // Mimicking Picasso's new OkHttpLoader(context), but with our custom OkHttpClient
      if (singleton == null) {
         OkHttpClient client = createClient();
         try {
            client.setResponseCache(createResponseCache(context));
         } catch (IOException ignored) {
         }
         singleton = new Picasso.Builder(context).loader(new OkHttpLoader(client)).build();
      }
      return singleton;
   }

   private static OkHttpClient createClient() {
      OkHttpClient client = new OkHttpClient();

      // Working around the libssl crash: https://github.com/square/okhttp/issues/184
      SSLContext sslContext;
      try {
         sslContext = SSLContext.getInstance("TLS");
         sslContext.init(null, null, null);
      } catch (GeneralSecurityException e) {
         throw new AssertionError(); // The system has no TLS. Just give up.
      }
      client.setSslSocketFactory(sslContext.getSocketFactory());
      return client;
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

   private static int calculateDiskCacheSize(File dir) {
      try {
         final Class<?> clazz = Class.forName("com.squareup.picasso.Utils");
         final Method method = clazz.getDeclaredMethod("calculateDiskCacheSize", File.class);
         method.setAccessible(true);
         return (Integer) method.invoke(null, dir);
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
      int maxSize = calculateDiskCacheSize(cacheDir);
      return new HttpResponseCache(cacheDir, maxSize);
   }
}