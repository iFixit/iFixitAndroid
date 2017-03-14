package com.dozuki.ifixit.model;

import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.util.JSONHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Comment implements Serializable {
   private static final long serialVersionUID = -1333520488223961692L;

   private static final int NO_PARENT_ID = -1;
   public int mContextid;
   public String mCommentSource;
   public int mCommentid;
   public String mLocale;
   public int mParentid;
   public String mContext; // What this comment is about
   public User mUser;
   public String mTitle;
   public String mTextRaw;
   public String mTextRendered;
   public int mRating;
   public Date mDate;
   public Date mModifiedDate;
   public Date mRepliedDate;
   public String mStatus;
   public ArrayList<Comment> mReplies;

   public Comment() { }

   public Comment(String json) throws JSONException {
      this(new JSONObject(json));
   }

   public Comment(JSONObject object) throws JSONException {
      mCommentSource = object.toString(4);
      mCommentid = object.getInt("commentid");
      mLocale = object.getString("locale");
      mParentid = object.isNull("parentid") ? NO_PARENT_ID : object.getInt("parentid");
      mUser = JSONHelper.parseUserLight(object.getJSONObject("author"));
      mTitle = object.getString("title");
      mContext = object.getString("context");
      mContextid = object.getInt("contextid");
      mTextRaw = object.getString("text_raw");
      mTextRendered = object.getString("text_rendered");
      mRating = object.getInt("rating");
      mDate = new Date(object.getLong("date") * 1000);
      mModifiedDate = new Date(object.getLong("modified_date") * 1000);
      mRepliedDate = new Date(object.getLong("replied_date") * 1000);
      mStatus = object.getString("status");
      mReplies = new ArrayList<Comment>();
      JSONArray replies = object.optJSONArray("replies");

      if (replies != null) {
         int numReplies = replies.length();
         for (int i = 0; i < numReplies; i++) {
            mReplies.add(new Comment(replies.getJSONObject(i)));
         }
      }
   }

   public boolean isReply() {
      return mParentid != NO_PARENT_ID;
   }

   @Override
   public String toString() {
      return mCommentSource;
   }
}
