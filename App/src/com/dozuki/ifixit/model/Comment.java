package com.dozuki.ifixit.model;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Comment implements Serializable {
   private static final long serialVersionUID = -1333520388124961696L;

   private static final int NO_PARENT_ID = -1;
   public JSONObject mComment;
   public int mCommentid;
   public String mLocale;
   public int mParentid;
   public int mUserid;
   public String mUsername;
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

   public Comment(JSONObject object) throws JSONException {
      Log.d("Comment", object.toString());
      mComment = object;
      mCommentid = object.getInt("commentid");
      mLocale = object.getString("locale");
      mParentid = object.isNull("parentid") ? NO_PARENT_ID : object.getInt("parentid");
      mUserid = object.getInt("userid");
      mUsername = object.getString("username");
      mTitle = object.getString("title");
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

   public View buildView(View v, LayoutInflater inflater, ViewGroup container) {
      if (v == null) {
         v = inflater.inflate(R.layout.comment_row, container, false);
      }

      SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyy");
      String commmentDetails = MainApplication.get().getString(R.string.by_on_comment_details, mUsername,
       df.format(mDate));

      ((TextView) v.findViewById(R.id.comment_text)).setText(Html.fromHtml(mTextRendered));
      ((TextView) v.findViewById(R.id.comment_details)).setText(commmentDetails);

      RelativeLayout wrap = (RelativeLayout) v.findViewById(R.id.comment_row_wrap);

      Log.d("Comment", "ParentId " + mParentid);
      if (mParentid != NO_PARENT_ID) {
         //LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
         wrap.setPadding(16, wrap.getPaddingTop(), 0, wrap.getPaddingBottom());

         //lp.setMargins(16, 0, 0, 0);
         //((RelativeLayout)v.findViewById(R.id.comment_row_wrap)).setLayoutParams(lp);
      }

      for (Comment reply : mReplies) {
         Log.d("Comment", reply.toString());
         //wrap.addView(reply.buildView(v, inflater, wrap));
      }

      return v;
   }

   @Override
   public String toString() {
      try {
         return mComment.toString(4);
      } catch (JSONException e) {
         e.printStackTrace();
      }

      return "";
   }
}
