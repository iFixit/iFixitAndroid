package com.dozuki.ifixit.ui.guide.view;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Window;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.ui.guide.FullScreenImageView;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.PicassoUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

import java.io.File;

public class FullImageViewActivity extends SherlockActivity {
   public static final String IMAGE_URL = "IMAGE_URL";

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      requestWindowFeature((int) Window.FEATURE_NO_TITLE);
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
       WindowManager.LayoutParams.FLAG_FULLSCREEN);
      ImageSizes sizes = MainApplication.get().getImageSizes();

      String url = (String) getIntent().getExtras().get(IMAGE_URL);
      Picasso picasso = PicassoUtils.with(this);

      setContentView(R.layout.full_screen_image);
      final FullScreenImageView image = (FullScreenImageView) findViewById(R.id.image_zoom);
      image.setImageUrl(url);
      image.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);

      if (url.startsWith("http")) {
         url += sizes.getFull();

         picasso.load(url)
          .error(R.drawable.no_image)
          .into((Target) image);
      } else if (url.startsWith("content://")) {
         picasso.load(url)
          .scale(0.5f)
          .error(R.drawable.no_image)
          .into((Target) image);
      } else {
         picasso.load(new File(url))
          .scale(0.5f)
          .error(R.drawable.no_image)
          .into((Target) image);
      }

      findViewById(R.id.full_screen_close).setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            finish();
         }
      });
   }
}
