package com.dozuki.ifixit.ui.guide.view;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncStatusObserver;
import android.os.Build;
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
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiContentProvider;
import com.dozuki.ifixit.util.api.ApiDatabase;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.dozuki.ifixit.util.api.ApiSyncAdapter;
import com.dozuki.ifixit.util.api.GuideMediaProgress;
import com.squareup.otto.Subscribe;

import java.util.Collections;
import java.util.Iterator;
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

   private final int SYNC_TIME_REFRESH_INTERVAL = 30000;
   private Handler mSyncStatusUpdateHandler;
   private Runnable mSyncStatusUpdateRunnable;

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

      App.sendScreenView("user/offline_guides");

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
            // Don't request a sync if there isn't any internet.
            if (!mIsSyncing && App.get().isConnected()) {
               App.sendEvent("ui_action", "button_press", "offline_guides_sync_now", null);
               app.requestSync(false);
            } else {
               // Cancel is handled by the cancel button.
            }
         }
      });

      mCancelButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            App.sendEvent("ui_action", "button_press", "offline_guides_cancel_sync", null);
            app.cancelSync();
         }
      });

      // Only check if this is the first onCreate. Otherwise the user will be logged out on
      // every orientation change.
      if (savedState == null && getIntent().getBooleanExtra(REAUTHENTICATE, false)) {
         App.sendEvent("ui_action", "button_press", "offline_guides_reauthenticate", null);
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
         if (savedState == null && hasInternet && app.getSyncAutomatically()) {
            app.requestSync(false);
         }
      }

      refreshSyncStatus(/* force */ true);
      startSyncStatusUpdate();
   }

   @Override
   public void onDestroy() {
      super.onDestroy();

      stopSyncStatusUpdate();
   }

   @Override
   public void onRestart() {
      super.onRestart();

      Loader loader = getSupportLoaderManager().getLoader(R.id.offline_guide_loaderid);

      if (loader != null) {
         loader.onContentChanged();
      }
   }

   /**
    * Sets up the repeating event to update the last sync time.
    */
   private void startSyncStatusUpdate() {
      mSyncStatusUpdateHandler = new Handler();
      mSyncStatusUpdateRunnable = new Runnable() {
         @Override
         public void run() {
            updateSyncStatus();
            mSyncStatusUpdateHandler.postDelayed(mSyncStatusUpdateRunnable,
             SYNC_TIME_REFRESH_INTERVAL);
         }
      };

      mSyncStatusUpdateRunnable.run();
   }

   private void stopSyncStatusUpdate() {
      mSyncStatusUpdateHandler.removeCallbacks(mSyncStatusUpdateRunnable);
   }

   private void updateSyncStatus() {
      long lastSyncStatus = App.get().getLastSyncTime();
      boolean isConnected = App.get().isConnected();

      if (mIsSyncing || lastSyncStatus == App.NEVER_SYNCED_VALUE) {
         mSyncStatusText.setVisibility(View.INVISIBLE);
      } else {
         mSyncStatusText.setVisibility(View.VISIBLE);
         mSyncStatusText.setText(getString(R.string.last_synced,
          Utils.getRelativeTime(this, lastSyncStatus)));
      }

      mSyncCommand.setText(isConnected ?
       (mIsSyncing ? R.string.sync_status_syncing : R.string.sync_now) :
       R.string.no_connection);

      RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mSyncCommand.getLayoutParams());
      lp.addRule(RelativeLayout.CENTER_HORIZONTAL);

      if (mIsSyncing || !isConnected) {
         lp.addRule(RelativeLayout.CENTER_VERTICAL);
      } else {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            lp.removeRule(RelativeLayout.CENTER_VERTICAL);
         } else {
            lp.addRule(RelativeLayout.CENTER_VERTICAL, 0);
         }
      }

      mSyncCommand.setLayoutParams(lp);

      int backgroundColor = mIsSyncing || !isConnected ? R.color.disabled_grey_bg : R.color.emphasis;
      mSyncBox.setBackgroundColor(getResources().getColor(backgroundColor));
   }

   @Override
   public void onLogin(LoginEvent.Login loginEvent) {
      super.onLogin(loginEvent);

      initLoader();
      App.get().requestSync(false);
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
      getSupportMenuInflater().inflate(R.menu.offline_guides, menu);
      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      MenuItem syncAutomatically = menu.findItem(R.id.sync_automatically);
      syncAutomatically.setChecked(App.get().getSyncAutomatically());

      return super.onPrepareOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.sync_automatically:
            // The checked state is updated after this is called so we must negate it.
            boolean enableSync = !item.isChecked();
            App.sendEvent("ui_action", "button_press",
             "offline_guides_auto_sync_" + (enableSync ? "on" : "off"), null);
            App.get().setSyncAutomatically(enableSync);
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
         if (guide.mGuideInfo.mGuideid == guideid) {
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
            mSyncProgressBar.setIndeterminate(true);
            mSyncProgressBar.setVisibility(View.VISIBLE);
         } else {
            // Hide it if we're not syncing.
            mSyncProgressBar.setVisibility(View.INVISIBLE);
         }

         updateSyncStatus();

         mCancelButton.setVisibility(mIsSyncing ? View.VISIBLE : View.GONE);
      }
   }

   @Subscribe
   public void onGuideFavorited(ApiEvent.FavoriteGuide event) {
      if (!event.hasError()) {
         App.sendEvent("ui_action", "button_press", "offline_guides_unfavorite", null);

         // Note: We assume that this is an unfavorite because that's the only
         // thing you can do in this view currently.
         int guideid = Integer.parseInt(event.mApiCall.getQuery());

         for (Iterator<GuideMediaProgress> itr = mGuides.iterator(); itr.hasNext();) {
            GuideMediaProgress guide = itr.next();

            if (guide.mGuideInfo.mGuideid == guideid) {
               // Remove the guide from the list and request a sync to actually remove it.
               itr.remove();
               App.get().requestSync(/* force */ true);
               mAdapter.notifyDataSetChanged();
               return;
            }
         }
      } else {
         Api.getErrorDialog(this, event).show();
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
   }
}
