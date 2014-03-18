package com.dozuki.ifixit.util.api;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.UnsupportedOperationException;

/**
 * WARNING: Not currently used. There are plans of using it for storing offline
 * guides.
 */
public class ApiDatabase extends SQLiteOpenHelper {
   private static final int DATABASE_VERSION = 1;
   private static final String DATABASE_NAME = "api";

   private static ApiDatabase sDatabase;

   public static ApiDatabase get(Context context) {
      throw new UnsupportedOperationException("ApiDatabase is not ready!");
      /*
      if (sDatabase == null) {
         sDatabase = new ApiDatabase(context.getApplicationContext());
      }

      return sDatabase;
      */
   }

   private ApiDatabase(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
   }

   @Override
   public void onCreate(SQLiteDatabase db) {
      db.execSQL(CREATE_API_RESULTS_TABLE);
   }

   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_API_RESULTS);

      // Create tables again.
      // TODO: This isn't a viable solution when the DB is upgraded.
      onCreate(db);
   }

   /**
    * Stores API responses for GET requests. Userid and URL are unique enough
    * to uniquely identify requests because the site is included in the URL,
    * every request method is `GET`, and all the request headers are the same.
    */
   private static final String TABLE_API_RESULTS = "api_results";
   private static final String KEY_ID = "_id";
   private static final String KEY_USERID = "userid";
   private static final String KEY_URL = "url";
   private static final String KEY_RESPONSE = "response";
   private static final String KEY_DATE = "date";

   private static final String CREATE_API_RESULTS_TABLE =
    "CREATE TABLE " + TABLE_API_RESULTS + "(" +
      KEY_ID + " INTEGER PRIMARY KEY, " +
      KEY_USERID + " INTEGER, " +
      KEY_URL + " TEXT, " +
      KEY_RESPONSE + " TEXT, " +
      KEY_DATE + " INTEGER" +
    ")";

   public String getResponse(String url, Integer userid) {
      SQLiteDatabase db = getReadableDatabase();

      String useridQuery;
      String[] selectionArgs;
      if (userid == null) {
         useridQuery = KEY_USERID + " IS NULL";
         selectionArgs = new String[] {url};
      } else {
         useridQuery = KEY_USERID + " = ?";
         selectionArgs = new String[] {url, userid.toString()};
      }

      Cursor cursor = db.query(
       TABLE_API_RESULTS,
       new String[] {KEY_RESPONSE},
       KEY_URL + " = ? AND " +
       useridQuery,
       selectionArgs,
       null,
       null,
       /* ORDER BY = */ "date DESC",
       /* LIMIT = */ "1");

      if (cursor == null || !cursor.moveToFirst()) {
         return null;
      }

      String result = cursor.getString(0);

      cursor.close();

      return result;
   }

   public void insertResponse(Integer userid, String url, String response) {
      SQLiteDatabase db = getWritableDatabase();
      ContentValues values = new ContentValues();

      values.put(KEY_USERID, userid);
      values.put(KEY_URL, url);
      values.put(KEY_RESPONSE, response);
      values.put(KEY_DATE, (int)(System.currentTimeMillis() / 1000));

      db.insert(TABLE_API_RESULTS, null, values);
   }
}
