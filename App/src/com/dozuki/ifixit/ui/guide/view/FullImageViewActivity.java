package com.dozuki.ifixit.ui.guide.view;

import android.content.Context;
import android.content.Intent;
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
import com.dozuki.ifixit.util.api.ApiSyncAdapter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;

import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class FullImageViewActivity extends SherlockActivity {
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

      String url = getIntent().getExtras().getString(IMAGE_URL);
      boolean offline = getIntent().getExtras().getBoolean(OFFLINE);

      requestWindowFeature((int) Window.FEATURE_NO_TITLE);
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
       WindowManager.LayoutParams.FLAG_FULLSCREEN);
      ImageSizes sizes = MainApplication.get().getImageSizes();

      Picasso picasso = PicassoUtils.with(this);

      setContentView(R.layout.full_screen_image);
      final FullScreenImageView image = (FullScreenImageView) findViewById(R.id.image_zoom);
      image.setImageUrl(url);
      image.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);

      if (url.startsWith("http")) {
         url += sizes.getFull();

         if (offline) {
            picasso.load(new File(ApiSyncAdapter.getOfflineMediaPath(url)))
             .error(R.drawable.no_image)
             .into((Target) image);
         } else {
            picasso.load(url)
             .error(R.drawable.no_image)
             .into((Target) image);
         }
      } else if (url.startsWith("content://")) {
         picasso.load(url)
          .error(R.drawable.no_image)
          .into((Target) image);
      } else {
         picasso.load(new File(url))
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
