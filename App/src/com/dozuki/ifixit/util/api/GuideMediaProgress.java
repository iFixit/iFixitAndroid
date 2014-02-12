package com.dozuki.ifixit.util.api;

import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.model.Video;
import com.dozuki.ifixit.model.guide.Guide;
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
   public Set<String> mMissingMedia;
   public int mTotalMedia;
   public int mMediaProgress;

   public GuideMediaProgress(ApiEvent.ViewGuide guideEvent, ImageSizes imageSizes) {
      this(guideEvent.getResult(), imageSizes);

      mGuideEvent = guideEvent;
   }

   public GuideMediaProgress(Guide guide, ImageSizes imageSizes) {
      mGuide = guide;
      mMissingMedia = new HashSet<String>();
      mTotalMedia = 0;

      // TODO: This intro image isn't always set. We should fall back to the first step's
      // image if that exists and perhaps the device's image if that fails.
      Image introImage = mGuide.getIntroImage();
      if (introImage.isValid()) {
         addMediaIfMissing(introImage.getPath(imageSizes.getGrid()));
      }

      for (GuideStep step : mGuide.getSteps()) {
         for (Image image : step.getImages()) {
            addMediaIfMissing(image.getPath(imageSizes.getMain()));
            addMediaIfMissing(image.getPath(imageSizes.getThumb()));
            addMediaIfMissing(image.getPath(imageSizes.getFull()));
         }

         if (step.hasVideo()) {
            Video video = step.getVideo();
            addMediaIfMissing(video.getThumbnail().getPath(imageSizes.getMain()));
            // TODO: I don't think that the order of the encodings is reliable so
            // we should pick one that we like and use that.
            addMediaIfMissing(video.getEncodings().get(0).getURL());
         }
      }

      mMediaProgress = mTotalMedia - mMissingMedia.size();
   }

   public GuideMediaProgress(Guide guide, int totalMedia, int mediaProgress) {
      mGuide = guide;
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
}
