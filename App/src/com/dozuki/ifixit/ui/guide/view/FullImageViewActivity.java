package com.dozuki.ifixit.ui.guide.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.ui.guide.FullScreenImageView;

import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class FullImageViewActivity extends AppCompatActivity {
   private static final String IMAGE_URL = "IMAGE_URL";
   private static final String OFFLINE = "OFFLINE";

   public static Intent viewImage(Context context, String url, boolean offline) {
      Intent intent = new Intent(context, FullImageViewActivity.class);
      intent.putExtra(IMAGE_URL, url);
      intent.putExtra(OFFLINE, offline);
      return intent;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setTheme(App.get().getSiteTheme());

      String url = getIntent().getExtras().getString(IMAGE_URL);
      boolean offline = getIntent().getExtras().getBoolean(OFFLINE);

      requestWindowFeature((int) Window.FEATURE_NO_TITLE);
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
       WindowManager.LayoutParams.FLAG_FULLSCREEN);

      setContentView(R.layout.full_screen_image);

      final FullScreenImageView image = (FullScreenImageView)findViewById(R.id.image_zoom);
      image.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
      image.loadImage(url, offline);

      findViewById(R.id.full_screen_close).setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            finish();
         }
      });
   }
}
