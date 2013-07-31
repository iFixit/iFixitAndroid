package com.dozuki.ifixit.ui.guide;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import com.dozuki.ifixit.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class FallbackImageView extends ImageView implements Target {
   private final Context mContext;
   private String mImageUrl = "";

   public FallbackImageView(Context context, AttributeSet attrs) {
      super(context, attrs);
      mContext = context;
   }

   public void setImageUrl(String url) {
      mImageUrl = url;
   }

   @Override
   public void onSuccess(Bitmap bitmap) {
      setImageBitmap(bitmap);
   }

   @Override
   public void onError() {
      if (mImageUrl.isEmpty()) {
         Log.e("FallbackImageView", "You must set the base Image url using setImageUrl.");
      }

      Picasso.with(mContext)
       .load(mImageUrl)
       .error(R.drawable.no_image)
       .into((Target) this);
   }
}
