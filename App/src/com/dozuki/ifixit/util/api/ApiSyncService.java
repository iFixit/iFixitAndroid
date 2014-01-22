package com.dozuki.ifixit.util.api;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Service for binding with the sync adapter framework.
 */
public class ApiSyncService extends Service {
   private static ApiSyncAdapter sSyncAdapter = null;
   private static final Object sSyncAdapterLock = new Object();

   @Override
   public void onCreate() {
      synchronized (sSyncAdapterLock) {
         if (sSyncAdapter == null) {
            sSyncAdapter = new ApiSyncAdapter(getApplicationContext(), true);
         }
      }
   }

   @Override
   public IBinder onBind(Intent intent) {
      return sSyncAdapter.getSyncAdapterBinder();
   }
}