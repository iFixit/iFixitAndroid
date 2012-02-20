package com.ifixit.android.ifixit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TopicGuideListFragment extends ListFragment {
   protected static final String GUIDEID = "guideid";

   private TopicGuideListAdapter mGuideAdapter;
   private TopicLeaf mTopicLeaf;
   private Context mContext;

   /**
    * Required for restoring fragments
    */
   public TopicGuideListFragment() {}

   public TopicGuideListFragment(TopicLeaf topicLeaf) {
      mTopicLeaf = topicLeaf;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      mGuideAdapter = new TopicGuideListAdapter();
      setTopicLeaf(mTopicLeaf);
   }

   private void setTopicLeaf(TopicLeaf topicLeaf) {
      mTopicLeaf = topicLeaf;
      mGuideAdapter.setTopic(mTopicLeaf);
      setListAdapter(mGuideAdapter);
   }

   @Override
   public void onListItemClick(ListView l, View v, int position, long id) {
      GuideInfo guide = mTopicLeaf.getGuides().get(position);
      Intent intent = new Intent(getActivity(), GuideView.class);

      intent.putExtra(GUIDEID, guide.getGuideid());
      startActivity(intent);
   }

   private class TopicGuideListAdapter extends BaseAdapter {
      private TopicLeaf mTopic;

      public void setTopic(TopicLeaf topic) {
         mTopic = topic;
      }

      public int getCount() {
         return mTopic.getGuides().size();
      }

      public Object getItem(int position) {
         return mTopic.getGuides().get(position);
      }

      public long getItemId(int position) {
         return position;
      }

      public View getView(int position, View convertView, ViewGroup parent) {
         TextView textView;

         if (convertView == null) {
            textView = new TextView(getActivity());
         } else {
            textView = (TextView)convertView;
         }

         textView.setText(mTopic.getGuides().get(position).getTitle());

         return textView;
      }
   }
}
