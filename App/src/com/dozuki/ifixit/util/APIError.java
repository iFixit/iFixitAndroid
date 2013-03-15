package com.dozuki.ifixit.util;

import android.content.Context;

import com.dozuki.ifixit.R;

import java.io.Serializable;


public class APIError implements Serializable {
   private static final long serialVersionUID = 1L;
   public static enum ErrorType {OTHER, INVALID_USER, PARSE, CONNECTION, INVALID_REVISION, FATAL};
   public String mTitle;
   public String mMessage;
   public ErrorType mType;

   public APIError(String title, String message, ErrorType type) {
      mTitle = title;
      mMessage = message;
      mType = type;
   }

   public static APIError getParseError(Context context) {
      return new APIError(context.getString(R.string.parse_error_title),
       context.getString(R.string.parse_error_message), ErrorType.OTHER);
   }

   public static APIError getConnectionError(Context context) {
      return new APIError(context.getString(R.string.no_connection_title),
       context.getString(R.string.no_connection), ErrorType.OTHER);
   }


   public static APIError getRevisionError(Context context) {
      return new APIError(context.getString(R.string.invalid_revision_error_title),
              context.getString(R.string.invalid_revision_error), ErrorType.INVALID_REVISION);
   }

   public static APIError getFatalError(Context context) {
      return new APIError(context.getString(R.string.fatal_error_title),
              context.getString(R.string.fatal_error), ErrorType.INVALID_REVISION);
   }
}
