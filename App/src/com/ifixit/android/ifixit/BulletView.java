package com.ifixit.android.ifixit;

import android.content.Context;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class BulletView extends View {
   private static final int BULLET_RADIUS = 6;
   private static final int Y_OFFSET = 8;
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
       
       canvas.drawCircle(BULLET_RADIUS, BULLET_RADIUS + Y_OFFSET,
        BULLET_RADIUS, mPaint);       
       
   }
}
