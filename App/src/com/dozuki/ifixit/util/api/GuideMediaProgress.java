package com.dozuki.ifixit.util.api;

import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.model.Video;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.util.ImageSizes;

import java.io.File;
import java.util.HashSet;
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
      if (introImage.isValid()) {
         addMediaIfMissing(introImage.getPath(ImageSizes.guideList));
      }

      for (GuideStep step : mGuide.getSteps()) {
         for (Image image : step.getImages()) {
            addMediaIfMissing(image.getPath(ImageSizes.stepThumb));
            addMediaIfMissing(image.getPath(ImageSizes.stepMain));
            addMediaIfMissing(image.getPath(ImageSizes.stepFull));
         }

         if (step.hasVideo()) {
            Video video = step.getVideo();
            addMediaIfMissing(video.getThumbnail().getPath(ImageSizes.stepMain));
            addMediaIfMissing(video.getVideoUrl());
         }
      }

      mMediaProgress = mTotalMedia - mMissingMedia.size();
   }

   public GuideMediaProgress(GuideInfo guideInfo, int totalMedia, int mediaProgress) {
      mGuideInfo = guideInfo;
      mTotalMedia = totalMedia;
      mMediaProgress = mediaProgress;
   }

   private void addMediaIfMissing(String imageUrl) {
      if (mMissingMedia.contains(imageUrl)) {
         // Don't acknowledge duplicates in the total.
         return;
      }

      mTotalMedia++;

      File file = new File(ApiSyncAdapter.getOfflineMediaPath(imageUrl));
      if (!file.exists()) {
         mMissingMedia.add(imageUrl);
      }
   }

   public boolean isComplete() {
      return mTotalMedia == mMediaProgress;
   }
}
