package com.dozuki.ifixit.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.dozuki.model.Site;
import com.dozuki.ifixit.login.model.User;
import com.dozuki.ifixit.util.APIError.ErrorType;
import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import org.json.JSONException;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
      private APIError mError;

      public Result(String response) {
         mResponse = response;
      }

      public Result(String response, Object result) {
         this(response);
         setResult(result);
      }

      public Result(APIError error) {
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

      public APIError getError() {
         return mError;
      }

      public void setError(APIError error) {
         mError = error;
      }
   }


   private interface Responder {
      public void setResult(Result result);
   }

   private static final String REQUEST_TARGET = "REQUEST_TARGET";
   private static final String REQUEST_QUERY = "REQUEST_QUERY";
   private static final String REQUEST_POST_DATA = "REQUEST_POST_DATA";
   private static final String REQUEST_UPLOAD_FILE_PATH =
    "REQUEST_UPLOAD_FILE_PATH";
   public static final String REQUEST_RESULT_INFORMATION =
    "REQUEST_RESULT_INFORMATION";
   public static final String INVALID_LOGIN_STRING = "Invalid login";

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
      @SuppressWarnings("unchecked")
      final Map<String, String> postData =
       (Map<String, String>)extras.getSerializable(REQUEST_POST_DATA);
      final String filePath = extras.getString(REQUEST_UPLOAD_FILE_PATH);

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

      performRequest(endpoint, requestQuery, postData, filePath,
       new Responder() {
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
      String error = JSONHelper.parseError(response);
      if (error != null) {
         if (error.equals(INVALID_LOGIN_STRING)) {
            return new Result(new APIError(getString(R.string.error_dialog_title),
             error, ErrorType.INVALID_USER));
         } else {
            return new Result(new APIError(getString(R.string.error_dialog_title),
             error, ErrorType.OTHER));
         }
      }

      try {
         Object parsedResult = endpoint.parseResult(response);
         return new Result(response, parsedResult);
      } catch (JSONException e) {
         return new Result(APIError.getParseError(this));
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
    String login, String password) {
      Bundle extras = new Bundle();
      Map<String, String> postData = new HashMap<String, String>();

      postData.put("login", login);
      postData.put("password", password);

      extras.putSerializable(REQUEST_POST_DATA, (Serializable)postData);

      return createIntent(context, APIEndpoint.LOGIN, NO_QUERY, extras);
   }

   /**
    * There are two login intent methods because the user has a sessionid
    * after logging in via OpenID but we still need to hit the login endpoint
    * to verify this and to get a username.
    *
    * TODO: Make this better. This method involves POSTing with a sessionid
    * rather than sending it in a Cookie like all of the other requests.
    */
   public static Intent getLoginIntent(Context context,
    String session) {
      Bundle extras = new Bundle();
      Map<String, String> postData = new HashMap<String, String>();

      postData.put("session", session);

      extras.putSerializable(REQUEST_POST_DATA, (Serializable)postData);

      return createIntent(context, APIEndpoint.LOGIN, NO_QUERY, extras);
   }

   public static Intent getRegisterIntent(Context context, String login,
    String password, String username) {
      Bundle extras = new Bundle();
      Map<String, String> postData = new HashMap<String, String>();

      postData.put("login", login);
      postData.put("password", password);
      postData.put("username", username);

      extras.putSerializable(REQUEST_POST_DATA, (Serializable)postData);

      return createIntent(context, APIEndpoint.REGISTER, NO_QUERY, extras);
   }

   public static Intent getUserImagesIntent(Context context, String query) {
      return createIntent(context, APIEndpoint.USER_IMAGES, query);
   }

   public static Intent getUploadImageIntent(Context context, String filePath,
    String extraInformation) {
      Bundle extras = new Bundle();

      extras.putString(REQUEST_RESULT_INFORMATION, extraInformation);
      extras.putString(REQUEST_UPLOAD_FILE_PATH, filePath);

      return createIntent(context, APIEndpoint.UPLOAD_IMAGE, filePath, extras);
   }

   public static Intent getDeleteImageIntent(Context context, String requestQuery) {
      return createIntent(context, APIEndpoint.DELETE_IMAGE, requestQuery);
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

   public static AlertDialog getErrorDialog(Context context, APIError error,
    Intent apiIntent) {
      return createErrorDialog(context, apiIntent, error);
   }

   public static AlertDialog getListMediaErrorDialog(Context context, APIError error,
    Intent apiIntent) {
       switch (error.mType) {
       case CONNECTION:
          return getErrorDialog(context, error, apiIntent);
       default:
          return getListMediaUnknownErrorDialog(context);
       }
   }

   public static AlertDialog getListMediaUnknownErrorDialog(final Context mContext) {
       AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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

   private static AlertDialog createErrorDialog(final Context context,
    final Intent apiIntent, APIError error) {
      AlertDialog.Builder builder = new AlertDialog.Builder(context);
      builder.setTitle(error.mTitle)
             .setMessage(error.mMessage)
             .setPositiveButton(context.getString(R.string.try_again),
              new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                    // Try performing the request again.
                    context.startService(apiIntent);
                    dialog.dismiss();
                 }
              });

      AlertDialog d = builder.create();
      d.setOnCancelListener(new OnCancelListener() {
         @Override
         public void onCancel(DialogInterface dialog) {
            ((Activity) context).finish();
         }
      });
      return d;
   }

   private void performRequest(final APIEndpoint endpoint,
    final String requestQuery, final Map<String, String> postData,
    final String filePath, final Responder responder) {
      if (!checkConnectivity(responder)) {
         return;
      }

      final String url = endpoint.getUrl(mSite, requestQuery);

      Log.i("iFixit", "Performing API call: " + url);

      new AsyncTask<String, Void, Result>() {
         @Override
         protected Result doInBackground(String... dummy) {
            /**
             * Unfortunately we must split the creation of the HttpRequest
             * object and the appropriate actions to take for a GET vs. a POST
             * request. The request headers and trustAllCerts calls must be
             * made before any data is sent. However, we must have an HttpRequest
             * object already.
             */
            HttpRequest request;

            try {
               /**
                * Create the HttpRequest with the appropriate method.
                */
               if (endpoint.mPost) {
                  request = HttpRequest.post(url);
               } else {
                  request = HttpRequest.get(url);
               }

               /**
                * Uncomment to test HTTPS API calls in development.
                *
                * request.trustAllCerts();
                * request.trustAllHosts();
                */

               /**
                * Send the session along in a Cookie.
                *
                * TODO: Also send it along if the current site is private.
                */
               if (endpoint.mAuthenticated) {
                  User user = ((MainApplication)getApplicationContext()).getUser();
                  String session = user.getSession();
                  request.header("Cookie", "session=" + session);
               }

               /**
                * Continue with constructing the request body.
                */
               if (endpoint.mPost) {
                  if (filePath != null) {
                     // POST the file if present.
                     request.send(new File(filePath));
                  } else if (postData != null) {
                     request.form(postData);
                  }
               } else {
                  // Do nothing extra for GET.
               }

               return new Result(request.body());
            } catch (HttpRequestException e) {
               return new Result(APIError.getParseError(APIService.this));
            }
         }

         @Override
         protected void onPostExecute(Result result) {
            responder.setResult(result);
         }
      }.execute();
   }

   private boolean checkConnectivity(Responder responder) {
      ConnectivityManager cm = (ConnectivityManager)
       getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo netInfo = cm.getActiveNetworkInfo();

      if (netInfo == null || !netInfo.isConnected()) {
         responder.setResult(new Result(APIError.getConnectionError(this)));
         return false;
      }

      return true;
   }
}
