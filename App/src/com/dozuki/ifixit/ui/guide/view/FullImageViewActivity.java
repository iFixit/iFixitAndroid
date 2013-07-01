package com.dozuki.ifixit.ui.guide.view;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Window;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.ImageSizes;
import com.squareup.picasso.Picasso;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;

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
      Picasso picasso = Picasso.with(this);

      setContentView(R.layout.full_screen_image);
      ImageViewTouch image = (ImageViewTouch) findViewById(R.id.imageZoom);

      if (url.startsWith("http")) {
         url += sizes.getFull();
         picasso.load(url)
          .error(R.drawable.no_image)
          .into(image);
      } else {
         picasso.load(new File(url))
          .scale(0.5f)
          .error(R.drawable.no_image)
          .into(image);
      }

      findViewById(R.id.fullScreenClose).setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            finish();
         }
      });
   }
}
