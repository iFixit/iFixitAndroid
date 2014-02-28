package com.dozuki.ifixit.util.api;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;

import java.io.Serializable;

public class ApiError implements Serializable {
   private static final long serialVersionUID = 1L;
   private static final int NO_INDEX = -1;

   public static enum Type {
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
         false,
         false
      ),
      NOT_FOUND( // 404
         R.string.error,
         R.string.not_found_error,
         false,
         true
      ),
      CONFLICT( // 409
         R.string.invalid_revision_error_title,
         R.string.invalid_revision_error,
         false,
         false
      ),
      VALIDATION(
         R.string.validation_error_title,
         R.string.validation_error_body,
         false,
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
      protected boolean mFinishActivity;

      private Type(int title, int message) {
         this(title, message, true, false);
      }

      private Type(int title, int message, boolean tryAgain, boolean finishActivity) {
         mTitle = title;
         mMessage = message;
         mTryAgain = tryAgain;
         mFinishActivity = finishActivity;
      }
   }

   public String mTitle;
   public String mMessage;
   public Type mType;
   public int mIndex;

   public ApiError(Type type) {
      this(type.mTitle, type.mMessage, type);
   }

   public ApiError(int title, int message, Type type) {
      this(App.get().getString(title), App.get().getString(message), type);
   }

   public ApiError(String title, String message, Type type) {
      this(title, message, type, NO_INDEX);
   }

   public ApiError(String title, String message, Type type, int index) {
      mTitle = title;
      mMessage = message;
      mType = type;
      mIndex = index;
   }

   public static ApiError getByStatusCode(int code) {
      Type error;

      switch (code) {
         case 403: error = Type.FORBIDDEN; break;
         case 404: error = Type.NOT_FOUND; break;
         case 409: error = Type.CONFLICT;  break;
         case 422: error = Type.VALIDATION; break;
         default:  error = Type.OTHER;
      }

      return new ApiError(error);
   }
}
