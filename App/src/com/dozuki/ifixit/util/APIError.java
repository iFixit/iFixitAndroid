package com.dozuki.ifixit.util;

import android.content.Context;
import com.dozuki.ifixit.R;

import java.io.Serializable;

public class APIError implements Serializable {
   private static final long serialVersionUID = 1L;
   public static enum ErrorType {
      OTHER(
         R.string.fatal_error_title,
         R.string.fatal_error
      ),
      PARSE(
         R.string.parse_error_title,
         R.string.parse_error_message
      ),
      CONNECTION(
         R.string.no_connection_title,
         R.string.no_connection
      ),
      INVALID_USER(
         R.string.error,
         R.string.login_error
      ),
      FORBIDDEN( // 403
         R.string.error,
         R.string.forbidden_error,
         false
      ),
      NOT_FOUND( // 404
         R.string.error,
         R.string.not_found_error,
         false
      ),
      CONFLICT( // 409
         R.string.invalid_revision_error_title,
         R.string.invalid_revision_error,
         false
      ),
      UNAUTHORIZED(
         // These values shouldn't ever be used because this is merely a signal
         // to open the login dialog.
         R.string.fatal_error_title,
         R.string.fatal_error
      );

      protected int mTitle;
      protected int mMessage;
      protected boolean mTryAgain;

      private ErrorType() {
         this(-1, -1, true);
      }

      private ErrorType(int title, int message) {
         this(title, message, true);
      }

      private ErrorType(int title, int message, boolean tryAgain) {
         mTitle = title;
         mMessage = message;
         mTryAgain = tryAgain;
      }
   }

   public String mTitle;
   public String mMessage;
   public ErrorType mType;

   public APIError(ErrorType type, Context context) {
      this(type.mTitle, type.mMessage, type, context);
   }

   public APIError(int title, int message, ErrorType type, Context context) {
      this(context.getString(title), context.getString(message), type);
   }

   public APIError(String title, String message, ErrorType type) {
      mTitle = title;
      mMessage = message;
      mType = type;
   }

   public static APIError getByStatusCode(int code, Context context) {
      ErrorType error;

      switch (code) {
         case 403: error = ErrorType.FORBIDDEN; break;
         case 404: error = ErrorType.NOT_FOUND; break;
         case 409: error = ErrorType.CONFLICT;  break;
         default:  error = ErrorType.OTHER;
      }

      return new APIError(error, context);
   }
}
