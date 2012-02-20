package com.ifixit.android.ifixit;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class TopicListAdapter extends BaseAdapter {
   private Context mContext;
   private TopicNode mTopic;

   public TopicListAdapter(Context context) {
      mContext = context;
   }

   public void setTopic(TopicNode topic) {
      mTopic = topic;
   }

   public int getCount() {
      return mTopic.getChildren().size();
   }

   public Object getItem(int position) {
      return mTopic.getChildren().get(position);
   }

   public long getItemId(int position) {
      return position;
   }

   public View getView(int position, View convertView, ViewGroup parent) {
      TopicListRow topicRow;

      if (convertView == null) {
         topicRow = new TopicListRow(mContext);
      }
      else {
         topicRow = (TopicListRow)convertView;
      }

      topicRow.setTopic(mTopic.getChildren().get(position));

      return topicRow;
   }
}
