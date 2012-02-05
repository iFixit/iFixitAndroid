package com.ifixit.android.ifixit;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TopicViewFragment extends Fragment {
   private static final String TOPIC_API_URL =
    "http://www.ifixit.com/api/0.1/topic/";

   private TopicNode mTopic;
   private TextView mTopicText;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.topic_view_fragment, container, false);

      mTopicText = (TextView)view.findViewById(R.id.topicName);

      return view;
   }

   public void setTopic(TopicNode topic) {
      mTopic = topic;

      mTopicText.setText(mTopic.getName());
   }
}
