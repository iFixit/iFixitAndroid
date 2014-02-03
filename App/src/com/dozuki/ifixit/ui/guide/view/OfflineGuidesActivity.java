package com.dozuki.ifixit.ui.guide.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.ui.BaseMenuDrawerActivity;
import com.dozuki.ifixit.util.api.ApiDatabase;
import com.dozuki.ifixit.util.api.GuideMediaProgress;

import java.util.List;

public class OfflineGuidesActivity extends BaseMenuDrawerActivity implements
 LoaderManager.LoaderCallbacks<List<GuideMediaProgress>> {
   private static final String TAG = "OfflineGuidesActivity";

   private static class OfflineGuideLoader extends AsyncTaskLoader<List<GuideMediaProgress>> {
      private Context mContext;
      private Site mSite;
      private User mUser;
      private List<GuideMediaProgress> mGuides;

      public OfflineGuideLoader(Context context, Site site, User user) {
         super(context);

         mContext = context;
         mSite = site;
         mUser = user;
      }

      @Override
      public List<GuideMediaProgress> loadInBackground() {
         mGuides = ApiDatabase.get(mContext).getOfflineGuides(mSite, mUser);

         return mGuides;
      }

      @Override
      public void onStartLoading() {
         if (mGuides != null) {
            deliverResult(mGuides);
         }

         if (takeContentChanged() || mGuides == null) {
            forceLoad();
         }
      }
   }

   @Override
   public void onCreate(Bundle savedState) {
      super.onCreate(savedState);

      getSupportLoaderManager().initLoader(0, null, this);
   }

   @Override
   public Loader<List<GuideMediaProgress>> onCreateLoader(int i, Bundle bundle) {
      MainApplication app = MainApplication.get();
      return new OfflineGuideLoader(this, app.getSite(), app.getUser());
   }

   @Override
   public void onLoadFinished(Loader<List<GuideMediaProgress>> loader,
    List<GuideMediaProgress> guides) {
      Log.w(TAG, "Size: " + guides.size());
   }

   @Override
   public void onLoaderReset(Loader<List<GuideMediaProgress>> listLoader) {
      // TODO: Reset view?
   }
}
