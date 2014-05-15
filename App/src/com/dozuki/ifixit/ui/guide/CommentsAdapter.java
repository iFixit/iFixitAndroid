package com.dozuki.ifixit.ui.guide;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Comment;

import java.util.ArrayList;

public class CommentsAdapter extends BaseAdapter {
   private Context mContext;
   private ArrayList<Comment> mComments;

   public CommentsAdapter(Context context, ArrayList<Comment> comments) {
      mComments = comments;
      mContext = context;
   }

   @Override
   public int getCount() {
      return mComments.size();
   }

   @Override
   public Comment getItem(int position) {
      return mComments.get(position);
   }

   @Override
   public long getItemId(int position) {
      return position;
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent) {
      CommentView v;

      if (convertView == null) {
         v = new CommentView(mContext);
      } else {
         v = (CommentView) convertView;
      }

      Comment comment = getItem(position);

      v.buildView(comment);

      LinearLayout replyContainer = (LinearLayout) v.findViewById(R.id.comment_replies);

      // Clear out all old replies from the container to prevent leaking of replies to other comments
      replyContainer.removeAllViews();

      for (Comment reply : comment.mReplies) {
         CommentView replyView = new CommentView(mContext);
         replyView.buildView(reply);
         replyView.setPadding(0, 0, 0, 0);
         replyContainer.addView(replyView);
      }

      replyContainer.setVisibility(replyContainer.getChildCount() != 0 ? View.VISIBLE : View.GONE);

      return v;
   }

   public void setComments(ArrayList<Comment> comments) {
      mComments = comments;
   }
}
