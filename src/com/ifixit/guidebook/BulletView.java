package com.ifixit.guidebook;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class BulletView extends View {
   private static final int BULLET_RADIUS = 6;
   
   private float x;
   private float y;
   private int r;
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
         colorConverted = Color.parseColor(color);
         mPaint.setColor(colorConverted);
      } catch(IllegalArgumentException e) {
         return;
      }
   }

   @Override
   protected void onDraw(Canvas canvas) {
       super.onDraw(canvas);
       
       canvas.drawCircle(BULLET_RADIUS, BULLET_RADIUS+8, BULLET_RADIUS, mPaint);
   }
}
