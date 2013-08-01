package com.dozuki.ifixit.ui.guide.create;

import android.content.Context;
import android.util.AttributeSet;
import com.viewpagerindicator.TitlePageIndicator;

public class LockableTitlePageIndicator extends TitlePageIndicator {

   private boolean isPagingEnabled = true;

   public LockableTitlePageIndicator(Context context) {
      super(context);
   }

   public LockableTitlePageIndicator(Context context, AttributeSet attrs) {
      super(context, attrs);
   }

   public LockableTitlePageIndicator(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
   }

   @Override
   public boolean onTouchEvent(android.view.MotionEvent ev) {
      if (this.isPagingEnabled) {
         return super.onTouchEvent(ev);
      }
      return false;
   }

   public void setPagingEnabled(boolean enabled) {
      this.isPagingEnabled = enabled;
   }
}
