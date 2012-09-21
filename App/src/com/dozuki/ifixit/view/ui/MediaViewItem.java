package com.dozuki.ifixit.view.ui;

import com.actionbarsherlock.view.ActionMode;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.view.model.UserImageInfo;
import com.ifixit.android.imagemanager.ImageManager;
import com.actionbarsherlock.view.*;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MediaViewItem extends RelativeLayout {
	ImageView imageview;
	RelativeLayout selectImage;
	public UserImageInfo listRef;
	private Context mContext;
	int id;
	private ImageManager mImageManager;
	public String localPath;


	public MediaViewItem(Context context, ImageManager imageManager) {
		super(context);
		mContext = context;
		mImageManager = imageManager;
		listRef = null;
		localPath = null;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(com.dozuki.ifixit.R.layout.media_cell, this, true);
		
		imageview = (ImageView) findViewById(com.dozuki.ifixit.R.id.media_image);
		selectImage =  (RelativeLayout) findViewById(com.dozuki.ifixit.R.id.selected_image);
		
		selectImage.setVisibility(View.INVISIBLE);
	}
	

	public void setImageItem(String image, Context context) {
		mContext = context;
		// mTitleView.setText(Html.fromHtml(title));

		mImageManager.displayImage(image, (Activity) mContext, imageview);
	}

	
}
