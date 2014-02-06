package com.dozuki.ifixit.ui.guide.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.user.LoginEvent;
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
   private static final String REAUTHENTICATE = "REAUTHENTICATE";
   protected OfflineGuideListAdapter mAdapter;
   protected List<GuideMediaProgress> mGuides = Collections.emptyList();
   protected ListView mListView;
   protected BroadcastReceiver mGuideProgressReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
         // TODO: This forces a full refetch and parsing of data as well as
         // a full UI redraw.
         // TODO: Attach it to the Loader because it's the one that owns the data?
         getSupportLoaderManager().getLoader(R.id.offline_guide_loaderid).onContentChanged();
      }
   };

   public static Intent view(Context context) {
      return new Intent(context, OfflineGuidesActivity.class);
   }

   public static Intent reauthenticate(Context context) {
      Intent intent = view(context);
      intent.putExtra(REAUTHENTICATE, true);
      return intent;
   }

   @Override
   public void onCreate(Bundle savedState) {
      super.onCreate(savedState);

      setTitle(getString(R.string.offline_guides));
      setContentView(R.layout.offline_guides);
      mAdapter = new OfflineGuideListAdapter();
      mListView = (ListView)findViewById(R.id.offline_guides_listview);
      mListView.setAdapter(mAdapter);

      if (getIntent().getBooleanExtra(REAUTHENTICATE, false)) {
         // The sync service indicates that the user is logged out so lets make sure
         // that we think that the user is so login can happen as normal.
         MainApplication.get().shallowLogout(false);
      }

      if (!openLoginDialogIfLoggedOut()) {
         // Initialize the loader if the user is logged in. Otherwise this will
         // happen when the user logs in.
         initLoader();
      }
   }

   @Override
   public void onLogin(LoginEvent.Login loginEvent) {
      initLoader();
   }

   private void initLoader() {
      showLoading(R.id.loading_container);
      getSupportLoaderManager().initLoader(R.id.offline_guide_loaderid, null, this);
   }

   @Override
   public void onResume() {
      super.onResume();

      registerReceiver(mGuideProgressReceiver, new IntentFilter(
       ApiDatabase.OFFLINE_GUIDE_DATA_CHANGED));
   }

   @Override
   public void onPause() {
      super.onPause();

      unregisterReceiver(mGuideProgressReceiver);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getSupportMenuInflater().inflate(R.menu.offline_guide_menu, menu);
      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId())  {
         case R.id.offline_guide_sync_now:
            MainApplication.get().requestSync();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Override
   public boolean finishActivityIfLoggedOut() {
      return true;
   }

   @Override
   public Loader<List<GuideMediaProgress>> onCreateLoader(int i, Bundle bundle) {
      MainApplication app = MainApplication.get();
      return new OfflineGuideLoader(this, app.getSite(), app.getUser());
   }

   @Override
   public void onLoadFinished(Loader<List<GuideMediaProgress>> loader,
    List<GuideMediaProgress> guides) {
      hideLoading();
      mGuides = guides;

      mAdapter.notifyDataSetChanged();
   }

   @Override
   public void onLoaderReset(Loader<List<GuideMediaProgress>> listLoader) {
      // TODO: Reset view?
   }
}
