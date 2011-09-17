package com.ifixit.guidebook;

import android.content.Context;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class BulletView extends View {
   private static final int BULLET_RADIUS = 6;
   
   private Paint mPaint;
   private int mResourceId;
   
   public BulletView(Context context) {
      super(context);
   }
   
   public BulletView(Context context, AttributeSet attrs) {
      super(context, attrs);
   }

   public BulletView(Context context, AttributeSet attrs, int def) {
      super(context, attrs, def);
   }
   
   public void setBullet(String color) {
      int colorConverted;
      try {
         Log.w("setBullet Color Check: ", ""+color);

         if (color.compareTo("black") == 0) {
            colorConverted = Color.WHITE;
         } else if (color.compareTo("orange") == 0) {
            colorConverted = Color.parseColor("ffa500");
         } else { 
            colorConverted = Color.parseColor(color);
         }
         mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
         mPaint.setColor(colorConverted);
      }
      catch(IllegalArgumentException e) {
         mResourceId = R.drawable.bullets_black;
      }
   }

   @Override
   protected void onDraw(Canvas canvas) {
       super.onDraw(canvas);
       
       if (mPaint != null)
          canvas.drawCircle(BULLET_RADIUS, BULLET_RADIUS+8, BULLET_RADIUS, mPaint);
       //else {
         // Resources resources = getResources();
          //Bitmap bitmap = BitmapFactory.decodeResource(resources, mResourceId);
          //Rect r = new Rect(0, 0, 0, 0);
          //Rect source = new Rect(0,-111, 50, -161);
        //  canvas.drawBitmap(bitmap, 0,0, null);
       //}
   }
}
