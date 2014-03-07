package com.dozuki.ifixit.ui.guide.view;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.user.LoginEvent;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.ui.BaseMenuDrawerActivity;
import com.dozuki.ifixit.ui.guide.create.OfflineGuideListItem;
import com.dozuki.ifixit.util.Utils;
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

         itemView.setRowData(currItem, mDisplayLiveImages, mIsSyncing);

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
   protected RelativeLayout mSyncBox;
   protected TextView mSyncCommand;
   protected TextView mSyncStatusText;
   protected Button mCancelButton;
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
         Bundle extras = intent.getExtras();

         updateGuideProgress(
            extras.getInt(ApiSyncAdapter.GUIDEID),
            extras.getInt(ApiSyncAdapter.GUIDE_MEDIA_DOWNLOADED),
            extras.getInt(ApiSyncAdapter.GUIDE_MEDIA_TOTAL)
         );

         updateTotalProgress(
            extras.getInt(ApiSyncAdapter.MEDIA_DOWNLOADED),
            extras.getInt(ApiSyncAdapter.MEDIA_TOTAL)
         );
      }
   };

   private final int SYNC_TIME_REFRESH_INTERVAL = 60000;
   private Handler mSyncTimeUpdateHandler;
   private Runnable mSyncTimeUpdateRunnable;

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

      final App app = App.get();
      final boolean hasInternet = app.isConnected();

      setTitle(R.string.favorites);
      setContentView(R.layout.offline_guides);
      mAdapter = new OfflineGuideListAdapter(hasInternet);
      mListView = (ListView)findViewById(R.id.offline_guides_listview);
      mSyncBox = (RelativeLayout)findViewById(R.id.sync_box);
      mSyncCommand = (TextView)findViewById(R.id.sync_status);
      mSyncStatusText = (TextView)findViewById(R.id.last_sync_time);
      mCancelButton = (Button)findViewById(R.id.sync_cancel_button);
      mSyncProgressBar = (ProgressBar)findViewById(R.id.sync_progress_bar);
      mListView.setAdapter(mAdapter);
      mListView.setEmptyView(findViewById(R.id.no_offline_guides_text));

      mSyncBox.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if (!mIsSyncing) {
               app.requestSync(false);
            } else {
               // Cancel is handled by the cancel button.
            }
         }
      });

      mCancelButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            app.cancelSync();
         }
      });

      // TODO: Make sure this only runs the first time the Activity is opened.
      // We don't want to log the user out every time the orientation is changed.
      if (getIntent().getBooleanExtra(REAUTHENTICATE, false)) {
         // The sync service indicates that the user is logged out so lets make sure
         // that we think that the user is so login can happen as normal.
         app.shallowLogout(false);
      }

      if (!openLoginDialogIfLoggedOut()) {
         // Initialize the loader if the user is logged in. Otherwise this will
         // happen when the user logs in.
         initLoader();

         // Initiate a sync the first time this Activity is opened if the user is logged
         // in. Otherwise it will happen automatically upon login.
         if (savedState == null && hasInternet) {
            app.requestSync(false);
         }
      }

      refreshSyncStatus(/* force */ true);
      startSyncTimeUpdate();
   }

   @Override
   public void onDestroy() {
      super.onDestroy();

      stopSyncTimeUpdate();
   }

   /**
    * Sets up the repeating event to update the last sync time.
    */
   private void startSyncTimeUpdate() {
      mSyncTimeUpdateHandler = new Handler();
      mSyncTimeUpdateRunnable = new Runnable() {
         @Override
         public void run() {
            updateLastSyncTime();
            mSyncTimeUpdateHandler.postDelayed(mSyncTimeUpdateRunnable,
             SYNC_TIME_REFRESH_INTERVAL);
         }
      };

      mSyncTimeUpdateRunnable.run();
   }

   private void stopSyncTimeUpdate() {
      mSyncTimeUpdateHandler.removeCallbacks(mSyncTimeUpdateRunnable);
   }

   private void updateLastSyncTime() {
      if (mIsSyncing) {
         mSyncStatusText.setVisibility(View.INVISIBLE);
         return;
      }

      mSyncStatusText.setVisibility(View.VISIBLE);

      long lastSyncTime = App.get().getLastSyncTime();
      CharSequence lastSyncTimeString;

      if (lastSyncTime == App.NEVER_SYNCED_VALUE) {
         // TODO: The phrasing of this doesn't make sense.
         lastSyncTimeString = getString(R.string.sync_status_never);
      } else {
         lastSyncTimeString = Utils.getRelativeTime(this, lastSyncTime);
      }

      mSyncStatusText.setText(getString(R.string.last_synced, lastSyncTimeString));

      // TODO: This doesn't work because it's not a button anymore.
      // Disable the sync button if we don't have internet.
      mSyncBox.setEnabled(App.get().isConnected());
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
      // TODO: Remove when above menu is totally removed.
      switch (item.getItemId())  {
         case R.id.offline_guide_sync_now:
            App.get().requestSync(false);
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   protected void updateTotalProgress(int progress, int total) {
      // Don't update progress if we aren't syncing. This caused the progress bar to
      // reappear after cancelling the sync even though the status has changed already.
      if (!mIsSyncing) {
         return;
      }

      mSyncProgressBar.setVisibility(View.VISIBLE);
      mSyncProgressBar.setIndeterminate(false);
      mSyncProgressBar.setMax(total);
      mSyncProgressBar.setProgress(progress);
   }

   protected void updateGuideProgress(int guideid, int progress, int total) {
      for (GuideMediaProgress guide : mGuides) {
         if (guide.mGuide.getGuideid() == guideid) {
            guide.mTotalMedia = total;
            guide.mMediaProgress = progress;
            mAdapter.notifyDataSetChanged();
            return;
         }
      }
   }

   protected void refreshSyncStatus(boolean forceUpdate) {
      final String authority = ApiContentProvider.getAuthority();
      final Account account = App.get().getUserAccount();
      boolean isSyncing = ContentResolver.isSyncActive(account, authority) ||
       ContentResolver.isSyncPending(account, authority);

      // This gets called a lot so we only update the UI if there is a change.
      if (forceUpdate || isSyncing != mIsSyncing) {
         mIsSyncing = isSyncing;

         if (mIsSyncing) {
            // Display as indeterminate. This will be overridden in updateTotalProgress.
            // TODO: We don't store the current progress at all across orientation changes
            // so this changes to indeterminate until an image is downloaded to give us
            // the current progress. This is a minor issue.
            mSyncProgressBar.setIndeterminate(true);
            mSyncProgressBar.setVisibility(View.VISIBLE);
         } else {
            // Hide it if we're not syncing.
            mSyncProgressBar.setVisibility(View.INVISIBLE);
         }

         updateLastSyncTime();
         mSyncCommand.setText(mIsSyncing ? R.string.sync_status_syncing : R.string.sync_now);

         mCancelButton.setVisibility(mIsSyncing ? View.VISIBLE : View.GONE);

         int backgroundColor = mIsSyncing ? R.color.disabled_grey_bg : R.color.holo_blue_dark;
         mSyncBox.setBackgroundColor(getResources().getColor(backgroundColor));
      }
   }

   @Override
   public boolean finishActivityIfLoggedOut() {
      return true;
   }

   @Override
   public Loader<List<GuideMediaProgress>> onCreateLoader(int i, Bundle bundle) {
      App app = App.get();
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
