package com.ifixit.android.ifixit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;

import android.text.Html;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

public class TopicGuideListFragment extends Fragment
 implements OnItemClickListener {
   protected static final String GUIDEID = "guideid";
   protected static final String SAVED_TOPIC = "SAVED_TOPIC";

   private TopicGuideListAdapter mGuideAdapter;
   private TopicLeaf mTopicLeaf;
   private GridView mGridView;

   /**
    * Required for restoring fragments
    */
   public TopicGuideListFragment() {}

   public TopicGuideListFragment(TopicLeaf topicLeaf) {
      mTopicLeaf = topicLeaf;
   }

   @Override
   public void onCreate(Bundle savedState) {
      super.onCreate(savedState);

      if (savedState != null && mTopicLeaf == null) {
         mTopicLeaf = (TopicLeaf)savedState.getSerializable(SAVED_TOPIC);
      }
   }
   
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
	  View view = inflater.inflate(R.layout.topic_guide_list, container, false);

      mGridView = (GridView)view.findViewById(R.id.gridview);

      mGuideAdapter = new TopicGuideListAdapter();
      mGuideAdapter.setTopic(mTopicLeaf);

      mGridView.setAdapter(mGuideAdapter);

	  return view;
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      state.putSerializable(SAVED_TOPIC, mTopicLeaf);
   }

   @Override
   public void onItemClick(AdapterView<?> l, View v, int position, long id) {
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
         TopicGuideItemView itemView;

         if (convertView == null) {
        	itemView = new TopicGuideItemView(getActivity());
        	
            String title = mTopic.getGuides().get(position).getTitle();
            Log.w("Topic Guide List Title: ", title);
            itemView.setGuideItem(title);
         } else {
        	itemView = (TopicGuideItemView)convertView;
         }

         return itemView;
      }
   }
}
