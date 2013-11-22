package com.dozuki.ifixit.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.dozuki.ifixit.R;

public class TouchableRelativeLayout extends RelativeLayout {

   private Drawable mTouchFeedbackDrawable;

   public TouchableRelativeLayout(Context context) {
      super(context);
   }

   public TouchableRelativeLayout(Context context, AttributeSet attrs) {
      super(context, attrs);
   }

   public TouchableRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
   }

   @Override
   protected void onAttachedToWindow(){
      super.onAttachedToWindow();

      mTouchFeedbackDrawable = getResources().getDrawable(R.drawable.selectable_item_background_transparent);
   }

   @Override
   protected void dispatchDraw(Canvas canvas){
      super.dispatchDraw(canvas);
      mTouchFeedbackDrawable.setBounds(0, 0, getWidth(), getHeight());
      mTouchFeedbackDrawable.draw(canvas);
   }

   @Override
   protected void drawableStateChanged() {
      if (mTouchFeedbackDrawable != null) {
         mTouchFeedbackDrawable.setState(getDrawableState());
         invalidate();
      }
      super.drawableStateChanged();
   }
}
