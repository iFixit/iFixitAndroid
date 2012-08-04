package com.dozuki.ifixit;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Service used to perform asynchronous API requests and broadcast results.
 *
 * Uses APIHelper to download the data and on receive sends out a broadcast for any
 * interested Activities/Fragments to update their UI.
 *
 * Future plans: Store the results in a database for later viewing.
 *               Add functionality to download multiple guides including images.
 */
public class APIService extends Service {
   private static final String REQUEST_TARGET = "REQUEST_TARGET";
   private static final String REQUEST_QUERY = "REQUEST_QUERY";
   private static final String REQUEST_BROADCAST_ACTION =
    "REQUEST_BROADCAST_ACTION";

   private static final int TARGET_CATEGORIES = 0;
   private static final int TARGET_GUIDE = 1;
   private static final int TARGET_TOPIC = 2;

   private static final String NO_QUERY = "";

   public static final String ACTION_CATEGORIES =
    "com.dozuki.ifixit.api.categories";
   public static final String ACTION_GUIDE =
    "com.dozuki.ifixit.api.guide";
   public static final String ACTION_TOPIC =
    "com.dozuki.ifixit.api.topic";

   public static final String RESULT = "RESULT";

   @Override
   public IBinder onBind(Intent intent) {
      return null; // Do nothing.
   }

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      Bundle extras = intent.getExtras();
      final int requestTarget = extras.getInt(REQUEST_TARGET);
      final String requestQuery = extras.getString(REQUEST_QUERY);
      final String broadcastAction = extras.getString(REQUEST_BROADCAST_ACTION);
      APIHelper.Responder responder = new APIHelper.Responder() {
         public void setResult(APIHelper.Result result) {
            Intent broadcast = new Intent();
            Bundle extras = new Bundle();

            extras.putSerializable(RESULT, result);
            broadcast.putExtras(extras);

            broadcast.setAction(broadcastAction);
            sendBroadcast(broadcast);
         }
      };

      switch (requestTarget) {
      case TARGET_CATEGORIES:
         APIHelper.getCategories(APIService.this, responder);
         break;
      case TARGET_GUIDE:
         APIHelper.getGuide(APIService.this,
          Integer.parseInt(requestQuery), responder);
         break;
      case TARGET_TOPIC:
         APIHelper.getTopic(APIService.this, requestQuery, responder);
         break;
      default:
         Log.w("iFixit", "Invalid request target: " + requestTarget);
      }

      return START_NOT_STICKY;
   }

   public static Intent getCategoriesIntent(Context context) {
      return createIntent(context, TARGET_CATEGORIES, NO_QUERY,
       ACTION_CATEGORIES);
   }

   public static Intent getGuideIntent(Context context, int guideid) {
      return createIntent(context, TARGET_GUIDE, "" + guideid, ACTION_GUIDE);
   }

   public static Intent getTopicIntent(Context context, String topicName) {
      return createIntent(context, TARGET_TOPIC, topicName, ACTION_TOPIC);
   }

   private static Intent createIntent(Context context, int target,
    String query, String action) {
      Intent intent = new Intent(context, APIService.class);
      Bundle extras = new Bundle();
      extras.putInt(REQUEST_TARGET, target);
      extras.putString(REQUEST_QUERY, query);
      extras.putString(REQUEST_BROADCAST_ACTION, action);
      intent.putExtras(extras);

      return intent;
   }
}
