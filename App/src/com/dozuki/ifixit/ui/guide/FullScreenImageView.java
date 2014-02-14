package com.dozuki.ifixit.ui.guide;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.PicassoUtils;
import com.dozuki.ifixit.util.api.ApiSyncAdapter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
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
      Picasso picasso = PicassoUtils.with(mContext);

      if (url.startsWith("http")) {
         url += ImageSizes.stepFull;

         if (offline) {
            picasso.load(new File(ApiSyncAdapter.getOfflineMediaPath(url)))
             .error(R.drawable.no_image)
             .into((Target)this);
         } else {
            picasso.load(url)
             .error(R.drawable.no_image)
             .into((Target)this);
         }
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
   public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
      setImageBitmap(bitmap);
   }

   @Override
   public void onBitmapFailed() {
      Picasso picasso = PicassoUtils.with(mContext);
      RequestCreator request;

      if (mOffline) {
         request = picasso.load(new File(ApiSyncAdapter.getOfflineMediaPath(mImageUrl)));
      } else {
         request = picasso.load(mImageUrl);
      }

      request.error(R.drawable.no_image)
       .into((Target)this);
   }
}
