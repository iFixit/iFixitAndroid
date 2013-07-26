package com.dozuki.ifixit.util;

import android.content.Context;
import com.dozuki.ifixit.R;

import java.io.Serializable;


public class APIError implements Serializable {
   private static final long serialVersionUID = 1L;
   public static enum ErrorType {
      OTHER,
      PARSE,
      CONNECTION,
      INVALID_USER,
      FATAL,

      UNAUTHORIZED, // 401
      FORBIDDEN, // 403
      NOT_FOUND, // 404
      CONFLICT // 409
   }

   public String mTitle;
   public String mMessage;
   public ErrorType mType;

   public APIError(int title, int message, ErrorType type, Context context) {
      this(context.getString(title), context.getString(message), type);
   }

   public APIError(String title, String message, ErrorType type) {
      mTitle = title;
      mMessage = message;
      mType = type;
   }

   public static APIError getByStatusCode(int code, Context context) {
      switch (code) {
      case 409:
         return getRevisionError(context);
      default:
         return getUnknownError(context);
      }
   }

   public static APIError getParseError(Context context) {
      return new APIError(R.string.parse_error_title,
       R.string.parse_error_message, ErrorType.PARSE, context);
   }

   public static APIError getConnectionError(Context context) {
      return new APIError(R.string.no_connection_title,
       R.string.no_connection, ErrorType.CONNECTION, context);
   }

   public static APIError getRevisionError(Context context) {
      return new APIError(R.string.invalid_revision_error_title,
       R.string.invalid_revision_error, ErrorType.CONFLICT, context);
   }

   public static APIError getFatalError(Context context) {
      return new APIError(R.string.fatal_error_title,
       R.string.fatal_error, ErrorType.FATAL, context);
   }

   public static APIError getUnknownError(Context context) {
      return new APIError(R.string.fatal_error_title,
       R.string.fatal_error, ErrorType.OTHER, context);
   }
}
