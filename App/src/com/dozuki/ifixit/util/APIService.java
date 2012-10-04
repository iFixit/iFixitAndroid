package com.dozuki.ifixit.util;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import org.apache.http.client.ResponseHandler;
import org.json.JSONException;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.dozuki.model.Site;
import com.dozuki.ifixit.view.model.AuthenicationPackage;

/**
 * Service used to perform asynchronous API requests and broadcast results.
 *
 * Future plans: Store the results in a database for later viewing.
 *               Add functionality to download multiple guides including images.
 */
public class APIService extends Service {
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
         setResult(result);
      }

      public Result(Error error) {
         setError(error);
      }

      public boolean hasError() {
         return mError != null;
      }

      public String getResponse() {
         return mResponse;
      }

      public Object getResult() {
         return mResult;
      }

      public void setResult(Object result) {
         mResult = result;
      }

      public Error getError() {
         return mError;
      }

      public void setError(Error error) {
         mError = error;
      }
   }

   public static enum Error {PARSE, CONNECTION};

   private interface Responder {
      public void setResult(Result result);
   }

   private static final String RESPONSE = "RESPONSE";
   private static final String API_DOMAIN = ".ifixit.com";
   private static final String REQUEST_TARGET = "REQUEST_TARGET";
   private static final String REQUEST_QUERY = "REQUEST_QUERY";
	private static final String REQUEST_AUTHENICATION_PACKAGE =
    "AUTHENICATION_PACKAGE";
	public static final String REQUEST_RESULT_INFORMATION =
    "REQUEST_RESULT_INFORMATION";

   private static final String NO_QUERY = "";

   public static final String RESULT = "RESULT";

   private static Site mSite;

   public static void setSite(Site site) {
      mSite = site;
   }

   @Override
   public IBinder onBind(Intent intent) {
      return null; // Do nothing.
   }

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      Bundle extras = intent.getExtras();
      final int requestTarget = extras.getInt(REQUEST_TARGET);
      final APIEndpoint endpoint = APIEndpoint.getByTarget(requestTarget);
      final String requestQuery = extras.getString(REQUEST_QUERY);
      final String resultInformation =
       extras.getString(REQUEST_RESULT_INFORMATION);
      final AuthenicationPackage authenicationPackage =
       (AuthenicationPackage)extras.getSerializable(
       REQUEST_AUTHENICATION_PACKAGE);

      if (authenicationPackage != null) {
         perfromAuthenicatedRequestHelper(this, endpoint,
          authenicationPackage, requestQuery, new Responder() {
             public void setResult(Result result) {

                if (!result.hasError()) {
                   result = parseResult(result.getResponse(), endpoint);
                }

                // Don't save if there a parse error.
                if (!result.hasError()) {
                   saveResult(result, requestTarget, requestQuery);
                }

                // Always broadcast the result despite any errors.
                broadcastResult(result, endpoint, resultInformation);
             }
          });
         return START_NOT_STICKY;
      }

      // Commented out because the DB code isn't ready yet.
      // APIDatabase db = new APIDatabase(this);
      // String fetchedResult = db.fetchResult(requestTarget, requestQuery);
      // db.close();

      // if (fetchedResult != null) {
      //    Result result = parseResult(fetchedResult, requestTarget,
      //     broadcastAction);

      //    if (!result.hasError()) {
      //       broadcastResult(result, broadcastAction);

      //       return START_NOT_STICKY;
      //    }
      // }

      performRequestHelper(this, endpoint, requestQuery, new Responder() {
         public void setResult(Result result) {
            // Don't parse if we've erred already.
            if (!result.hasError()) {
               result = parseResult(result.getResponse(), endpoint);
            }

            // Don't save if there a parse error.
            if (!result.hasError()) {
               saveResult(result, requestTarget, requestQuery);
            }

            // Always broadcast the result despite any errors.
            broadcastResult(result, endpoint, resultInformation);
         }
      });

      return START_NOT_STICKY;
   }

   /**
    * Parse the response in the given result with the given requestTarget.
    */
   private Result parseResult(String response, APIEndpoint endpoint) {
      Object parsedResult = null;
      try {
         parsedResult = endpoint.parseResult(response);

         return new Result(response, parsedResult);
      } catch (JSONException e) {
         return new Result(Error.PARSE);
      }
   }

   private void saveResult(Result result, int requestTarget,
    String requestQuery) {
      // Commented out because the DB code isn't ready yet.
      // APIDatabase db = new APIDatabase(this);
      // db.insertResult(result.getResponse(), requestTarget, requestQuery);
      // db.close();
   }

   private void broadcastResult(Result result, APIEndpoint endpoint,
    String extraResultInfo) {
      Intent broadcast = new Intent();
      Bundle extras = new Bundle();

      extras.putSerializable(RESULT, result);
      extras.putInt(REQUEST_TARGET, endpoint.getTarget());
      extras.putString(REQUEST_RESULT_INFORMATION, extraResultInfo);
      broadcast.putExtras(extras);

      broadcast.setAction(endpoint.mAction);
      sendBroadcast(broadcast);
   }

   public static Intent getCategoriesIntent(Context context) {
      return createIntent(context, APIEndpoint.CATEGORIES, NO_QUERY);
   }

   public static Intent getGuideIntent(Context context, int guideid) {
      return createIntent(context, APIEndpoint.GUIDE, "" + guideid);
   }

   public static Intent getTopicIntent(Context context, String topicName) {
      return createIntent(context, APIEndpoint.TOPIC, topicName);
   }

   public static Intent getLoginIntent(Context context,
    AuthenicationPackage authenicationPackage) {
      Bundle extras = new Bundle();

      extras.putSerializable(REQUEST_AUTHENICATION_PACKAGE,
       authenicationPackage);

      return createIntent(context, APIEndpoint.LOGIN, NO_QUERY, extras);
   }

   public static Intent getRegisterIntent(Context context,
    AuthenicationPackage authenicationPackage) {
      Bundle extras = new Bundle();

      extras.putSerializable(REQUEST_AUTHENICATION_PACKAGE,
       authenicationPackage);

      return createIntent(context, APIEndpoint.REGISTER, NO_QUERY, extras);
   }

   public static Intent getUserImagesIntent(Context context,
    AuthenicationPackage authenicationPackage, String query) {
      Bundle extras = new Bundle();

      extras.putSerializable(REQUEST_AUTHENICATION_PACKAGE,
       authenicationPackage);

      return createIntent(context, APIEndpoint.USER_IMAGES, query, extras);
   }

   public static Intent getUploadImageIntent(Context context,
    AuthenicationPackage authenicationPackage, String filePath,
    String extraInformation) {
      Bundle extras = new Bundle();

      extras.putSerializable(REQUEST_AUTHENICATION_PACKAGE,
       authenicationPackage);
      extras.putString(REQUEST_RESULT_INFORMATION, extraInformation);

      return createIntent(context, APIEndpoint.UPLOAD_IMAGE, filePath, extras);
   }

   public static Intent getDeleteImageIntent(Context context,
    AuthenicationPackage authenicationPackage, String requestQuery) {
      Bundle extras = new Bundle();

      extras.putSerializable(REQUEST_AUTHENICATION_PACKAGE,
       authenicationPackage);

      return createIntent(context, APIEndpoint.DELETE_IMAGE, requestQuery,
       extras);
   }

   public static Intent getSitesIntent(Context context) {
      return createIntent(context, APIEndpoint.SITES, NO_QUERY);
   }

   private static Intent createIntent(Context context, APIEndpoint endpoint,
    String query) {
      return createIntent(context, endpoint, query, new Bundle());
   }

   private static Intent createIntent(Context context, APIEndpoint endpoint,
    String query, Bundle extras) {
      Intent intent = new Intent(context, APIService.class);

      extras.putInt(REQUEST_TARGET, endpoint.getTarget());
      extras.putString(REQUEST_QUERY, query);
      intent.putExtras(extras);

      return intent;
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

   public static AlertDialog getListMediaErrorDialog(final Context mContext) {
      HoloAlertDialogBuilder builder = new HoloAlertDialogBuilder(mContext);
      builder.setTitle(mContext.getString(R.string.media_error_title))
         .setPositiveButton(mContext.getString(R.string.media_error_confirm),
         new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               //kill the media activity, and have them try again later
               //incase the server needs some rest
               ((SherlockFragmentActivity)mContext).finish();
               dialog.cancel();
            }
         });

      return builder.create();
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

   private static void performRequestHelper(Context context,
    APIEndpoint endpoint, String requestQuery, Responder responder) {
      if (!checkConnectivity(context, responder)) {
         return;
      }

      performRequest(endpoint.getUrl(mSite, requestQuery), responder);
   }

   private static void perfromAuthenicatedRequestHelper(Context context,
      APIEndpoint endpoint, AuthenicationPackage authenicationPackage,
      String requestQuery, Responder responder) {
      if (!checkConnectivity(context, responder)) {
         return;
      }

      int requestTarget = endpoint.getTarget();

      /**
       * TODO: Remove these if statements and move logic into APIEndpoint.
       */
      String url;
      File file = null;
      if (requestTarget == APIEndpoint.LOGIN.getTarget()) {
         url = endpoint.getUrl(mSite);
      } else if (requestTarget == APIEndpoint.REGISTER.getTarget()) {
         url = endpoint.getUrl(mSite);
      } else if (requestTarget == APIEndpoint.USER_IMAGES.getTarget()) {
         url = endpoint.getUrl(mSite, requestQuery);
         authenicationPackage.login = null;
         authenicationPackage.password = null;
         authenicationPackage.username = null;
      } else if (requestTarget == APIEndpoint.UPLOAD_IMAGE.getTarget()) {
         file = new File(requestQuery);
         url = endpoint.getUrl(mSite, file.getName());
         authenicationPackage.login = null;
         authenicationPackage.password = null;
         authenicationPackage.username = null;
      } else if (requestTarget == APIEndpoint.DELETE_IMAGE.getTarget()) {
         url = endpoint.getUrl(mSite, requestQuery);
         authenicationPackage.login = null;
         authenicationPackage.password = null;
         authenicationPackage.username = null;
      } else {
         Log.w("iFixit", "Invalid request target: " + requestTarget);
         responder.setResult(new Result(Error.PARSE));
         return;
      }
      performAuthenicatedRequest(url, authenicationPackage, file, responder);
   }

   private static void  performAuthenicatedRequest(final String url,
    final AuthenicationPackage authenicationPackage, final File file,
    final Responder responder) {
      final Handler handler = new Handler() {
         public void handleMessage(Message message) {
            String response = message.getData().getString(RESPONSE);

            responder.setResult(new Result(response));
         }
      };

      final ResponseHandler<String> responseHandler =
       HTTPRequestHelper.getResponseHandlerInstance(handler);

      new Thread() {
         public void run() {
            HTTPRequestHelper helper = new HTTPRequestHelper(responseHandler);
            HashMap<String, String> params = new HashMap<String, String>();
            HashMap<String, String> header = new HashMap<String, String>();

            params.put("login", authenicationPackage.login);
            params.put("password", authenicationPackage.password);
            params.put("username", authenicationPackage.username);

            if (file != null) {
               params.put("file", file.getName());
            }

            try {
               helper.performPostWithSessionCookie(url, null, null,
                authenicationPackage.session, API_DOMAIN, header, params, file);
            } catch (Exception e) {
               Log.w("iFixit", "Encoding error: " + e.getMessage());
            }
         }
      }.start();
   }

   private static void performRequest(final String url,
    final Responder responder) {
      final Handler handler = new Handler() {
         public void handleMessage(Message message) {
            String response = message.getData().getString(RESPONSE);

            responder.setResult(new Result(response));
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
         responder.setResult(new Result(Error.CONNECTION));
         return false;
      }

      return true;
   }
}
