package com.dozuki.ifixit.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class APIDatabase extends SQLiteOpenHelper {
   private static final int DATABASE_VERSION = 1;
   private static final String DATABASE_NAME = "api";
   private static final String TABLE_API_RESULTS = "api_results";

   // api_results column names
   private static final String KEY_ID = "_id";
   private static final String KEY_TARGET = "target";
   private static final String KEY_QUERY = "query";
   private static final String KEY_RESULT = "result";
   private static final String KEY_DATE = "date";

   private static final String CREATE_API_RESULTS_TABLE =
    "CREATE TABLE " + TABLE_API_RESULTS + "(" +
    KEY_ID + " INTEGER PRIMARY KEY, " +
    KEY_TARGET + " INTEGER, " +
    KEY_QUERY + " TEXT, " +
    KEY_RESULT + " TEXT, " +
    KEY_DATE + " INTEGER" + ")";

   public APIDatabase(Context context) throws Exception {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
      throw new Exception("Database not implemented yet");
   }

   @Override
   public void onCreate(SQLiteDatabase db) {
      db.execSQL(CREATE_API_RESULTS_TABLE);
   }

   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_API_RESULTS);

      // Create tables again.
      onCreate(db);
   }

   public String fetchResult(int target, String query) {
      SQLiteDatabase db = getReadableDatabase();

      Cursor cursor = db.query(
       TABLE_API_RESULTS,
       new String[] {KEY_RESULT},
       KEY_TARGET + " = ? AND " +
       KEY_QUERY + " = ? ",
       new String[] {String.valueOf(target),
                     query},
       null, null, null, null);

      if (cursor == null || !cursor.moveToFirst()) {
         return null;
      }

      String result = cursor.getString(0);

      cursor.close();
      db.close();

      return result;
   }

   public void insertResult(String result, int target, String query) {
      SQLiteDatabase db = getWritableDatabase();
      ContentValues values = new ContentValues();

      values.put(KEY_TARGET, target);
      values.put(KEY_QUERY, query);
      values.put(KEY_RESULT, result);
      values.put(KEY_DATE, (int)(System.currentTimeMillis() / 1000));

      db.insert(TABLE_API_RESULTS, null, values);
      db.close();
   }
}
