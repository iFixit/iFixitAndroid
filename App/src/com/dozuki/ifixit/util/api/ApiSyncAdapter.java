package com.dozuki.ifixit.util.api;

import android.accounts.Account;
import android.app.NotificationManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.model.auth.Authenticator;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.util.ImageSizes;
import com.github.kevinsawicki.http.HttpRequest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ApiSyncAdapter extends AbstractThreadedSyncAdapter {
   private static final String TAG = "ApiSyncAdapter";
   public static final int SYNC_NOTIFICATION_ID = 0;

   private static class ApiSyncException extends RuntimeException {
      public ApiSyncException() {
         super();
      }
      public ApiSyncException(Exception e) {
         super(e);
      }
   }

   /**
    * Displays sync progress in a Notification.
    */
   private NotificationManager mNotificationManager;
   private NotificationCompat.Builder mNotificationBuilder;

   public ApiSyncAdapter(Context context, boolean autoInitialize) {
      super(context, autoInitialize);
   }

   public ApiSyncAdapter(Context context, boolean autoInitialize,
    boolean allowParallelSyncs) {
      super(context, autoInitialize, allowParallelSyncs);
   }

   @Override
   public void onPerformSync(Account account, Bundle extras, String authority,
    ContentProviderClient provider, SyncResult syncResult) {
      mNotificationManager = (NotificationManager)MainApplication.get().
       getSystemService(Context.NOTIFICATION_SERVICE);
      mNotificationBuilder = new NotificationCompat.Builder(MainApplication.get());

      // TODO: Update text and icon.
      // Play Music has "Keeping requested music...".
      mNotificationBuilder.setContentTitle("Offline Sync");
      // Play Music displays the percentage as text rather than content text.
      mNotificationBuilder.setContentText("Syncing offline guides");
      mNotificationBuilder.setSmallIcon(R.drawable.icon);
      mNotificationBuilder.setOngoing(true);

      // TODO: setContentIntent to open to the offline guides list.

      Authenticator authenticator = new Authenticator(getContext());
      User user = authenticator.createUser(account);
      Site site = MainApplication.get().getSite();

      if (!site.mName.equals(user.mSiteName)) {
         // TODO: Retrieve the correct site so we can continue.
         Log.e("ApiSyncAdapter", "Sites do not match! " + site.mName + " vs. " +
          user.mSiteName);
         return;
      }

      try {
         updateNotificationProgress(0, 0, true);
         OfflineGuideSyncer syncer = new OfflineGuideSyncer(site, user);
         boolean changes = syncer.syncOfflineGuides();

         if (changes) {
            // TODO: Update text.
            mNotificationBuilder.setContentTitle("Offline Sync Complete");
            mNotificationBuilder.setOngoing(false);
            updateNotificationProgress(0, 0, false);
         } else {
            // Remove the notification if there aren't any changes.
            removeNotification();
         }
      } catch (ApiSyncException e) {
         Log.e(TAG, "Sync failed", e);
         // TODO: Notify the user?
      }
   }

   protected void updateNotificationProgress(int max, int progress, boolean indeterminate) {
      mNotificationBuilder.setProgress(max, progress, indeterminate);
      mNotificationManager.notify(SYNC_NOTIFICATION_ID, mNotificationBuilder.build());
   }

   protected void removeNotification() {
      mNotificationManager.cancel(SYNC_NOTIFICATION_ID);
   }

   private static String sBaseAppDirectory;
   private static String getBaseAppDirectory() {
      if (sBaseAppDirectory == null) {
         sBaseAppDirectory = MainApplication.get().getFilesDir().getAbsolutePath();
      }

      return sBaseAppDirectory;
   }

   public static String getOfflinePath(String imageUrl) {
      return getBaseAppDirectory() + "/offline_guides/images/" + imageUrl.hashCode();
   }

   private class OfflineGuideSyncer {

      private class GuideImageSet {
         public Guide mGuide;
         public Set<String> mMissingImages;
         public int mTotalImages;
         public int mImagesRemaining;

         public GuideImageSet(Guide guide) {
            mGuide = guide;
            mMissingImages = new HashSet<String>();
            mTotalImages = 0;

            addImageIfMissing(guide.getIntroImage().getPath(mImageSizes.getGrid()));

            for (GuideStep step : guide.getSteps()) {
               for (Image image : step.getImages()) {
                  addImageIfMissing(image.getPath(mImageSizes.getMain()));

                  // The counting is off because thumb is the same as getMain so we think
                  // we need to download double the number of images we actually need to.
                  //addImageIfMissing(image.getPath(mImageSizes.getThumb()));
               }
            }

            mImagesRemaining = mMissingImages.size();
         }

         private void addImageIfMissing(String imageUrl) {
            // Always add to the total.
            mTotalImages++;

            File file = new File(getOfflinePath(imageUrl));
            if (!file.exists()) {
               mMissingImages.add(imageUrl);
            }
         }
      }

      private final Site mSite;
      private final User mUser;
      private final ApiDatabase mDb;
      protected final ImageSizes mImageSizes;
      private boolean mChanges;

      public OfflineGuideSyncer(Site site, User user) {
         mSite = site;
         mUser = user;
         mDb = ApiDatabase.get(MainApplication.get());
         mImageSizes = MainApplication.get().getImageSizes();
         mChanges = false;
      }

      /**
       * Does the heavy lifting for finding stale guides, updating their contents,
       * and downloading images.
       *
       * TODO: It would be nice to delete images that are no longer referenced.
       */
      protected boolean syncOfflineGuides() {
         ApiCall apiCall = ApiCall.userFavorites(10000, 0);
         ApiEvent.UserFavorites favorites = performApiCall(apiCall, ApiEvent.UserFavorites.class);

         ArrayList<GuideInfo> staleGuides = getStaleGuides(favorites.getResult());
         ArrayList<Guide> updatedGuides = updateGuides(staleGuides);

         // TODO: We need to verify that all the images of all the guides are downloaded
         // in case the sync fails partway through and doesn't finish downloading all the
         downloadMissingImages(updatedGuides);

         return mChanges;
      }

      /**
       * Returns all guides that need syncing due to being brand new or having changes.
       * This also deletes all of the user's offline guides that are no longer favorited.
       */
      private ArrayList<GuideInfo> getStaleGuides(ArrayList<GuideInfo> favorites) {
         // TODO: Get guide info from manually added offline guides (those not coming
         // from favorites) and merge it with the favorite guides.

         Map<Integer, Double> modifiedDates = mDb.getGuideModifiedDates(mSite, mUser);
         ArrayList<GuideInfo> staleGuides = new ArrayList<GuideInfo>();

         for (GuideInfo guide : favorites) {
            Double modifiedDate = modifiedDates.get(guide.mGuideid);
            modifiedDates.remove(guide.mGuideid);

            if (hasNewerModifiedDate(modifiedDate, guide.getAbsoluteModifiedDate())) {
               staleGuides.add(guide);
            }
         }

         if (modifiedDates.size() > 0 || staleGuides.size() > 0) {
            mChanges = true;
         }

         // Delete any guides that are currently in the DB but are no longer favorited.
         mDb.deleteGuides(mSite, mUser, modifiedDates.keySet());

         return staleGuides;
      }

      private boolean hasNewerModifiedDate(Double existing, double updated) {
         // Double precision for such large values is pretty finicky... Realistically
         // a difference of a second in modified time isn't going to cause any problems.
         final double MAX_DATE_DISCREPANCY = 1.0;
         if (existing == null) {
            return true;
         }

         return (updated - existing) > MAX_DATE_DISCREPANCY;
      }

      /**
       * Updates the provided guides by downloading the full guide and adding/updating
       * the value stored in the DB.
       */
      private ArrayList<Guide> updateGuides(ArrayList<GuideInfo> staleGuides) {
         ArrayList<Guide> guides = new ArrayList<Guide>();

         for (GuideInfo staleGuide : staleGuides) {
            ApiEvent.ViewGuide fullGuide = performApiCall(ApiCall.guide(staleGuide.mGuideid),
             ApiEvent.ViewGuide.class);

            mDb.saveGuide(mSite, mUser, fullGuide);

            guides.add(fullGuide.getResult());
         }

         return guides;
      }

      /**
       * Downloads all new images contained in the guides.
       */
      private int downloadMissingImages(ArrayList<Guide> guides) {
         List<GuideImageSet> missingGuideImages = getMissingGuideImages(guides);
         int totalMissingImages = getTotalMissingImages(missingGuideImages);
         int imagesDownloaded = 0;

         createImageDirectories();

         for (GuideImageSet guideImages : missingGuideImages) {
            for (String imageUrl : guideImages.mMissingImages) {
               File file = new File(getOfflinePath(imageUrl));

               if (!file.exists()) {
                  try {
                     Log.d(TAG, "Downloading: " + imageUrl);

                     file.createNewFile();
                     HttpRequest request = HttpRequest.get(imageUrl);
                     request.receive(file);

                     imagesDownloaded++;
                     guideImages.mImagesRemaining--;
                  } catch (IOException e) {
                     Log.e(TAG, "Failed to download image", e);
                     throw new ApiSyncException(e);
                  } catch (HttpRequest.HttpRequestException e) {
                     Log.e(TAG, "Failed to download image", e);
                     throw new ApiSyncException(e);
                  }
               } else {
                  Log.d(TAG, "Skipping: " + imageUrl);
                  // Happens if guides share images.
                  imagesDownloaded++;
                  guideImages.mImagesRemaining--;
               }

               Log.w(TAG, guideImages.mTotalImages - guideImages.mImagesRemaining + "/" +
                guideImages.mTotalImages + " | Total downloaded: " + imagesDownloaded);

               updateNotificationProgress(totalMissingImages, imagesDownloaded, false);
            }

            // TODO: Mark guide as complete.
         }

         if (imagesDownloaded > 0) {
            mChanges = true;
         }

         Log.w(TAG, "Images: " + imagesDownloaded + "/" + totalMissingImages);

         return imagesDownloaded;
      }

      /**
       * Creates the necessary directories for storing images. This is purely so
       * File.mkdirs() isn't called for every single image downloaded. This makes it so
       * it is called at most once per sync.
       */
      private void createImageDirectories() {
         // The ending file name doesn't matter as long as the parents are the same
         // as a valid image path.
         File testFile = new File(getOfflinePath("test"));
         testFile.mkdirs();
      }

      /**
       * Returns a list of guides with all of the images that need to be downloaded.
       *
       * TODO: Verify that these are the only sizes used for these images.
       */
      private List<GuideImageSet> getMissingGuideImages(ArrayList<Guide> guides) {
         List<GuideImageSet> guideImages = new ArrayList<GuideImageSet>();

         for (Guide guide : guides) {
            GuideImageSet images = new GuideImageSet(guide);
            guideImages.add(images);
         }

         return guideImages;
      }

      private int getTotalMissingImages(List<GuideImageSet> guideImages) {
         int total = 0;

         for (GuideImageSet guide : guideImages) {
            total += guide.mMissingImages.size();
         }

         return total;
      }

      /**
       * Wrapper for Api.performAndParseApiCall() that throws an ApiSyncException on errors
       * and ensures type safety.
       *
       * TODO: Add retries.
       * TODO: 404's shouldn't cause the sync to fail -- it should remove the data if
       * it doesn't exist on the backend anymore.
       */
      private <T> T performApiCall(ApiCall apiCall, Class<T> type) {
         apiCall.updateUser(mUser);
         apiCall.mSite = mSite;

         ApiEvent<?> result = Api.performAndParseApiCall(apiCall);

         // TODO: Fail if it's a stored response i.e. we don't have internet.
         if (!result.hasError() && type.isInstance(result)) {
            return (T)result;
         } else {
            throw new ApiSyncException();
         }
      }
   }
}
