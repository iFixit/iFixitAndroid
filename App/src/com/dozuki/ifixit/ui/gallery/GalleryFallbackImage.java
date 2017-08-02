package com.dozuki.ifixit.ui.gallery;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.util.ImageSizes;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import java.io.File;

public class GalleryFallbackImage extends android.support.v7.widget.AppCompatImageView implements Target {
   private static final String TAG = "GalleryFallbackImage";
   private boolean mTryLocalPath = true;
   private Image mImage;

   public GalleryFallbackImage(Context context, AttributeSet attrs) {
      super(context, attrs);
   }

   public void setImage(Image image) {
      mImage = image;
   }

   @Override
   public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
      setImageBitmap(bitmap);
   }

   @Override
   public void onBitmapFailed(Drawable drawable) {
      RequestCreator builder;
      Resources res = getContext().getResources();

      Log.d("IMG FALLBACK", mImage.toString());

      if (mImage.hasLocalPath()) {
         builder = Picasso.with(getContext()).load(new File(mImage.getLocalPath()));
      } else {
         builder = Picasso.with(getContext()).load(mImage.getPath(ImageSizes.stepThumb));
      }

      builder
       .resize(res.getDimensionPixelSize(R.dimen.gallery_grid_column_width),
         res.getDimensionPixelSize(R.dimen.gallery_grid_item_height))
       .centerCrop()
       .error(R.drawable.no_image)
       .into((AppCompatImageView) this);
   }

   @Override
   public void onPrepareLoad(Drawable drawable) {
      // Do nothing.
   }
}

