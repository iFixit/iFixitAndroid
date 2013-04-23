package com.dozuki.ifixit.ui.guide.create;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class LockableViewPager extends ViewPager {

   private boolean isPagingEnabled;

   public LockableViewPager(Context context, AttributeSet attrs) {
      super(context, attrs);
      this.isPagingEnabled = true;
   }

   @Override
   public boolean onTouchEvent(MotionEvent event) {
      if (this.isPagingEnabled) {
         return super.onTouchEvent(event);
      }
      return false;
   }

   @Override
   public boolean onInterceptTouchEvent(MotionEvent event) {
      if (this.isPagingEnabled) {
         return super.onInterceptTouchEvent(event);
      }
      return false;
   }

   public void setPagingEnabled(boolean enabled) {
      this.isPagingEnabled = enabled;
   }
}
