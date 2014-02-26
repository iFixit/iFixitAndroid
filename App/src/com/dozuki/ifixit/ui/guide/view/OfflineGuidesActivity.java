package com.dozuki.ifixit.ui.guide.view;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.user.LoginEvent;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.ui.BaseMenuDrawerActivity;
import com.dozuki.ifixit.ui.guide.create.OfflineGuideListItem;
import com.dozuki.ifixit.util.api.ApiContentProvider;
import com.dozuki.ifixit.util.api.ApiDatabase;
import com.dozuki.ifixit.util.api.ApiSyncAdapter;
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
      protected boolean mDisplayLiveImages;

      public OfflineGuideListAdapter(boolean displayLiveImages) {
         mDisplayLiveImages = displayLiveImages;
      }

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

         itemView.setRowData(currItem, mDisplayLiveImages);

         return itemView;
      }
   }

   private static final String TAG = "OfflineGuidesActivity";
   private static final String REAUTHENTICATE = "REAUTHENTICATE";

   protected SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
      @Override
      public void onStatusChanged(int which) {
         runOnUiThread(new Runnable() {
            @Override
            public void run() {
               refreshSyncStatus(/* force */ false);
            }
         });
      }
   };
   protected Object mSyncObserverHandle;

   protected OfflineGuideListAdapter mAdapter;
   protected List<GuideMediaProgress> mGuides = Collections.emptyList();
   protected ListView mListView;
   protected Button mSyncButton;
   protected TextView mSyncStatusText;
   protected ProgressBar mSyncProgressBar;
   protected boolean mIsSyncing;
   protected BroadcastReceiver mNewGuideReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
         getSupportLoaderManager().getLoader(R.id.offline_guide_loaderid).onContentChanged();
      }
   };

   protected BroadcastReceiver mGuideProgressReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
         Log.w(TAG, "PROGRESS");
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
      mAdapter = new OfflineGuideListAdapter(MainApplication.get().isConnected());
      mListView = (ListView)findViewById(R.id.offline_guides_listview);
      mSyncButton = (Button)findViewById(R.id.sync_button);
      mSyncStatusText = (TextView)findViewById(R.id.sync_status_text);
      mSyncProgressBar = (ProgressBar)findViewById(R.id.sync_progress_bar);
      mListView.setAdapter(mAdapter);
      mListView.setEmptyView(findViewById(R.id.no_offline_guides_text));

      mSyncButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if (mIsSyncing) {
               MainApplication.get().cancelSync();
            } else {
               MainApplication.get().requestSync();
            }
         }
      });

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

      refreshSyncStatus(/* force */ true);
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

      registerReceiver(mNewGuideReceiver, new IntentFilter(
       ApiSyncAdapter.NEW_OFFLINE_GUIDE_ACTION));
      registerReceiver(mGuideProgressReceiver, new IntentFilter(
       ApiSyncAdapter.GUIDE_PROGRESS_ACTION));

      mSyncObserverHandle = ContentResolver.addStatusChangeListener(
       ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE | ContentResolver.SYNC_OBSERVER_TYPE_PENDING,
       mSyncStatusObserver);
   }

   @Override
   public void onPause() {
      super.onPause();

      unregisterReceiver(mNewGuideReceiver);
      unregisterReceiver(mGuideProgressReceiver);

      if (mSyncObserverHandle != null) {
         ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
      }
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // TODO: Remove menu if nothing else is added to it.
      //getSupportMenuInflater().inflate(R.menu.offline_guide_menu, menu);
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

   protected void refreshSyncStatus(boolean forceUpdate) {
      final String authority = ApiContentProvider.getAuthority();
      final Account account = MainApplication.get().getUserAccount();
      boolean isSyncing = ContentResolver.isSyncActive(account, authority) ||
       ContentResolver.isSyncPending(account, authority);

      // This gets called a lot so we only update the UI if there is a change.
      if (forceUpdate || isSyncing != mIsSyncing) {
         mIsSyncing = isSyncing;

         // TODO: Clean up strings/values/etc.
         mSyncProgressBar.setMax(100);
         mSyncProgressBar.setProgress(mIsSyncing ? 50 : 0);

         long lastSyncTime = MainApplication.get().getLastSyncTime();
         CharSequence lastSyncTimeString;

         if (lastSyncTime == MainApplication.NEVER_SYNCED_VALUE) {
            lastSyncTimeString = "Never";
         } else {
            lastSyncTimeString = DateUtils.getRelativeTimeSpanString(lastSyncTime);
         }

         mSyncStatusText.setText(mIsSyncing ? "Syncing" : "Last synced: " + lastSyncTimeString);

         mSyncButton.setText(mIsSyncing ? "Cancel" : "Refresh");
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
