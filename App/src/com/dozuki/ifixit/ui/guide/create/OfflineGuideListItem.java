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
import com.f2prateek.progressbutton.ProgressButton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import java.io.File;

public class OfflineGuideListItem extends TouchableRelativeLayout implements
 View.OnClickListener {
   private TextView mTitleView;
   private ProgressButton mProgressButton;
   private ImageView mThumbnail;
   private Activity mActivity;
   private GuideMediaProgress mGuideMedia;

   public OfflineGuideListItem(Activity activity) {
      super(activity);
      mActivity = activity;

      LayoutInflater inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.offline_guide_item, this, true);

      mTitleView = (TextView)findViewById(R.id.offline_guide_title);
      mProgressButton = (ProgressButton)findViewById(R.id.offline_guide_progress_button);
      mThumbnail = (ImageView)findViewById(R.id.offline_guide_thumbnail);

      setOnClickListener(this);
   }

   public void setRowData(GuideMediaProgress guideMedia, boolean displayLiveImages) {
      mGuideMedia = guideMedia;

      mTitleView.setText(Html.fromHtml(mGuideMedia.mGuide.getTitle()));
      mProgressButton.setPinned(true);

      if (mGuideMedia.mTotalMedia == 0) {
         // It's valid for guides to have no images whatsoever so we must pretend that
         // it has 1 out of 1 images downloaded so ProgressButton doesn't crash on a
         // max value of 0
         mProgressButton.setProgressAndMax(1, 1);
      } else {
         mProgressButton.setProgressAndMax(mGuideMedia.mMediaProgress, mGuideMedia.mTotalMedia);
      }

      Picasso picasso = PicassoUtils.with(mActivity);
      Transformation transform = new RoundedTransformation(4, 0);
      Image image = mGuideMedia.mGuide.getIntroImage();

      if (image != null) {
         RequestCreator request;

         if (displayLiveImages) {
            request = picasso.load(image.getPath(".standard"));
         } else {
            // TODO: Put size into ImageSizes and make sure we sync it.
            request = picasso.load(new File(image.getPath(".standard", true)));
         }

         request
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
