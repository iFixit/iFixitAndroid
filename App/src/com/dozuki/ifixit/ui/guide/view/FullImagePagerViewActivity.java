package com.dozuki.ifixit.ui.guide.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Window;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.ui.guide.FullScreenImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class FullImagePagerViewActivity extends SherlockActivity {
   private static final String IMAGES_ARRAY = "IMAGES_ARRAY";
   private static final String IMAGE_POSITION = "IMAGE_POSITION";

   private TextView pageIndicatorText;
   private ArrayList<Image> mGallery;


   public static Intent viewImage(Context context, ArrayList<Image> images, int position) {
      Intent intent = new Intent(context, FullImagePagerViewActivity.class);
      intent.putExtra(IMAGES_ARRAY, images);
      intent.putExtra(IMAGE_POSITION, position);
      return intent;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      requestWindowFeature((int) Window.FEATURE_NO_TITLE);
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
              WindowManager.LayoutParams.FLAG_FULLSCREEN);

      setContentView(R.layout.full_screen_image_pager);

      mGallery = (ArrayList<Image>)getIntent().getSerializableExtra(IMAGES_ARRAY);
      int position = getIntent().getIntExtra(IMAGE_POSITION,0);

      findViewById(R.id.pageIndicator).setVisibility(mGallery.size()>1?View.VISIBLE:View.GONE);
      pageIndicatorText = (TextView) findViewById(R.id.pageIndicatorText);
      pageIndicatorText.setText(getPageIndicator(position));

      GalleryPagerAdapter adapter = new GalleryPagerAdapter(this);
      ViewPager pager = (ViewPager) findViewById(R.id.multimediaPager);
      pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
         @Override
         public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

         }

         @Override
         public void onPageSelected(int position) {
            pageIndicatorText.setText(getPageIndicator(position));

         }

         @Override
         public void onPageScrollStateChanged(int state) {

         }
      });

      pager.setAdapter(adapter);
      adapter.setImageViewItems(mGallery);

      pager.setCurrentItem(position);


      findViewById(R.id.full_screen_close).setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            finish();
         }
      });
   }

   private String getPageIndicator(int position){
      if (mGallery==null) return "";
      return (position+1)+"/"+mGallery.size();
   }

   private class GalleryPagerAdapter extends PagerAdapter {

      private ArrayList<Image> mGallery = new ArrayList<>();
      private Context context;

      public GalleryPagerAdapter(Context context){
         this.context = context;
      }

      public void setImageViewItems(ArrayList<Image> imageViewItems){
         this.mGallery = imageViewItems;
         notifyDataSetChanged();
      }

      @Override
      public int getCount() {
         return mGallery.size();
      }

      @Override
      public boolean isViewFromObject(View view, Object object) {
         return view == ((View) object);
      }

      @Override
      public void destroyItem(ViewGroup container, int position, Object object) {
         ((ViewGroup) container).removeView((View) object);
      }

      @Override
      public Object instantiateItem(ViewGroup container, int position) {

         Image imageViewItem = mGallery.get(position);

         LayoutInflater inflater = LayoutInflater.from(context);

         final FullScreenImageView layout = (FullScreenImageView) inflater.inflate(R.layout.full_screen_image_item, null);
         layout.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
         layout.loadImage(imageViewItem);

         container.addView(layout);

         return layout;
      }


   }
}
