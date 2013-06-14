package com.dozuki.ifixit.ui.gallery;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.gallery.MediaInfo;
import com.squareup.picasso.Picasso;

public class MediaViewItem extends RelativeLayout {
   private MediaInfo mListRef;
   private String mLocalPath;

   private RelativeLayout mSelectImage;
   private FadeInImageView mImageView;
   private ProgressBar mLoadingBar;
   private Context mContext;

   public MediaViewItem(Context context) {
      super(context);
      mContext = context;
      mListRef = null;
      mLocalPath = null;
      LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.gallery_cell, this, true);

      mImageView = (FadeInImageView)findViewById(R.id.media_image);
      mSelectImage = ((RelativeLayout)findViewById(R.id.selected_image));
      mLoadingBar = (ProgressBar)findViewById(R.id.gallery_cell_progress_bar);

      mSelectImage.setVisibility(View.INVISIBLE);
      mLoadingBar.setVisibility(View.GONE);
   }

   public void setImageItem(String image) {
      Picasso
       .with(mContext)
       .load(image)
       .into(mImageView);
   }

   public void setImageItem(Uri image) {
      Picasso
       .with(mContext)
       .load(image)
       .into(mImageView);
   }

   public void setLoading(boolean loading) {
      if (loading) {
         mLoadingBar.setVisibility(View.VISIBLE);
         AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
         alpha.setDuration(0); // Make animation instant.
         alpha.setFillAfter(true); // Persist after the animation ends.
         mImageView.startAnimation(alpha);
      } else {
         mLoadingBar.setVisibility(View.GONE);
         mImageView.clearAnimation();
      }
   }

   public void setListRef(MediaInfo listRef) {
      mListRef = listRef;
   }

   public MediaInfo getListRef() {
      return mListRef;
   }

   public void toggleSelected(boolean selected) {
      mSelectImage.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
   }
}
