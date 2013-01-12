package com.dozuki.ifixit.util;

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
import com.dozuki.ifixit.login.ui.LoginFragment;
import com.dozuki.ifixit.util.APIError.ErrorType;
import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

/**
 * Service used to perform asynchronous API requests and broadcast results.
 *
 * Future plans: Store the results in a database for later viewing.
 *               Add functionality to download multiple guides including images.
 */
public class APIService extends Service {
   private interface Responder {
      public void setResult(APIEvent<?> result);
   }

   private static final String API_CALL = "API_CALL";

   public static final String INVALID_LOGIN_STRING = "Invalid login";

   private static final String NO_QUERY = "";

   public static final String RESULT = "RESULT";

   /**
    * Pending API call. This is set when an authenticated request is performed
    * but the user is not logged in. This is then performed once the user has
    * authenticated.
    */
   private static APICall sPendingApiCall;

   /**
    * Current site.
    */
   private static Site mSite;

   public static void setSite(Site site) {
      mSite = site;
   }

   @Override
   public IBinder onBind(Intent intent) {
      return null; // Do nothing.
   }

   /**
    * Returns true if the the user needs to be authenticated for the given site and endpoint.
    */
   private static boolean requireAuthentication(Site site, APIEndpoint endpoint) {
      return (endpoint.mAuthenticated || !mSite.mPublic) && !endpoint.mForcePublic;
   }

   /**
    * Performs the API call defined by the given Intent. This takes care of opening a
    * login dialog and saving the Intent if the user isn't authenticated but should be.
    */
   public static void call(Activity activity, APICall apiCall) {
      APIEndpoint endpoint = apiCall.mEndpoint;

      // User needs to be logged in for an authenticated endpoint with the exception of login.
      if (requireAuthentication(mSite, endpoint) && !MainApplication.get().isUserLoggedIn()) {
         sPendingApiCall = apiCall;

         // Don't display the login dialog twice.
         if (!MainApplication.get().isLoggingIn()) {
            LoginFragment.newInstance().show(activity.getSupportFragmentManager());
         }
      } else {
         activity.startService(makeApiIntent(activity, apiCall));
      }
   }

   /**
    * Returns the pending API call and sets it to null. Returns null if no pending API call.
    */
   public static Intent getAndRemovePendingApiCall(Context context) {
      APICall pendingApiCall = sPendingApiCall;
      sPendingApiCall = null;

      if (pendingApiCall != null) {
         return makeApiIntent(context, pendingApiCall);
      } else {
         return null;
      }
   }

   /**
    * Constructs an Intent that can be used to start the APIService and perform
    * the given APIcall.
    */
   private static Intent makeApiIntent(Context context, APICall apiCall) {
      Intent intent = new Intent(context, APIService.class);
      Bundle extras = new Bundle();

      extras.putSerializable(API_CALL, apiCall);
      intent.putExtras(extras);

      return intent;
   }

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      Bundle extras = intent.getExtras();
      final APICall apiCall = (APICall)extras.getSerializable(API_CALL);

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

      performRequest(apiCall, new Responder() {
         public void setResult(APIEvent<?> result) {
            // Don't parse if we've erred already.
            if (!result.hasError()) {
               result = parseResult(result.getResponse(), apiCall.mEndpoint);
            }

            // Don't save if there a parse error.
            if (!result.hasError()) {
               saveResult(result, apiCall.mEndpoint.getTarget(), apiCall.mQuery);
            }

            result.setExtraInfo(apiCall.mExtraInfo);

            /**
             * Always post the result despite any errors. This actually sends it off
             * to Activities etc. that care about API calls.
             */
            MainApplication.getBus().post(result);
         }
      });

      return START_NOT_STICKY;
   }

   /**
    * Parse the response in the given result with the given requestTarget.
    */
   private APIEvent<?> parseResult(String response, APIEndpoint endpoint) {
      String error = JSONHelper.parseError(response);
      if (error != null) {
         if (error.equals(INVALID_LOGIN_STRING)) {
            return endpoint.getEvent().setError(new APIError(getString(
             R.string.error_dialog_title), error, ErrorType.INVALID_USER));
         } else {
            return endpoint.getEvent().setError(new APIError(getString(
             R.string.error_dialog_title), error, ErrorType.OTHER));
         }
      }

      try {
         return endpoint.parseResult(response);
      } catch (JSONException e) {
         return endpoint.getEvent().setError(APIError.getParseError(this));
      }
   }

   private void saveResult(APIEvent<?> result, int requestTarget,
    String requestQuery) {
      // Commented out because the DB code isn't ready yet.
      // APIDatabase db = new APIDatabase(this);
      // db.insertResult(result.getResponse(), requestTarget, requestQuery);
      // db.close();
   }

   public static APICall getCategoriesAPICall() {
      return new APICall(APIEndpoint.CATEGORIES, NO_QUERY);
   }

   public static APICall getGuideAPICall(int guideid) {
      return new APICall(APIEndpoint.GUIDE, "" + guideid);
   }

   public static APICall getTopicAPICall(String topicName) {
      return new APICall(APIEndpoint.TOPIC, topicName);
   }

   public static APICall getLoginAPICall(String login, String password) {
      JSONObject requestBody = new JSONObject();

      try {
         requestBody.put("login", login);
         requestBody.put("password", password);
      } catch (JSONException e) {
         return null;
      }

      return new APICall(APIEndpoint.LOGIN, NO_QUERY, requestBody.toString());
   }

   public static APICall getRegisterAPICall(String login, String password, String username) {
      JSONObject requestBody = new JSONObject();

      try {
         requestBody.put("login", login);
         requestBody.put("password", password);
         requestBody.put("username", username);
      } catch (JSONException e) {
         return null;
      }

      return new APICall(APIEndpoint.REGISTER, NO_QUERY, requestBody.toString());
   }

   public static APICall getUserImagesAPICall(String query) {
      return new APICall(APIEndpoint.USER_IMAGES, query);
   }

   public static APICall getUploadImageAPICall(String filePath, String extraInformation) {
      return new APICall(APIEndpoint.UPLOAD_IMAGE, filePath, null, extraInformation,
       filePath);
   }

   public static APICall getDeleteImageAPICall(List<Integer> deleteList) {
      StringBuilder stringBuilder = new StringBuilder();
      String separator = "";

      stringBuilder.append("?imageids=");

      /**
       * Construct a string of imageids separated by comma's.
       */
      for (Integer imageid : deleteList) {
         stringBuilder.append(separator).append(imageid);
         separator = ",";
      }

      return new APICall(APIEndpoint.DELETE_IMAGE, stringBuilder.toString());
   }

   public static APICall getSitesAPICall() {
      return new APICall(APIEndpoint.SITES, NO_QUERY);
   }

   public static APICall getUserInfoAPICall(String session) {
      APICall apiCall = new APICall(APIEndpoint.USER_INFO, NO_QUERY);

      apiCall.mSessionid = session;

      return apiCall;
   }

   public static AlertDialog getErrorDialog(Context context, APIError error,
    APICall apiCall) {
      return createErrorDialog(context, apiCall, error);
   }

   public static AlertDialog getListMediaErrorDialog(Context context, APIError error,
    APICall apiCall) {
       switch (error.mType) {
       case CONNECTION:
          return getErrorDialog(context, error, apiCall);
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
    final APICall apiCall, APIError error) {
      AlertDialog.Builder builder = new AlertDialog.Builder(context);
      builder.setTitle(error.mTitle)
             .setMessage(error.mMessage)
             .setPositiveButton(context.getString(R.string.try_again),
              new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                    // Try performing the request again.
                    context.startService(makeApiIntent(context, apiCall));
                    dialog.dismiss();
                 }
              });

      AlertDialog d = builder.create();
      d.setOnCancelListener(new OnCancelListener() {
         @Override
         public void onCancel(DialogInterface dialog) {
            ((Activity)context).finish();
         }
      });
      return d;
   }

   private void performRequest(final APICall apiCall, final Responder responder) {
      final APIEndpoint endpoint = apiCall.mEndpoint;
      if (!checkConnectivity(responder, endpoint)) {
         return;
      }

      final String url = endpoint.getUrl(mSite, apiCall.mQuery);

      Log.i("iFixit", "Performing API call: " + url);
      Log.i("iFixit", "Request body: " + apiCall.mRequestBody);

      new AsyncTask<String, Void, APIEvent<?>>() {
         @Override
         protected APIEvent<?> doInBackground(String... dummy) {
            /**
             * Unfortunately we must split the creation of the HttpRequest
             * object and the appropriate actions to take for a GET vs. a POST
             * request. The request headers and trustAllCerts calls must be
             * made before any data is sent. However, we must have an HttpRequest
             * object already.
             */
            HttpRequest request;

            try {
               request = new HttpRequest(url, endpoint.mMethod);

               /**
                * Uncomment to test HTTPS API calls in development.
                */
               //request.trustAllCerts();
               //request.trustAllHosts();

               String sessionid = null;
               /**
                * Get an appropriate sessionid.
                */
               if (apiCall.mSessionid != null) {
                  // This sessionid overrides all other requirements/sessionids.
                  sessionid = apiCall.mSessionid;
               } else if (requireAuthentication(mSite, endpoint)) {
                  User user = ((MainApplication)getApplicationContext()).getUser();
                  sessionid = user.getSession();
               }

               /**
                * Send along the sessionid if we found one.
                */
               if (sessionid != null) {
                  request.header("Cookie", "session=" + sessionid);
               }

               /**
                * Continue with constructing the request body.
                */
               if (apiCall.mFilePath != null) {
                  // POST the file if present.
                  request.send(new File(apiCall.mFilePath));
               } else if (apiCall.mRequestBody != null) {
                  request.send(apiCall.mRequestBody);
               }

               /**
                * The order is important here. If the code() is called first an IOException
                * is thrown in some cases (invalid login for one, maybe more).
                */
               String responseBody = request.body();
               int code = request.code();

               Log.i("iFixit", "Response code: " + code);
               Log.i("iFixit", "Response body: " + responseBody);

               return endpoint.getEvent().setResponse(responseBody);
            } catch (HttpRequestException e) {
               Log.e("iFixit", "API error", e);
               return endpoint.getEvent().setError(APIError.getParseError(APIService.this));
            }
         }

         @Override
         protected void onPostExecute(APIEvent<?> result) {
            responder.setResult(result);
         }
      }.execute();
   }

   private boolean checkConnectivity(Responder responder, APIEndpoint endpoint) {
      ConnectivityManager cm = (ConnectivityManager)
       getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo netInfo = cm.getActiveNetworkInfo();

      if (netInfo == null || !netInfo.isConnected()) {
         responder.setResult(endpoint.getEvent().setError(APIError.getConnectionError(this)));
         return false;
      }

      return true;
   }
}
