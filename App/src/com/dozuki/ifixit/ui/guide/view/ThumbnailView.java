package com.dozuki.ifixit.ui.guide.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.APIImage;
import com.dozuki.ifixit.util.ImageSizes;
import com.marczych.androidimagemanager.ImageManager;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.LinearLayout;

import java.util.ArrayList;

public class ThumbnailView extends LinearLayout implements View.OnClickListener {

   /**
    * Used for logging
    */
   private static final String TAG = "ThumbnailView";

   private ArrayList<ImageView> mThumbs;
   private ImageView mMainImage;
   private ImageView mAddThumbButton;
   private ImageManager mImageManager;
   private Context mContext;
   private String mCurrentURL;
   private ImageSizes mImageSizes;
   private boolean mShowSingle = false;
   private boolean mCanEdit;
   private ArrayList<APIImage> mThumbnails;
   private DisplayMetrics mDisplayMetrics;
   private float mNavigationHeight;

   public ThumbnailView(Context context) {
      super(context);
      init(context);
   }

   public ThumbnailView(Context context, AttributeSet attrs) {

      super(context, attrs);
      init(context);

      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ThumbnailView);

      mShowSingle = a.getBoolean(R.styleable.ThumbnailView_show_single, false);
      mCanEdit = a.getBoolean(R.styleable.ThumbnailView_can_edit, false);

      if (mCanEdit) {
         mAddThumbButton = (ImageView) findViewById(R.id.add_thumbnail_icon);
         mAddThumbButton.setVisibility(VISIBLE);
      }

      a.recycle();
   }

   private void init(Context context) {

      mImageManager = MainApplication.get().getImageManager();
      mContext = context;

      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

      inflater.inflate(R.layout.thumbnail_list, this, true);

      mThumbs = new ArrayList<ImageView>();

   }

   public void setAddThumbButtonOnClick(OnClickListener listener) {
      if (mCanEdit) {
         mAddThumbButton.setOnClickListener(listener);
      }
   }

   @Override
   public void onClick(View v) {
      for (ImageView image : mThumbs) {
         if (v.getId() == image.getId()) {
            setCurrentThumb((String) v.getTag());
         }
      }
   }

   public void setImageSizes(ImageSizes imageSizes) {
      mImageSizes = imageSizes;
   }

   public void setThumbs(ArrayList<APIImage> images) {

      if (images.size() <= 1 && !mShowSingle) {
         setVisibility(GONE);
      }

      if (!images.isEmpty()) {
         for (APIImage image : images) {
            addThumb(image, false);
         }
      }
   }

   public void addThumb(APIImage image, boolean fromDisk) {
      String path;

      LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      ImageView thumb = (ImageView) inflater.inflate(R.layout.thumbnail, null);
      thumb.setOnClickListener(this);
      thumb.setVisibility(VISIBLE);
      thumb.setTag(image.mBaseUrl);

      if (fromDisk) {
         path = image.mBaseUrl;
         thumb.setImageDrawable(Drawable.createFromPath(path));
      } else {
         path = image.getSize(mImageSizes.getThumb());
         mImageManager.displayImage(path, (Activity) mContext, thumb);
      }

      mThumbs.add(thumb);

      this.addView(thumb, mThumbs.size() - 1);

      if (mThumbs.size() > 2 && mAddThumbButton != null) {
         mAddThumbButton.setVisibility(GONE);
      }

      fitToSpace();
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

   public void setDisplayMetrics(DisplayMetrics metrics) {
      mDisplayMetrics = metrics;
   }

   public void fitToSpace() {
      float thumbnailHeight = 0f;
      float thumbnailWidth = 0f;
      float width = 0f;
      float height = 0f;
      boolean hasThumbnail = mThumbs.size() > 0;

      mNavigationHeight += viewPadding(R.dimen.page_padding);

      mNavigationHeight += viewPadding(R.dimen.guide_thumbnail_padding);

      if (inPortraitMode()) {
         if (hasThumbnail && mShowSingle) {
            mNavigationHeight += getResources().getDimensionPixelSize(R.dimen.guide_image_spacing_right);

            // Main image is 4/5ths of the available screen height
            width = (((mDisplayMetrics.widthPixels - mNavigationHeight) / 5f) * 4f);
            height = width * (3f / 4f);

            // Screen height minus everything else that occupies horizontal space
            thumbnailWidth = (mDisplayMetrics.widthPixels - width - mNavigationHeight);
            thumbnailHeight = thumbnailWidth * (3f / 4f);

            fitProgressIndicator((width - .5f) + thumbnailWidth, height);
         } else {
            setVisibility(View.GONE);

            width = mDisplayMetrics.widthPixels - mNavigationHeight;
            height = width * (3f / 4f);

            fitProgressIndicator(width, height);
         }

      } else {
         mNavigationHeight += getResources().getDimensionPixelSize(R.dimen.guide_image_spacing_bottom);

         // Main image is 4/5ths of the available screen height
         height = (((mDisplayMetrics.heightPixels - mNavigationHeight) / 5f) * 4f);
         width = height * (4f / 3f);

         // Screen height minus everything else that occupies vertical space
         thumbnailHeight = (mDisplayMetrics.heightPixels - height - mNavigationHeight);
         thumbnailWidth = (thumbnailHeight * (4f / 3f));

         fitProgressIndicator(width, (height - .5f) + thumbnailHeight);
      }

      // Set the width and height of the main image
      mMainImage.getLayoutParams().height = (int) (height - .5f);
      mMainImage.getLayoutParams().width = (int) (width - .5f);

      if (hasThumbnail) {
         setThumbnailDimensions(thumbnailHeight, thumbnailWidth);
      }

      if (mAddThumbButton != null) {
         mAddThumbButton.getLayoutParams().height = (int) (thumbnailHeight + .5f);
         mAddThumbButton.getLayoutParams().width = (int) (thumbnailWidth + .5f);
      }
   }

   public void setNavigationHeight(float padding) {
      mNavigationHeight = padding;
   }

   public void setThumbnailDimensions(float height, float width) {
      for (ImageView thumb : mThumbs) {
         LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
          (int) (width + .5f),
          (int) (height + .5f),
          Gravity.NO_GRAVITY
         );

         if (!inPortraitMode()) {
            lp.setMargins(0, 0, 16, 0);
         } else {
            lp.setMargins(0, 0, 0, 16);
         }

         thumb.setLayoutParams(lp);
      }
   }

   public void setMainImage(ImageView image) {
      mMainImage = image;

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
