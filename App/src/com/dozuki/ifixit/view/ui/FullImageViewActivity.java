package com.dozuki.ifixit.view.ui;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;

import com.actionbarsherlock.view.Window;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.ImageSizes;
import com.ifixit.android.imagemanager.ImageManager;

public class FullImageViewActivity extends Activity {
	private String mImageUrl;
	private ImageViewTouch mImageZoom;
	private ImageView mCloseFullScreen;
	private ImageManager mImageManager;
	private ImageSizes mImageSizes;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature((int) Window.FEATURE_NO_TITLE);
		getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		Bundle extras = getIntent().getExtras();
		mImageUrl = (String) extras
				.get(GuideStepViewFragment.IMAGE_URL);
		MainApplication application = ((MainApplication) getApplication());
		mImageManager = application.getImageManager();
		mImageSizes = application.getImageSizes();

		setContentView(R.layout.full_screen_image);

		mImageZoom = (ImageViewTouch) findViewById(R.id.imageZoom);
		Boolean localUri = (Boolean) extras
				.get(MediaFragment.LOCAL_URL);

		if (localUri != null) {
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inSampleSize = 2;
			opt.inDither = false;
			opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
			mImageZoom.setImageBitmapReset(BitmapFactory
					.decodeFile(mImageUrl, opt), true);
			mImageZoom.setVisibility(View.VISIBLE);
		} else {
			mImageManager.displayImage(mImageUrl
					+ mImageSizes.getFull(), this,
					mImageZoom);
		}
		mCloseFullScreen = (ImageView) findViewById(R.id.fullScreenClose);
		mCloseFullScreen
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						finish();
					}
				});
	}
}
