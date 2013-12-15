package com.dozuki.ifixit.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import com.squareup.picasso.Transformation;

public class RoundedTransformation implements Transformation {
   private final int mRadius;
   private final int mMargin;

   /**
    * Applies a rounded corder transformation to an image loaded by Picasso
    *
    * @param radius in DP
    * @param margin in DP
    */
   public RoundedTransformation(final int radius, final int margin) {
      mRadius = radius;
      mMargin = margin;
   }

   @Override
   public Bitmap transform(final Bitmap source) {
      final Paint paint = new Paint();
      paint.setAntiAlias(true);
      paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

      Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());
      Canvas canvas = new Canvas(output);
      canvas.drawRoundRect(new RectF(mMargin, mMargin, source.getWidth() - mMargin, source.getHeight() - mMargin),
       mRadius, mRadius, paint);

      if (source != output) {
         source.recycle();
      }

      return output;
   }

   @Override
   public String key() {
      return "rounded";
   }
}
