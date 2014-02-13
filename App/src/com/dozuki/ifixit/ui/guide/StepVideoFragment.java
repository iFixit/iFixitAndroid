package com.dozuki.ifixit.ui.guide;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Video;
import com.dozuki.ifixit.model.VideoThumbnail;
import com.dozuki.ifixit.ui.BaseFragment;
import com.dozuki.ifixit.ui.guide.view.VideoViewActivity;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.PicassoUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;

public class StepVideoFragment extends BaseFragment {
   private static final String GUIDE_VIDEO_KEY = "GUIDE_VIDEO_KEY";
   private static final String IS_OFFLINE_GUIDE= "IS_OFFLINE_GUIDE";

   private Activity mContext;
   private VideoThumbnail mVideoPoster;
   private Video mVideo;
   private boolean mIsOfflineGuide;
   private Resources mResources;
   private DisplayMetrics mMetrics;

   public static StepVideoFragment newInstance(Video video, boolean isOfflineGuide) {
      Bundle args = new Bundle();
      args.putSerializable(GUIDE_VIDEO_KEY, video);
      args.putBoolean(IS_OFFLINE_GUIDE, isOfflineGuide);
      StepVideoFragment frag = new StepVideoFragment();
      frag.setArguments(args);

      return frag;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      mContext = getActivity();

      super.onCreate(savedInstanceState);

      mResources = mContext.getResources();

      mMetrics = new DisplayMetrics();
      mContext.getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      mVideo = (Video)getArguments().getSerializable(GUIDE_VIDEO_KEY);
      mIsOfflineGuide = getArguments().getBoolean(IS_OFFLINE_GUIDE);

      // Inflate the layout for this fragment
      View v = LayoutInflater.from(mContext).inflate(R.layout.guide_step_video, container, false);

      if (mVideo != null) {
         mVideoPoster = mVideo.getThumbnail();
      }

      ImageView poster = (ImageView) v.findViewById(R.id.step_edit_video_poster);
      RelativeLayout playButtonContainer = (RelativeLayout) v.findViewById(R.id.video_play_button_container);
      ImageButton playButton = (ImageButton) v.findViewById(R.id.video_play_button);

      // Size the video preview screenshot within the available screen space
      ViewGroup.LayoutParams params = fitToSpace(poster, mVideoPoster.getWidth(), mVideoPoster.getHeight());
      poster.setLayoutParams(params);
      playButtonContainer.setLayoutParams(params);

      Picasso picasso = PicassoUtils.with(mContext);
      RequestCreator request;
      String imageUrl = mVideoPoster.getPath(ImageSizes.stepMain, mIsOfflineGuide);

      if (mIsOfflineGuide) {
         request = picasso.load(new File(imageUrl));
      } else {
         request = picasso.load(imageUrl);
      }

      request.error(R.drawable.no_image)
       .into(poster);

      final String videoUrl = mVideo.getEncodings().get(0).getURL();
      playButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            startActivity(VideoViewActivity.viewVideo(mContext, videoUrl, mIsOfflineGuide));
         }
      });

      return v;
   }

   /////////////////////////////////////////////////////
   // NOTIFICATION LISTENERS
   /////////////////////////////////////////////////////

   private ViewGroup.LayoutParams fitToSpace(View view, float width, float height) {
      float newWidth, newHeight, padding = 0f;

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
      int actionBarHeight, indicatorHeight = 50;

      actionBarHeight = mResources.getDimensionPixelSize(
       com.actionbarsherlock.R.dimen.abs__action_bar_default_height);

      float pagePadding = viewPadding(R.dimen.page_padding);

      return actionBarHeight + indicatorHeight + pagePadding;
   }

   private float viewPadding(int view) {
      return mResources.getDimensionPixelSize(view) * 2f;
   }
}
