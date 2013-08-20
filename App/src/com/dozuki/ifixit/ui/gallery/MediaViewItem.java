package com.dozuki.ifixit.ui.gallery;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.PicassoUtils;
import com.dozuki.ifixit.util.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestBuilder;

import java.io.File;

public class MediaViewItem extends RelativeLayout {
   private RelativeLayout mSelectImage;
   private ImageView mImageView;
   private ProgressBar mLoadingBar;
   private int mTargetWidth;
   private int mTargetHeight;
   private Picasso mPicasso;

   public MediaViewItem(Context context) {
      super(context);
      mPicasso = PicassoUtils.with(context);

      LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.gallery_cell, this, true);

      mImageView = (ImageView)findViewById(R.id.media_image);
      mSelectImage = ((RelativeLayout)findViewById(R.id.selected_image));
      mLoadingBar = (ProgressBar)findViewById(R.id.gallery_cell_progress_bar);

      mSelectImage.setVisibility(View.INVISIBLE);
      mLoadingBar.setVisibility(View.GONE);
      Resources res = context.getResources();
      mTargetWidth = res.getDimensionPixelSize(R.dimen.gallery_grid_column_width);
      mTargetHeight = res.getDimensionPixelSize(R.dimen.gallery_grid_item_height);
   }

   public void setImageItem(String image) {
      buildImage(mPicasso.load(image));
   }

   public void setImageItem(File image) {
      buildImage(mPicasso.load(image));
   }

   public void setImageItem(Uri image) {
      buildImage(mPicasso.load(image));
   }

   private void buildImage(RequestBuilder builder) {
      builder
       .resize(mTargetWidth, mTargetHeight)
       .centerCrop()
       .error(R.drawable.no_image)
       .into(mImageView);
   }

   public void setSelected(boolean selected) {
      mSelectImage.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
   }

   public void clearImage() {
      Utils.safeStripImageView(mImageView);
      mImageView.setImageResource(R.color.image_border);
   }
}
