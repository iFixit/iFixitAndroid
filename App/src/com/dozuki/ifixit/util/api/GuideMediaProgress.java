package com.dozuki.ifixit.util.api;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.model.Video;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.util.ImageSizes;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Stores progress information about syncing guide media.
 */
public class GuideMediaProgress {
   public ApiEvent.ViewGuide mGuideEvent;
   public Guide mGuide;
   public GuideInfo mGuideInfo;
   public Set<String> mMissingMedia;
   public int mTotalMedia;
   public int mMediaProgress;

   public GuideMediaProgress(ApiEvent.ViewGuide guideEvent) {
      this(guideEvent.getResult());

      mGuideEvent = guideEvent;
   }

   public GuideMediaProgress(Guide guide) {
      mGuide = guide;
      mMissingMedia = new HashSet<String>();
      mTotalMedia = 0;

      Image introImage = mGuide.getIntroImage();
      HashSet<String> guideMedia = new HashSet<>();

      if (introImage.isValid()) {
         guideMedia.add(introImage.getPath(ImageSizes.guideList));
      }

      for (GuideStep step : mGuide.getSteps()) {
         for (Image image : step.getImages()) {
            guideMedia.add(image.getPath(ImageSizes.stepThumb));
            guideMedia.add(image.getPath(ImageSizes.stepMain));
            guideMedia.add(image.getPath(ImageSizes.stepFull));
         }

         if (step.hasVideo()) {
            Video video = step.getVideo();
            guideMedia.add(video.getThumbnail().getPath(ImageSizes.stepMain));
            guideMedia.add(video.getVideoUrl());
         }
      }

      Iterator<String> cachedUrls = null;
      try {
         cachedUrls = App.getClient().cache().urls();
      } catch (IOException e) {
         e.printStackTrace();
      }

      mTotalMedia = guideMedia.size();

      cachedUrls.forEachRemaining(url -> {
         if (guideMedia.contains(url)) {
            guideMedia.remove(url);
         }
      });

      mMediaProgress = guideMedia.size() - guideMedia.size();
   }

   public GuideMediaProgress(GuideInfo guideInfo, int totalMedia, int mediaProgress) {
      mGuideInfo = guideInfo;
      mTotalMedia = totalMedia;
      mMediaProgress = mediaProgress;
   }

   public boolean isComplete() {
      return mTotalMedia == mMediaProgress;
   }

   public void showProgress() {
      mMediaProgress++;
   }
}
