package com.dozuki.ifixit.ui.guide;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Comment;
import com.dozuki.ifixit.util.transformations.CircleTransformation;
import com.dozuki.ifixit.util.PicassoUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CommentsAdapter extends BaseAdapter {

   private Context mContext;
   private ArrayList<Comment> mComments;
   private final LayoutInflater mInflater;

   public CommentsAdapter(Context context, ArrayList<Comment> comments) {
      mComments = comments;
      mContext = context;
      mInflater = LayoutInflater.from(context);
   }

   @Override
   public int getCount() {
      return mComments.size();
   }

   @Override
   public Object getItem(int position) {
      return mComments.get(position);
   }

   @Override
   public long getItemId(int position) {
      return position;
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent) {
      View v;

      if (convertView == null) {
         v = mInflater.inflate(R.layout.comment_row, parent, false);
      } else {
         v = convertView;
      }

      Comment comment = (Comment) getItem(position);

      SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyy");
      String commmentDetails =
       MainApplication.get().getString(R.string.by_on_comment_details, "<b>" + comment.mUser.getUsername() + "</b>",
        df.format(comment.mDate));

      ((TextView) v.findViewById(R.id.comment_text)).setText(Html.fromHtml(comment.mTextRendered));
      ((TextView) v.findViewById(R.id.comment_details)).setText(Html.fromHtml(commmentDetails));

      ImageView avatar = (ImageView) v.findViewById(R.id.comment_author);

      Log.d("Comment", "Avatar: " + comment.mUser.getAvatar().getPath());
      PicassoUtils
       .with(mContext)
       .load(comment.mUser.getAvatar().getPath(".thumbnail"))
       .error(R.drawable.no_image)
       .fit()
       .centerInside()
       .transform(new CircleTransformation())
       .into(avatar);

      /*RelativeLayout wrap = (RelativeLayout) v.findViewById(R.id.comment_row_wrap);

      Log.d("Comment", "ParentId " + comment.mParentid);
      if (comment.mParentid != NO_PARENT_ID) {
         //LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
         //wrap.setPadding(16, wrap.getPaddingTop(), 0, wrap.getPaddingBottom());
         //lp.setMargins(16, 0, 0, 0);
         //((RelativeLayout)v.findViewById(R.id.comment_row_wrap)).setLayoutParams(lp);
      }

      for (Comment reply : mReplies) {
         Log.d("Comment", reply.toString());
         //wrap.addView(reply.buildView(v, inflater, wrap));
      }*/

      return v;
   }

   public void setComments(ArrayList<Comment> comments) {
      mComments = comments;
   }
}
