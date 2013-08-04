package com.dozuki.ifixit.ui.guide;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.util.PicassoUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestBuilder;
import com.squareup.picasso.Target;

import java.io.File;

public class FallbackImageView extends ImageView implements Target {
   private static final String TAG = "FallbackImageView";
   private String mImageUrl = "";
   private Image mImage;

   public FallbackImageView(Context context, AttributeSet attrs) {
      super(context, attrs);
   }

   public void setImageUrl(String url) {
      mImageUrl = url;
   }

   public void setImage(Image image) {
      mImage = image;
   }

   @Override
   public void onSuccess(Bitmap bitmap) {
      setImageBitmap(bitmap);
   }

   @Override
   public void onError() {
      Picasso picasso = PicassoUtils.with(getContext());
      RequestBuilder builder;

      if (mImage == null) {
         if (mImageUrl.isEmpty()) {
            Log.e("FallbackImageView", "You must either set a fallback image object or simply a path to a fallback " +
             "image.");
            throw new RuntimeException();
         } else {
            Log.d(TAG, "Falling back to remote image from url" + mImageUrl);
            builder = picasso.load(mImageUrl);
         }
      } else {
         if (mImage.hasLocalPath()) {
            Log.d(TAG, "Falling back to local image " + mImage.getLocalPath());
            builder = picasso.load(new File(mImage.getLocalPath()));
         } else {
            Log.d(TAG, "Falling back to remote image " + mImage.getPath());
            builder = picasso.load(mImage.getPath());
         }
      }

      builder
       .error(R.drawable.no_image)
       .into((Target) this);
   }
}
