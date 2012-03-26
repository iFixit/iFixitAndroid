package com.dozuki.ifixit;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class FullImageView extends FragmentActivity {
   private String mImageUrl;
   private ImageViewTouch mImageZoom;
   private ImageManager mImageManager;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      Bundle extras;
      super.onCreate(savedInstanceState);

      extras = getIntent().getExtras();
      mImageUrl = (String)extras.get(GuideStepViewFragment.IMAGE_URL);
      mImageManager = ((MainApplication)getApplication()).getImageManager();

      setContentView(R.layout.full_screen_image);

      mImageZoom = (ImageViewTouch)findViewById(R.id.imageZoom);
      //mImageManager.displayImage(mImageUrl, this, mImageZoom);
   }
}
