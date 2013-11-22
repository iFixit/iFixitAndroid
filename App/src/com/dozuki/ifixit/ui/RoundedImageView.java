package com.dozuki.ifixit.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import com.dozuki.ifixit.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class RoundedImageView extends ImageView implements Target {

   private static final float DEFAULT_CORNER_RADIUS = 4f;
   private Bitmap mBitmap;
   private float mCornerRadius;
   private RectF mRect;
   private Paint mPaint;

   public RoundedImageView(Context context) {
      super(context);
      mCornerRadius = DEFAULT_CORNER_RADIUS;

      init();
   }

   public RoundedImageView(Context context, AttributeSet attrs) {
      super(context, attrs);
      TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView);
      if (array != null) {
         mCornerRadius = array.getDimension(R.styleable.RoundedImageView_corner_radius, 0);
         Log.d("RoundedImageView", "mCornerRadius: " + mCornerRadius);

         array.recycle();
      }

      init();
   }

   private void init() {
      mBitmap = ((BitmapDrawable)getDrawable()).getBitmap();
      int width = mBitmap.getWidth();
      int height = mBitmap.getHeight();

      mRect = new RectF(0.0f, 0.0f, width, height);

      BitmapShader shader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

      mPaint = new Paint();
      mPaint.setAntiAlias(true);
      mPaint.setShader(shader);
   }

   @Override
   protected void onDraw(Canvas canvas) {
      canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mPaint);
   }

   @Override
   public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
      int width = bitmap.getWidth();
      int height = bitmap.getHeight();

      Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

      Canvas canvas = new Canvas(output);

      BitmapShader shader;
      shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

      Paint paint = new Paint();
      paint.setAntiAlias(true);
      paint.setShader(shader);

      RectF rect = new RectF(0.0f, 0.0f, width, height);

      Log.d("RoundedImageView", "mCornerRadius: " + mCornerRadius);

      // rect contains the bounds of the shape
      // radius is the radius in pixels of the rounded corners
      // paint contains the shader that will texture the shape
      canvas.drawRoundRect(rect, mCornerRadius, mCornerRadius, paint);

      setImageBitmap(output);
   }

   @Override
   public void onBitmapFailed() { /* Nothing */ }
}
