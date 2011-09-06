package com.ifixit.guidebook;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class StepLine {
   protected String mColor;
   protected int mLevel;
   protected String mText;

   public void setColor(String color) {
      mColor = color;
   }

   public void setLevel(int level) {
      mLevel = level;
   }

   public void setText(String text) {
      mText = text;
   }
   
   public String getText() {
      return mText;
   }

   public int getLevel() {
      return mLevel;
   }
   
   public String getColor() {
      return mColor;
   }
   
   public String toString() {
      return "{StepLine: " + mColor + ", " + mLevel + ", " + mText + "}";
   }
   
   private class BulletPoint extends View {
      private final float x;
      private final float y;
      private final int r;
      private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
   
      public BulletPoint(Context context, int color) {
          super(context);
          mPaint.setColor(color);
          this.x = 10;
          this.y = 10;
          this.r = 10;
      }
      
      @Override
      protected void onDraw(Canvas canvas) {
          super.onDraw(canvas);
          canvas.drawCircle(x, y, r, mPaint);
      }
  }
}
