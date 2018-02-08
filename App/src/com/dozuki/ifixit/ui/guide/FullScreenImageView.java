package com.dozuki.ifixit.ui.guide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.PicassoUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class FullScreenImageView extends ImageViewTouch implements Target {
   private final Context mContext;
   private String mImageUrl;
   private boolean mOffline;

   public FullScreenImageView(Context context, AttributeSet attrs) {
      super(context, attrs);
      mContext = context;
   }

   public void loadImage(String url, boolean offline) {
      mImageUrl = url;
      mOffline = offline;
      Picasso picasso = Picasso.with(mContext);

      if (url.startsWith("http")) {
         url += ImageSizes.stepFull;

         PicassoUtils.displayImage(picasso, url, offline)
          .error(R.drawable.no_image)
          .into((Target) this);
      } else if (url.startsWith("content://")) {
         picasso.load(url)
          .error(R.drawable.no_image)
          .into((Target)this);
      } else {
         picasso.load(new File(url))
          .error(R.drawable.no_image)
          .into((Target)this);
      }
   }

   @Override
   public void onBitmapLoaded(Bitmap bitmap, com.squareup.picasso.Picasso.LoadedFrom loadedFrom) {
      setImageBitmap(bitmap);
   }

   @Override
   public void onBitmapFailed(Drawable drawable) {
      Picasso.with(mContext)
       .load(mImageUrl)
       .error(R.drawable.no_image)
       .into((Target)this);
   }

   @Override
   public void onPrepareLoad(Drawable drawable) {
      // Do nothing.
   }
}
