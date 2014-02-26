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

public class ApiDatabase extends SQLiteOpenHelper {
   public static final String TAG = "ApiDatabase";
   private static final int DATABASE_VERSION = 1;
   private static final String DATABASE_NAME = "api";

   private static ApiDatabase sDatabase;

   private final Context mContext;

   public static ApiDatabase get(Context context) {
      if (sDatabase == null) {
         sDatabase = new ApiDatabase(context.getApplicationContext());
      }

      return sDatabase;
   }

   private ApiDatabase(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);

      mContext = context;
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
   private static final String KEY_SITEID = "siteid";
   private static final String KEY_USERID = "userid";
   private static final String KEY_GUIDEID = "guideid";
   private static final String KEY_MODIFIED_DATE = "modified_date";
   private static final String KEY_MEDIA_TOTAL = "media_total";
   private static final String KEY_MEDIA_DOWNLOADED = "media_downloaded";
   private static final String KEY_JSON = "json";

   private static final String CREATE_API_RESULTS_TABLE =
    "CREATE TABLE " + TABLE_OFFLINE_GUIDES + "(" +
       KEY_ID + " INTEGER PRIMARY KEY, " +
       KEY_SITEID + " INTEGER, " +
       KEY_USERID + " INTEGER, " +
       KEY_GUIDEID + " INTEGER, " +
       KEY_MODIFIED_DATE + " REAL, " +
       KEY_MEDIA_TOTAL + " INTEGER, " +
       KEY_MEDIA_DOWNLOADED + " INTEGER, " +
       KEY_JSON + " TEXT, " +
       "UNIQUE (" +
          KEY_SITEID + ", " +
          KEY_USERID + ", " +
          KEY_GUIDEID +
       ") ON CONFLICT REPLACE " +
    ")";

   public ArrayList<GuideMediaProgress> getOfflineGuides(Site site, User user) {
      final int GUIDE_JSON_INDEX = 0;
      final int TOTAL_MEDIA_INDEX = 1;
      final int MEDIA_DOWNLOADED_INDEX = 2;
      Cursor cursor = getReadableDatabase().query(
       TABLE_OFFLINE_GUIDES,
       new String[] {KEY_JSON, KEY_MEDIA_TOTAL, KEY_MEDIA_DOWNLOADED},
       KEY_SITEID + " = ? AND " +
       KEY_USERID + " = ?",
       new String[] {site.mSiteid + "", user.getUserid() + ""},
       null,
       null,
       KEY_ID + " DESC"
      );

      ArrayList<GuideMediaProgress> guideMedia = new ArrayList<GuideMediaProgress>();

      while (cursor.moveToNext()) {
         guideMedia.add(new GuideMediaProgress(
            getGuideFromCursor(cursor, GUIDE_JSON_INDEX, false),
            cursor.getInt(TOTAL_MEDIA_INDEX),
            cursor.getInt(MEDIA_DOWNLOADED_INDEX)
         ));
      }

      return guideMedia;
   }

   public Guide getOfflineGuide(Site site, User user, int guideid) {
      Cursor cursor = getReadableDatabase().query(
       TABLE_OFFLINE_GUIDES,
       new String[] {KEY_JSON},
       KEY_SITEID + " = ? AND " +
       KEY_USERID + " = ? AND " +
       KEY_GUIDEID + " = ? ",
       new String[] {site.mSiteid + "", user.getUserid() + "", guideid + ""},
       null,
       null,
       null
      );
      cursor.moveToFirst();
      return getGuideFromCursor(cursor, 0, true);
   }

   /**
    * Returns guides that have been downloaded but some of the images are missing.
    */
   public ArrayList<Guide> getUncompleteGuides(Site site, User user) {
      SQLiteDatabase db = getReadableDatabase();

      Cursor cursor = db.query(
       TABLE_OFFLINE_GUIDES,
       new String[] {KEY_JSON},
       KEY_SITEID + " = ? AND " +
       KEY_USERID + " = ? AND " +
       KEY_MEDIA_DOWNLOADED + " != " + KEY_MEDIA_TOTAL,
       new String[] {site.mSiteid + "", user.getUserid() + ""},
       null,
       null,
       null);

      return getGuidesFromCursor(cursor, 0);
   }

   /**
    * Returns a list of Guides from the cursor with json at the provided index.
    */
   private ArrayList<Guide> getGuidesFromCursor(Cursor cursor, int jsonIndex) {
      ArrayList<Guide> guides = new ArrayList<Guide>();

      while (cursor.moveToNext()) {
         guides.add(getGuideFromCursor(cursor, jsonIndex, false));
      }

      cursor.close();

      return guides;
   }

   /**
    * Creates a guide from the cursor with JSON found at the provided index.
    */
   private Guide getGuideFromCursor(Cursor cursor, int jsonIndex, boolean closeCursor) {
      try {
         // Invalid cursor position.
         if (cursor.isBeforeFirst() || cursor.isAfterLast()) {
            return null;
         }
         String guideJson = cursor.getString(jsonIndex);
         return JSONHelper.parseGuide(guideJson);
      } catch (JSONException e) {
         Log.e(TAG, "Cannot parse stored guide!", e);
         return null;
      } finally {
         if (closeCursor) {
            cursor.close();
         }
      }
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
       KEY_SITEID + " = ? AND " +
       KEY_USERID + " = ?",
       new String[] {site.mSiteid + "", user.getUserid() + ""},
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
       KEY_SITEID + " = ? AND " +
       KEY_USERID + " = ? AND " +
       KEY_GUIDEID + " IN (");
      final int NUM_NON_GUIDE_PARAMS = 2;
      int i = NUM_NON_GUIDE_PARAMS;
      String[] params = new String[guideids.size() + NUM_NON_GUIDE_PARAMS];

      params[0] = site.mSiteid + "";
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

   public void saveGuide(Site site, User user, ApiEvent<Guide> guideEvent, int imagesTotal,
    int imagesDownloaded) {
      if (guideEvent == null) {
         throw new IllegalArgumentException("ApiEvent<Guide> guideEvent");
      }
      SQLiteDatabase db = getWritableDatabase();
      ContentValues values = new ContentValues();
      Guide guide = guideEvent.getResult();

      values.put(KEY_SITEID, site.mSiteid);
      values.put(KEY_USERID, user.getUserid());
      values.put(KEY_GUIDEID, guide.getGuideid());
      values.put(KEY_MODIFIED_DATE, guide.getAbsoluteModifiedDate());
      values.put(KEY_MEDIA_TOTAL, imagesTotal);
      values.put(KEY_MEDIA_DOWNLOADED, imagesDownloaded);
      values.put(KEY_JSON, guideEvent.getResponse());

      db.insertWithOnConflict(TABLE_OFFLINE_GUIDES, null, values,
       SQLiteDatabase.CONFLICT_REPLACE);
   }

   public void updateGuideProgress(Site site, User user, int guideid, int imagesTotal,
    int imagesDownloaded) {
      ContentValues values = new ContentValues();
      values.put(KEY_MEDIA_TOTAL, imagesTotal);
      values.put(KEY_MEDIA_DOWNLOADED, imagesDownloaded);

      getWritableDatabase().update(
       TABLE_OFFLINE_GUIDES,
       values,
       KEY_SITEID + " = ? AND " +
       KEY_USERID + " = ? AND " +
       KEY_GUIDEID + " = ?",
       new String[] {site.mSiteid + "", user.getUserid() + "", guideid + ""}
      );
   }
}
