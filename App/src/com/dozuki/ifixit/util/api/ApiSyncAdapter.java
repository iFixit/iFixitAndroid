package com.dozuki.ifixit.util.api;

import android.accounts.Account;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.auth.Authenticator;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.ui.guide.view.OfflineGuidesActivity;
import com.dozuki.ifixit.util.ImageSizes;
import com.github.kevinsawicki.http.HttpRequest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApiSyncAdapter extends AbstractThreadedSyncAdapter {
   private static final String TAG = "ApiSyncAdapter";

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
      boolean manualSync = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL);

      Authenticator authenticator = new Authenticator(getContext());
      User user = authenticator.createUser(account);
      Site site = MainApplication.get().getSite();

      if (!site.mName.equals(user.mSiteName)) {
         // TODO: Retrieve the correct site so we can continue.
         Log.e("ApiSyncAdapter", "Sites do not match! " + site.mName + " vs. " +
          user.mSiteName);
         return;
      }

      if (manualSync) {
         initializeNotification();
      }

      try {
         OfflineGuideSyncer syncer = new OfflineGuideSyncer(site, user, manualSync);
         syncer.syncOfflineGuides();
         updateNotificationSuccess();
      } catch (ApiSyncException e) {
         Log.e(TAG, "Sync failed", e);
         // TODO: Notify the user?
      }
   }

   protected void initializeNotification() {
      if (mNotificationBuilder != null) return;

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
      Intent intent = new Intent(MainApplication.get(), OfflineGuidesActivity.class);
      PendingIntent pendingIntent = PendingIntent.getActivity(MainApplication.get(),
       /* requestCode = */ 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      mNotificationBuilder.setContentIntent(pendingIntent);

      // Set indeterminate progress and display it.
      updateNotificationProgress(0, 0, true);
   }

   protected void updateNotificationProgress(int max, int progress, boolean indeterminate) {
      if (mNotificationBuilder == null) return;

      mNotificationBuilder.setProgress(max, progress, indeterminate);
      mNotificationManager.notify(R.id.guide_sync_notificationid, mNotificationBuilder.build());
   }

   protected void updateNotificationSuccess() {
      if (mNotificationBuilder == null) return;

      // TODO: Update text.
      mNotificationBuilder.setContentTitle("Offline Sync Complete");
      mNotificationBuilder.setOngoing(false);
      updateNotificationProgress(0, 0, false);
   }

   protected void removeNotification() {
      if (mNotificationBuilder == null) return;

      mNotificationManager.cancel(R.id.guide_sync_notificationid);
   }

   private static String sBaseAppDirectory;
   private static String getBaseAppDirectory() {
      if (sBaseAppDirectory == null) {
         sBaseAppDirectory = MainApplication.get().getFilesDir().getAbsolutePath();
      }

      return sBaseAppDirectory;
   }

   public static String getOfflineMediaPath(String mediaUrl) {
      String path = getBaseAppDirectory() + "/offline_guides/media/" + mediaUrl.hashCode();

      return path;
   }

   private class OfflineGuideSyncer {
      // Update at most every 5 seconds so we don't spend all of our time updating
      // values in the DB.
      private static final int GUIDE_PROGRESS_INTERVAL_MS = 5000;

      private final Site mSite;
      private final User mUser;
      private final ApiDatabase mDb;
      protected final ImageSizes mImageSizes;
      private long mLastProgressUpdate;
      private boolean mManualSync;

      public OfflineGuideSyncer(Site site, User user, boolean manualSync) {
         mSite = site;
         mUser = user;
         mManualSync = manualSync;
         mDb = ApiDatabase.get(MainApplication.get());
         mImageSizes = MainApplication.get().getImageSizes();
         mLastProgressUpdate = 0;
      }

      /**
       * Does the heavy lifting for finding stale guides, updating their contents,
       * and downloading media.
       *
       * TODO: It would be nice to delete media that are no longer referenced.
       */
      protected void syncOfflineGuides() {
         ArrayList<GuideMediaProgress> uncompletedGuides = getUncompletedGuides();
         ArrayList<GuideInfo> staleGuides = getStaleGuides();
         ArrayList<GuideMediaProgress> updatedGuides = updateGuides(staleGuides);

         // Merge updated guides with guides with missing media and fetch all of
         // their media.
         uncompletedGuides.addAll(updatedGuides);
         downloadMissingMedia(uncompletedGuides);
      }

      private ArrayList<GuideMediaProgress> getUncompletedGuides() {
         ArrayList<GuideMediaProgress> guideMedia = new ArrayList<GuideMediaProgress>();

         for (Guide guide : mDb.getUncompleteGuides(mSite, mUser)) {
            guideMedia.add(new GuideMediaProgress(guide, mImageSizes));
         }

         return guideMedia;
      }

      /**
       * Returns all guides that need syncing due to being brand new or having changes.
       * This also deletes all of the user's offline guides that are no longer favorited.
       */
      private ArrayList<GuideInfo> getStaleGuides() {
         ApiCall apiCall = ApiCall.userFavorites(10000, 0);
         ApiEvent.UserFavorites favoritesEvent = performApiCall(apiCall,
          ApiEvent.UserFavorites.class);
         ArrayList<GuideInfo> favorites = favoritesEvent.getResult();

         // TODO: Get guide info from manually added offline guides (those not coming
         // from favorites) and merge it with the favorite guides.

         Map<Integer, Double> modifiedDates = mDb.getGuideModifiedDates(mSite, mUser);
         ArrayList<GuideInfo> staleGuides = new ArrayList<GuideInfo>();

         for (GuideInfo guide : favorites) {
            Double modifiedDate = modifiedDates.get(guide.mGuideid);
            modifiedDates.remove(guide.mGuideid);

            // Initialize the notification if there is a new guide being synced.
            if (modifiedDate == null) {
               initializeNotification();
            }

            if (hasNewerModifiedDate(modifiedDate, guide.getAbsoluteModifiedDate())) {
               staleGuides.add(guide);
            }
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
      private ArrayList<GuideMediaProgress> updateGuides(ArrayList<GuideInfo> staleGuides) {
         ArrayList<GuideMediaProgress> guides = new ArrayList<GuideMediaProgress>();

         for (GuideInfo staleGuide : staleGuides) {
            ApiEvent.ViewGuide fullGuide = performApiCall(ApiCall.guide(staleGuide.mGuideid),
             ApiEvent.ViewGuide.class);
            GuideMediaProgress guideMedia = new GuideMediaProgress(fullGuide, mImageSizes);

            mDb.saveGuide(mSite, mUser, guideMedia.mGuideEvent, guideMedia.mTotalMedia,
             guideMedia.mMediaProgress);

            guides.add(guideMedia);
         }

         return guides;
      }

      /**
       * Downloads all new images contained in the guides.
       */
      private void downloadMissingMedia(List<GuideMediaProgress> missingGuideMedia) {
         int totalMissingMedia = getTotalMissingMedia(missingGuideMedia);
         int mediaDownloaded = 0;

         createMediaDirectories();

         for (GuideMediaProgress guideMedia : missingGuideMedia) {
            for (String mediaUrl : guideMedia.mMissingMedia) {
               File file = new File(getOfflineMediaPath(mediaUrl));

               if (!file.exists()) {
                  try {
                     Log.d(TAG, "Downloading: " + mediaUrl);

                     file.createNewFile();
                     HttpRequest request = HttpRequest.get(mediaUrl);
                     request.receive(file);

                     if (request.code() == 404) {
                        Log.e(TAG, "404 FOR MEDIA! " + mediaUrl);
                        // If it's a .huge, download the original size instead because
                        // FallBackImageView will use that one instead.
                     }

                     mediaDownloaded++;
                     guideMedia.mMediaProgress++;
                  } catch (IOException e) {
                     Log.e(TAG, "Failed to download medium", e);
                     throw new ApiSyncException(e);
                  } catch (HttpRequest.HttpRequestException e) {
                     Log.e(TAG, "Failed to download medium", e);
                     throw new ApiSyncException(e);
                  }
               } else {
                  Log.d(TAG, "Skipping: " + mediaUrl);
                  // Happens if guides share media.
                  mediaDownloaded++;
                  guideMedia.mMediaProgress++;
               }

               updateNotificationProgress(totalMissingMedia, mediaDownloaded, false);

               // Only update at the end when the guide is complete if this is a manual sync
               // because it isn't likely that the user will see the updates. This avoids
               // lots of unnecessary DB writes.
               if (!mManualSync) {
                  updateGuideProgress(guideMedia, true);
               }
            }

            // Make sure the guide is marked as complete.
            updateGuideProgress(guideMedia, false);
         }

         Log.w(TAG, "Media: " + mediaDownloaded + "/" + totalMissingMedia);
      }

      private void updateGuideProgress(GuideMediaProgress guide, boolean rateLimit) {
         if (!rateLimit || System.currentTimeMillis() > mLastProgressUpdate +
          GUIDE_PROGRESS_INTERVAL_MS) {

            Log.w(TAG, "Updating progress: " + guide.mMediaProgress + "/" + guide.mTotalMedia);

            mDb.updateGuideProgress(mSite, mUser, guide.mGuide.getGuideid(), guide.mTotalMedia,
             guide.mMediaProgress);

            mLastProgressUpdate = System.currentTimeMillis();
         }
      }

      /**
       * Creates the necessary directories for storing media. This is purely so
       * File.mkdirs() isn't called for every single medium downloaded. This makes it so
       * it is called at most once per sync.
       */
      private void createMediaDirectories() {
         // The ending file name doesn't matter as long as the parents are the same
         // as a valid media path.
         File testFile = new File(getOfflineMediaPath("test"));
         testFile.mkdirs();
      }

      private int getTotalMissingMedia(List<GuideMediaProgress> guideMedia) {
         int total = 0;

         for (GuideMediaProgress guide : guideMedia) {
            total += guide.mMissingMedia.size();
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
