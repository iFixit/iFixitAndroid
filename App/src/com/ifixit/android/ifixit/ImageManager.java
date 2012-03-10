package com.ifixit.android.ifixit;

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
   private static final int MAX_STORED_IMAGES = 9;
   private static final int MAX_LOADING_IMAGES = 9;
   private static final int DEFAULT_NUM_DOWNLOAD_THREADS = 5;
   private static final int DEFAULT_NUM_WRITE_THREADS = 2;

   private HashMap<String, Bitmap> mImageMap;
   private HashMap<String, ImageRef> mLoadingImages;
   private LinkedList<BitmapFile> mWriteQueue;
   private LinkedList<String> mRecentBitmaps;
   private File mCacheDir;
   private ImageQueue mImageQueue;
   private Thread[] mDownloadThreads;
   private Thread[] mWriteThreads;
   private final int mNumDownloadThreads;
   private final int mNumWriteThreads;

   public ImageManager(Context context) {
      this(context, DEFAULT_NUM_DOWNLOAD_THREADS, DEFAULT_NUM_WRITE_THREADS);
   }

   public ImageManager(Context context, int downloadThreads, int writeThreads) {
      mNumDownloadThreads = downloadThreads;
      mNumWriteThreads = writeThreads;
      mImageMap = new HashMap<String, Bitmap>();
      mWriteQueue = new LinkedList<BitmapFile>();
      mRecentBitmaps = new LinkedList<String>();
      mImageQueue = new ImageQueue();
      mDownloadThreads = new Thread[mNumDownloadThreads];
      mWriteThreads = new Thread[mNumWriteThreads];
      mLoadingImages = new HashMap<String, ImageRef>();

      for (int i = 0; i < mDownloadThreads.length; i++) {
         mDownloadThreads[i] = new Thread(new ImageQueueManager());
         mDownloadThreads[i].setPriority(IMAGE_THREAD_PRIORITY);
         mDownloadThreads[i].start();
      }

      for (int i = 0; i < mWriteThreads.length; i ++) {
         mWriteThreads[i] = new Thread(new BitmapWriter());
         mWriteThreads[i].setPriority(IMAGE_THREAD_PRIORITY);
         mWriteThreads[i].start();
      }

      mCacheDir = context.getCacheDir();

      if (!mCacheDir.exists()) {
         mCacheDir.mkdirs();
      }
   }

   public void displayImage(String url, Activity activity,
    LoaderImage imageView) {
      Bitmap bitmap = mImageMap.get(url);
      if (bitmap != null) {
         imageView.setImageBitmap(bitmap);
      }
      else {
         queueImage(url, activity, imageView);
      }
   }

   private void queueImage(String url, Activity activity,
    LoaderImage imageView) {
      ImageRef imageRef;

      synchronized (mLoadingImages) {
         imageRef = mLoadingImages.get(url);
         if (imageRef != null) {
            imageRef.addImage(imageView);
            return;
         }
      }

      synchronized (mImageQueue.imageRefs) {
         for (ImageRef image : mImageQueue.imageRefs) {
            if (image.getUrl().equals(url)) {
               image.addImage(imageView);
               return;
            }
         }

         imageRef = new ImageRef(url, imageView);

         if (mImageQueue.imageRefs.size() > MAX_LOADING_IMAGES)
            mImageQueue.imageRefs.removeLast();

         mImageQueue.imageRefs.push(imageRef);
         mImageQueue.imageRefs.notify();
      }
   }

   public String getFilePath(String url) {
      File file = new File(mCacheDir, getFileName(url));

      if (file.exists()) {
         return file.getAbsolutePath();
      } else {
         return null;
      }
   }

   private String getFileName(String url) {
      return String.valueOf(url.hashCode()) + ".png";
   }

   private Bitmap getBitmap(String url) {
      String filename = getFileName(url);
      File file = new File(mCacheDir, filename);
      URLConnection connection;
      Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());

      if (bitmap != null)
         return bitmap;

      try {
         connection = new URL(url).openConnection();
         bitmap = BitmapFactory.decodeStream(connection.getInputStream());
         addToWriteQueue(bitmap, file);

         return bitmap;
      }
      catch (Exception e) {
         return null;
      }
   }

   private void addToWriteQueue(Bitmap bitmap, File file) {
      synchronized (mWriteQueue) {
         mWriteQueue.push(new BitmapFile(bitmap, file));
         mWriteQueue.notify();
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
      if (mRecentBitmaps.size() >= MAX_STORED_IMAGES) {
         mImageMap.remove(mRecentBitmaps.removeFirst());
      }

      if (mImageMap.put(url, bitmap) == null) {
         mRecentBitmaps.addLast(url);
      }
   }

   private class BitmapFile {
      protected Bitmap mBitmap;
      protected File mFile;

      public BitmapFile(Bitmap bitmap, File file) {
         mBitmap = bitmap;
         mFile = file;
      }
   }

   private class ImageRef {
      protected String mUrl;
      protected LinkedList<LoaderImage> mImageViews;

      public ImageRef(String url, LoaderImage imageView) {
         mUrl = url;
         mImageViews = new LinkedList<LoaderImage>();
         addImage(imageView);
      }

      public void addImage(LoaderImage imageView) {
         mImageViews.addFirst(imageView);
      }

      public LinkedList<LoaderImage> getImageViews() {
         return mImageViews;
      }

      public String getUrl() {
         return mUrl;
      }
   }

   private class ImageQueue {
      public LinkedList<ImageRef> imageRefs = new LinkedList<ImageRef>();
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
               synchronized (mImageQueue.imageRefs) {
                  if (mImageQueue.imageRefs.size() == 0)
                     mImageQueue.imageRefs.wait();
               }

               synchronized (mImageQueue.imageRefs) {
                  if (mImageQueue.imageRefs.size() == 0)
                     continue;

                  imageToLoad = mImageQueue.imageRefs.pop();
                  synchronized (mLoadingImages) {
                     mLoadingImages.put(imageToLoad.getUrl(), imageToLoad);
                  }
               }

               bitmap = getBitmap(imageToLoad.getUrl());
               storeImage(imageToLoad.getUrl(), bitmap);

               bitmapDisplayer = new BitmapDisplayer(bitmap,
                imageToLoad.getImageViews(), imageToLoad.getUrl());
               activity = (Activity)imageToLoad.getImageViews().get(0).
                getContext();
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

   private class BitmapWriter implements Runnable {
      @Override
      public void run() {
         BitmapFile bitmapFile;

         try {
            while (true) {
               synchronized (mWriteQueue) {
                  if (mWriteQueue.size() == 0) {
                     mWriteQueue.wait();
                  }
               }

               synchronized (mWriteQueue) {
                  if (mWriteQueue.size() == 0) {
                     continue;
                  }

                  bitmapFile = mWriteQueue.pop();
               }

               writeFile(bitmapFile.mBitmap, bitmapFile.mFile);

               bitmapFile = null;
            }
         } catch (InterruptedException e) {}
      }
   }

   private class BitmapDisplayer implements Runnable {
      Bitmap mBitmap;
      LinkedList<LoaderImage> mImageViews;
      String mUrl;

      public BitmapDisplayer(Bitmap bitmap, LinkedList<LoaderImage> imageViews,
       String url) {
         mBitmap = bitmap;
         mImageViews = imageViews;
         mUrl = url;
      }

      public void run() {
         if (mBitmap != null) {
            for (LoaderImage image : mImageViews) {
               image.setImageBitmap(mBitmap);
            }
         }
         else {
            for (LoaderImage image : mImageViews) {
               image.setImageResource(R.drawable.loading);
            }
         }

         synchronized (mLoadingImages) {
            mLoadingImages.remove(mUrl);
         }
      }
   }
}
