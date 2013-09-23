package com.dozuki.ifixit.ui.guide;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.PicassoUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class FullScreenImageView extends ImageViewTouch implements Target {
   private final Context mContext;
   private String mImageUrl = "";

   public FullScreenImageView(Context context, AttributeSet attrs) {
      super(context, attrs);
      mContext = context;
   }

   public void setImageUrl(String url) {
      mImageUrl = url;
   }

   @Override
   public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
      setImageBitmap(bitmap);
   }

   @Override
   public void onBitmapFailed() {
      if (mImageUrl.length() == 0) {
         Log.e("FallbackImageView", "You must set the base Image url using setImageUrl.");
      }

      PicassoUtils.with(mContext)
       .load(mImageUrl)
       .error(R.drawable.no_image)
       .into((Target) this);
   }
}
