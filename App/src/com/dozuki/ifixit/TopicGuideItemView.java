package com.dozuki.ifixit;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

public class TopicGuideItemView extends RelativeLayout {
   private AutoResizeTextView mTitleView;	
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

      mTitleView = (AutoResizeTextView)findViewById(R.id.topic_guide_title);
      mThumbnail = (LoaderImage)findViewById(R.id.topic_guide_thumbnail);
   }

   public void setGuideItem(String title, String image, Context context) {
      mContext = context;

      mTitleView.setText(Html.fromHtml(title));
      mImageManager.displayImage(image, (Activity)mContext, mThumbnail);
   }
}
