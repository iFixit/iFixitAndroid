package com.dozuki.ifixit.util;

import java.io.File;
import java.io.Serializable;
import java.net.URLEncoder;
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

   private static final String SITES_API_URL = "/api/1.0/sites?limit=1000";
   private static final String TOPIC_API_URL = "/api/1.0/topic/";
   private static final String GUIDE_API_URL = "/api/1.0/guide/";

   /**
    * TODO Use https and 1.0.
    */
	private static final String LOGIN_API_URL = "/api/0.1/login";
	private static final String REGISTER_API_URL = "/api/0.1/register";

	private static final String USER_IMAGES_API_URL = "/api/1.0/image/user";
	private static final String UPLOAD_MEDIA_API_URL = "api/1.0/image/upload";
	private static final String DELETE_MEDIA_API_URL = "/api/1.0/image/delete";

	private static final String API_DOMAIN = ".ifixit.com";

   private static final String REQUEST_TARGET = "REQUEST_TARGET";
   private static final String REQUEST_QUERY = "REQUEST_QUERY";
   private static final String REQUEST_BROADCAST_ACTION =
    "REQUEST_BROADCAST_ACTION";
	private static final String REQUEST_AUTHENICATION_PACKAGE =
    "AUTHENICATION_PACKAGE";
	public static final String REQUEST_RESULT_INFORMATION =
    "REQUEST_RESULT_INFORMATION";

   private static final int TARGET_GUIDE = 1;
   private static final int TARGET_TOPIC = 2;
	private static final int TARGET_LOGIN = 3;
	private static final int TARGET_REGISTER = 4;
	private static final int TARGET_MEDIA_LIST = 5;
	private static final int TARGET_UPLOAD_MEDIA = 6;
	private static final int TARGET_DELETE_MEDIA = 7;
   private static final int TARGET_SITES = 8;

   private static final String NO_QUERY = "";

   public static final String ACTION_GUIDE = "com.dozuki.ifixit.api.guide";
   public static final String ACTION_TOPIC = "com.dozuki.ifixit.api.topic";
	public static final String ACTION_LOGIN = "com.dozuki.ifixit.api.login";
	public static final String ACTION_REGISTER =
    "com.dozuki.ifixit.api.register";
	public static final String ACTION_USER_MEDIA =
    "com.dozuki.ifixit.api.images";
	public static final String ACTION_UPLOAD_MEDIA =
    "com.dozuki.ifixit.api.upload";
	public static final String ACTION_DELETE_MEDIA =
    "com.dozuki.ifixit.api.delete";
   public static final String ACTION_SITES = "com.dozuki.ifixit.api.sites";

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
      final String broadcastAction = extras.getString(REQUEST_BROADCAST_ACTION);
      final String resultInformation =
       extras.getString(REQUEST_RESULT_INFORMATION);
      final AuthenicationPackage authenicationPackage =
       (AuthenicationPackage)extras.getSerializable(
       REQUEST_AUTHENICATION_PACKAGE);

      if (authenicationPackage != null) {
         perfromAuthenicatedRequestHelper(this, requestTarget,
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
                broadcastResult(result, broadcastAction,
                 requestTarget, resultInformation);
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

      performRequestHelper(this, requestTarget, requestQuery, new Responder() {
         public void setResult(Result result) {
            // Don't parse if we've errored already.
            if (!result.hasError()) {
               result = parseResult(result.getResponse(), endpoint);
            }

            // Don't save if there a parse error.
            if (!result.hasError()) {
               saveResult(result, requestTarget, requestQuery);
            }

            // Always broadcast the result despite any errors.
            broadcastResult(result, broadcastAction,
             requestTarget, resultInformation);
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

   private void broadcastResult(Result result, String broadcastAction,
    int initialAction, String extraResultInfo) {
      Intent broadcast = new Intent();
      Bundle extras = new Bundle();

      extras.putSerializable(RESULT, result);
      extras.putInt(REQUEST_TARGET, initialAction);
      extras.putString(REQUEST_RESULT_INFORMATION, extraResultInfo);
      broadcast.putExtras(extras);

      broadcast.setAction(broadcastAction);
      sendBroadcast(broadcast);
   }

   public static Intent getCategoriesIntent(Context context) {
      return createIntent(context, APIEndpoint.SITES, NO_QUERY);
   }

   public static Intent getGuideIntent(Context context, int guideid) {
      return createIntent(context, APIEndpoint.GUIDE, "" + guideid);
   }

   public static Intent getTopicIntent(Context context, String topicName) {
      return createIntent(context, APIEndpoint.TOPIC, topicName);
   }

   public static Intent getLoginIntent(Context context,
    AuthenicationPackage authenicationPackage) {
      return createLoginIntent(context, TARGET_LOGIN, authenicationPackage,
       ACTION_LOGIN);
   }

   public static Intent getUploadImageIntent(Context context,
    AuthenicationPackage authenicationPackage, String filePath,
    String extraInformation) {
      return createUploadImageIntent(context, TARGET_UPLOAD_MEDIA,
       authenicationPackage, ACTION_UPLOAD_MEDIA, filePath, extraInformation);
   }

   public static Intent getDeleteMediaIntent(Context context,
    AuthenicationPackage authenicationPackage, String requestQuery) {
      return createDeleteMediaIntent(context, TARGET_DELETE_MEDIA,
       authenicationPackage, requestQuery, ACTION_DELETE_MEDIA);
   }

   public static Intent userMediaIntent(Context context,
    AuthenicationPackage authenicationPackage, String query) {
      return createUserMediaIntent(context, TARGET_MEDIA_LIST,
       authenicationPackage, query,ACTION_USER_MEDIA);
   }

   public static Intent getRegisterIntent(Context mContext,
    AuthenicationPackage authenicationPackage) {
      return createRegisterIntent(mContext, TARGET_REGISTER,
       authenicationPackage , ACTION_REGISTER);
   }

   public static Intent getSitesIntent(Context context) {
      return createIntent(context, APIEndpoint.SITES, NO_QUERY);
   }

   private static Intent createIntent(Context context, APIEndpoint endpoint,
    String query) {
      Intent intent = new Intent(context, APIService.class);
      Bundle extras = new Bundle();

      extras.putInt(REQUEST_TARGET, endpoint.mTarget);
      extras.putString(REQUEST_BROADCAST_ACTION, endpoint.mAction);
      extras.putString(REQUEST_QUERY, query);
      intent.putExtras(extras);

      return intent;
   }

   private static Intent createLoginIntent(Context context, int target,
    AuthenicationPackage authenicationPackage, String action) {
      Intent intent = new Intent(context, APIService.class);
      Bundle extras = new Bundle();

      extras.putInt(REQUEST_TARGET, target);
      extras.putSerializable(REQUEST_AUTHENICATION_PACKAGE,
       authenicationPackage);
      extras.putString(REQUEST_BROADCAST_ACTION, action);
      intent.putExtras(extras);

      return intent;
   }

   private static Intent createUserMediaIntent(Context context, int target,
    AuthenicationPackage authenicationPackage, String query, String action) {
      Intent intent = new Intent(context, APIService.class);
      Bundle extras = new Bundle();
      extras.putInt(REQUEST_TARGET, target);
      extras.putString(REQUEST_QUERY, query);
      extras.putSerializable(REQUEST_AUTHENICATION_PACKAGE,
       authenicationPackage);
      extras.putString(REQUEST_BROADCAST_ACTION, action);

      intent.putExtras(extras);

      return intent;
   }

   private static Intent createRegisterIntent(Context context, int target,
    AuthenicationPackage authenicationPackage, String action) {
      Intent intent = new Intent(context, APIService.class);
      Bundle extras = new Bundle();

      extras.putInt(REQUEST_TARGET, target);
      extras.putSerializable(REQUEST_AUTHENICATION_PACKAGE,
       authenicationPackage);
      extras.putString(REQUEST_BROADCAST_ACTION, action);
      intent.putExtras(extras);

      return intent;
   }

   private static Intent createUploadImageIntent(Context context, int target,
    AuthenicationPackage authenicationPackage, String action, String filePath,
    String extraInformation) {
      Intent intent = new Intent(context, APIService.class);
      Bundle extras = new Bundle();

      extras.putInt(REQUEST_TARGET, target);
      extras.putString(REQUEST_QUERY, filePath);
      extras.putSerializable(REQUEST_AUTHENICATION_PACKAGE,
       authenicationPackage);
      extras.putString(REQUEST_BROADCAST_ACTION, action);
      extras.putString(REQUEST_RESULT_INFORMATION, extraInformation);
      intent.putExtras(extras);

      return intent;
   }

   private static Intent createDeleteMediaIntent(Context context, int target,
    AuthenicationPackage authenicationPackage, String query, String action) {
      Intent intent = new Intent(context, APIService.class);
      Bundle extras = new Bundle();

      extras.putInt(REQUEST_TARGET, target);
      extras.putString(REQUEST_QUERY, query);
      extras.putSerializable(REQUEST_AUTHENICATION_PACKAGE,
       authenicationPackage);
      extras.putString(REQUEST_BROADCAST_ACTION, action);
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

   public static AlertDialog getListMediaErrorDialog(Context mContext) {
      return createMediaErrorDialog(mContext);
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

   private static AlertDialog createMediaErrorDialog(final Context mContext) {
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

   private static void performRequestHelper(Context context, int requestTarget,
    String requestQuery, Responder responder) {
      if (!checkConnectivity(context, responder)) {
         return;
      }

      String relativeUrl = null;

      if (requestTarget == APIEndpoint.CATEGORIES.mTarget) {
         relativeUrl = APIEndpoint.CATEGORIES.getUrl(mSite);
      } else if (requestTarget == APIEndpoint.GUIDE.mTarget) {
         relativeUrl = APIEndpoint.GUIDE.getUrl(mSite, requestQuery);
      } else if (requestTarget == APIEndpoint.TOPIC.mTarget) {
         try {
            relativeUrl = TOPIC_API_URL + URLEncoder.encode(requestQuery,
             "UTF-8");
         } catch (Exception e) {
            Log.w("iFixit", "Encoding error: " + e.getMessage());
            responder.setResult(new Result(Error.PARSE));
            return;
         }
      } else if (requestTarget == APIEndpoint.SITES.mTarget) {
         relativeUrl = SITES_API_URL;
      } else {
         Log.w("iFixit", "Invalid request target: " + requestTarget);
         responder.setResult(new Result(Error.PARSE));
         return;
      }

      String absoluteUrl = getUrl(relativeUrl);

      performRequest(absoluteUrl, responder);
   }

   /**
    * TODO Remove since its functionality is duplicated in APIEndpoint.
    */
   private static String getUrl(String endPoint) {
      String domain;

      if (mSite != null) {
         domain = mSite.mDomain;
      } else {
         domain = "www.ifixit.com";
      }

      return "http://" + domain + endPoint;
   }

   private static void perfromAuthenicatedRequestHelper(Context context,
      int requestTarget, AuthenicationPackage authenicationPackage,
      String requestQuery, Responder responder) {
      if (!checkConnectivity(context, responder)) {
         return;
      }

      String url;
      File file = null;
      switch (requestTarget) {
         case TARGET_LOGIN:
            url = LOGIN_API_URL;
            break;
         case TARGET_REGISTER:
            url = REGISTER_API_URL;
            Log.e("REGUSTER", url);
            break;
         case TARGET_MEDIA_LIST:
            url = USER_IMAGES_API_URL + requestQuery;
            authenicationPackage.login = null;
            authenicationPackage.password = null;
            authenicationPackage.username = null;
            break;
         case TARGET_UPLOAD_MEDIA:
            file = new File(requestQuery);
            url = UPLOAD_MEDIA_API_URL + "?file=" + file.getName();
            authenicationPackage.login = null;
            authenicationPackage.password = null;
            authenicationPackage.username = null;
            break;

         case TARGET_DELETE_MEDIA:
            url = DELETE_MEDIA_API_URL + requestQuery;
            authenicationPackage.login = null;
            authenicationPackage.password = null;
            authenicationPackage.username = null;
            break;
         default:
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
