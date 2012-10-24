package com.dozuki.ifixit.util;

import java.io.Serializable;

import android.content.Context;

import com.dozuki.ifixit.R;


public class Error implements Serializable {
   private static final long serialVersionUID = 1L;
   public static enum ErrorType {OTHER, INVALID_USER, PARSE, CONNECTION};
   public String mTitle;
   public String mMessage;
   public ErrorType mType;

   public Error(String title, String message, ErrorType type) {
      mTitle = title;
      mMessage = message;
      mType = type;
   }

   public static Error getParseError(Context context) {
      return new Error(context.getString(R.string.parse_error_title),
       context.getString(R.string.parse_error_message), ErrorType.OTHER);
   }

   public static Error getConnectionError(Context context) {
      return new Error(context.getString(R.string.no_connection_title),
       context.getString(R.string.no_connection), ErrorType.OTHER);
   }
}
