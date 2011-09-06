package com.ifixit.guidebook;

import java.io.File;
import java.io.FileOutputStream;

import java.net.URL;

import java.util.HashMap;
import java.util.Stack;

import android.app.Activity;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.util.Log;

import android.widget.ImageView;

/**
 * Based largely on cacois's example:
 * http://codehenge.net/blog/2011/06/android-development-tutorial-
 * asynchronous-lazy-loading-and-caching-of-listview-images/
 */
public class ImageManager {
   private static final int IMAGE_THREAD_PRIORITY = Thread.NORM_PRIORITY - 1;

   private HashMap<String, Bitmap> mImageMap;
   private File mCacheDir;
   private ImageQueue mImageQueue;
   private Thread mImageLoaderThread;

   public ImageManager(Context context) {
      String sdState;

      mImageMap = new HashMap<String, Bitmap>();
      mImageQueue = new ImageQueue();
      mImageLoaderThread = new Thread(new ImageQueueManager());

      mImageLoaderThread.setPriority(IMAGE_THREAD_PRIORITY);

      sdState = android.os.Environment.getExternalStorageState();

      if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
         mCacheDir = new File(android.os.Environment.
          getExternalStorageDirectory(), "data/guidebook");
      }
      else
         mCacheDir = context.getCacheDir();

      if (!mCacheDir.exists()) {
         mCacheDir.mkdirs(); 
      }
   }

   public void displayImage(String url, Activity activity, ImageView imageView) {
      if (mImageMap.containsKey(url))
         imageView.setImageBitmap(mImageMap.get(url));
      else {
         queueImage(url, activity, imageView);
         imageView.setImageResource(R.drawable.icon);
      }
   }

   private void queueImage(String url, Activity activity, ImageView imageView) {
      ImageRef imageRef;

      mImageQueue.clean(imageView);
      imageRef = new ImageRef(url, imageView);

      synchronized(mImageQueue.imageRefs) {
         mImageQueue.imageRefs.push(imageRef);
         mImageQueue.imageRefs.notifyAll();
      }

      if (mImageLoaderThread.getState() == Thread.State.NEW)
         mImageLoaderThread.start();
   }

   private Bitmap getBitmap(String url) {
      String filename = String.valueOf(url.hashCode());
      File file = new File(mCacheDir, filename);
      Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());

      if (bitmap != null)
         return bitmap;

      try {
         bitmap = BitmapFactory.decodeStream(new URL(url).openConnection()
          .getInputStream());
         writeFile(bitmap, file);

         return bitmap;
      }
      catch (Exception e) {
         Log.e("ImageManager", "getBitmap: " + e.getMessage());
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

   private class ImageRef {
      public String url;
      public ImageView imageView;

      public ImageRef(String url, ImageView imageView) {
         this.url = url;
         this.imageView = imageView;
      }
   }

   private class ImageQueue {
      public Stack<ImageRef> imageRefs = new Stack<ImageRef>();

      public void clean(ImageView view) {
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
         Object tag;
         BitmapDisplayer bitmapDisplayer;
         Activity activity;

         try {
            while (true) {
               if (mImageQueue.imageRefs.size() == 0)
                  synchronized(mImageQueue.imageRefs) {
                     mImageQueue.imageRefs.wait();
                  }

               if (mImageQueue.imageRefs.size() != 0) {
                  synchronized(mImageQueue.imageRefs) {
                     imageToLoad = mImageQueue.imageRefs.pop();
                  }

                  bitmap = getBitmap(imageToLoad.url);
                  mImageMap.put(imageToLoad.url, bitmap);
                  tag = imageToLoad.imageView.getTag();

                  if (tag != null && ((String)tag).equals(imageToLoad.url)) {
                     bitmapDisplayer = new BitmapDisplayer(bitmap,
                      imageToLoad.imageView);
                     activity = (Activity)imageToLoad.imageView.getContext();
                     activity.runOnUiThread(bitmapDisplayer);
                  }
               }
            }
         }
         catch (InterruptedException e) {}
      }
   }

   private class BitmapDisplayer implements Runnable {
      Bitmap mBitmap;
      ImageView mImageView;

      public BitmapDisplayer(Bitmap bitmap, ImageView imageView) {
         mBitmap = bitmap;
         mImageView = imageView;
      }

      public void run() {
         if (mBitmap != null)
            mImageView.setImageBitmap(mBitmap);
         else
            mImageView.setImageResource(R.drawable.icon);
      }
   }
}
