package com.dozuki.ifixit.util.api;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.dozuki.ifixit.MainApplication;
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
import java.util.Map;
import java.util.Set;

public class ApiSyncAdapter extends AbstractThreadedSyncAdapter {
   private static final String TAG = "ApiSyncAdapter";
   private static class ApiSyncException extends RuntimeException {}

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
         OfflineGuideSyncer syncer = new OfflineGuideSyncer(site, user);
         syncer.syncOfflineGuides();
      } catch (ApiSyncException e) {
         Log.e(TAG, "Sync failed", e);
         // TODO: Notify the user?
      }
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

   private static class OfflineGuideSyncer {
      private final Site mSite;
      private final User mUser;
      private final ApiDatabase mDb;
      private final ImageSizes mImageSizes;

      public OfflineGuideSyncer(Site site, User user) {
         mSite = site;
         mUser = user;
         mDb = ApiDatabase.get(MainApplication.get());
         mImageSizes = MainApplication.get().getImageSizes();
      }

      /**
       * Does the heavy lifting for finding stale guides, updating their contents,
       * and downloading images.
       *
       * TODO: It would be nice to delete images that are no longer referenced.
       */
      protected void syncOfflineGuides() {
         ApiCall apiCall = ApiCall.userFavorites(10000, 0);
         ApiEvent.UserFavorites favorites = performApiCall(apiCall, ApiEvent.UserFavorites.class);

         ArrayList<GuideInfo> staleGuides = getStaleGuides(favorites.getResult());
         ArrayList<Guide> updatedGuides = updateGuides(staleGuides);

         // TODO: We need to verify that all the images of all the guides are downloaded
         // in case the sync fails partway through and doesn't finish downloading all the
         downloadMissingImages(updatedGuides);
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

         // Delete any guides that are currently in the DB but are no longer favorited.
         mDb.deleteGuides(mSite, mUser, modifiedDates.keySet());

         return staleGuides;
      }

      private static boolean hasNewerModifiedDate(Double existing, double updated) {
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
      private void downloadMissingImages(ArrayList<Guide> guides) {
         Set<String> allImages = getAllRequiredImages(guides);

         createImageDirectories();

         for (String imageUrl : allImages) {
            File file = new File(getOfflinePath(imageUrl));
            if (!file.exists()) {
               try {
                  file.createNewFile();
                  Log.d(TAG, "Downloading: " + imageUrl);
                  HttpRequest request = HttpRequest.get(imageUrl);
                  request.receive(file);
               } catch (IOException e) {
                  Log.e(TAG, "Failed to download image", e);
               } catch (HttpRequest.HttpRequestException e) {
                  Log.e(TAG, "Failed to download image", e);
               }
            } else {
               Log.e(TAG, "Skipping: " + imageUrl);
            }
         }
      }

      /**
       * Creates the necessary directories for storing images. This is purely so
       * File.mkdirs() isn't called for every single image downloaded. This makes it so
       * it is called at most once per sync.
       */
      private static void createImageDirectories() {
         // The ending file name doesn't matter as long as the parents are the same
         // as a valid image path.
         File testFile = new File(getOfflinePath("test"));
         testFile.mkdirs();
      }

      /**
       * Returns a complete set of required images for the list of guides.
       *
       * TODO: Verify that these are the only sizes used for these images.
       */
      private Set<String> getAllRequiredImages(ArrayList<Guide> guides) {
         Set<String> images = new HashSet<String>();

         for (Guide guide : guides) {
            images.add(guide.getIntroImage().getPath(mImageSizes.getGrid()));

            for (GuideStep step : guide.getSteps()) {
               for (Image image : step.getImages()) {
                  images.add(image.getPath(mImageSizes.getMain()));
                  images.add(image.getPath(mImageSizes.getThumb()));
               }
            }
         }

         return images;
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
