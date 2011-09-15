package com.ifixit.guidebook;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class BulletView extends View {
   private static final int BULLET_RADIUS = 6;
   
   private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
   
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
         } else { 
            colorConverted = Color.parseColor(color);
         }
      }
      catch(IllegalArgumentException e) {
         colorConverted = 0;
      }

      mPaint.setColor(colorConverted);
   }

   @Override
   protected void onDraw(Canvas canvas) {
       super.onDraw(canvas);
       
       canvas.drawCircle(BULLET_RADIUS, BULLET_RADIUS+8, BULLET_RADIUS, mPaint);
   }
}
