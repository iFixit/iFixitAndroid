package com.dozuki.ifixit.util.api;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.model.auth.Authenticator;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.model.user.User;

import java.util.ArrayList;

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

   private static class OfflineGuideSyncer {
      private final Site mSite;
      private final User mUser;

      public OfflineGuideSyncer(Site site, User user) {
         mSite = site;
         mUser = user;
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

         deleteUnfavoritedGuides(favorites.getResult());
         ArrayList<GuideInfo> staleGuides = getStaleGuides(favorites.getResult());
         ArrayList<Guide> updatedGuides = updateGuides(staleGuides);
         downloadMissingImages(updatedGuides);
      }

      /**
       * Deletes any guides that are currently stored offline due to being favorited
       * but are no longer favorited now.
       */
      private void deleteUnfavoritedGuides(ArrayList<GuideInfo> favorites) {
         // TODO: The thing.
      }

      /**
       * Returns all guides that need syncing due to being brand new or having changes.
       */
      private ArrayList<GuideInfo> getStaleGuides(ArrayList<GuideInfo> favorites) {
         // TODO: Get guide info from manually added offline guides (those not coming
         // from favorites) and merge it with the favorite guides.
         // TODO: Compare this list with the stored guides to return only new and
         // modified guides.

         return favorites;
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

            // TODO: Store the guide.

            guides.add(fullGuide.getResult());
         }

         return guides;
      }

      /**
       * Downloads all new images contained in the guides.
       */
      private void downloadMissingImages(ArrayList<Guide> guides) {
         // TODO: The thing. Specifically: Gather a set of all images required to view the
         // guide on this device (for thumbnail, main, and full screen) and limit it to
         // ones that don't already exist.
      }

      /**
       * Wrapper for Api.performAndParseApiCall() that throws an ApiSyncException on errors
       * and ensures type safety.
       */
      private <T> T performApiCall(ApiCall apiCall, Class<T> type) {
         apiCall.updateUser(mUser);
         apiCall.mSite = mSite;

         ApiEvent<?> result = Api.performAndParseApiCall(apiCall);

         if (!result.hasError() && type.isInstance(result)) {
            return (T)result;
         } else {
            throw new ApiSyncException();
         }
      }
   }
}
