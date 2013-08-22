package com.dozuki.ifixit.ui.guide;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.ui.guide.view.FullImageViewActivity;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.PicassoUtils;
import com.dozuki.ifixit.util.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestBuilder;
import com.squareup.picasso.Target;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class ThumbnailView extends LinearLayout {
   private static final String TAG = "ThumbnailView";
   private ArrayList<ViewHolder> mThumbs;
   private FallbackImageView mMainImage;
   private ImageView mAddThumbButton;
   private ImageSizes mImageSizes;
   private boolean mShowSingle = false;
   private boolean mCanEdit = false;
   private DisplayMetrics mDisplayMetrics;
   private float mNavigationHeight;
   private float mThumbnailWidth = 0;
   private float mThumbnailHeight = 0;
   private float mMainWidth = 0;
   private float mMainHeight = 0;
   private int mThumbnailSpacing;

   static class ViewHolder {
      FallbackImageView image;
      FrameLayout container;
   }

   private OnLongClickListener mLongClickListener;
   private OnClickListener mAddThumbListener;
   private Picasso mPicasso;
   private LinearLayout mThumbnailContainer;
   private FrameLayout mMainImageContainer;

   public ThumbnailView(Context context) {
      super(context);
      init(context);
   }

   public ThumbnailView(Context context, AttributeSet attrs) {
      super(context, attrs);
      init(context, attrs);
   }

   public ThumbnailView(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
      init(context, attrs);
   }

   public void destroy() {
      mPicasso.cancelRequest((Target) mMainImage);

      Utils.safeStripImageView(mMainImage);
      for (ViewHolder view : mThumbs) {
         mPicasso.cancelRequest((Target) view.image);
         Utils.safeStripImageView(view.image);
      }

      mMainImage = null;
      mThumbs = null;
      mLongClickListener = null;
      mAddThumbListener = null;
      mPicasso = null;
   }

   private void init(Context context) {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.thumbnail_viewer, this, true);


      mThumbnailSpacing = getResources().getDimensionPixelSize(R.dimen.guide_thumbnail_spacing);

      mPicasso = PicassoUtils.with(getContext());
      mImageSizes = MainApplication.get().getImageSizes();

      if (MainApplication.inDebug()) {
         mPicasso.setDebugging(true);
      }

      if (mCanEdit) {
         mAddThumbButton = (ImageView) findViewById(R.id.add_thumbnail_icon);
         mAddThumbButton.setVisibility(VISIBLE);
      }

      mThumbnailContainer = (LinearLayout) findViewById(R.id.thumbnail_list);

      mMainImageContainer = (FrameLayout) findViewById(R.id.thumbnail_main_image);
      mMainImageContainer.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            String url = (String) v.getTag();

            if (url == null || (url.equals("") || url.startsWith("."))) return;
            Context context = getContext();
            Intent intent = new Intent(context, FullImageViewActivity.class);
            intent.putExtra(FullImageViewActivity.IMAGE_URL, url);
            context.startActivity(intent);
         }
      });

      mMainImage = (FallbackImageView) mMainImageContainer.findViewById(R.id.main_image_view);

      mThumbs = new ArrayList<ViewHolder>();
      setOrientation(MainApplication.get().inPortraitMode() ? HORIZONTAL : VERTICAL);
   }

   private void init(Context context, AttributeSet attrs) {
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ThumbnailView);

      mShowSingle = a.getBoolean(R.styleable.ThumbnailView_show_single, false);
      mCanEdit = a.getBoolean(R.styleable.ThumbnailView_can_edit, false);

      a.recycle();

      init(context);
   }

   public void setThumbs(ArrayList<Image> images) {
      boolean hideOnSingleThumb = (images.size() <= 1 && !mShowSingle);

      calculateDimensions(hideOnSingleThumb);

      mThumbnailContainer.setVisibility(hideOnSingleThumb ? GONE : VISIBLE);

      if (!images.isEmpty()) {
         for (ViewHolder view : mThumbs) {
            mThumbnailContainer.removeView(view.container);
         }
         mThumbs.clear();

         for (Image image : images) {
            addThumb(image);
         }
      } else {
         if (mAddThumbButton != null) {
            mAddThumbButton.setVisibility(GONE);
         }
      }

      if (!((Image) mThumbs.get(0).container.getTag()).isLocal()) {
         setCurrentThumb(((Image) mThumbs.get(0).container.getTag()).getPath());
      }

      fitToSpace();
   }

   public void setDefaultMainImage() {
      mMainImageContainer.setTag(null);

      mPicasso.load(R.drawable.no_image)
       .fit()
       .into((Target) mMainImage);
   }

   public void setAddImageMain() {
      mMainImage.setImageDrawable(getResources().getDrawable(R.drawable.add_photos));
      mMainImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
      mMainImageContainer.setOnClickListener(mAddThumbListener);
      mMainImageContainer.setTag(null);
   }

   public void setAddThumbButtonOnClick(OnClickListener listener) {
      if (mCanEdit) {
         mAddThumbListener = listener;
         mAddThumbButton.setOnClickListener(listener);
         if (mThumbs.isEmpty()) {
            mMainImageContainer.setOnClickListener(listener);
         }
      }
   }

   private int addThumb(Image image) {
      LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View view = inflater.inflate(R.layout.thumbnail, mThumbnailContainer, false);

      ViewHolder thumb = new ViewHolder();
      thumb.container = (FrameLayout) view.findViewById(R.id.thumbnail_wrapper);
      thumb.image = (FallbackImageView) view.findViewById(R.id.thumbnail_image);

      thumb.container.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            for (ViewHolder view : mThumbs) {
               if (v.getId() == view.container.getId() && v.getTag() instanceof Image) {
                  Image image = (Image) v.getTag();
                  setCurrentThumb(image.getPath());
               }
            }
         }
      });

      if (mLongClickListener != null) {
         thumb.container.setOnLongClickListener(mLongClickListener);
      }

      thumb.image.setImage(image);

      if (image.hasLocalPath()) {
         File file = new File(image.getLocalPath());
         buildImage(mPicasso.load(file)
          .resize((int) (mThumbnailWidth - 0.5f), (int) (mThumbnailHeight - 0.5f))
          .centerCrop(),
          thumb.image);
         buildImage(mPicasso.load(file)
          .resize((int) (mMainWidth - 0.5f), (int) (mMainHeight - 0.5f))
          .centerCrop(),
          mMainImage);
      } else {
         String url = image.getPath(mImageSizes.getThumb());
         buildImage(mPicasso.load(url), thumb.image);
      }

      setThumbnailDimensions(thumb, mThumbnailHeight, mThumbnailWidth);
      thumb.container.setTag(image);

      mThumbs.add(thumb);

      int thumbnailPosition = mThumbs.size() - 1;
      mThumbnailContainer.addView(thumb.container, thumbnailPosition);

      if ((mThumbs.size() > 2 || mThumbs.size() < 1) && mAddThumbButton != null) {
         mAddThumbButton.setVisibility(GONE);
      }

      // Return the position of the newly added thumbnail
      return thumbnailPosition;
   }

   public void removeThumb(Object view) {
      Iterator<ViewHolder> it = mThumbs.iterator();

      while (it.hasNext()) {
         ViewHolder thumbHolder = it.next();

         if (thumbHolder.container == view) {
            it.remove();
            mThumbnailContainer.removeView(thumbHolder.container);
         }
      }

      if ((mThumbs.size() < 3 && mThumbs.size() > 0) && mAddThumbButton != null) {
         mAddThumbButton.setVisibility(VISIBLE);
      }

      if (mThumbs.size() < 1) {
         setAddImageMain();
      } else {
         Image image = (Image) mThumbs.get(mThumbs.size() - 1).container.getTag();
         setCurrentThumb(image.getPath());
      }
   }

   public void updateThumb(Image newImage) {
      for (ViewHolder thumb : mThumbs) {
         Image thumbImage = (Image) thumb.container.getTag();

         if (thumbImage.isLocal()) {
            thumb.container.setTag(newImage);
            invalidate();
            break;
         }
      }
   }

   public void updateThumb(Image newImage, int position) {
      mPicasso
       .load(newImage.getPath(mImageSizes.getThumb()))
       .into((Target) mThumbs.get(position));

      mThumbs.get(position).container.setTag(newImage.getPath(mImageSizes.getThumb()));
      invalidate();
   }

   public void setThumbsOnLongClickListener(View.OnLongClickListener listener) {
      mLongClickListener = listener;
      for (ViewHolder thumb : mThumbs)
         thumb.container.setOnLongClickListener(mLongClickListener);
   }

   public void setCurrentThumb(String url) {
      // Set the images tag as the url before we append .{size} to it, otherwise FullImageView is passed a smaller
      // version of the image.
      mMainImageContainer.setTag(url);
      mMainImage.setImageUrl(url);

      if (url.startsWith("http")) {
         url = url + mImageSizes.getMain();

         buildImage(mPicasso.load(url), mMainImage);
      } else {
         buildImage(mPicasso.load(new File(url))
          .resize((int) (mMainWidth - 0.5f), (int) (mMainHeight - 0.5f))
          .centerCrop(), mMainImage);
      }

   }

   public void setCurrentThumb(File file) {
      mMainImageContainer.setTag(file.getPath());

      buildImage(mPicasso.load(file), mMainImage);
   }

   public void setDisplayMetrics(DisplayMetrics metrics) {
      mDisplayMetrics = metrics;
   }

   public void fitToSpace() {
      calculateDimensions((mThumbs.size() <= 1 && !mShowSingle));

      setMainImageDimensions(mMainHeight, mMainWidth);

      if (mThumbs.size() > 0) {
         for (ViewHolder thumb : mThumbs) {
            setThumbnailDimensions(thumb, mThumbnailHeight, mThumbnailWidth);
         }
      }

      if (mAddThumbButton != null) {
         LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
          (int) (mThumbnailWidth - .5f),
          (int) (mThumbnailHeight - .5f)
         );

         if (!MainApplication.get().inPortraitMode()) {
            lp.gravity = Gravity.NO_GRAVITY;
            lp.setMargins(0, 0, mThumbnailSpacing, 0);
         } else {
            lp.gravity = Gravity.RIGHT;
            lp.setMargins(0, 0, 0, mThumbnailSpacing);
         }

         mAddThumbButton.setLayoutParams(lp);
      }
   }

   public void setNavigationHeight(float navHeight) {
      mNavigationHeight = navHeight;
   }

   private void setMainImageDimensions(float height, float width) {
      // Set the width and height of the main image
      mMainImage.getLayoutParams().height = (int) (height - 0.5f);
      mMainImage.getLayoutParams().width = (int) (width - 0.5f);

      mMainImage.setScaleType(
       (mThumbs.size() == 0) ? ImageView.ScaleType.CENTER_INSIDE : ImageView.ScaleType.FIT_CENTER);
   }

   private void setThumbnailDimensions(ViewHolder thumb, float height, float width) {
      FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(
       (int) (width - .5f),
       (int) (height - .5f)
      );

      LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
       LayoutParams.WRAP_CONTENT);
      if (!MainApplication.get().inPortraitMode()) {
         llp.gravity = Gravity.NO_GRAVITY;
         llp.setMargins(0, 0, mThumbnailSpacing, 0);
      } else {
         llp.gravity = Gravity.RIGHT;
         llp.setMargins(0, 0, 0, mThumbnailSpacing);
      }

      thumb.image.setLayoutParams(flp);
      thumb.container.setLayoutParams(llp);
   }

   private void buildImage(RequestBuilder builder, FallbackImageView image) {
      builder
       .error(R.drawable.no_image)
       .into((Target) image);
   }

   private void calculateDimensions(boolean fullScreen) {
      if (mMainWidth == 0 || mMainHeight == 0) {
         getMainImageDimensions(fullScreen);
      }

      if (mThumbnailWidth == 0 || mThumbnailHeight == 0) {
         getThumbnailDimensions();
      }
   }

   private void getMainImageDimensions(boolean fullScreen) {
      if (MainApplication.get().inPortraitMode()) {
         float pagePadding = (getResources().getDimensionPixelSize(R.dimen.page_padding) * 2f);

         // If we are hiding the thumbnails when there's 0 or 1 images on the step,
         // the main image should expand to fill the available screen space.
         if (fullScreen) {
            mMainWidth = (mDisplayMetrics.widthPixels - pagePadding);
         } else {
            // Main image is 4/5ths of the available screen width
            mMainWidth = (((mDisplayMetrics.widthPixels - pagePadding
             - getResources().getDimensionPixelSize(R.dimen.guide_image_spacing_right)) / 5f) * 4f);
         }

         mMainHeight = mMainWidth * (3f / 4f);
      } else {
         mNavigationHeight += getResources().getDimensionPixelSize(R.dimen.landscape_navigation_height);
         mNavigationHeight += getResources().getDimensionPixelSize(R.dimen.guide_image_spacing_bottom);

         // Main image is 4/5ths of the available screen height
         mMainHeight = (((mDisplayMetrics.heightPixels - mNavigationHeight) / 5f) * 4f);
         mMainWidth = mMainHeight * (4f / 3f);
      }
   }

   private void getThumbnailDimensions() {
      if (MainApplication.get().inPortraitMode()) {
         float pagePadding = (getResources().getDimensionPixelSize(R.dimen.page_padding) * 2f)
          + getResources().getDimensionPixelSize(R.dimen.guide_image_spacing_right);
         // Screen height minus everything else that occupies horizontal space
         mThumbnailWidth = (mDisplayMetrics.widthPixels - mMainWidth - pagePadding);
         mThumbnailHeight = mThumbnailWidth * (3f / 4f);
      } else {
         float pagePadding = (getResources().getDimensionPixelSize(R.dimen.page_padding) * 2f);
         // Screen height minus everything else that occupies vertical space
         mThumbnailHeight = (mDisplayMetrics.heightPixels - mMainHeight - mNavigationHeight);
         mThumbnailWidth = (mThumbnailHeight * (4f / 3f));
      }
   }
}
