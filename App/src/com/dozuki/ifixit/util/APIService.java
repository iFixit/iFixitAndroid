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
import com.dozuki.ifixit.guide_create.model.GuideCreateObject;
import com.dozuki.ifixit.guide_create.model.GuideCreateStepObject;
import com.dozuki.ifixit.guide_create.model.UserGuide;
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

   public static final String BASE_USER_AGENT = "iFixitAndroid/";

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
         Log.e("iFixit", "API parse error", e);
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

   public static APICall getLoginAPICall(String email, String password) {
      JSONObject requestBody = new JSONObject();

      try {
         requestBody.put("email", email);
         requestBody.put("password", password);
      } catch (JSONException e) {
         return null;
      }

      return new APICall(APIEndpoint.LOGIN, NO_QUERY, requestBody.toString());
   }

   public static APICall getLogoutAPICall(User user) {
      // Can't log out an already logged out user.
      if (user == null) {
         return null;
      }

      APICall apiCall = new APICall(APIEndpoint.LOGOUT, NO_QUERY);

      apiCall.mAuthToken = user.getAuthToken();

      return apiCall;
   }


   public static APICall getCreateGuideAPICall(UserGuide guide) {
      JSONObject requestBody = new JSONObject();

      try {
         requestBody.put("topic", guide.getTopic());
         requestBody.put("type", guide.getType());
         requestBody.put("subject", guide.getSubject());
         requestBody.put("title", guide.getTitle());
         requestBody.put("summary", guide.getSummary());
         requestBody.put("introduction", guide.getIntro());
      } catch (JSONException e) {
         return null;
      }

      return new APICall(APIEndpoint.CREATE_GUIDE, NO_QUERY, requestBody.toString());
   }
   
   /**
    * TODO: Pass in entire guide so parameters can easily be changed later.
    */
   public static APICall getEditGuideAPICall(int guideid, String device, String title,
    String summary, String intro, String guideType, String subject, int revisionid) {
      JSONObject requestBody = new JSONObject();

      try {
         requestBody.put("topic", device);
         requestBody.put("type", guideType);
         requestBody.put("subject", subject);
         requestBody.put("title", title);
         requestBody.put("summary", summary);
         requestBody.put("introduction", intro);
      } catch (JSONException e) {
         return null;
      }

      return new APICall(APIEndpoint.EDIT_GUIDE, "" + guideid + "?revisionid="
              + revisionid, requestBody.toString());
   }


   public static APICall getPublishGuideAPICall(int guideid, int revisionid) {


      return new APICall(APIEndpoint.PUBLISH_GUIDE, "" + guideid +  "/public" + "?revisionid="
              + revisionid, "");
   }

   public static APICall getUnPublishGuideAPICall(int guideid, int revisionid) {


      return new APICall(APIEndpoint.UNPUBLISH_GUIDE, "" + guideid +  "/public" + "?revisionid="
              + revisionid, "");
   }

   public static APICall getEditStepAPICall(GuideCreateStepObject step, int guideid) {
      JSONObject requestBody = new JSONObject();

      try {
         requestBody.put("title", step.getTitle());
         requestBody.put("lines", JSONHelper.createLineArray(step.getLines()));
         requestBody.put("media", JSONHelper.createStepMediaJsonObject(step));
      } catch (JSONException e) {
         return null;
      }

      return new APICall(APIEndpoint.UPDATE_GUIDE_STEP, "" + guideid + "/steps/" + step.getStepId() + "?revisionid="
         + step.getRevisionid(), requestBody.toString());
   }

   public static APICall getAddStepAPICall(GuideCreateStepObject step, int guideid, int stepPosition, int revisionid) {
      JSONObject requestBody = new JSONObject();

      try {
         requestBody.put("title", step.getTitle());
         requestBody.put("lines", JSONHelper.createLineArray(step.getLines()));
         requestBody.put("orderby", stepPosition);
         requestBody.put("media", JSONHelper.createStepMediaJsonObject(step));
      } catch (JSONException e) {
         return null;
      }

      return new APICall(APIEndpoint.ADD_GUIDE_STEP, "" + guideid + "/steps" + "?revisionid=" + revisionid,
         requestBody.toString());
   }

   public static APICall getRemoveStepAPICall(int guideid, int guideRevisionID, GuideCreateStepObject step) {
      JSONObject requestBody = new JSONObject();

      try {
         requestBody.put("revisionid", step.getRevisionid());
      } catch (JSONException e) {
         return null;
      }

      return new APICall(APIEndpoint.DELETE_GUIDE_STEP, "" + guideid + "/steps/" + step.getStepId() + "?revisionid="
         + step.getRevisionid(), requestBody.toString());
   }

   public static APICall getStepReorderAPICall(GuideCreateObject guide) {
      JSONObject requestBody = new JSONObject();

      try {
         requestBody.put("stepids", JSONHelper.createStepIdArray(guide.getSteps()));
      } catch (JSONException e) {
         return null;
      }

      return new APICall(APIEndpoint.REORDER_GUIDE_STEPS, "" + guide.getGuideid() + "/steporder" + "?revisionid="
         + guide.getRevisionid(), requestBody.toString());
   }

   /**
    * TODO: Paginate.
    */
   public static APICall getUserGuidesAPICall() {
      return new APICall(APIEndpoint.USER_GUIDES, NO_QUERY);
   }

   public static APICall getGuideForEditAPICall(int guideid) {
      return new APICall(APIEndpoint.GUIDE_FOR_EDIT, "" + guideid);
   }

   public static APICall getRegisterAPICall(String email, String password, String username) {
      JSONObject requestBody = new JSONObject();

      try {
         requestBody.put("email", email);
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
   
   public static APICall getUserVideosAPICall(String query) {
      return new APICall(APIEndpoint.USER_VIDEOS, query);
   }
   
   public static APICall getUserEmbedsAPICall(String query) {
      return new APICall(APIEndpoint.USER_EMBEDS, query);
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

   public static APICall getUserInfoAPICall(String authToken) {
      APICall apiCall = new APICall(APIEndpoint.USER_INFO, NO_QUERY);

      apiCall.mAuthToken = authToken;

      return apiCall;
   }

   public static AlertDialog getErrorDialog(Context context, APIError error,
    APICall apiCall) {
      switch (error.mType) {
         case INVALID_REVISION:
            return createFatalErrorDialog(context, error);
         default:
            return createErrorDialog(context, apiCall, error);
      }
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
         .setPositiveButton(mContext.getString(R.string.error_confirm),
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


   private static AlertDialog createFatalErrorDialog(final Context context, APIError error) {
      AlertDialog.Builder builder = new AlertDialog.Builder(context);
      builder.setTitle(error.mTitle)
              .setMessage(error.mMessage)
              .setPositiveButton(context.getString(R.string.error_confirm),
                      new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {
                            ((Activity)context).finish();
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

      Log.i("iFixit", "Performing API call: " + endpoint.mMethod + " " + url);
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
               String requestMethod;
               if (endpoint.mMethod.equals("GET")) {
                  request = HttpRequest.get(url);
               } else {
                  /**
                   * For all methods other than get we actually perform a POST but send
                   * a header indicating the actual request we are performing. This is
                   * because http-request's underlying HTTPRequest library doesn't
                   * suupport PATCH requests.
                   */
                  request = HttpRequest.post(url);
                  request.header("X-REQUEST-METHOD-OVERRIDE", endpoint.mMethod);
               }

               /**
                * Uncomment to test HTTPS API calls in development.
                */
               request.trustAllCerts();
              request.trustAllHosts();

               String authToken = null;
               /**
                * Get an appropriate auth token.
                */
               if (apiCall.mAuthToken != null) {
                  // This auth token overrides all other requirements/auth tokens.
                  authToken = apiCall.mAuthToken;
               } else if (requireAuthentication(mSite, endpoint)) {
                  User user = ((MainApplication)getApplicationContext()).getUser();
                  authToken = user.getAuthToken();
               }

               request.userAgent(MainApplication.get().getUserAgent());

               /**
                * Send along the auth token if we found one.
                */
               if (authToken != null) {
                  request.header("Authorization", authToken);
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
