package com.dozuki.ifixit;

import java.net.URLEncoder;
import java.util.ArrayList;

import org.acra.ErrorReporter;
import org.apache.http.client.ResponseHandler;
import org.json.JSONException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;

public class APIHelper {
   public interface APIResponder<T> {
      public void execute();

      public void setResult(T result);

      public void error(AlertDialog dialog);
   }

   private interface StringHandler {
      public void handleString(String string) throws JSONException;
   }

   private static final String RESPONSE = "RESPONSE";
   private static final String SITES_API_URL = "/api/1.0/sites?limit=1000";
   private static final String TOPIC_API_URL = "/api/1.0/topic/";
   private static final String GUIDE_API_URL = "/api/1.0/guide/";
   private static final String CATEGORIES_API_URL = "/api/1.0/categories/";

   private static Site mSite;

   public static void setSite(Site site) {
      mSite = site;
   }

   private static String getUrl(String endPoint) {
      String domain;

      if (mSite != null) {
         domain = mSite.mDomain;
      } else {
         domain = "www.ifixit.com";
      }

      return "http://" + domain + endPoint;
   }

   public static void getSites(Context context,
    final APIResponder<ArrayList<Site>> responder) {
      if (!checkConnectivity(context, responder)) {
         return;
      }

      performRequest(getUrl(SITES_API_URL), new StringHandler() {
         public void handleString(String response) {
            responder.setResult(JSONHelper.parseSites(response));
         }
      }, context, responder);
   }

   public static void getTopic(Context context, String topic,
    final APIResponder<TopicLeaf> responder) {
      if (!checkConnectivity(context, responder)) {
         return;
      }

      try {
         String url = getUrl(TOPIC_API_URL) + URLEncoder.encode(topic, "UTF-8");
         performRequest(url, new StringHandler() {
            public void handleString(String response) throws JSONException {
               responder.setResult(JSONHelper.parseTopicLeaf(response));
            }
         }, context, responder);
      } catch (Exception e) {
         Log.w("iFixit", "Encoding error: " + e.getMessage());
         responder.setResult(null);
      }
   }

   public static void getGuide(Context context, int guideid,
    final APIResponder<Guide> responder) {
      if (!checkConnectivity(context, responder)) {
         return;
      }

      String url = getUrl(GUIDE_API_URL) + guideid;

      performRequest(url, new StringHandler() {
         public void handleString(String response) throws JSONException {
            responder.setResult(JSONHelper.parseGuide(response));
         }
      }, context, responder);
   }

   public static void getCategories(Context context,
    final APIResponder<TopicNode> responder) {
      if (!checkConnectivity(context, responder)) {
         return;
      }

      performRequest(getUrl(CATEGORIES_API_URL), new StringHandler() {
         public void handleString(String response) throws JSONException {
            responder.setResult(JSONHelper.parseTopics(response));
         }
      }, context, responder);
   }

   private static AlertDialog getParseErrorDialog(final Context context,
    final APIResponder<?> responder) {
      return getErrorDialog(context, responder, R.string.parse_error_title,
       R.string.parse_error_message, R.string.try_again);
   }

   private static AlertDialog getConnectionErrorDialog(final Context context,
    final APIResponder<?> responder) {
      return getErrorDialog(context, responder, R.string.no_connection_title,
       R.string.no_connection, R.string.try_again);
   }

   private static AlertDialog getErrorDialog(final Context context,
    final APIResponder<?> responder, int titleRes, int messageRes,
    int buttonRes) {
      HoloAlertDialogBuilder builder = new HoloAlertDialogBuilder(context);
      builder.setTitle(context.getString(titleRes))
             .setMessage(context.getString(messageRes))
             .setPositiveButton(context.getString(buttonRes),
              new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                   // Try performing the request again.
                   responder.execute();
                   dialog.cancel();
                }
             });

      return builder.create();
   }

   private static void performRequest(final String url,
    final StringHandler stringHandler, final Context context,
    final APIResponder<?> responder) {
      final Handler handler = new Handler() {
         public void handleMessage(Message message) {
            String response = message.getData().getString(RESPONSE);

            try {
               stringHandler.handleString(response);
            } catch (JSONException e) {
               // Send detailed error reports.
               ErrorReporter.getInstance().handleSilentException(
                new Exception("Parse error, json: " + response, e));

                responder.error(getParseErrorDialog(context, responder));
            }
         }
      };

      final ResponseHandler<String> responseHandler =
       HTTPRequestHelper.getResponseHandlerInstance(handler);

      new Thread() {
         public void run() {
            HTTPRequestHelper helper = new HTTPRequestHelper(responseHandler);

            try {
               helper.performGet(url);
            } catch (Exception e) {
               Log.w("iFixit", "Encoding error: " + e.getMessage());
            }
         }
      }.start();
   }

   private static boolean checkConnectivity(Context context,
    APIResponder<?> responder) {
      ConnectivityManager cm = (ConnectivityManager)context.
       getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo netInfo = cm.getActiveNetworkInfo();

      if (netInfo == null || !netInfo.isConnected()) {
         responder.error(getConnectionErrorDialog(context, responder));
         return false;
      }

      return true;
   }
}
