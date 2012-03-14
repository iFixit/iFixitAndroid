package com.ifixit.android.ifixit;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TopicGuideItemView extends LinearLayout {

   private LinearLayout mBox; 
   private TextView mTitleView;	
   private LoaderImage mThumbnail;
   private ImageManager mImageManager;
   private Context mContext;

   public TopicGuideItemView (Context context, ImageManager imageManager) {
      super(context);
      mContext = context;
      mImageManager = imageManager;

      LayoutInflater inflater = (LayoutInflater)context.getSystemService(
       Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.topic_guide_item, this, true);

      mBox = (LinearLayout)findViewById(R.id.topic_grid_box);
      mTitleView = (TextView)findViewById(R.id.topic_guide_title);
      mThumbnail = (LoaderImage)findViewById(R.id.topic_guide_thumbnail);
   }

   public void setGuideItem(String title, String thumbUrl, Context context) {
      mContext = context;

      mTitleView.setText(title);
      mImageManager.displayImage(thumbUrl, (Activity)mContext, mThumbnail);
   }
}
