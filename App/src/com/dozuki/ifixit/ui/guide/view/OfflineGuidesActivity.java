package com.dozuki.ifixit.ui.guide.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.ui.BaseMenuDrawerActivity;
import com.dozuki.ifixit.ui.guide.create.OfflineGuideListItem;
import com.dozuki.ifixit.util.api.ApiDatabase;
import com.dozuki.ifixit.util.api.GuideMediaProgress;

import java.util.Collections;
import java.util.List;

public class OfflineGuidesActivity extends BaseMenuDrawerActivity implements
 LoaderManager.LoaderCallbacks<List<GuideMediaProgress>> {

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

   private class OfflineGuideListAdapter extends BaseAdapter {
      @Override
      public int getCount() {
         return mGuides.size();
      }

      @Override
      public Object getItem(int position) {
         return mGuides.get(position);
      }

      @Override
      public long getItemId(int position) {
         return position;
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
         OfflineGuideListItem itemView;
         GuideMediaProgress currItem = (GuideMediaProgress)getItem(position);

         if (convertView != null) {
            itemView = (OfflineGuideListItem)convertView;
         } else {
            itemView = new OfflineGuideListItem(OfflineGuidesActivity.this);
         }

         itemView.setRowData(currItem);

         return itemView;
      }
   }

   private static final String TAG = "OfflineGuidesActivity";
   protected OfflineGuideListAdapter mAdapter;
   protected List<GuideMediaProgress> mGuides = Collections.emptyList();
   protected ListView mListView;

   @Override
   public void onCreate(Bundle savedState) {
      super.onCreate(savedState);

      setTitle(getString(R.string.offline_guides));
      setContentView(R.layout.offline_guides);
      mAdapter = new OfflineGuideListAdapter();
      mListView = (ListView)findViewById(R.id.offline_guides_listview);
      mListView.setAdapter(mAdapter);

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
      mGuides = guides;

      mAdapter.notifyDataSetChanged();
   }

   @Override
   public void onLoaderReset(Loader<List<GuideMediaProgress>> listLoader) {
      // TODO: Reset view?
   }
}
