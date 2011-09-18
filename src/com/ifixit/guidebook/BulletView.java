package com.ifixit.guidebook;

import android.content.Context;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class BulletView extends View {
   private static final int BULLET_RADIUS = 6;
   private static final int ORANGE = 0xFFFF7F00;
   
   private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
   
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
         if (color.equals("black")) {
            colorConverted = Color.WHITE;
         } else if (color.equals("orange")) {
            colorConverted = ORANGE;
         } else { 
            colorConverted = Color.parseColor(color);
         }
      }
      catch(IllegalArgumentException e) {
         colorConverted = Color.WHITE;
      }

      mPaint.setColor(colorConverted);
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
