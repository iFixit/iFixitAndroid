package com.dozuki.ifixit.ui.guide.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.APIImage;
import com.dozuki.ifixit.util.ImageSizes;
import com.marczych.androidimagemanager.ImageManager;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.LinearLayout;

import java.util.ArrayList;

public class ThumbnailView extends LinearLayout implements View.OnClickListener {

   private ArrayList<ImageView> mThumbs;
   private ImageView mMainImage;
   private ImageManager mImageManager;
   private Context mContext;
   private String mCurrentURL;
   private ImageSizes mImageSizes;
   private boolean mShouldHide = false;

   public ThumbnailView(Context context) {
      super(context);
      init(context);
   }

   public ThumbnailView(Context context, AttributeSet attrs) {
      super(context, attrs);
      init(context);
   }

   private void init(Context context) {
      LayoutInflater inflater = (LayoutInflater) context
       .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

      inflater.inflate(R.layout.thumbnail_list, this, true);

      mThumbs = new ArrayList<ImageView>();

      mThumbs.add((ImageView) findViewById(R.id.thumbnail_1));
      mThumbs.add((ImageView) findViewById(R.id.thumbnail_2));
      mThumbs.add((ImageView) findViewById(R.id.thumbnail_3));

      for (ImageView thumb : mThumbs)
         thumb.setOnClickListener(this);
   }

   @Override
   public void onClick(View v) {
      for (ImageView image : mThumbs)
         if (v.getId() == image.getId()) {
            setCurrentThumb((String) v.getTag());
         }
   }

   public void setImageSizes(ImageSizes imageSizes) {
      mImageSizes = imageSizes;
   }

   public void setThumbs(ArrayList<APIImage> images,
    ImageManager imageManager, Context context) {
      if (images.size() <= 1 && mShouldHide) {
         setVisibility(INVISIBLE);
      }

      mImageManager = imageManager;
      mContext = context;

      if (!images.isEmpty()) {
         for (int thumbId = 0; thumbId < images.size(); thumbId++) {
            ImageView thumb = mThumbs.get(thumbId);
            thumb.setVisibility(VISIBLE);
            thumb.setTag(images.get(thumbId).mBaseUrl);

            mImageManager.displayImage(images.get(thumbId).getSize(
             mImageSizes.getThumb()), (Activity) mContext, thumb);
         }
      }
   }

   public void setThumbsOnLongClickListener(View.OnLongClickListener listener) {
      for (ImageView thumb : mThumbs)
         thumb.setOnLongClickListener(listener);
   }

   public void setCurrentThumb(String url) {
      mImageManager.displayImage(url + mImageSizes.getMain(),
       (Activity) mContext, mMainImage);
      mMainImage.setTag(url);
   }

   public void fitToSpace(DisplayMetrics metrics, float padding) {
      float thumbnailHeight = 0f;
      float thumbnailWidth = 0f;
      float width = 0f;
      float height = 0f;
      boolean hasThumbnail = mThumbs.size() > 0;

      padding += viewPadding(R.dimen.page_padding);

      padding += viewPadding(R.dimen.guide_thumbnail_padding);

      if (inPortraitMode()) {
         if (hasThumbnail && !mShouldHide) {
            padding += getResources().getDimensionPixelSize(R.dimen.guide_image_spacing_right);

            // Main image is 4/5ths of the available screen height
            width = (((metrics.widthPixels - padding) / 5f) * 4f);
            height = width * (3f / 4f);

            // Screen height minus everything else that occupies horizontal space
            thumbnailWidth = (metrics.widthPixels - width - padding);
            thumbnailHeight = thumbnailWidth * (3f / 4f);

            fitProgressIndicator((width - .5f) + thumbnailWidth, height);
         } else {
            setVisibility(View.GONE);

            width = metrics.widthPixels - padding;
            height = width * (3f / 4f);

            fitProgressIndicator(width, height);
         }

      } else {
         padding += getResources().getDimensionPixelSize(R.dimen.guide_image_spacing_bottom);

         // Main image is 4/5ths of the available screen height
         height = (((metrics.heightPixels - padding) / 5f) * 4f);
         width = height * (4f / 3f);

         // Screen height minus everything else that occupies vertical space
         thumbnailHeight = (metrics.heightPixels - height - padding);
         thumbnailWidth = (thumbnailHeight * (4f / 3f));

         fitProgressIndicator(width, (height - .5f) + thumbnailHeight);
      }

      // Set the width and height of the main image
      mMainImage.getLayoutParams().height = (int) (height - .5f);
      mMainImage.getLayoutParams().width = (int) (width - .5f);

      if (hasThumbnail) {
         setThumbnailDimensions(thumbnailHeight, thumbnailWidth);
      }
   }

   public void setThumbnailDimensions(float height, float width) {
      for (int i = 0; i < mThumbs.size(); i++) {
         mThumbs.get(i).getLayoutParams().height = (int) (height + .5f);
         mThumbs.get(i).getLayoutParams().width = (int) (width + .5f);
      }
   }

   public void setHideThumbnailsSingleImage(boolean shouldHide) {
      mShouldHide = shouldHide;
   }

   public void setMainImage(ImageView mainImg) {
      mMainImage = mainImg;

      mMainImage.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            String url = (String) v.getTag();

            if (url != null && (url.equals("") || url.indexOf(".") == 0)) return;

            Intent intent = new Intent(mContext, FullImageViewActivity.class);
            intent.putExtra(FullImageViewActivity.IMAGE_URL, url);
            mContext.startActivity(intent);
         }
      });
   }

   private void fitProgressIndicator(float width, float height) {
      //mMainProgress.getLayoutParams().height = (int) (height - .5f);
      //mMainProgress.getLayoutParams().width = (int) (width - .5f);
   }

   private boolean inPortraitMode() {
      return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
   }

   private float viewPadding(int view) {
      return getResources().getDimensionPixelSize(view) * 2f;
   }
}
