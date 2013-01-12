package com.dozuki.ifixit.topic_view.ui;

import android.content.Context;
import android.graphics.Color;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.topic_view.model.TopicNode;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.TextView;

public class TopicListRowView extends LinearLayout {
   private TextView mTopicName;
   private TopicNode mTopic;

   public TopicListRowView(Context context) {
      super(context);

      LayoutInflater inflater = (LayoutInflater)context.getSystemService(
       Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.topic_list_row, this, true);

      mTopicName = (TextView)findViewById(R.id.topic_title);
   }

   public void setTopic(TopicNode topic) {
      mTopic = topic;
      mTopicName.setText(mTopic.getName());
   }

   public void setCurrentTopicStyle() {
      if (mTopic.isLeaf()) {
         setBackgroundColor(Color.parseColor("#CCCCCC"));
      }
   }

   public void clearCurrentTopicStyle() {
      setBackgroundColor(Color.WHITE);
   }
}
