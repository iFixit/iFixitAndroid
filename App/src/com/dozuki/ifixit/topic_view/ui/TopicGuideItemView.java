package com.dozuki.ifixit.topic_view.ui;

import android.content.Context;
import android.text.Html;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_view.model.GuideInfo;
import com.dozuki.ifixit.util.ImageSizes;
import com.marczych.androidimagemanager.ImageManager;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.TextView;

public class TopicGuideItemView extends RelativeLayout {
   private TextView mTitleView;
   private ImageView mThumbnail;
   private ImageManager mImageManager;
   private GuideInfo mGuideInfo;

   public TopicGuideItemView(Context context, ImageManager imageManager) {
      super(context);
      mImageManager = imageManager;

      LayoutInflater inflater = (LayoutInflater)context
       .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.topic_guide_item, this, true);

      mTitleView = (TextView)findViewById(R.id.topic_guide_title);
      mThumbnail = (ImageView)findViewById(R.id.topic_guide_thumbnail);
   }

   public void setGuideItem(GuideInfo guide, Activity activity) {
      ImageSizes imageSizes = MainApplication.get().getImageSizes();
      mGuideInfo = guide;

      mTitleView.setText(Html.fromHtml(guide.mTitle));

      mImageManager.displayImage(guide.mImage.getSize(imageSizes.getGrid()),
       (Activity)activity, mThumbnail);
   }
}
