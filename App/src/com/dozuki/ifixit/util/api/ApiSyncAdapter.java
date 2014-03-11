package com.dozuki.ifixit.util.api;

import android.accounts.Account;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.auth.Authenticator;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.ui.BaseActivity;
import com.dozuki.ifixit.ui.guide.view.OfflineGuidesActivity;
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
   public static final String RESTART_SYNC = "RESTART_SYNC";
   // The value doesn't actually matter as long as it's not 0.
   private static final int INTENT_REQUEST_CODE = 1234;

   /**
    * Constants for BroadcastReceiver updates.
    */
   public static final String NEW_OFFLINE_GUIDE_ACTION =
    "com.dozuki.ifixit.util.api.new_offline_guide";
   public static final String GUIDE_PROGRESS_ACTION =
    "com.dozuki.ifixit.util.api.offline_guide_data_changed";
   public static final String GUIDEID = "GUIDEID";
   public static final String GUIDE_MEDIA_TOTAL = "GUIDE_MEDIA_TOTAL";
   public static final String GUIDE_MEDIA_DOWNLOADED = "GUIDE_MEDIA_DOWNLOADED";
   public static final String MEDIA_TOTAL = "MEDIA_TOTAL";
   public static final String MEDIA_DOWNLOADED = "MEDIA_DOWNLOADED";

   private static class ApiSyncException extends RuntimeException {
      public static final int GENERAL_EXCEPTION = 0;
      public static final int AUTH_EXCEPTION = 1;
      public static final int CONNECTION_EXCEPTION = 2;
      public static final int CANCELED_EXCEPTION = 3;
      public static final int RESTART_EXCEPTION = 4;

      public final int mExceptionType;
      public ApiSyncException(int exceptionType) {
         super();

         mExceptionType = exceptionType;
      }
      public ApiSyncException(int exceptionType, Exception e) {
         super(e);

         mExceptionType = exceptionType;
      }
   }

   /**
    * Displays sync progress in a Notification.
    */
   private NotificationManager mNotificationManager;
   private NotificationCompat.Builder mNotificationBuilder;
   protected boolean mIsSyncCanceled;
   protected boolean mRestartSync;
   protected Context mContext;

   protected BroadcastReceiver mRestartListener = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
         // Signal to restart the sync.
         mRestartSync = true;
      }
   };

   public ApiSyncAdapter(Context context, boolean autoInitialize) {
      super(context, autoInitialize);
      mContext = context;
   }

   public ApiSyncAdapter(Context context, boolean autoInitialize,
    boolean allowParallelSyncs) {
      super(context, autoInitialize, allowParallelSyncs);
      mContext = context;
   }

   /**
    * Sets a flag to restart the sync if it is running.
    */
   public static void restartSync(Context context) {
      Intent broadcast = new Intent();
      broadcast.setAction(RESTART_SYNC);
      context.sendBroadcast(broadcast);
   }

   @Override
   public void onPerformSync(Account account, Bundle extras, String authority,
    ContentProviderClient provider, SyncResult syncResult) {
      Authenticator authenticator = new Authenticator(getContext());
      User user = authenticator.createUser(account);
      App app = App.get();
      Site site = app.getSite();

      if (!site.mName.equals(user.mSiteName)) {
         // This can only happen on Dozuki because there is exactly one site on
         // every other app so it's guaranteed that the user will match the
         // default site.
         Site newSite = fetchSite(user.mSiteName);
         if (newSite == null) {
            Log.e(TAG, "Can't find site '" + user.mSiteName + "'!");
            return;
         }

         site = newSite;
      }

      try {
         app.registerReceiver(mRestartListener, new IntentFilter(RESTART_SYNC));
         boolean restart = false;
         do {
            try {
               restart = false;
               OfflineGuideSyncer syncer = new OfflineGuideSyncer(site, user);
               syncer.syncOfflineGuides();
               updateNotificationSuccess();
               App.get().setLastSyncTime(site, user);
            } catch (ApiSyncException e) {
               Log.e(TAG, "Sync failed", e);

               switch (e.mExceptionType) {
                  case ApiSyncException.AUTH_EXCEPTION:
                     syncResult.stats.numAuthExceptions++;
                     setAuthenticationNotification(site, user);
                     break;
                  case ApiSyncException.CANCELED_EXCEPTION:
                     // Let the system use the default retry mechanism for canceled syncs.
                     removeNotification();
                     break;
                  case ApiSyncException.RESTART_EXCEPTION:
                     restart = true;
                     break;
                  case ApiSyncException.GENERAL_EXCEPTION:
                  case ApiSyncException.CONNECTION_EXCEPTION:
                  default:
                     // Triggers a soft error that the system will use to determine how
                     // to reschedule the sync operation. Also remove the notification
                     // because it will likely work next time.
                     syncResult.stats.numIoExceptions++;
                     removeNotification();
                     break;
               }
            }
         } while (restart);
      } finally {
         app.unregisterReceiver(mRestartListener);
      }

      // Reset notification objects. Otherwise performing a sync will trigger the
      // notification success message because the builders aren't null. This means
      // that the same ApiSyncAdapter instance is used across multiple syncs.
      mNotificationBuilder = null;
      mNotificationManager = null;
   }

   /**
    * Returns the site with the given name or null if it isn't found.
    */
   private Site fetchSite(String siteName) {
      ApiEvent.Sites sites = performApiCall(ApiCall.sites(),
       App.get().getSite(), null, ApiEvent.Sites.class);

      for (Site site : sites.getResult()) {
         if (site.mName.equals(siteName)) {
            return site;
         }
      }

      return null;
   }

   @Override
   public void onSyncCanceled() {
      super.onSyncCanceled();

      // Set a flag that we will check periodically through the sync process so we can
      // gracefully cancel.
      mIsSyncCanceled = true;
   }

   protected void finishSyncIfCanceled() {
      if (mIsSyncCanceled) {
         mIsSyncCanceled = false;
         throw new ApiSyncException(ApiSyncException.CANCELED_EXCEPTION);
      }
      if (mRestartSync) {
         mRestartSync = false;
         throw new ApiSyncException(ApiSyncException.RESTART_EXCEPTION);
      }
   }

   protected void initializeNotification(Site site) {
      if (mNotificationBuilder != null) return;

      mNotificationManager = (NotificationManager) App.get().
       getSystemService(Context.NOTIFICATION_SERVICE);
      mNotificationBuilder = new NotificationCompat.Builder(App.get());

      mNotificationBuilder.setContentTitle(mContext.getString(R.string.notification_syncing));
      // TODO: Move icon to site res directory and make them for other flavors.
      mNotificationBuilder.setSmallIcon(R.drawable.ic_notification_icon);
      mNotificationBuilder.setOngoing(true);
      mNotificationBuilder.setAutoCancel(true);

      // TODO: The PendingIntent doesn't consistently work.
      Intent intent = BaseActivity.addSite(
       OfflineGuidesActivity.view(mContext), site);
      PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
       INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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

      mNotificationBuilder.setContentTitle(mContext.getString(R.string.notification_sync_complete));
      mNotificationBuilder.setOngoing(false);
      updateNotificationProgress(0, 0, false);
   }

   protected void setAuthenticationNotification(Site site, User user) {
      initializeNotification(site);

      Intent intent = BaseActivity.addSite(
       OfflineGuidesActivity.reauthenticate(mContext), site);
      PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
       INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      mNotificationBuilder.setContentIntent(pendingIntent);

      mNotificationBuilder.setContentTitle(mContext.getString(R.string.signin_error, user.mEmail));
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
         sBaseAppDirectory = App.get().getFilesDir().getAbsolutePath();
      }

      return sBaseAppDirectory;
   }

   public static String getOfflineMediaPath(String mediaUrl) {
      String path = getBaseAppDirectory() + "/offline_guides/media/" + mediaUrl.hashCode();

      return path;
   }

   /**
    * Wrapper for Api.performAndParseApiCall() that throws an ApiSyncException on errors
    * and ensures type safety. Returns null if the content is no longer available.
    */
   protected <T> T performApiCall(ApiCall apiCall, Site site, User user, Class<T> type) {
      apiCall.updateUser(user);
      apiCall.mSite = site;

      ApiEvent<?> result = Api.performAndParseApiCall(apiCall);

      if (result.mStoredResponse) {
         // Don't continue if we're getting stored responses i.e. we lost internet.
         throw new ApiSyncException(ApiSyncException.CONNECTION_EXCEPTION);
      } else if (!result.hasError() && type.isInstance(result)) {
         // The result is valid -- return it.
         return (T)result;
      } else if (result.mCode == 401) {
         // We are no longer authenticated and must ask the user to reauthenticate.
         throw new ApiSyncException(ApiSyncException.AUTH_EXCEPTION);
      } else if (result.mCode == 404 || result.mCode == 403) {
         // Return null to indicate that the content is no longer available.
         return null;
      } else {
         throw new ApiSyncException(ApiSyncException.GENERAL_EXCEPTION);
      }
   }

   private class OfflineGuideSyncer {
      // Update at most every 10 seconds so we don't spend all of our time updating
      // values in the DB.
      private static final int GUIDE_PROGRESS_INTERVAL_MS = 10000;

      private final Site mSite;
      private final User mUser;
      private final ApiDatabase mDb;
      private long mLastProgressUpdate;

      public OfflineGuideSyncer(Site site, User user) {
         mSite = site;
         mUser = user;
         mDb = ApiDatabase.get(App.get());
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
            guideMedia.add(new GuideMediaProgress(guide));
         }

         return guideMedia;
      }

      /**
       * Returns all guides that need syncing due to being brand new or having changes.
       * This also deletes all of the user's offline guides that are no longer favorited.
       */
      private ArrayList<GuideInfo> getStaleGuides() {
         ApiCall apiCall = ApiCall.userFavorites(10000, 0);
         ApiEvent.UserFavorites favoritesEvent = apiCall(apiCall,
          ApiEvent.UserFavorites.class);
         ArrayList<GuideInfo> favorites = favoritesEvent.getResult();

         Map<Integer, Double> modifiedDates = mDb.getGuideModifiedDates(mSite, mUser);
         ArrayList<GuideInfo> staleGuides = new ArrayList<GuideInfo>();

         for (GuideInfo guide : favorites) {
            Double modifiedDate = modifiedDates.get(guide.mGuideid);
            modifiedDates.remove(guide.mGuideid);

            // Initialize the notification if there is a new guide being synced.
            if (modifiedDate == null) {
               initializeNotification(mSite);
            }

            if (hasNewerModifiedDate(modifiedDate, guide.getAbsoluteModifiedDate())) {
               staleGuides.add(guide);
            }
         }

         // Delete any guides that are currently in the DB but are no longer favorited.
         if (!modifiedDates.isEmpty()) {
            mDb.deleteGuides(mSite, mUser, modifiedDates.keySet());
            // Although not technically a "new guide", this triggers a UI refresh.
            sendNewGuideBroadcast();
         }

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
         Set<Integer> guidesToDelete = null;

         for (GuideInfo staleGuide : staleGuides) {
            finishSyncIfCanceled();
            ApiEvent.ViewGuide fullGuide = apiCall(ApiCall.guide(staleGuide.mGuideid),
             ApiEvent.ViewGuide.class);

            if (fullGuide == null) {
               Log.w(TAG, "Guide not found! Deleting..." + staleGuide.mGuideid);
               if (guidesToDelete == null) {
                  // Lazy initialization.
                  guidesToDelete = new HashSet<Integer>();
               }

               // Guide is now inaccessible so we need to remove it from the DB.
               guidesToDelete.add(staleGuide.mGuideid);
               continue;
            }

            GuideMediaProgress guideMedia = new GuideMediaProgress(fullGuide);

            mDb.saveGuide(mSite, mUser, guideMedia.mGuideEvent, guideMedia.mTotalMedia,
             guideMedia.mMediaProgress);
            sendNewGuideBroadcast();

            guides.add(guideMedia);
         }

         if (guidesToDelete != null) {
            mDb.deleteGuides(mSite, mUser, guidesToDelete);
            // Although not technically a new guide, this triggers an update of the UI.
            sendNewGuideBroadcast();
         }

         return guides;
      }

      /**
       * Notifies receivers that there was a new offline guide added.
       */
      private void sendNewGuideBroadcast() {
         Intent broadcast = new Intent();
         broadcast.setAction(NEW_OFFLINE_GUIDE_ACTION);
         mContext.sendBroadcast(broadcast);
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
               finishSyncIfCanceled();

               if (downloadMedium(mediaUrl)) {
                  mediaDownloaded++;
                  guideMedia.mMediaProgress++;
               } else if (mediaUrl.contains(".huge")) {
                  // Download the original image instead because FullScreenImageView will
                  // default to that one.
                  String originalUrl = mediaUrl.replace(".huge", "");
                  if (downloadMedium(originalUrl)) {
                     mediaDownloaded++;
                     guideMedia.mMediaProgress++;
                  } else {
                     // Continue on with the next medium. The progress won't be updated
                     // because the medium wasn't successfully retrieved. Note that other
                     // failures such as missing internet will throw an exception which
                     // will cause the sync process to exit immediately.
                     continue;
                  }
               }

               updateTotalProgress(guideMedia, totalMissingMedia, mediaDownloaded);
               updateNotificationProgress(totalMissingMedia, mediaDownloaded, false);
               updateGuideProgress(guideMedia, true);
            }

            // Make sure the guide is marked as complete.
            updateGuideProgress(guideMedia, false);
         }

         Log.w(TAG, "Media: " + mediaDownloaded + "/" + totalMissingMedia);
      }

      /**
       * Downloads the medium to the persistent location. Returns true if the medium is
       * persisted, false otherwise.
       */
      private boolean downloadMedium(String mediaUrl) {
         File file = new File(getOfflineMediaPath(mediaUrl));

         if (!file.exists()) {
            try {
               Log.d(TAG, "Downloading: " + mediaUrl);

               file.createNewFile();
               HttpRequest request = HttpRequest.get(mediaUrl);
               request.receive(file);

               if (request.code() < 200 || request.code() >= 300) {
                  Log.e(TAG, "MEDIA FAIL! " + mediaUrl);
                  return false;
               }
            } catch (IOException e) {
               Log.e(TAG, "Failed to download medium", e);
               throw new ApiSyncException(ApiSyncException.CONNECTION_EXCEPTION, e);
            } catch (HttpRequest.HttpRequestException e) {
               Log.e(TAG, "Failed to download medium", e);
               throw new ApiSyncException(ApiSyncException.CONNECTION_EXCEPTION, e);
            }
         } else {
            Log.d(TAG, "Skipping: " + mediaUrl);
            // Happens if guides share media.
         }

         return true;
      }

      /**
       * Sends out an update to BroadcastReceivers anytime guide progress is updated.
       */
      private void updateTotalProgress(GuideMediaProgress guide, int totalMissingMedia,
       int mediaDownloaded) {
         Intent broadcast = new Intent();
         broadcast.setAction(GUIDE_PROGRESS_ACTION);

         broadcast.putExtra(GUIDEID, guide.mGuide.getGuideid());
         broadcast.putExtra(GUIDE_MEDIA_TOTAL, guide.mTotalMedia);
         broadcast.putExtra(GUIDE_MEDIA_DOWNLOADED, guide.mMediaProgress);
         broadcast.putExtra(MEDIA_TOTAL, totalMissingMedia);
         broadcast.putExtra(MEDIA_DOWNLOADED, mediaDownloaded);

         mContext.sendBroadcast(broadcast);
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

      private<T> T apiCall(ApiCall apiCall, Class<T> type) {
         return performApiCall(apiCall, mSite, mUser, type);
      }
   }
}
