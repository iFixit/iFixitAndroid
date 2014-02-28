package com.dozuki.ifixit.ui;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.PicassoUtils;
import com.dozuki.ifixit.util.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;


public class GuideItemView extends TouchableRelativeLayout {
   private Picasso mPicasso;
   private TextView mTitleView;
   private ImageView mThumbnail;
   private boolean mShortTitle;

   public GuideItemView(Context context, boolean shortTitle) {
      super(context);

      LayoutInflater.from(context).inflate(R.layout.guide_grid_item, this, true);

      mShortTitle = shortTitle;
      mTitleView = (TextView)findViewById(R.id.guide_grid_item_title);
      mThumbnail = (ImageView)findViewById(R.id.guide_grid_item_thumbnail);
      mPicasso = PicassoUtils.with(context);
   }

   public void setGuideItem(GuideInfo guide) {
      ImageSizes imageSizes = App.get().getImageSizes();

      mTitleView.setText(mShortTitle && guide.hasSubject() ?
       guide.mSubject : Html.fromHtml(guide.mTitle));

      Transformation transform = new RoundedTransformation(4, 0);

      if (guide.hasImage()) {
         // Clear image before setting it to make sure the old image isn't the background while the new one is loading
         Utils.safeStripImageView(mThumbnail);
         mPicasso.cancelRequest(mThumbnail);

         String url = guide.getImagePath(imageSizes.getGrid());
         mPicasso
          .load(url)
          .transform(transform)
          .error(R.drawable.no_image)
          .into(mThumbnail);
      } else {
         mPicasso
          .load(R.drawable.no_image)
          .fit()
          .transform(transform)
          .into(mThumbnail);
      }
   }
}
