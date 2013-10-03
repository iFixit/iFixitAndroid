package com.dozuki.ifixit.ui.guide;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.dozuki.ifixit.model.Comment;

import java.util.ArrayList;

public class CommentsAdapter extends BaseAdapter {

   private final ArrayList<Comment> mComments;
   private final LayoutInflater mInflater;

   public CommentsAdapter(Context context, ArrayList<Comment> comments) {
      mComments = comments;
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
      return ((Comment)getItem(position)).buildView(convertView, mInflater, parent);
   }
}
