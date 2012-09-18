package com.dozuki.ifixit.view.ui;

import com.dozuki.ifixit.R;
import com.ifixit.android.imagemanager.ImageManager;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MediaViewItem extends RelativeLayout
{
	ImageView imageview;
	CheckBox checkbox;
	private Context mContext;
	int id;
	private ImageManager mImageManager;
	
	
	
	public MediaViewItem(Context context, ImageManager imageManager) {
		super(context);
		 mContext = context;
		 mImageManager = imageManager;
		
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(com.dozuki.ifixit.R.layout.media_cell, this, true);

		imageview = (ImageView)findViewById(com.dozuki.ifixit.R.id.media_image);
		checkbox = (CheckBox) findViewById(com.dozuki.ifixit.R.id.del_box);
		
		
	}
	
	public void setImageItem(String image, Context context) {
		mContext = context;

		//mTitleView.setText(Html.fromHtml(title));

		mImageManager.displayImage(image, (Activity) mContext, imageview);
	}
}
