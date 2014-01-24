package com.dozuki.ifixit.util.api;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.util.JSONHelper;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * WARNING: Not currently used. There are plans of using it for storing offline
 * guides.
 */
public class ApiDatabase extends SQLiteOpenHelper {
   private static final int DATABASE_VERSION = 1;
   private static final String DATABASE_NAME = "api";

   private static ApiDatabase sDatabase;

   public static ApiDatabase get(Context context) {
      if (sDatabase == null) {
         sDatabase = new ApiDatabase(context.getApplicationContext());
      }

      return sDatabase;
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
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_OFFLINE_GUIDES);

      // Create tables again.
      // TODO: This isn't a viable solution when the DB is upgraded.
      onCreate(db);
   }

   /**
    * Stores the JSON for guides stored offline.
    */
   private static final String TABLE_OFFLINE_GUIDES = "offline_guides";
   private static final String KEY_ID = "_id";
   private static final String KEY_SITE_NAME = "site_name";
   private static final String KEY_USERID = "userid";
   private static final String KEY_GUIDEID = "guideid";
   private static final String KEY_MODIFIED_DATE = "modified_date";
   private static final String KEY_JSON = "json";

   private static final String CREATE_API_RESULTS_TABLE =
    "CREATE TABLE " + TABLE_OFFLINE_GUIDES + "(" +
       KEY_ID + " INTEGER PRIMARY KEY, " +
       KEY_SITE_NAME + " TEXT, " +
       KEY_USERID + " INTEGER, " +
       KEY_GUIDEID + " INTEGER, " +
       KEY_MODIFIED_DATE + " REAL, " +
       KEY_JSON + " TEXT, " +
       "UNIQUE (" +
          KEY_SITE_NAME + ", " +
          KEY_USERID + ", " +
          KEY_GUIDEID +
       ") ON CONFLICT REPLACE " +
    ")";

   public ArrayList<Guide> getOfflineGuides(Site site, User user) {
      final int JSON_INDEX = 0;
      SQLiteDatabase db = getReadableDatabase();

      Cursor cursor = db.query(
       TABLE_OFFLINE_GUIDES,
       new String[] {KEY_JSON},
       KEY_SITE_NAME + " = ? AND " +
       KEY_USERID + " = ?",
       new String[] {site.mName, user.getUserid() + ""},
       null,
       null,
       null,
       null);

      ArrayList<Guide> guides = new ArrayList<Guide>();

      while (cursor.moveToNext()) {
         String guideJson = cursor.getString(JSON_INDEX);
         try {
            guides.add(JSONHelper.parseGuide(guideJson));
         } catch (JSONException e) {
            Log.e("ApiDatabase", "Cannot parse stored guide!", e);
         }
      }

      cursor.close();

      return guides;
   }

   /**
    * Returns a map of guideid to modified date for all of the user's offline guides.
    */
   public Map<Integer, Double> getGuideModifiedDates(Site site, User user) {
      final int GUIDEID_INDEX = 0;
      final int MODIFIED_DATE_INDEX = 1;
      SQLiteDatabase db = getReadableDatabase();

      Cursor cursor = db.query(
       TABLE_OFFLINE_GUIDES,
       new String[] {KEY_GUIDEID, KEY_MODIFIED_DATE},
       KEY_SITE_NAME + " = ? AND " +
        KEY_USERID + " = ?",
       new String[] {site.mName, user.getUserid() + ""},
       null,
       null,
       null,
       null);

      Map<Integer, Double> modifiedDates = new HashMap<Integer, Double>();

      while (cursor.moveToNext()) {
         modifiedDates.put(
            cursor.getInt(GUIDEID_INDEX),
            cursor.getDouble(MODIFIED_DATE_INDEX)
         );
      }

      cursor.close();

      return modifiedDates;
   }

   public void deleteGuides(Site site, User user, Set<Integer> guideids) {
      if (guideids.isEmpty()) {
         return;
      }

      StringBuilder where = new StringBuilder(
       KEY_SITE_NAME + " = ? AND " +
       KEY_USERID + " = ? AND " +
       KEY_GUIDEID + " IN (");
      final int NUM_NON_GUIDE_PARAMS = 2;
      int i = NUM_NON_GUIDE_PARAMS;
      String[] params = new String[guideids.size() + NUM_NON_GUIDE_PARAMS];

      params[0] = site.mName;
      params[1] = user.getUserid() + "";

      for (Integer guideid : guideids) {
         params[i] = guideid.toString();
         where.append("?,");
         i++;
      }
      where.deleteCharAt(where.length() - 1); // Delete trailing comma.
      where.append(")");

      getWritableDatabase().delete(
       TABLE_OFFLINE_GUIDES,
       where.toString(),
       params
      );
   }

   public void saveGuide(Site site, User user, ApiEvent<Guide> guideEvent) {
      SQLiteDatabase db = getWritableDatabase();
      ContentValues values = new ContentValues();
      Guide guide = guideEvent.getResult();

      values.put(KEY_SITE_NAME, site.mName);
      values.put(KEY_USERID, user.getUserid());
      values.put(KEY_GUIDEID, guide.getGuideid());
      values.put(KEY_MODIFIED_DATE, guide.getAbsoluteModifiedDate());
      values.put(KEY_JSON, guideEvent.getResponse());

      db.insertWithOnConflict(TABLE_OFFLINE_GUIDES, null, values,
       SQLiteDatabase.CONFLICT_REPLACE);
   }
}
