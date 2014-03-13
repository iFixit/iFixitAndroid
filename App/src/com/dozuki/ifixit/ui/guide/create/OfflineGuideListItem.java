package com.dozuki.ifixit.ui.guide.create;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.ui.RoundedTransformation;
import com.dozuki.ifixit.ui.TouchableRelativeLayout;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.PicassoUtils;
import com.dozuki.ifixit.util.Utils;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.GuideMediaProgress;
import com.f2prateek.progressbutton.ProgressButton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

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

      LayoutInflater inflater = (LayoutInflater)activity.getSystemService(
       Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.offline_guide_item, this, true);

      mTitleView = (TextView)findViewById(R.id.offline_guide_title);
      mProgressButton = (ProgressButton)findViewById(R.id.offline_guide_progress_button);
      mThumbnail = (ImageView)findViewById(R.id.offline_guide_thumbnail);

      mProgressButton.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View view) {
            App.sendEvent("ui_action", "button_press", "offline_guides_unfavorite_click", null);
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

            builder
             .setTitle(R.string.unfavorite_guide)
             .setMessage(R.string.unfavorite_confirmation)
             .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                   Api.call(mActivity, ApiCall.favoriteGuide(
                    mGuideMedia.mGuideInfo.mGuideid, false));
                   dialog.dismiss();
                }
             })
             .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                   dialog.cancel();
                }
             })
             .setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                   mProgressButton.setPinned(true);
                   mProgressButton.invalidate();
                }
             })
             .setCancelable(true);

            AlertDialog dialog = builder.create();
            dialog.show();
         }
      });

      setOnClickListener(this);
   }

   public void setRowData(GuideMediaProgress guideMedia, boolean displayLiveImages,
    boolean isSyncing) {
      mGuideMedia = guideMedia;

      mTitleView.setText(Html.fromHtml(mGuideMedia.mGuideInfo.mTitle));
      mProgressButton.setPinned(true);
      mProgressButton.setCircleColor(getResources().getColor(
       R.color.progress_button_background));

      int progressColor = isSyncing || mGuideMedia.isComplete() ?
       R.color.progress_default_progress_color : R.color.progress_button_progress_disabled;
      mProgressButton.setProgressColor(getResources().getColor(progressColor));

      if (mGuideMedia.mTotalMedia == 0) {
         // It's valid for guides to have no images whatsoever so we must pretend that
         // it has 1 out of 1 images downloaded so ProgressButton doesn't crash on a
         // max value of 0.
         mProgressButton.setProgressAndMax(1, 1);
      } else {
         mProgressButton.setProgressAndMax(mGuideMedia.mMediaProgress, mGuideMedia.mTotalMedia);
      }

      Picasso picasso = PicassoUtils.with(mActivity);
      Transformation transform = new RoundedTransformation(4, 0);
      Image image = mGuideMedia.mGuideInfo.mImage;

      if (image != null) {
         Utils.displayImage(picasso, image.getPath(ImageSizes.guideList), !displayLiveImages)
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
       mGuideMedia.mGuideInfo.mGuideid));
   }
}
