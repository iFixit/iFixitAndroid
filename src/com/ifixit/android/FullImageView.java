package com.ifixit.android;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

public class FullImageView extends Activity {
   private static final String IMAGE_SIZE = ".large";

   private LoaderImage mImage;
   private ImageManager mImageManager;
   private String mUrl;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      Bundle extras;
      super.onCreate(savedInstanceState);
            
      setContentView(R.layout.full_screen_image);
      extras = getIntent().getExtras();
      mUrl = (String)extras.get(GuideStepView.IMAGEID) + IMAGE_SIZE;
       
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
      
      mImage = (LoaderImage)findViewById(R.id.full_image_view);
      mImageManager = ((GuideApplication)getApplication()).getImageManager();
      mImageManager.displayImage(mUrl, this, mImage);
   }  
}
