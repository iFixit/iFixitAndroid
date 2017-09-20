package com.dozuki.ifixit.util.api;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.dozuki.ifixit.BuildConfig;

/**
 * Currently just a stub so we can use the sync adapter framework.
 */
public class ApiContentProvider extends ContentProvider {
   private static String sAuthority;
   public static String getAuthority() {
      if (sAuthority == null) {
         sAuthority = "com.dozuki." + BuildConfig.SITE_NAME + ".provider";
      }

      return sAuthority;
   }

   @Override
   public boolean onCreate() {
      // Always return to to indicate that it loaded correctly.
      return true;
   }

   @Override
   public Cursor query(Uri uri, String[] projection, String selection,
                       String[] selectionArgs, String sortOrder) {
      return null;
   }

   @Override
   public String getType(Uri uri) {
      // Empty string for MIME type.
      return new String();
   }

   @Override
   public Uri insert(Uri uri, ContentValues values) {
      return null;
   }

   @Override
   public int delete(Uri uri, String selection, String[] selectionArgs) {
      return 0;
   }

   @Override
   public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
      return 0;
   }
}