package com.ifixit.guidebook;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Based largely on cacois's example:
 * http://codehenge.net/blog/2011/06/android-development-tutorial-
 * asynchronous-lazy-loading-and-caching-of-listview-images/
 */
public class ImageManager {
   private static final int IMAGE_THREAD_PRIORITY = Thread.NORM_PRIORITY - 1;
   private static final int MAX_STORED_IMAGES = 20;
   private static final int MAX_LOADING_IMAGES = 15;
   private static final int DEFAULT_NUM_THREADS = 5;

   private HashMap<String, Bitmap> mImageMap;
   private LinkedList<String> mRecentBitmaps;
   private File mCacheDir;
   private ImageQueue mImageQueue;
   private Thread[] mThreads;
   private final int mNumThreads;

   public ImageManager(Context context) {
      this(context, DEFAULT_NUM_THREADS);
   }

   public ImageManager(Context context, int numThreads) {
      mNumThreads = numThreads;
      mImageMap = new HashMap<String, Bitmap>();
      mRecentBitmaps = new LinkedList<String>();
      mImageQueue = new ImageQueue();
      mThreads = new Thread[mNumThreads];

      for (int i = 0; i < mNumThreads; i++) {
         mThreads[i] = new Thread(new ImageQueueManager());
         mThreads[i].setPriority(IMAGE_THREAD_PRIORITY);
         mThreads[i].start();
      }

      mCacheDir = context.getCacheDir();

      if (!mCacheDir.exists()) {
         mCacheDir.mkdirs(); 
      }
   }
   
   public void displayImage(String url, Activity activity,
    LoaderImage imageView) {
      if (mImageMap.containsKey(url)) {
         imageView.setImageBitmap(mImageMap.get(url));
      }
      else {
         queueImage(url, activity, imageView);
      }
   }

   private void queueImage(String url, Activity activity,
    LoaderImage imageView) {
      ImageRef imageRef;

      synchronized (mImageQueue.imageRefs) {
         mImageQueue.clean(imageView);
         imageRef = new ImageRef(url, imageView);

         if (mImageQueue.imageRefs.size() > MAX_LOADING_IMAGES)
            mImageQueue.imageRefs.removeLast();

         mImageQueue.imageRefs.push(imageRef);
         mImageQueue.imageRefs.notify();
      }
   }

   private Bitmap getBitmap(String url) {
      String filename = String.valueOf(url.hashCode());
      File file = new File(mCacheDir, filename);
      URLConnection connection;
      Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());

      if (bitmap != null)
         return bitmap;

      try {
         connection = new URL(url).openConnection();
         bitmap = BitmapFactory.decodeStream(connection.getInputStream());
         writeFile(bitmap, file);

         return bitmap;
      }
      catch (Exception e) {
         return null;
      }
   }

   private void writeFile(Bitmap bitmap, File file) {
      FileOutputStream out = null;

      try {
         out = new FileOutputStream(file);
         bitmap.compress(Bitmap.CompressFormat.PNG, 80, out);
      }
      catch (Exception e) {
         Log.e("ImageManager", "writeFile: " + e.getMessage());
      }
      finally {
         try {
            if (out != null)
               out.close();
         }
         catch (Exception ex) {}
      }
   }

   private void storeImage(String url, Bitmap bitmap) {
      if (mRecentBitmaps.size() >= MAX_STORED_IMAGES)
         mImageMap.remove(mRecentBitmaps.removeFirst());

      if (mImageMap.put(url, bitmap) == null)
         mRecentBitmaps.addLast(url);
   }

   private class ImageRef {
      public String url;
      public LoaderImage imageView;

      public ImageRef(String url, LoaderImage imageView) {
         this.url = url;
         this.imageView = imageView;
      }
   }

   private class ImageQueue {
      public LinkedList<ImageRef> imageRefs = new LinkedList<ImageRef>();

      public void clean(LoaderImage view) {
         for (int i = 0; i < imageRefs.size();) {
            if (imageRefs.get(i).imageView == view)
               imageRefs.remove(i);
            else
               i++;
         }
      }
   }

   private class ImageQueueManager implements Runnable {
      @Override
      public void run() {
         ImageRef imageToLoad;
         Bitmap bitmap;
         BitmapDisplayer bitmapDisplayer;
         Activity activity;

         try {
            while (true) {
               synchronized(mImageQueue.imageRefs) {
                  if (mImageQueue.imageRefs.size() == 0)
                     mImageQueue.imageRefs.wait();
               }

               synchronized(mImageQueue.imageRefs) {
                  if (mImageQueue.imageRefs.size() == 0)
                     continue;

                  imageToLoad = mImageQueue.imageRefs.pop();
               }

               bitmap = getBitmap(imageToLoad.url);
               storeImage(imageToLoad.url, bitmap);

               bitmapDisplayer = new BitmapDisplayer(bitmap,
                imageToLoad.imageView);
               activity = (Activity)imageToLoad.imageView.getContext();
               activity.runOnUiThread(bitmapDisplayer);

               imageToLoad = null;
               bitmap = null;
               activity = null;
               bitmapDisplayer = null;
            }
         }
         catch (InterruptedException e) {}
      }
   }

   private class BitmapDisplayer implements Runnable {
      Bitmap mBitmap;
      LoaderImage mImageView;

      public BitmapDisplayer(Bitmap bitmap, LoaderImage imageView) {
         mBitmap = bitmap;
         mImageView = imageView;
      }

      public void run() {
         if (mBitmap != null)
            mImageView.setImageBitmap(mBitmap);
         else 
            mImageView.setImageResource(R.drawable.loading);
      }
   }
}
