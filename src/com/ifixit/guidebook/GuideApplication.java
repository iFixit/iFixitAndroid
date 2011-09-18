package com.ifixit.guidebook;

import android.app.Application;

public class GuideApplication extends Application {
	private ImageManager mImageManager;
	
	public ImageManager getImageManager() {
		if (mImageManager == null)
		   mImageManager = new ImageManager(this);
		
		return mImageManager;
	}
}
