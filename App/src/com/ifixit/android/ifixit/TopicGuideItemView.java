package com.ifixit.android.ifixit;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TopicGuideItemView extends LinearLayout {
   
   private LinearLayout mBox; 
   private TextView mTitleView;	
	
   public TopicGuideItemView (Context context) {
	  super(context);
	  
      LayoutInflater inflater = (LayoutInflater)context.getSystemService(
       Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.topic_guide_item, this, true);

      mBox = (LinearLayout)findViewById(R.id.topic_grid_box);
	  mTitleView = (TextView)findViewById(R.id.topic_guide_title);
   }
   
   public void setGuideItem(String title) {
	   mTitleView.setText(title);
   }
}
