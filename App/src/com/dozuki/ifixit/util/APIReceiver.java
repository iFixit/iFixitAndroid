package com.dozuki.ifixit.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Class to be used in conjunction with APIService to receive API results.
 */
public abstract class APIReceiver extends BroadcastReceiver {
   @Override
   public void onReceive(Context context, Intent intent) {
      APIService.Result result = (APIService.Result)intent.getExtras().
       getSerializable(APIService.RESULT);

      if (!result.hasError()) {
         onSuccess(result.getResult(), intent);
      } else {
         onFailure(result.getError(), intent);
      }
   }

   /**
    * Method that is called on a successful API call.
    *
    * @param result An object that has been parsed and constructed by the
    * APIEndpoint's parseResult function.
    *
    * @param intent The Intent provided to onReceive that contains various
    * information from APIService.
    */
   public abstract void onSuccess(Object result, Intent intent);

   /**
    * Method that is called on a failed API call. This can happen if the user
    * doesn't have internet or the request fails. Usually this function creates
    * and shows an error dialog using APIService.getErrorDialog().
    *
    * @param error An Error that describes the type of error that occurred.
    * @param intent The Intent provided to onReceive that contains various
    * information from APIService.
    */
   public abstract void onFailure(APIError error, Intent intent);
}
