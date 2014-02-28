package com.dozuki.ifixit.util.api;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.BuildConfig;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.ui.BaseActivity;
import com.dozuki.ifixit.util.JSONHelper;
import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import com.squareup.otto.DeadEvent;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Class that performs asynchronous API calls and posts the results to the
 * event bus.
 */
public class Api {
   private interface Responder {
      public void setResult(ApiEvent<?> result);
   }

   private static final int INVALID_LOGIN_CODE = 401;

   /**
    * Pending API call. This is set when an authenticated request is performed
    * but the user is not logged in. This is then performed once the user has
    * authenticated.
    */
   private static ApiCall sPendingApiCall;

   /**
    * List of events that have been sent but not received by any subscribers.
    */
   private static List<ApiEvent<?>> sDeadApiEvents;

   /**
    * Returns true if the the user needs to be authenticated for the given site and endpoint.
    */
   private static boolean requireAuthentication(ApiEndpoint endpoint) {
      return (endpoint.mAuthenticated || !App.get().getSite().mPublic) &&
       !endpoint.mForcePublic;
   }

   /**
    * Performs the API call defined by the given Intent. This takes care of opening a
    * login dialog and saving the Intent if the user isn't authenticated but should be.
    */
   public static void call(Activity activity, final ApiCall apiCall) {
      ApiEndpoint endpoint = apiCall.mEndpoint;
      if (activity != null) {
         apiCall.mActivityid = ((BaseActivity)activity).getActivityid();
      } else if (apiCall.mActivityid == -1) {
         Log.w("Api", "Missing activityid!", new Exception());
      }

      // User needs to be logged in for an authenticated endpoint with the exception of login.
      if (requireAuthentication(endpoint) && !App.get().isUserLoggedIn()) {
         App.getBus().post(getUnauthorizedEvent(apiCall));
      } else {
         performRequest(apiCall, new Responder() {
            public void setResult(ApiEvent<?> result) {
               // Don't parse if we've erred already.
               if (!result.hasError()) {
                  result = parseResult(result, apiCall.mEndpoint);
               }

               // Don't save if there a parse error.
               if (!result.hasError()) {
                  saveResult(result, apiCall.mEndpoint.getTarget(), apiCall.mQuery);
               }

               if (apiCall.mEndpoint.mPostResults) {
                  /**
                   * Always post the result despite any errors. This actually sends it off
                   * to BaseActivity which posts the underlying ApiEvent<?> if the ApiCall
                   * was initiated by that Activity instance.
                   */
                  App.getBus().post(new ApiEvent.ActivityProxy(result));
               }
            }
         });
      }
   }

   /**
    * Returns an ApiEvent that triggers a login dialog and sets up the ApiCall to be performed
    * once the user successfully logs in.
    */
   private static ApiEvent<?> getUnauthorizedEvent(ApiCall apiCall) {
      sPendingApiCall = apiCall;

      // We aren't logged in anymore so lets make sure we don't think we are.
      App.get().shallowLogout();

      // The ApiError doesn't matter as long as one exists.
      return new ApiEvent.Unauthorized().
         setCode(INVALID_LOGIN_CODE).
         setError(new ApiError("", "", ApiError.Type.UNAUTHORIZED)).
         setApiCall(apiCall);
   }

   /**
    * Returns the pending API call and sets it to null. Returns null if no pending API call.
    */
   public static ApiCall getAndRemovePendingApiCall(Context context) {
      ApiCall pendingApiCall = sPendingApiCall;
      sPendingApiCall = null;

      if (pendingApiCall != null) {
         return pendingApiCall;
      } else {
         return null;
      }
   }

   /**
    * Parse the response in the given result with the given requestTarget.
    */
   private static ApiEvent<?> parseResult(ApiEvent<?> result, ApiEndpoint endpoint) {
      ApiEvent<?> event;

      int code = result.mCode;
      String response = result.getResponse();

      ApiError error = null;

      if (!isSuccess(code)) {
         error = JSONHelper.parseError(response, code);
      }

      if (error != null) {
         event = result.setError(error);
      } else {
         try {
            // We don't know the type of ApiEvent it is so we must let the endpoint's
            // parseResult return the correct one...
            event = endpoint.parseResult(response);

            // ... and then we can copy over the other values we need.
            event.mCode = code;
            event.mApiCall = result.mApiCall;
            event.mResponse = result.mResponse;
         } catch (Exception e) {
            // This is meant to catch JSON and GSON parse exceptions but enumerating
            // all different types of Exceptions and putting error handling code
            // in one place is tedious.
            Log.e("Api", "API parse error", e);
            result.setError(new ApiError(ApiError.Type.PARSE));

            event = result;
         }

         if (!isSuccess(code)) {
            event.setError(ApiError.getByStatusCode(code));
         }
      }

      return event;
   }

   private static boolean isSuccess(int code) {
      return code >= 200 && code < 300;
   }

   private static void saveResult(ApiEvent<?> result, int requestTarget,
    String requestQuery) {
      // Commented out because the DB code isn't ready yet.
      // ApiDatabase db = new ApiDatabase(this);
      // db.insertResult(result.getResponse(), requestTarget, requestQuery);
      // db.close();
   }

   public static AlertDialog getErrorDialog(final Activity activity,
    final ApiEvent<?> event) {
      ApiError error = event.getError();

      int positiveButton = error.mType.mTryAgain ?
       R.string.try_again : R.string.error_confirm;

      AlertDialog.Builder builder = new AlertDialog.Builder(activity);
      builder.setTitle(error.mTitle)
       .setMessage(error.mMessage)
       .setPositiveButton(positiveButton,
        new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
              // Try performing the request again.
              if (event.mError.mType.mTryAgain) {
                 call(activity, event.mApiCall);
              }

              dialog.dismiss();

              if (event.mError.mType.mFinishActivity) {
                 activity.finish();
              }
           }
        });

      AlertDialog dialog = builder.create();
      dialog.setOnCancelListener(new OnCancelListener() {
         @Override
         public void onCancel(DialogInterface dialog) {
            activity.finish();
         }
      });

      return dialog;
   }

   public static void init() {
      sDeadApiEvents = new LinkedList<ApiEvent<?>>();

      App.getBus().register(new Object() {
         @Subscribe
         public void onDeadEvent(DeadEvent deadEvent) {
            Object event = deadEvent.event;

            if (BuildConfig.DEBUG) {
               Log.i("Api", "onDeadEvent: " + event.getClass().getName());
            }

            if (event instanceof ApiEvent<?>) {
               addDeadApiEvent((ApiEvent<?>)event);
            } else if (event instanceof ApiEvent.ActivityProxy) {
               addDeadApiEvent(((ApiEvent.ActivityProxy)event).getApiEvent());
            }
         }

      });
   }

   private static void addDeadApiEvent(ApiEvent<?> apiEvent) {
      synchronized (sDeadApiEvents) {
         sDeadApiEvents.add(apiEvent);
      }
   }

   public static void retryDeadEvents(BaseActivity activity) {
      synchronized (sDeadApiEvents) {
         if (sDeadApiEvents.isEmpty()) {
            return;
         }

         List<ApiEvent<?>> deadApiEvents = sDeadApiEvents;
         sDeadApiEvents = new LinkedList<ApiEvent<?>>();
         int activityid = activity.getActivityid();

         // Iterate over all the dead events, firing off each one.  If it fails,
         // it is recaught by the @Subscribe onDeadEvent, and added back to the list.
         for (ApiEvent<?> apiEvent : deadApiEvents) {
            // Fire the event If the activityids match, otherwise add it back
            // to the list of dead events so we can try it again later.
            if (activityid == apiEvent.mApiCall.mActivityid) {
               if (BuildConfig.DEBUG) {
                  Log.i("Api", "Retrying dead event: " +
                   apiEvent.getClass().getName());
               }

               App.getBus().post(apiEvent);
            } else {
               if (BuildConfig.DEBUG) {
                  Log.i("Api", "Adding dead event: " + apiEvent.getClass().toString());
               }

               sDeadApiEvents.add(apiEvent);
            }
         }

         if (BuildConfig.DEBUG && sDeadApiEvents.size() > 0) {
            Log.i("Api", "Skipped " + sDeadApiEvents.size() + " dead events");
         }
      }
   }

   private static void performRequest(final ApiCall apiCall, final Responder responder) {
      final ApiEndpoint endpoint = apiCall.mEndpoint;

      if (!checkConnectivity(responder, endpoint, apiCall)) {
         return;
      }

      final String url = endpoint.getUrl(App.get().getSite(), apiCall.mQuery);

      if (App.inDebug()) {
         Log.i("Api", "Performing API call: " + endpoint.mMethod + " " + url);
         Log.i("Api", "Request body: " + apiCall.mRequestBody);
      }

      AsyncTask<String, Void, ApiEvent<?>> as = new AsyncTask<String, Void, ApiEvent<?>>() {
         @Override
         protected ApiEvent<?> doInBackground(String... dummy) {
            ApiEvent<?> event = endpoint.getEvent();
            event.setApiCall(apiCall);

            /**
             * Unfortunately we must split the creation of the HttpRequest
             * object and the appropriate actions to take for a GET vs. a POST
             * request. The request headers and trustAllCerts calls must be
             * made before any data is sent. However, we must have an HttpRequest
             * object already.
             */
            HttpRequest request;

            try {
               long startTime = System.currentTimeMillis();

               if (endpoint.mMethod.equals("GET")) {
                  request = HttpRequest.get(url);
               } else {
                  /**
                   * For all methods other than get we actually perform a POST but send
                   * a header indicating the actual request we are performing. This is
                   * because http-request's underlying HTTPRequest library doesn't
                   * support PATCH requests.
                   */
                  request = HttpRequest.post(url);
                  request.header("X-HTTP-Method-Override", endpoint.mMethod);
               }

               String authToken = null;

               /**
                * Get an appropriate auth token.
                */
               if (apiCall.mAuthToken != null) {
                  // This auth token overrides all other requirements/auth tokens.
                  authToken = apiCall.mAuthToken;
               } else if (App.get().isUserLoggedIn()) {
                  // Always include it if the user is logged in.
                  User user = App.get().getUser();
                  authToken = user.getAuthToken();
               }

               /**
                * Send along the auth token if we found one.
                */
               if (authToken != null) {
                  request.header("Authorization", "api " + authToken);
               }

               request.userAgent(App.get().getUserAgent());

               request.header("X-App-Id", BuildConfig.APP_ID);

               // Trust all certs and hosts in development
               if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO || App.inDebug()) {
                  request.trustAllCerts();
                  request.trustAllHosts();
               }

               request.followRedirects(false);

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

               if (App.inDebug()) {
                  long endTime = System.currentTimeMillis();

                  Log.d("Api", "Response code: " + code);
                  Log.d("Api", "Response body: " + responseBody);
                  Log.d("Api", "Request time: " + (endTime - startTime) + "ms");
               }

               /**
                * If the server responds with a 401, the user is logged out even though we
                * think that they are logged in. Return an Unauthorized event to prompt the
                * user to log in. Don't do this if we are logging in because the login dialog
                * will automatically handle these errors.
                */
               if (code == INVALID_LOGIN_CODE && !App.get().isLoggingIn()) {
                  return getUnauthorizedEvent(apiCall);
               } else {
                  return event.setCode(code).setResponse(responseBody);
               }
            } catch (HttpRequestException e) {
               if (e.getCause() != null) {
                  e.getCause().printStackTrace();
                  Log.e("Api", "IOException from request", e.getCause());
               } else {
                  e.printStackTrace();
                  Log.e("Api", "API error", e);
               }

               return event.setError(new ApiError(ApiError.Type.PARSE));
            }
         }

         @Override
         protected void onPostExecute(ApiEvent<?> result) {
            responder.setResult(result);
         }
      };

      if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) {
         as.execute();
      } else {
         as.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
      }
   }

   private static boolean checkConnectivity(Responder responder, ApiEndpoint endpoint,
    ApiCall apiCall) {
      ConnectivityManager cm = (ConnectivityManager)
       App.get().getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo netInfo = cm.getActiveNetworkInfo();

      if (netInfo == null || !netInfo.isConnected()) {
         responder.setResult(endpoint.getEvent()
          .setApiCall(apiCall)
          .setError(new ApiError(ApiError.Type.CONNECTION)));
         return false;
      }

      return true;
   }
}
