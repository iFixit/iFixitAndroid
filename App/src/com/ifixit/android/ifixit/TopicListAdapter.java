package com.ifixit.android.ifixit;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.ifixit.android.sectionheaders.Section;

import android.widget.AdapterView;

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

   public View getView(int position, View convertView, ViewGroup parent) {
      TopicListRow topicRow;

      if (convertView == null) {
         topicRow = new TopicListRow(mContext);
      } else {
         topicRow = (TopicListRow)convertView;
      }

      topicRow.setTopic(mTopicList.get(position));

      return topicRow;
   }

   @Override
   public Object getHeaderItem() {
      return mHeader;
   }

   @Override
   public View getHeaderView(View convertView, ViewGroup parent) {
      TopicHeaderRow header = (TopicHeaderRow)convertView;

      if (header == null) {
         header = new TopicHeaderRow(mContext);
      }

      header.setHeader(mHeader);

      return header;
   }

   @Override
   public void onItemClick(AdapterView<?> adapterView, View view,
    int position, long id) {
      if (mTopicListener != null) {
         mTopicListener.onTopicSelected(mTopicList.get(position));
      }
   }
}
