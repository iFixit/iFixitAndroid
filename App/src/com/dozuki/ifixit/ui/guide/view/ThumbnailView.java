package com.dozuki.ifixit.ui.guide.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.APIImage;
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
   private float mThumbnailWidth;
   private float mThumbnailHeight;
   private float mMainWidth;
   private float mMainHeight;

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

      setOrientation(MainApplication.get().inPortraitMode() ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);

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
      } else {
         setVisibility(VISIBLE);
      }

      if (!images.isEmpty()) {
         for (APIImage image : images) {
            addThumb(image, false);
         }
      }

      fitToSpace();
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
         setThumbnailDimensions(thumb, mThumbnailHeight, mThumbnailWidth);
      } else {
         path = image.getSize(mImageSizes.getThumb());
         mImageManager.displayImage(path, (Activity) mContext, thumb);
      }

      mThumbs.add(thumb);

      this.addView(thumb, mThumbs.size() - 1);

      if (mThumbs.size() > 2 && mAddThumbButton != null) {
         mAddThumbButton.setVisibility(GONE);
      }
   }

   public void setThumbsOnLongClickListener(View.OnLongClickListener listener) {
      for (ImageView thumb : mThumbs)
         thumb.setOnLongClickListener(listener);
   }

   public void setCurrentThumb(String url) {
      if (url.startsWith("http")) {
         url += mImageSizes.getMain();
         mImageManager.displayImage(url, (Activity) mContext, mMainImage);
      } else {
         mMainImage.setImageDrawable(Drawable.createFromPath(url));
         setMainImageDimensions(mMainHeight, mMainWidth);
      }
      mMainImage.setTag(url);
   }

   public void setDisplayMetrics(DisplayMetrics metrics) {
      mDisplayMetrics = metrics;
   }

   public void fitToSpace() {

      if (MainApplication.get().inPortraitMode()) {
         fitProgressIndicator(mMainWidth + mThumbnailWidth, mMainHeight);
      } else {
         fitProgressIndicator(mMainWidth, mMainHeight + mThumbnailHeight);
      }

      setMainImageDimensions(mMainHeight, mMainWidth);

      if (mThumbs.size() > 0) {
         for (ImageView thumb : mThumbs) {
            setThumbnailDimensions(thumb, mThumbnailHeight, mThumbnailWidth);
         }
      }

      if (mAddThumbButton != null) {
         setThumbnailDimensions(mAddThumbButton, mThumbnailHeight, mThumbnailWidth);
      }
   }

   public void setNavigationHeight(float padding) {
      mNavigationHeight = padding;
   }

   public void setMainImageDimensions(float height, float width) {
      // Set the width and height of the main image
      mMainImage.getLayoutParams().height = (int)(height-0.5f);
      mMainImage.getLayoutParams().width = (int)(width-0.5f);
   }

   public void setThumbnailDimensions(ImageView thumb, float height, float width) {
      LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
       (int) (width - .5f),
       (int) (height - .5f)
      );

      if (!MainApplication.get().inPortraitMode()) {
         lp.gravity = Gravity.NO_GRAVITY;
         lp.setMargins(0, 0, 16, 0);
      } else {
         lp.gravity = Gravity.RIGHT;
         lp.setMargins(0, 0, 0, 16);
      }

      thumb.setLayoutParams(lp);
   }

   public void setMainImage(ImageView image) {
      mMainImage = image;

      mMainImage.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            String url = (String) v.getTag();

            if (url == null || url != null && (url.equals("") || url.indexOf(".") == 0)) return;

            Intent intent = new Intent(mContext, FullImageViewActivity.class);

            if (!url.startsWith("http")) {
               intent.putExtra(FullImageViewActivity.LOCAL_URL, true);
            }
            intent.putExtra(FullImageViewActivity.IMAGE_URL, url);
            mContext.startActivity(intent);
         }
      });

      mainImageDimensions();
      thumbnailDimensions();
   }

   private void mainImageDimensions() {
      if (MainApplication.get().inPortraitMode()) {
         mNavigationHeight += getResources().getDimensionPixelSize(R.dimen.guide_image_spacing_right);

         // Main image is 4/5ths of the available screen height
         mMainWidth = (((mDisplayMetrics.widthPixels - mNavigationHeight) / 5f) * 4f);
         mMainHeight = mMainWidth * (3f / 4f);

      } else {
         mNavigationHeight += getResources().getDimensionPixelSize(R.dimen.guide_image_spacing_bottom);

         // Main image is 4/5ths of the available screen height
         mMainHeight = (((mDisplayMetrics.heightPixels - mNavigationHeight) / 5f) * 4f);
         mMainWidth = mMainHeight * (4f / 3f);
      }
   }

   private void thumbnailDimensions() {
      float height = 0f;
      float width = 0f;

      if (MainApplication.get().inPortraitMode()) {
         // Screen height minus everything else that occupies horizontal space
         mThumbnailWidth = (mDisplayMetrics.widthPixels - mMainWidth - mNavigationHeight);
         mThumbnailHeight = width * (3f / 4f);
      } else {
         // Screen height minus everything else that occupies vertical space
         mThumbnailHeight = (mDisplayMetrics.heightPixels - mMainHeight - mNavigationHeight);
         mThumbnailWidth = (mThumbnailHeight * (4f / 3f));
      }
   }


   private void fitProgressIndicator(float width, float height) {
      //mMainProgress.getLayoutParams().height = (int) (height - .5f);
      //mMainProgress.getLayoutParams().width = (int) (width - .5f);
   }
}
