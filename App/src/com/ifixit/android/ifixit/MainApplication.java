package com.ifixit.android.ifixit;

import android.app.Application;

public class MainApplication extends Application {
	private ImageManager mImageManager;
	
	public ImageManager getImageManager() {
		if (mImageManager == null)
		   mImageManager = new ImageManager(this);
		
		return mImageManager;
	}
}
