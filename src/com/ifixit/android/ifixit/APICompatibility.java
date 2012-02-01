package com.ifixit.android.ifixit;

/**
 * Simple class used to determine when certain actions are allowed based on
 * the current device's API level
 */
public class APICompatibility {
   static {
      API_LEVEL = android.os.Build.VERSION.SDK_INT;
   }

   private static final int API_LEVEL;

   public static boolean hasActionBar() {
      return API_LEVEL >= 11;
   }
}
