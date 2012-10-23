package com.dozuki.ifixit.util;

import java.io.Serializable;


public class Error implements Serializable {
   private static final long serialVersionUID = 1L;
   public static enum ErrorType {OTHER, PARSE, CONNECTION};
   String mMessege;
   ErrorType mType;

   public Error(String msg, ErrorType type) {
      mMessege = msg;
      mType = type;
   }
}
