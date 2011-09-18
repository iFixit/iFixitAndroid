package com.ifixit.guidebook;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

public class FullImageView extends Activity {
   private LoaderImage mImage;
   private ImageManager mImageManager;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      Bundle extras;
      super.onCreate(savedInstanceState);
      
      mImageManager = new ImageManager(this);
      
      setContentView(R.layout.full_screen_image);
      extras = getIntent().getExtras();

      String image = (String)extras.get(GuideStepView.IMAGEID);
       
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
      
      mImage = (LoaderImage)findViewById(R.id.full_image_view);
      mImageManager.displayImage(image, this, mImage);
   }  
}
