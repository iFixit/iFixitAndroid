package com.dozuki.ifixit.model;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.util.JSONHelper;
import com.dozuki.ifixit.util.PicassoUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Comment implements Serializable {
   private static final long serialVersionUID = -1333520488223961692L;

   private static final int NO_PARENT_ID = -1;
   public String mCommentSource;
   public int mCommentid;
   public String mLocale;
   public int mParentid;
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

   public Comment(JSONObject object) throws JSONException {
      mCommentSource = object.toString(4);
      mCommentid = object.getInt("commentid");
      mLocale = object.getString("locale");
      mParentid = object.isNull("parentid") ? NO_PARENT_ID : object.getInt("parentid");
      mUser = JSONHelper.parseUserLight(object.getJSONObject("author"));
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

   public View buildView(Context context, View v, LayoutInflater inflater, ViewGroup container) {
      if (v == null) {
         v = inflater.inflate(R.layout.comment_row, container, false);
      }

      SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyy");
      String commmentDetails = MainApplication.get().getString(R.string.by_on_comment_details, mUser.getUsername(),
       df.format(mDate));

      ((TextView) v.findViewById(R.id.comment_text)).setText(Html.fromHtml(mTextRendered));
      ((TextView) v.findViewById(R.id.comment_details)).setText(commmentDetails);

      ImageView avatar = (ImageView) v.findViewById(R.id.comment_author);

      Log.d("Comment", "Avatar: " + mUser.getAvatar().getPath());
      PicassoUtils
       .with(context)
       .load(mUser.getAvatar().getPath())
       .error(R.drawable.no_image)
       .resize(100, 100)
       .into(avatar);

      RelativeLayout wrap = (RelativeLayout) v.findViewById(R.id.comment_row_wrap);

      Log.d("Comment", "ParentId " + mParentid);
      if (mParentid != NO_PARENT_ID) {
         //LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
         //wrap.setPadding(16, wrap.getPaddingTop(), 0, wrap.getPaddingBottom());
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
      return mCommentSource;
   }
}
