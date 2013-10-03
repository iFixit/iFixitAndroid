package com.dozuki.ifixit.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dozuki.ifixit.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

public class Comment implements Serializable {
   private static final long serialVersionUID = -1333520388124961696L;

   private static final int NO_PARENT_ID = -1;
   public int mCommentid;
   public String mLocale;
   public int mParentid;
   public int mUserid;
   public String mUsername;
   public String mTitle;
   public String mText;
   public int mRating;
   public Date mDate;
   public Date mModifiedDate;
   public Date mRepliedDate;
   public String mStatus;

   public Comment() { }
   public Comment(JSONObject object) throws JSONException {
      mCommentid = object.getInt("commentid");
      mLocale = object.getString("locale");
      mParentid = object.isNull("parentid") ? NO_PARENT_ID : object.getInt("parentid");
      mUserid = object.getInt("userid");
      mUsername = object.getString("username");
      mTitle = object.getString("title");
      mText = object.getString("text");
      mRating = object.getInt("rating");
      mDate = new Date(object.getLong("date"));
      mModifiedDate = new Date(object.getLong("modified_date"));
      mRepliedDate = new Date(object.getLong("replied_date"));
      mStatus = object.getString("status");
   }

   public View buildView(View  v, LayoutInflater inflater, ViewGroup container) {
      if (v == null) {
         v = inflater.inflate(R.layout.comment_row, container, false);
      }

      ((TextView)v.findViewById(R.id.comment_text)).setText(mText);
      ((TextView)v.findViewById(R.id.comment_author)).setText(mUsername);
      ((TextView)v.findViewById(R.id.comment_date)).setText(mDate.toString());

      return v;
   }
}
