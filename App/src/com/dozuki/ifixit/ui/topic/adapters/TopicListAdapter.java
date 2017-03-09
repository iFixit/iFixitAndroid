package com.dozuki.ifixit.ui.topic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.topic.TopicNode;
import com.dozuki.ifixit.ui.topic.TopicListHeaderView;
import com.dozuki.ifixit.ui.topic.TopicSelectedListener;
import com.marczych.androidsectionheaders.Section;

import java.util.ArrayList;

public class TopicListAdapter extends Section {
   private Context mContext;
   private ArrayList<TopicNode> mTopicList;
   private String mHeader;
   private TopicSelectedListener mTopicListener;

   public TopicListAdapter(Context context, String header,
    ArrayList<TopicNode> topicList) {
      mContext = context;
      mHeader = header;
      mTopicList = topicList;
   }

   public void setTopicList(ArrayList<TopicNode> topicList) {
      mTopicList = topicList;
   }

   public void setTopicSelectedListener(TopicSelectedListener topicListener) {
      mTopicListener = topicListener;
   }

   public int getCount() {
      return mTopicList.size();
   }

   public Object getItem(int position) {
      return mTopicList.get(position);
   }

   public long getItemId(int position) {
      return position;
   }

   public View getView(int position, View view, ViewGroup parent) {
      TextView topicName;
      View row;

      if (view == null) {
         LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(
          Context.LAYOUT_INFLATER_SERVICE);
         row = inflater.inflate(R.layout.topic_list_row, null);
      } else {
         row = view;
      }

      topicName = (TextView)row.findViewById(R.id.topic_title);

      topicName.setText(mTopicList.get(position).getDisplayName());

      return row;
   }

   @Override
   public Object getHeaderItem() {
      return mHeader;
   }

   @Override
   public View getHeaderView(View convertView, ViewGroup parent) {
      TopicListHeaderView header = (TopicListHeaderView)convertView;

      if (header == null) {
         header = new TopicListHeaderView(mContext);
      }

      header.setHeader(mHeader);

      return header;
   }

   @Override
   public void onItemClick(AdapterView<?> adapterView, View view, int position,
    long id) {
      if (mTopicListener != null) {
         mTopicListener.onTopicSelected(mTopicList.get(position));
      }
   }
}
