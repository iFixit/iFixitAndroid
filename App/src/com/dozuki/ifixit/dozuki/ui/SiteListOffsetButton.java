package com.dozuki.ifixit.dozuki.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

import com.dozuki.ifixit.R;

public class SiteListOffsetButton extends Button {

    public SiteListOffsetButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SiteListOffsetButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SiteListOffsetButton(Context context) {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean value = super.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_UP) {
            setBackgroundResource(R.drawable.site_list_background);
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            setBackgroundResource(R.drawable.site_list_background_pressed);
            setPadding(getPaddingLeft(), getPaddingTop() + 2, getPaddingRight(),
             getPaddingBottom() - 2);
        }

        return value;
    }
}