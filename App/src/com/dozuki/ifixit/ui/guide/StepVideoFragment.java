package com.dozuki.ifixit.ui.guide;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.StepVideo;
import com.dozuki.ifixit.model.guide.StepVideoThumbnail;
import com.dozuki.ifixit.ui.guide.view.VideoViewActivity;
import com.marczych.androidimagemanager.ImageManager;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;

public class StepVideoFragment extends Fragment {

   public static final String GUIDE_VIDEO_KEY = "GUIDE_VIDEO_KEY";
   private Activity mContext;
   private ImageView mPoster;
   private StepVideoThumbnail mVideoPoster;
   private StepVideo mVideo;
   private ImageManager mImageManager;
   private Resources mResources;
   private DisplayMetrics mMetrics;
   private RelativeLayout mVideoPlayButtonContainer;
   private ImageButton mVideoPlayButton;

   /////////////////////////////////////////////////////
   // LIFECYCLE
   /////////////////////////////////////////////////////

   @Override
   public void onCreate(Bundle savedInstanceState) {
      mContext = (Activity) getActivity();

      MainApplication app = (MainApplication) mContext.getApplication();

      super.onCreate(savedInstanceState);

      if (mImageManager == null) {
         mImageManager = app.getImageManager();
      }

      mResources = mContext.getResources();

      mMetrics = new DisplayMetrics();
      mContext.getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

      // Inflate the layout for this fragment
      View v = LayoutInflater.from(mContext)
       .inflate(R.layout.guide_create_step_edit_video, container, false);

      Bundle extras = getArguments();
      if (extras != null) {
         mVideo = (StepVideo)extras.getSerializable(GUIDE_VIDEO_KEY);
      }

      if (savedInstanceState != null) {
         mVideo = (StepVideo)savedInstanceState.getSerializable(GUIDE_VIDEO_KEY);
      }

      if (mVideo != null) {
         mVideoPoster = mVideo.getThumbnail();
      }

      mPoster = (ImageView) v.findViewById(R.id.step_edit_video_poster);
      mVideoPlayButtonContainer = (RelativeLayout) v.findViewById(R.id.video_play_button_container);
      mVideoPlayButton = (ImageButton) v.findViewById(R.id.video_play_button);

      // Size the video preview screenshot within the available screen space
      ViewGroup.LayoutParams params = fitToSpace(mPoster, mVideoPoster.getWidth(), mVideoPoster.getHeight());
      mPoster.setLayoutParams(params);
      mVideoPlayButtonContainer.setLayoutParams(params);

      mImageManager.displayImage(mVideoPoster.getUrl(), mContext, mPoster);

      mVideoPlayButton.setTag(R.id.guide_step_view_video_url, mVideo.getEncodings().get(0).getURL());
      mVideoPlayButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            String url = (String) v.getTag(R.id.guide_step_view_video_url);

            Intent i = new Intent(mContext, VideoViewActivity.class);
            i.putExtra(VideoViewActivity.VIDEO_URL, url);
            startActivity(i);
         }
      });

      return v;
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);

      savedInstanceState.putSerializable(GUIDE_VIDEO_KEY, mVideo);

   }
/*
   @Override
   public void onResume() {
      super.onResume();
      MainApplication.getBus().register(this);
   }

   @Override
   public void onPause() {
      super.onPause();
      MainApplication.getBus().unregister(this);
   }
*/

   /////////////////////////////////////////////////////
   // NOTIFICATION LISTENERS
   /////////////////////////////////////////////////////

   private ViewGroup.LayoutParams fitToSpace(View view, float width, float height) {
      float newWidth = 0f;
      float newHeight = 0f;
      float padding = 0f;

      if (MainApplication.get().inPortraitMode()) {
         padding = viewPadding(R.dimen.page_padding);

         newWidth = mMetrics.widthPixels - padding;
         newHeight = newWidth * (height / width);
      } else {
         padding += navigationHeight();

         newHeight = ((mMetrics.heightPixels - padding) * 3f) / 5f;
         newWidth = (newHeight * (width / height));

         // Correct height to match ratio of image
         newHeight = newWidth * (height / width);
      }

      //fitProgressIndicator(newWidth, newHeight);

      ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
      layoutParams.width = (int) (newWidth - .5f);
      layoutParams.height = (int) (newHeight - .5f);

      return layoutParams;
   }

   private float navigationHeight() {
      int actionBarHeight = 0, indicatorHeight = 50;

      actionBarHeight = mResources.getDimensionPixelSize(
       com.actionbarsherlock.R.dimen.abs__action_bar_default_height);

      float pagePadding = viewPadding(R.dimen.page_padding);

      return actionBarHeight + indicatorHeight + pagePadding;
   }

   private float viewPadding(int view) {
      return mResources.getDimensionPixelSize(view) * 2f;
   }
}