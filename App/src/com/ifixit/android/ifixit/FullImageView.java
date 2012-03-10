package com.ifixit.android.ifixit;

import android.os.Bundle;

import android.support.v4.app.FragmentActivity;

public class FullImageView extends FragmentActivity {
   private String mFilePath;
   private WebViewFragment mWebView;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      Bundle extras;
      super.onCreate(savedInstanceState);

      extras = getIntent().getExtras();
      mFilePath = (String)extras.get(GuideStepViewFragment.IMAGE_FILE_PATH);

      setContentView(R.layout.full_screen_image);
      mWebView = (WebViewFragment)getSupportFragmentManager()
       .findFragmentById(R.id.web_view_fragment);
      mWebView.loadUrl("file://" + mFilePath);
   }
}
