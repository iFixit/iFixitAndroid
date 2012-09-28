package com.dozuki.ifixit.view.ui;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.dozuki.ifixit.view.model.TopicNode;
import com.dozuki.ifixit.view.model.TopicSelectedListener;
import com.ifixit.android.sectionheaders.Section;

public class TopicListAdapter extends Section {
   private Context mContext;
   private ArrayList<TopicNode> mTopicList;
   private String mHeader;
   private TopicSelectedListener mTopicListener;
   private TopicListRowView prevSelected = null;

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
      TopicListRowView topicRow;

      if (convertView == null) {
         topicRow = new TopicListRowView(mContext);
      } else {
         topicRow = (TopicListRowView) convertView;
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
      TopicListHeaderView header = (TopicListHeaderView) convertView;

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

         if (prevSelected != null) {
            ((TopicListRowView) prevSelected).clearCurrentTopicStyle();
         }

         ((TopicListRowView) view).setCurrentTopicStyle();
         prevSelected = (TopicListRowView) view;
      }
   }
}
