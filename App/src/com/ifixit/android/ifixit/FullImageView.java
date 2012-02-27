package com.ifixit.android.ifixit;

import android.app.Activity;
import android.os.Bundle;

public class FullImageView extends Activity {
   private LoaderImage mImage;
   private ImageManager mImageManager;
   private String mUrl;
   private ImageSizes mImageSizes;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      Bundle extras;
      super.onCreate(savedInstanceState);
            
      setContentView(R.layout.full_screen_image);
      extras = getIntent().getExtras();
      MainApplication application = (MainApplication)getApplication();
      
      mImageSizes = application.getImageSizes();
      mUrl = (String)extras.get(GuideStepViewFragment.IMAGEID) +
       mImageSizes.getFull();

      mImage = (LoaderImage)findViewById(R.id.full_image_view);
      mImageManager = application.getImageManager();
      mImageManager.displayImage(mUrl, this, mImage);
   }  
}
