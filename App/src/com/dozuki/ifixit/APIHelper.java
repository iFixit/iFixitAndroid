package com.dozuki.ifixit;

import java.io.Serializable;
import java.net.URLEncoder;

import org.acra.ErrorReporter;
import org.apache.http.client.ResponseHandler;
import org.json.JSONException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;

/**
 * Class used to perform asynchronous API requests with a callback.
 *
 * This class can leak Activities if used directly. APIService provides the
 * same functionality but uses broadcasts to avoid this problem.
 */
public class APIHelper {
   public interface Responder {
      public void setResult(Result result);
   }

   public static class Result implements Serializable {
      private static final long serialVersionUID = 1L;

      private String mResponse;
      private Object mResult;
      private Error mError;

      public Result(String response) {
         mResponse = response;
      }

      public Result(String response, Object result) {
         this(response);
         mResult = result;
      }

      public Result(String response, Error error) {
         this(response);
         mError = error;
      }

      public String getResponse() {
         return mResponse;
      }

      public Object getResult() {
         return mResult;
      }

      public Error getError() {
         return mError;
      }
   }

   public static enum Error {PARSE, CONNECTION};

   private interface StringHandler {
      public void handleString(String string) throws JSONException;
   }

   private static final String RESPONSE = "RESPONSE";
   private static final String TOPIC_API_URL =
    "http://www.ifixit.com/api/1.0/topic/";
   private static final String GUIDE_API_URL =
    "http://www.ifixit.com/api/1.0/guide/";
   private static final String CATEGORIES_API_URL =
    "http://www.ifixit.com/api/1.0/categories/";

   public static void getTopic(Context context, String topic,
    final Responder responder) {
      if (!checkConnectivity(context, responder)) {
         return;
      }

      try {
         String url = TOPIC_API_URL + URLEncoder.encode(topic, "UTF-8");
         performRequest(url, new StringHandler() {
            public void handleString(String response) throws JSONException {
               responder.setResult(new Result(response,
                JSONHelper.parseTopicLeaf(response)));
            }
         }, context, responder);
      } catch (Exception e) {
         Log.w("iFixit", "Encoding error: " + e.getMessage());
         responder.setResult(new Result(null, Error.PARSE));
      }
   }

   public static void getGuide(Context context, int guideid,
    final Responder responder) {
      if (!checkConnectivity(context, responder)) {
         return;
      }

      String url = GUIDE_API_URL + guideid;

      performRequest(url, new StringHandler() {
         public void handleString(String response) throws JSONException {
            responder.setResult(new Result(response,
             JSONHelper.parseGuide(response)));
         }
      }, context, responder);
   }

   public static void getCategories(Context context,
    final Responder responder) {
      if (!checkConnectivity(context, responder)) {
         return;
      }

      performRequest(CATEGORIES_API_URL, new StringHandler() {
         public void handleString(String response) throws JSONException {
            responder.setResult(new Result(response,
             JSONHelper.parseTopics(response)));
         }
      }, context, responder);
   }

   public static AlertDialog getErrorDialog(Context context, Error error,
    Intent apiIntent) {
      switch (error) {
      case CONNECTION:
         return getConnectionErrorDialog(context, apiIntent);
      case PARSE:
      default:
         return getParseErrorDialog(context, apiIntent);
      }
   }

   private static AlertDialog getParseErrorDialog(final Context context,
    Intent apiIntent) {
      return createErrorDialog(context, apiIntent, R.string.parse_error_title,
       R.string.parse_error_message, R.string.try_again);
   }

   private static AlertDialog getConnectionErrorDialog(final Context context,
    Intent apiIntent) {
      return createErrorDialog(context, apiIntent, R.string.no_connection_title,
       R.string.no_connection, R.string.try_again);
   }

   private static AlertDialog createErrorDialog(final Context context,
    final Intent apiIntent, int titleRes, int messageRes,
    int buttonRes) {
      HoloAlertDialogBuilder builder = new HoloAlertDialogBuilder(context);
      builder.setTitle(context.getString(titleRes))
             .setMessage(context.getString(messageRes))
             .setPositiveButton(context.getString(buttonRes),
              new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                   // Try performing the request again.
                   context.startService(apiIntent);
                   dialog.cancel();
                }
             });

      return builder.create();
   }

   private static void performRequest(final String url,
    final StringHandler stringHandler, final Context context,
    final Responder responder) {
      final Handler handler = new Handler() {
         public void handleMessage(Message message) {
            String response = message.getData().getString(RESPONSE);

            try {
               stringHandler.handleString(response);
            } catch (JSONException e) {
               // Send detailed error reports.
               ErrorReporter.getInstance().handleSilentException(
                new Exception("Parse error, json: " + response, e));

                responder.setResult(new Result(null, Error.PARSE));
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
    Responder responder) {
      ConnectivityManager cm = (ConnectivityManager)context.
       getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo netInfo = cm.getActiveNetworkInfo();

      if (netInfo == null || !netInfo.isConnected()) {
         responder.setResult(new Result(null, Error.CONNECTION));
         return false;
      }

      return true;
   }
}
