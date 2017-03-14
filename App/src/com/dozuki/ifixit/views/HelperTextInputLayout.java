package com.dozuki.ifixit.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.TextView;

import com.dozuki.ifixit.R;

public class HelperTextInputLayout extends TextInputLayout {
   private static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();;
   private CharSequence mHelperText;
   private ColorStateList mHelperTextColor;
   private boolean mHelperTextEnabled = false;
   private boolean mErrorEnabled = false;
   private TextView mHelperView;
   private int mHelperTextAppearance = R.style.HelperTextAppearance;

   public HelperTextInputLayout(Context context) {
      super(context);
   }


   public HelperTextInputLayout(Context _context, AttributeSet _attrs) {
      super(_context, _attrs);

      final TypedArray a = getContext().obtainStyledAttributes(
       _attrs,
       R.styleable.HelperTextInputLayout,0,0);
      try {
         mHelperTextColor = a.getColorStateList(R.styleable.HelperTextInputLayout_helperTextColor);
         mHelperText = a.getText(R.styleable.HelperTextInputLayout_helperText);
      } finally {
         a.recycle();
      }
   }

   @Override
   public void addView(View child, int index, ViewGroup.LayoutParams params) {
      super.addView(child, index, params);
      if (child instanceof EditText) {
         if (!TextUtils.isEmpty(mHelperText)) {
            setHelperText(mHelperText);
         }
      }
   }

   public int getHelperTextAppearance() {
      return mHelperTextAppearance;
   }

   public void setHelperTextAppearance(int _helperTextAppearanceResId) {
      mHelperTextAppearance = _helperTextAppearanceResId;
   }

   public void setHelperTextColor(ColorStateList _helperTextColor) {
      mHelperTextColor = _helperTextColor;
   }

   public void setHelperTextEnabled(boolean _enabled) {
      if (mHelperTextEnabled == _enabled) return;
      if (_enabled && mErrorEnabled) {
         setErrorEnabled(false);
      }
      if (mHelperTextEnabled != _enabled) {
         if (_enabled) {
            mHelperView = new TextView(this.getContext());
            mHelperView.setTextAppearance(getContext(), mHelperTextAppearance);
            if (mHelperTextColor != null){
               mHelperView.setTextColor(mHelperTextColor);
            }
            mHelperView.setText(mHelperText);
            mHelperView.setVisibility(VISIBLE);
            addView(mHelperView);
            if (mHelperView != null) {
               ViewCompat.setPaddingRelative(
                mHelperView,
                ViewCompat.getPaddingStart(getEditText()),
                0, ViewCompat.getPaddingEnd(getEditText()),
                getEditText().getPaddingBottom());
            }
         } else {
            removeView(mHelperView);
            mHelperView = null;
         }

         mHelperTextEnabled = _enabled;
      }
   }

   public void setHelperText(CharSequence _helperText) {
      mHelperText = _helperText;
      if (!mHelperTextEnabled) {
         if (TextUtils.isEmpty(mHelperText)) {
            return;
         }
         setHelperTextEnabled(true);
      }

      if (!TextUtils.isEmpty(mHelperText)) {
         mHelperView.setText(mHelperText);
         mHelperView.setVisibility(VISIBLE);
         ViewCompat.setAlpha(mHelperView, 0.0F);
         ViewCompat.animate(mHelperView)
          .alpha(1.0F).setDuration(200L)
          .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
          .setListener(null).start();
      } else if (mHelperView.getVisibility() == VISIBLE) {
         ViewCompat.animate(mHelperView)
          .alpha(0.0F).setDuration(200L)
          .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
          .setListener(new ViewPropertyAnimatorListenerAdapter() {
             public void onAnimationEnd(View view) {
                mHelperView.setText(null);
                mHelperView.setVisibility(INVISIBLE);
             }
          }).start();
      }
      sendAccessibilityEvent(2048);
   }

   @Override
   public void setErrorEnabled(boolean _enabled) {
      if (mErrorEnabled == _enabled) return;
      mErrorEnabled = _enabled;
      if (_enabled && mHelperTextEnabled) {
         setHelperTextEnabled(false);
      }

      super.setErrorEnabled(_enabled);

      if (!(_enabled || TextUtils.isEmpty(mHelperText))) {
         setHelperText(mHelperText);
      }
   }
}
