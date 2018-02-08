package com.dozuki.ifixit.ui;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ScrollingView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Fixes unreliable "Fling" behaviour with AppBarLayouts used in TopicViewActivity
 * Source: http://stackoverflow.com/questions/30923889/flinging-with-recyclerview-appbarlayout
 * Read More: https://code.google.com/p/android/issues/detail?id=177729&q=appbarlayout&colspec=ID%20Type%20Status%20Owner%20Summary%20Stars
 */

public final class FlingBehavior extends AppBarLayout.Behavior {
   public FlingBehavior() {
   }

   public FlingBehavior(Context context, AttributeSet attrs) {
      super(context, attrs);
   }

   @Override
   public boolean onNestedFling(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, float velocityX, float velocityY, boolean consumed) {
      if (target instanceof ScrollingView) {
         final ScrollingView scrollingView = (ScrollingView) target;
         consumed = velocityY > 0 || scrollingView.computeVerticalScrollOffset() > 0;
      }
      return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
   }
}