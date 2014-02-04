package com.dozuki.ifixit.ui.guide.create;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.ui.RoundedTransformation;
import com.dozuki.ifixit.ui.TouchableRelativeLayout;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.util.PicassoUtils;
import com.dozuki.ifixit.util.api.GuideMediaProgress;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;

public class OfflineGuideListItem extends TouchableRelativeLayout implements
 View.OnClickListener {
   private TextView mTitleView;
   private TextView mProgressText;
   private ImageView mThumbnail;
   private Activity mActivity;
   private GuideMediaProgress mGuideMedia;

   public OfflineGuideListItem(Activity activity) {
      super(activity);
      mActivity = activity;

      LayoutInflater inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.offline_guide_item, this, true);

      mTitleView = (TextView)findViewById(R.id.offline_guide_title);
      mProgressText = (TextView)findViewById(R.id.offline_guide_progress_text);
      mThumbnail = (ImageView)findViewById(R.id.offline_guide_thumbnail);

      setOnClickListener(this);
   }

   public void setRowData(GuideMediaProgress guideMedia) {
      mGuideMedia = guideMedia;

      mTitleView.setText(Html.fromHtml(mGuideMedia.mGuide.getTitle()));
      mProgressText.setText(mGuideMedia.mTotalMedia - mGuideMedia.mMediaRemaining + " / " + mGuideMedia.mTotalMedia);

      Picasso picasso = PicassoUtils.with(mActivity);
      Transformation transform = new RoundedTransformation(4, 0);
      Image image = mGuideMedia.mGuide.getIntroImage();

      if (image != null) {
         picasso
          // TODO: Put size into ImageSizes and make sure we sync it.
          .load(new File(image.getPath(".standard", true)))
          .noFade()
          .fit()
          .transform(transform)
          .error(R.drawable.no_image)
          .into(mThumbnail);
      } else {
         picasso
          .load(R.drawable.no_image)
          .noFade()
          .fit()
          .transform(transform)
          .into(mThumbnail);
      }
   }

   @Override
   public void onClick(View view) {
      // TODO: Force offline mode? Send guide along?
      mActivity.startActivity(GuideViewActivity.viewGuideid(mActivity,
       mGuideMedia.mGuide.getGuideid()));
   }
}
