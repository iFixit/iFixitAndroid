package com.dozuki.ifixit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;

public class TopicGuideListFragment extends Fragment {
   protected static final String GUIDEID = "guideid";
   protected static final String SAVED_TOPIC = "SAVED_TOPIC";

   private TopicGuideListAdapter mGuideAdapter;
   private TopicLeaf mTopicLeaf;
   private GridView mGridView;
   private ImageManager mImageManager;
   private ImageSizes mImageSizes;

   /**
    * Required for restoring fragments
    */
   public TopicGuideListFragment() {}

   public TopicGuideListFragment(ImageManager imageManager, TopicLeaf topicLeaf) {
      mTopicLeaf = topicLeaf;
      mImageManager = imageManager;
   }

   @Override
   public void onCreate(Bundle savedState) {
      super.onCreate(savedState);
      
      if (savedState != null && mTopicLeaf == null) {
         mTopicLeaf = (TopicLeaf)savedState.getSerializable(SAVED_TOPIC);
      }
     
      if (mImageManager == null) {
          mImageManager = ((MainApplication)getActivity().getApplication()).
           getImageManager();
      }

      mImageSizes = ((MainApplication)getActivity().getApplication()).
       getImageSizes();
   }
   
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
	  View view = inflater.inflate(R.layout.topic_guide_list, container, false);

      mGridView = (GridView)view.findViewById(R.id.gridview);

      mGuideAdapter = new TopicGuideListAdapter();
      mGuideAdapter.setTopic(mTopicLeaf);

      mGridView.setAdapter(mGuideAdapter);
      mGridView.setOnItemClickListener(new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
		   Log.w("Item Click", mTopicLeaf.getGuides().get(position).toString());
	       GuideInfo guide = mTopicLeaf.getGuides().get(position);
	       Intent intent = new Intent(getActivity(), GuideView.class);
	
	       intent.putExtra(GUIDEID, guide.getGuideid());
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
            itemView = new TopicGuideItemView(getActivity(), mImageManager);
         }

         String title = mTopic.getGuides().get(position).getTitle();
         String image = mTopic.getGuides().get(position).getImage() +
          mImageSizes.getGrid();
         Log.w("Topic Guide info", mTopic.getGuides().get(position).toString());
         itemView.setGuideItem(title, image, getActivity());

         return itemView;
      }
   }
}
