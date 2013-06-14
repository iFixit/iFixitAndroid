package com.dozuki.ifixit.ui.topic_view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.model.topic.TopicLeaf;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;

public class TopicGuideListFragment extends SherlockFragment {
   private static final int MAX_LOADING_IMAGES = 20;
   private static final int MAX_STORED_IMAGES = 30;
   private static final int MAX_WRITING_IMAGES = 20;
   public static final String GUIDEID = "guideid";
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

      mGridView = (GridView)view.findViewById(R.id.topic_guide_grid);

      mGuideAdapter = new TopicGuideListAdapter();
      mGuideAdapter.setTopic(mTopicLeaf);

      mGridView.setAdapter(mGuideAdapter);
      mGridView.setOnItemClickListener(new OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> arg0, View view, int position,
          long id) {
            GuideInfo guide = mTopicLeaf.getGuides().get(position);
            Intent intent = new Intent(getSherlockActivity(), GuideViewActivity.class);

            intent.putExtra(GUIDEID, guide.mGuideid);
            startActivity(intent);
         }
      });

      return view;
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      state.putSerializable(SAVED_TOPIC, mTopicLeaf);
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
         TopicGuideItemView itemView = (TopicGuideItemView)convertView;

         if (convertView == null) {
            itemView = new TopicGuideItemView(getSherlockActivity());
         }

         itemView.setGuideItem(mTopicLeaf.getGuides().get(position));

         return itemView;
      }
   }
}
