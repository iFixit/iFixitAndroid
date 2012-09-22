package com.dozuki.ifixit.view.ui;

import com.dozuki.ifixit.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


public class FadeInImageView extends ImageView{

	Context mContext;
	public FadeInImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
	}
	
	
	public FadeInImageView(Context context, AttributeSet attrs, int integer) {
		super(context, attrs, integer);
		// TODO Auto-generated constructor stub
		mContext = context;
	}
	
	
	@Override
	public void setImageBitmap (Bitmap bm) 
	{
		super.setImageBitmap(bm);
		this.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
		super.setImageBitmap(bm);
	}



}
