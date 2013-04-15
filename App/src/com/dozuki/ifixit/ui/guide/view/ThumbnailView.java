package com.dozuki.ifixit.ui.guide.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.ui.gallery.GalleryActivity;
import com.dozuki.ifixit.util.APIImage;
import com.dozuki.ifixit.util.ImageSizes;
import com.marczych.androidimagemanager.ImageManager;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.widget.LinearLayout;

import java.util.ArrayList;

public class ThumbnailView extends LinearLayout implements View.OnClickListener {

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
         mAddThumbButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

               builder.setTitle("Attach media from")
                .setItems(R.array.step_image_actions, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                      Intent intent;
                      switch (which) {
                         case 0:
                
                            break;
                         case 1:
                            intent = new Intent(mContext, GalleryActivity.class);
                            intent.putExtra(GalleryActivity.ACTIVITY_RETURN_MODE, 1);
                            mContext.startActivity(intent);
                            break;
                      }
                   }
                });
               builder.show();
            }
         });

      }

      a.recycle();
   }

   private void init(Context context) {

      mImageManager = MainApplication.get().getImageManager();
      mContext = context;

      LayoutInflater inflater = (LayoutInflater) context
       .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

      inflater.inflate(R.layout.thumbnail_list, this, true);

      mThumbs = new ArrayList<ImageView>();

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

   public void setThumbs(ArrayList<APIImage> images) {

      if (images.size() <= 1 && !mShowSingle) {
         setVisibility(GONE);
      }

      Log.w("ThumbnailView", "Num Images: " + images.size());
      if (!images.isEmpty()) {
         if (images.size() > 2 && mAddThumbButton != null) {
            mAddThumbButton.setVisibility(GONE);
         }
         for (APIImage image : images) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ImageView thumb = (ImageView) inflater.inflate(R.layout.thumbnail, null);
            thumb.setOnClickListener(this);
            thumb.setVisibility(VISIBLE);
            thumb.setTag(image.mBaseUrl);

            mImageManager.displayImage(image.getSize(mImageSizes.getThumb()), (Activity) mContext, thumb);

            mThumbs.add(thumb);
            Log.w("ThumbnailView", "Num Thumbs: " + mThumbs.size());

            this.addView(thumb, mThumbs.size() - 1);
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
         if (hasThumbnail && mShowSingle) {
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

      if (mAddThumbButton != null) {
         mAddThumbButton.getLayoutParams().height = (int) (thumbnailHeight + .5f);
         mAddThumbButton.getLayoutParams().width = (int) (thumbnailWidth + .5f);
      }
   }

   public void setThumbnailDimensions(float height, float width) {
      for (int i = 0; i < mThumbs.size(); i++) {
         mThumbs.get(i).getLayoutParams().height = (int) (height + .5f);
         mThumbs.get(i).getLayoutParams().width = (int) (width + .5f);
      }
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
