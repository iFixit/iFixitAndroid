package com.dozuki.ifixit.util;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
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
import com.dozuki.ifixit.R;
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
   private static final String TOPIC_API_URL =
    "http://www.ifixit.com/api/1.0/topic/";
   private static final String GUIDE_API_URL =
    "http://www.ifixit.com/api/1.0/guide/";
   private static final String CATEGORIES_API_URL =
    "http://www.ifixit.com/api/1.0/categories/";
   private static final String LOGIN_API_URL =
	    "https://www.ifixit.com/api/0.1/login";
   private static final String REGISTER_API_URL =
	    "https://www.ifixit.com/api/0.1/register";
   
   private static final String USER_IMAGES_API_URL =
	    "http://www.ifixit.com/api/1.0/image/user";
   
   private static final String UPLOAD_MEDIA_API_URL =
	    "http://www.ifixit.com/api/1.0/image/upload";
   
   private static final String DELETE_MEDIA_API_URL =
	    "http://www.ifixit.com/api/1.0/image/delete";
   
   
   private static final String API_DOMAIN =  ".ifixit.com";

   private static final String REQUEST_TARGET = "REQUEST_TARGET";
   private static final String REQUEST_QUERY = "REQUEST_QUERY";
   private static final String REQUEST_BROADCAST_ACTION =
	    "REQUEST_BROADCAST_ACTION";
   private static final String REQUEST_AUTHENICATION_PACKAGE = 
	   "AUTHENICATION_PACKAGE";

   private static final int TARGET_CATEGORIES = 0;
   private static final int TARGET_GUIDE = 1;
   private static final int TARGET_TOPIC = 2;
   private static final int TARGET_LOGIN = 3;
   private static final int TARGET_REGISTER = 4;
   private static final int TARGET_MEDIA_LIST= 5;
   private static final int TARGET_UPLOAD_MEDIA= 6;
   private static final int TARGET_DELETE_MEDIA= 7;

   private static final String NO_QUERY = "";

   public static final String ACTION_CATEGORIES =
    "com.dozuki.ifixit.api.categories";
   public static final String ACTION_GUIDE =
    "com.dozuki.ifixit.api.guide";
   public static final String ACTION_TOPIC =
    "com.dozuki.ifixit.api.topic";
   public static final String ACTION_LOGIN =
	    "com.dozuki.ifixit.api.login";
   public static final String ACTION_REGISTER =
	    "com.dozuki.ifixit.api.resgister";
   public static final String ACTION_USER_MEDIA =
	    "com.dozuki.ifixit.api.images";
   public static final String ACTION_UPLOAD_MEDIA =
	    "com.dozuki.ifixit.api.upload";
   public static final String ACTION_DELETE_MEDIA =
	    "com.dozuki.ifixit.api.delete";

   
 
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
      final AuthenicationPackage authenicationPackage = (AuthenicationPackage) extras.getSerializable(REQUEST_AUTHENICATION_PACKAGE);
      
      
      if(authenicationPackage != null)
      {
    	  perfromAuthenicatedRequestHelper(this, requestTarget, authenicationPackage, requestQuery,  new Responder() {
    	         public void setResult(Result result) {

    	            if (!result.hasError()) {
    	               result = parseResult(result.getResponse(), requestTarget,
    	                broadcastAction);
    	            }

    	            // Don't save if there a parse error.
    	            if (!result.hasError()) {
    	               saveResult(result, requestTarget, requestQuery);
    	            }

    	            // Always broadcast the result despite any errors.
    	            broadcastResult(result, broadcastAction);
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
               result = parseResult(result.getResponse(), requestTarget,
                broadcastAction);
            }

            // Don't save if there a parse error.
            if (!result.hasError()) {
               saveResult(result, requestTarget, requestQuery);
            }

            // Always broadcast the result despite any errors.
            broadcastResult(result, broadcastAction);
         }
      });

      return START_NOT_STICKY;
   }

   /**
    * Parse the response in the given result with the given requestTarget.
    */
   private Result parseResult(String response, int requestTarget,
    String broadcastAction) {
      Object parsedResult = null;
     Log.e("IN PASE", response);
      try {
         switch (requestTarget) {
         case TARGET_CATEGORIES:
            parsedResult = JSONHelper.parseTopics(response);
            break;
         case TARGET_GUIDE:
            parsedResult = JSONHelper.parseGuide(response);
            break;
         case TARGET_TOPIC:
            parsedResult = JSONHelper.parseTopicLeaf(response);
            break;
         case TARGET_LOGIN:
            parsedResult = JSONHelper.parseLoginInfo(response);
             break;
             
         case TARGET_MEDIA_LIST:
        	 parsedResult = JSONHelper.parseUserImages(response);
        	 break;
         case TARGET_UPLOAD_MEDIA:
        	 parsedResult="";
        	 break;
         case TARGET_DELETE_MEDIA:
        	 parsedResult="";
        	 break;
         default:
            Log.w("iFixit", "Invalid request target: " + requestTarget);
            return new Result(Error.PARSE);
         }

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

   private void broadcastResult(Result result, String broadcastAction) {
      Intent broadcast = new Intent();
      Bundle extras = new Bundle();

      extras.putSerializable(RESULT, result);
      broadcast.putExtras(extras);

      broadcast.setAction(broadcastAction);
      sendBroadcast(broadcast);
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
   
   public static Intent getLoginIntent(Context context, AuthenicationPackage authenicationPackage) {
	      return createLoginIntent(context, TARGET_LOGIN, authenicationPackage , ACTION_LOGIN);
	   }
   
	public static Intent getUploadImageIntent(Context context,
			AuthenicationPackage authenicationPackage, String filePath) {
		return createUploadImageIntent(context, TARGET_UPLOAD_MEDIA,
				authenicationPackage, ACTION_UPLOAD_MEDIA, filePath);
	}

	public static Intent getDeleteMediaIntent(Context context,
			AuthenicationPackage authenicationPackage, String requestQuery) {
		return createDeleteMediaIntent(context, TARGET_DELETE_MEDIA,
				authenicationPackage, requestQuery, ACTION_DELETE_MEDIA);
	}

	public static Intent userMediaIntent(Context context,
			AuthenicationPackage authenicationPackage) {
		return createUserMediaIntent(context, TARGET_MEDIA_LIST,
				authenicationPackage, ACTION_USER_MEDIA);
	}

   public static Intent getRegisterIntent(Context mContext,
			AuthenicationPackage authenicationPackage) {
	   return createRegisterIntent(mContext, TARGET_REGISTER, authenicationPackage , ACTION_REGISTER);
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
   
   private static Intent createLoginIntent(Context context, int target,AuthenicationPackage authenicationPackage, String action) {
	   
		      Intent intent = new Intent(context, APIService.class);
		      Bundle extras = new Bundle();

		      extras.putInt(REQUEST_TARGET, target);
		     // extras.putString(REQUEST_LOGIN, login);
		     // extras.putString(REQUEST_PASSWORD, password);
		      extras.putSerializable(REQUEST_AUTHENICATION_PACKAGE, authenicationPackage);
		      extras.putString(REQUEST_BROADCAST_ACTION, action);
		      intent.putExtras(extras);

		      return intent;
		   }
   
   private static Intent createUserMediaIntent(Context context, int target,AuthenicationPackage authenicationPackage, String action) {
	   
	      Intent intent = new Intent(context, APIService.class);
	      Bundle extras = new Bundle();
	      extras.putInt(REQUEST_TARGET, target);
	      extras.putSerializable(REQUEST_AUTHENICATION_PACKAGE, authenicationPackage);
	      extras.putString(REQUEST_BROADCAST_ACTION, action);
	      intent.putExtras(extras);

	      return intent;
	   }

   
   private static Intent createRegisterIntent(Context context, int target,AuthenicationPackage authenicationPackage, String action) {
	   
	      Intent intent = new Intent(context, APIService.class);
	      Bundle extras = new Bundle();

	      extras.putInt(REQUEST_TARGET, target);
	      extras.putSerializable(REQUEST_AUTHENICATION_PACKAGE, authenicationPackage);
	      extras.putString(REQUEST_BROADCAST_ACTION, action);
	      intent.putExtras(extras);

	      return intent;
	   }
   
   private static Intent createUploadImageIntent(Context context,
			int target, AuthenicationPackage authenicationPackage,
			String action,String filePath) {
	      Intent intent = new Intent(context, APIService.class);
	      Bundle extras = new Bundle();

	      extras.putInt(REQUEST_TARGET, target);
	      extras.putString(REQUEST_QUERY, filePath);
	      extras.putSerializable(REQUEST_AUTHENICATION_PACKAGE, authenicationPackage);
	      extras.putString(REQUEST_BROADCAST_ACTION, action);
	      intent.putExtras(extras);

	      return intent;
	}
   
   
   private static Intent createDeleteMediaIntent(Context context,
			int target, AuthenicationPackage authenicationPackage, String query,
			String action) {
	      Intent intent = new Intent(context, APIService.class);
	      Bundle extras = new Bundle();

	      extras.putInt(REQUEST_TARGET, target);
	      extras.putString(REQUEST_QUERY, query);
	      extras.putSerializable(REQUEST_AUTHENICATION_PACKAGE, authenicationPackage);
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

   private static void performRequestHelper(Context context, int requestTarget,
    String requestQuery, Responder responder) {
      if (!checkConnectivity(context, responder)) {
         return;
      }

      String url = null;

      switch (requestTarget) {
      case TARGET_CATEGORIES:
         url = CATEGORIES_API_URL;
         break;
      case TARGET_GUIDE:
         url = GUIDE_API_URL + requestQuery;
         break;
      case TARGET_TOPIC:
         try {
            url = TOPIC_API_URL + URLEncoder.encode(requestQuery,
             "UTF-8");
         } catch (Exception e) {
            Log.w("iFixit", "Encoding error: " + e.getMessage());
            responder.setResult(new Result(Error.PARSE));
            return;
         }
         break;
      default:
         Log.w("iFixit", "Invalid request target: " + requestTarget);
         responder.setResult(new Result(Error.PARSE));
         return;
      }

      performRequest(url, responder);
   }
   
	private static void perfromAuthenicatedRequestHelper(Context context, int requestTarget, AuthenicationPackage authenicationPackage, String requestQuery, Responder responder) {
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
		case TARGET_MEDIA_LIST:
			url = USER_IMAGES_API_URL;
			authenicationPackage.login = null;
			authenicationPackage.password = null;
			authenicationPackage.username = null;
			break;
		case TARGET_UPLOAD_MEDIA:

			file = new File(requestQuery);
			url = UPLOAD_MEDIA_API_URL + "?file=" + file.getName();//URLEncoder.encode(file.getName(),
         //   "UTF-8");
		
			authenicationPackage.login = null;
			authenicationPackage.password = null;
			authenicationPackage.username = null;
			break;
			
		case TARGET_DELETE_MEDIA:
		
			url = DELETE_MEDIA_API_URL + requestQuery;//URLEncoder.encode(requestQuery,
           // "UTF-8");
			
			  authenicationPackage.login = null;
			  authenicationPackage.password = null;
		      authenicationPackage.username = null;
			break;
		default:
			Log.w("iFixit", "Invalid request target: " + requestTarget);
			responder.setResult(new Result(Error.PARSE));
			return;
		}
		Log.e("URL", url);
		performAuthenicatedRequest(url, authenicationPackage, file, responder);
	}

 
   private static void  performAuthenicatedRequest(final String url, final AuthenicationPackage authenicationPackage, final File file,  final Responder responder) {
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
	            HashMap<String,String> params = new HashMap<String,String>();
	            HashMap<String,String> header = new HashMap<String,String>();
 
	            params.put("login", authenicationPackage.login);
			    params.put("password", authenicationPackage.password);
			    params.put("username", authenicationPackage.username);
			    if(file != null)
			    {
			    	params.put("file", file.getName());
			    }
			   // params.put("password", authenicationPackage.password);

	            try {
	               helper.performPostWithSessionCookie(url, null, null, authenicationPackage.session, API_DOMAIN, header, params, file);
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
